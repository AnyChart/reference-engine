import fs from 'fs-extra';
import path from 'path';
import os from 'os';
import crypto from 'crypto';
import { execa } from 'execa';
import glob from 'fast-glob';
import pMap from 'p-map';

async function convertToJsdoc(srcPath, tmpJsdocPath) {
  try {
    await fs.ensureDir(tmpJsdocPath);
  } catch (e) {}
  await fs.copy(srcPath, tmpJsdocPath, { overwrite: true });
  const adocFiles = await glob('**/*.adoc', {cwd: tmpJsdocPath, absolute: true});
  await Promise.all(adocFiles.map(async f => {
    const content = await fs.readFile(f, 'utf8');
    const replaced = content.replace(/@define/g, '@const');
    await fs.writeFile(`${f}.js`, replaced);
  }));
  return;
}

async function computeSourceHash(srcPath) {
  const adocFiles = await glob('**/*.adoc', { cwd: srcPath, absolute: true });
  adocFiles.sort();
  const hash = crypto.createHash('sha256');
  for (const f of adocFiles) {
    hash.update(f);
    hash.update(await fs.readFile(f));
  }
  return hash.digest('hex');
}

const CACHE_DIR = path.join(os.tmpdir(), 'dts-generator-cache');

function chunkArray(arr, n) {
  const len = arr.length;
  if (n <= 1) return [arr];
  const per = Math.ceil(len / n);
  const out = [];
  for (let i=0;i<len;i+=per) {
    out.push(arr.slice(i, i+per));
  }
  return out;
}

async function runJsdocOnGroup(jsdocBin, groupFiles) {
  const tempDir = await fs.mkdtemp(path.join(os.tmpdir(), 'dts-jsdoc-'));
  const configPath = path.join(tempDir, 'jsdoc.config.json');
  await fs.writeFile(configPath, JSON.stringify(createJsdocConfig(groupFiles)), 'utf8');

  try {
    // Use config file to avoid Windows command-line length limits with large file groups.
    const {stdout} = await execa(jsdocBin, ['-X', '-c', configPath], {maxBuffer: 100 * 1024 * 1024});
    return JSON.parse(stdout);
  } finally {
    try {
      await fs.remove(tempDir);
    } catch (e) {}
  }
}

function createJsdocConfig(groupFiles) {
  return {
    source: {
      include: groupFiles
    }
  };
}

async function getAllDoclets(dataDir, maxGroups, jsdocBin, version) {
  const srcPath = path.join(dataDir, 'versions', version);

  // Ensure srcPath exists
  if (!await fs.pathExists(srcPath)) {
    throw new Error(`Source path does not exist: ${srcPath}`);
  }

  // Check cache: hash source files, skip JSDoc if unchanged
  const sourceHash = await computeSourceHash(srcPath);
  const cacheFile = path.join(CACHE_DIR, `doclets-${version}-${sourceHash}.json`);

  if (await fs.pathExists(cacheFile)) {
    const cached = JSON.parse(await fs.readFile(cacheFile, 'utf8'));
    return normalizeAndFilterDoclets(cached, version);
  }

  const randomSuffix = Math.floor(Math.random() * 1000000);
  const jsdocPath = path.join(dataDir, `versions-tmp-${randomSuffix}`, version);

  await convertToJsdoc(srcPath, jsdocPath);
  const adocJsFiles = await glob('**/*.adoc.js', {cwd: jsdocPath, absolute: true});
  const groups = chunkArray(adocJsFiles, Math.min(maxGroups, adocJsFiles.length));

  // run jsdoc in parallel but limit concurrency
  const results = await pMap(groups, async (group) => {
    return runJsdocOnGroup(jsdocBin, group);
  }, { concurrency: Math.min(groups.length, 6) });

  // flatten
  const doclets = results.flat();

  // Save raw doclets to cache (before normalization, since version is baked in during normalize)
  try {
    await fs.ensureDir(CACHE_DIR);
    await fs.writeFile(cacheFile, JSON.stringify(doclets));
  } catch (e) {
    // Cache write failure is non-fatal
  }

  // do replacements and filtering to mirror Clojure
  const filtered = normalizeAndFilterDoclets(doclets, version);

  // Cleanup
  try {
    await fs.remove(path.dirname(jsdocPath));
  } catch (e) {}

  return filtered;
}

function replaceStringsInPlace(obj, version) {
  if (obj == null || typeof obj !== 'object') return obj;
  if (Array.isArray(obj)) {
    for (let i = 0; i < obj.length; i++) {
      if (typeof obj[i] === 'string') {
        obj[i] = obj[i].replace(/acgraph/g, 'anychart.graphics').replace(/\{\{branch-name\}\}/g, version);
      } else if (typeof obj[i] === 'object') {
        replaceStringsInPlace(obj[i], version);
      }
    }
    return obj;
  }
  for (const key of Object.keys(obj)) {
    const val = obj[key];
    if (typeof val === 'string') {
      obj[key] = val.replace(/acgraph/g, 'anychart.graphics').replace(/\{\{branch-name\}\}/g, version);
    } else if (typeof val === 'object' && val !== null) {
      replaceStringsInPlace(val, version);
    }
  }
  return obj;
}

const FILTERED_ACCESS = new Set(['private', 'protected', 'inner']);

function normalizeAndFilterDoclets(doclets, version) {
  const result = [];
  for (const d of doclets) {
    if (!d.name || FILTERED_ACCESS.has(d.access) || d.inherited) continue;
    replaceStringsInPlace(d, version);
    result.push(d);
  }
  return result;
}

export { getAllDoclets, runJsdocOnGroup, createJsdocConfig, normalizeAndFilterDoclets };
