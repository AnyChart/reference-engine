// src/typedef-builder.js

/**
 * Port of reference.adoc.typedef-builder.
 * Adjusts typedefs for TypeScript.
 */
function buildTypedefs(topLevel) {
  // In Clojure, this fixes typedefs depending on output format.
  // For TS, we mostly keep them as is or ensure properties are correctly shaped.
  
  topLevel.typedefs.forEach(td => {
    if (td.properties && td.properties.length > 0) {
      // Ensure each property has a type
      td.properties.forEach(p => {
        if (!p.type || p.type.length === 0) {
          p.type = ['any'];
        }
      });
    }
  });

  return topLevel;
}

export { buildTypedefs };
