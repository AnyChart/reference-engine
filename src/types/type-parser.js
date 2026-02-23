import { parse as peggyParse } from './jsdoc-type-parser-compiled.js';

function parse(jsdocTypeString) {
  if (!jsdocTypeString || jsdocTypeString.trim() === "") return null;
  try {
    return peggyParse(jsdocTypeString.trim());
  } catch (err) {
    console.info("TS parse error:", jsdocTypeString);
    console.info(err.message);
    return jsdocTypeString; // fallback (same behavior as Clojure)
  }
}

export { parse };
