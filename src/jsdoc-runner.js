import fs from 'fs-extra';
import path from 'path';
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
  // run: jsdoc -X <files...>, return parsed JSON
  const {stdout} = await execa(jsdocBin, ['-X', ...groupFiles], {maxBuffer: 100 * 1024 * 1024});
  return JSON.parse(stdout);
}

async function getAllDoclets(dataDir, maxGroups, jsdocBin, version) {
  const srcPath = path.join(dataDir, 'versions', version);
  const randomSuffix = Math.floor(Math.random() * 1000000);
  const jsdocPath = path.join(dataDir, `versions-tmp-${randomSuffix}`, version);
  
  // Ensure srcPath exists
  if (!await fs.pathExists(srcPath)) {
    throw new Error(`Source path does not exist: ${srcPath}`);
  }

  await convertToJsdoc(srcPath, jsdocPath);
  const adocJsFiles = await glob('**/*.adoc.js', {cwd: jsdocPath, absolute: true});
  const groups = chunkArray(adocJsFiles, Math.min(maxGroups, adocJsFiles.length));
  
  // run jsdoc in parallel but limit concurrency
  const results = await pMap(groups, async (group) => {
    return runJsdocOnGroup(jsdocBin, group);
  }, { concurrency: Math.min(groups.length, 6) });
  
  // flatten
  const doclets = results.flat();
  
  // do replacements and filtering to mirror Clojure
  const replaced = doclets.map(d => {
    // convert acgraph -> anychart.graphics
    const s = JSON.stringify(d);
    const s2 = s.replace(/acgraph/g, 'anychart.graphics').replace(/\{\{branch-name\}\}/g, version);
    return JSON.parse(s2);
  });
  
  // filter doclets as in Clojure get-all-doclets
  const filtered = replaced.filter(d => d.name && !['private','protected','inner'].includes(d.access) && !d.inherited);
  return filtered;
}

export { getAllDoclets };
