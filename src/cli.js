import path from 'node:path';
import { parseArgs } from 'node:util';
import { runPipeline } from './pipeline.js';

async function main() {
  const { values } = parseArgs({
    options: {
      'data-dir':   { type: 'string', default: './data' },
      'version':    { type: 'string', default: 'latest' },
      'output-dir': { type: 'string', default: './.tmp' },
      'flat':       { type: 'boolean', default: false }
    },
    strict: false
  });

  const options = {
    dataDir: path.resolve(values['data-dir']),
    version: values.version,
    outputDir: path.resolve(values['output-dir']),
    noVersionDir: values.flat
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
