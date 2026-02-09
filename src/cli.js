// src/cli.js
import minimist from 'minimist';
import path from 'path';
import { runPipeline } from './pipeline.js';

async function main() {
  const argv = minimist(process.argv.slice(2), {
    string: ['data-dir', 'version', 'jsdoc-bin', 'output-dir'],
    number: ['max-groups'],
    default: {
      'data-dir': './data',
      'version': 'latest',
      'jsdoc-bin': './node_modules/.bin/jsdoc',
      'max-groups': 8,
      'output-dir': './.tmp'
    }
  });

  const options = {
    dataDir: path.resolve(argv['data-dir']),
    version: argv.version,
    jsdocBin: path.resolve(argv['jsdoc-bin']),
    maxGroups: argv['max-groups'],
    outputDir: path.resolve(argv['output-dir'])
  };

  try {
    await runPipeline(options);
  } catch (err) {
    console.error('Pipeline failed:');
    console.error(err);
    process.exit(1);
  }
}

main();
