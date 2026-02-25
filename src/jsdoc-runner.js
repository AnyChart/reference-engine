import fs from 'node:fs/promises';
import path from 'node:path';
import os from 'node:os';
import crypto from 'node:crypto';
import { parseAdocFile } from './adoc-parser.js';

async function pathExists(p) {
  try { await fs.access(p); return true; } catch { return false; }
}

async function findAdocFiles(srcPath) {
  const results = [];
  async function walk(dir) {
    const entries = await fs.readdir(dir, { withFileTypes: true });
    for (const entry of entries) {
      const full = path.join(dir, entry.name);
      if (entry.isDirectory()) await walk(full);
      else if (entry.name.endsWith('.adoc')) results.push(full);
    }
  }
  await walk(srcPath);
  results.sort();
  return results;
}

async function computeSourceHash(srcPath) {
  const adocFiles = await findAdocFiles(srcPath);
  const hash = crypto.createHash('sha256');
  for (const f of adocFiles) {
    hash.update(f);
    hash.update(await fs.readFile(f));
  }
  return hash.digest('hex');
}

const CACHE_DIR = path.join(os.tmpdir(), 'dts-generator-cache');

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

async function getAllDoclets(dataDir, version) {
  const srcPath = path.join(dataDir, 'versions', version);

  if (!await pathExists(srcPath)) {
    throw new Error(`Source path does not exist: ${srcPath}`);
  }

  // Check cache
  const sourceHash = await computeSourceHash(srcPath);
  const cacheFile = path.join(CACHE_DIR, `doclets-v2-${version}-${sourceHash}.json`);

  if (await pathExists(cacheFile)) {
    const cached = JSON.parse(await fs.readFile(cacheFile, 'utf8'));
    return normalizeAndFilterDoclets(cached, version);
  }

  // Parse all .adoc files directly
  const adocFiles = await findAdocFiles(srcPath);

  const allDoclets = [];
  for (const filePath of adocFiles) {
    const content = await fs.readFile(filePath, 'utf8');
    const replaced = content.replace(/@define/g, '@const');
    const doclets = parseAdocFile(replaced, filePath);
    allDoclets.push(...doclets);
  }

  // Save to cache
  try {
    await fs.mkdir(CACHE_DIR, { recursive: true });
    await fs.writeFile(cacheFile, JSON.stringify(allDoclets));
  } catch (e) {
    // Cache write failure is non-fatal
  }

  return normalizeAndFilterDoclets(allDoclets, version);
}

export { getAllDoclets, normalizeAndFilterDoclets };
