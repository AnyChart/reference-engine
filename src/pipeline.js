import path from 'node:path';
import fs from 'node:fs/promises';
import { execSync } from 'node:child_process';
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
    outputDir,
    noVersionDir
  } = options;

  console.log(`Starting pipeline for version ${version}...`);

  // 1. Get doclets
  console.log('Extracting doclets...');
  const doclets = await getAllDoclets(dataDir, version);
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

  // 5. Output
  const outDir = noVersionDir ? outputDir : path.join(outputDir, version);
  await fs.mkdir(outDir, { recursive: true });

  await fs.writeFile(path.join(outDir, 'index.d.ts'), indexTs);
  await fs.writeFile(path.join(outDir, 'graphics.d.ts'), graphicsTs);

  console.log(`Successfully generated declarations in ${outDir}`);

  // 6. Validate with TypeScript compiler
  const indexPath = path.join(outDir, 'index.d.ts');
  const graphicsPath = path.join(outDir, 'graphics.d.ts');
  console.log('Validating TypeScript declarations...');
  try {
    execSync(`npx tsc --noEmit --strict "${indexPath}"`, { stdio: 'pipe' });
    execSync(`npx tsc --noEmit --strict "${graphicsPath}"`, { stdio: 'pipe' });
    console.log('TypeScript validation passed.');
  } catch (e) {
    const stderr = e.stderr ? e.stderr.toString() : e.message;
    console.error('TypeScript validation FAILED:\n' + stderr);
    throw new Error('Generated declarations have TypeScript errors');
  }

  return {
    indexTsPath: indexPath,
    graphicsTsPath: graphicsPath
  };
}

export { runPipeline };
export { removeIndexRectSpecialCase };
