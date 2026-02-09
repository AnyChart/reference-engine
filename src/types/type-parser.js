import fs from 'fs';
import path from 'path';
import peggy from 'peggy';
import { fileURLToPath } from 'url';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

const grammarPath = path.join(__dirname, 'jsdoc-type-parser.pegjs');
const grammar = fs.readFileSync(grammarPath, 'utf8');

let parser;
try {
  parser = peggy.generate(grammar);
} catch (e) {
  console.error('Failed to generate Peggy parser');
  console.error(e.message);
  if (e.location) {
    console.error(`Line ${e.location.start.line}, column ${e.location.start.column}`);
  }
  process.exit(1);
}

function parse(jsdocTypeString) {
  if (!jsdocTypeString || jsdocTypeString.trim() === "") return null;
  try {
    return parser.parse(jsdocTypeString.trim());
  } catch (err) {
    console.info("TS parse error:", jsdocTypeString);
    console.info(err.message);
    return jsdocTypeString; // fallback (same behavior as Clojure)
  }
}

export { parse };
