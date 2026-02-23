import path from 'path';
import fs from 'fs-extra';
import { getAllDoclets } from './jsdoc-runner.js';
import { structurize } from './structurize.js';
import { buildTypedefs } from './typedef-builder.js';
import { resolveInheritance } from './inheritance.js';
import { generateTSDeclarations, generateGraphicsTSDeclarations } from './ts-generator.js';

function removeIndexRectSpecialCase(topLevel) {
  const ns = (topLevel.namespaces || []).find(n => n.fullName === 'anychart.graphics.math');
  if (ns && Array.isArray(ns.functions)) {
    ns.functions = ns.functions.filter(fn => fn.name !== 'rect');
  }
  return topLevel;
}

async function runPipeline(options) {
  const {
    dataDir,
    version,
    jsdocBin,
    maxGroups,
    outputDir
  } = options;

  console.log(`Starting pipeline for version ${version}...`);

  // 1. Get doclets
  console.log('Extracting doclets...');
  const doclets = await getAllDoclets(dataDir, maxGroups, jsdocBin, version);
  console.log(`Found ${doclets.length} doclets.`);

  // 2. Structurize
  console.log('Structurizing doclets...');
  let topLevel = structurize(doclets);

  // 3. Inheritance & Typedefs
  console.log('Resolving inheritance...');
  resolveInheritance(topLevel);

  console.log('Building inheritance and typedefs...');
  topLevel = buildTypedefs(topLevel);
  topLevel = removeIndexRectSpecialCase(topLevel);

  // 4. Generate TS
  console.log('Generating TypeScript declarations...');
  const indexTs = generateTSDeclarations(version, topLevel);
  const graphicsTs = generateGraphicsTSDeclarations(version, topLevel);

  // 4. Output
  const versionOutputDir = path.join(outputDir, version);
  await fs.ensureDir(versionOutputDir);
  
  await fs.writeFile(path.join(versionOutputDir, 'index.d.ts'), indexTs);
  await fs.writeFile(path.join(versionOutputDir, 'graphics.d.ts'), graphicsTs);
  await fs.writeFile(path.join(versionOutputDir, `index-${version}.d.ts`), indexTs);

  console.log(`Successfully generated declarations in ${versionOutputDir}`);
  
  return {
    indexTsPath: path.join(versionOutputDir, 'index.d.ts'),
    graphicsTsPath: path.join(versionOutputDir, 'graphics.d.ts')
  };
}

export { runPipeline };
export { removeIndexRectSpecialCase };
