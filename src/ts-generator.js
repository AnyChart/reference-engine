import { jsdocToTs, setReplacements } from './types/type-writer.js';
import fs from 'fs';
import path from 'path';
import { fileURLToPath } from 'url';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

// Load class order from control file (extracted from index.d.ts)
let classOrder = {};
try {
  const orderPath = path.join(__dirname, 'class-order.json');
  classOrder = JSON.parse(fs.readFileSync(orderPath, 'utf8'));
} catch (e) {
  // If order file doesn't exist, fall back to no ordering
}

const p4 = '    ';
const p8 = '        ';

// helper
function checkParam(param) {
  if (!param) return param;
  if (param === 'function') return 'func';
  if (param === 'this') return 'obj';
  return param.replace(/-/g, '');
}

function getType(t) {
  if (!t) return 'any';
  
  if (t === 'any' || t === '*') {
    return 'any';
  }

  // pass through jsdoc->ts
  return jsdocToTs(t);
}

function getTypes(types) {
  if (!types || types.length === 0) return 'void';
  // map anychart.enums.* => string (same as Clojure)
  const cleaned = Array.from(new Set(types.map(t => t.replace(/anychart\.enums\.[a-zA-Z0-9]+/g, 'string'))))
                       .filter(t => t !== 'null' && t !== 'undefined')
                       .map(getType);
  return cleaned.join(' | ');
}

function functionReturn(returns) {
  if (returns && returns.length > 0) {
    const types = returns.flatMap(r => r.types || r);
    return getTypes(types);
  }
  return 'void';
}

function checkOptional(param, name) {
  if (param && param.optional) return `${name}?`;
  return name;
}

function functionParam(param) {
  if (!param || !param.name) {
    console.error('param error', param);
    return '_error_';
  }
  const paramName = checkParam(param.name);
  if (paramName === 'var_args') {
    return `...${checkOptional(param, paramName)}: (${functionReturn([param])})[]`;
  }
  return `${checkOptional(param, paramName)}: ${functionReturn([param])}`;
}

function functionParams(params) {
  if (!params) return '';
  return params.map(functionParam).join(', ');
}

function functionDeclaration(f) {
  return `${p4}function ${f.name}(${functionParams(f.params)}): ${functionReturn(f.returns)};`;
}

function functionDeclarations(functions) {
  // functions is array of function objects with overrides
  const flattenOverrides = functions.flatMap(f => f.overrides || f);
  const sorted = flattenOverrides.sort((a, b) => {
    if (a.isMain && !b.isMain) return -1;
    if (!a.isMain && b.isMain) return 1;

    const distA = a.ancestorDistance || 0;
    const distB = b.ancestorDistance || 0;
    if (distA !== distB) return distA - distB;

    if (a.name !== b.name) {
       return a.name < b.name ? -1 : 1;
    }
    return (a.originalIndex || 0) - (b.originalIndex || 0);
  });
  return sorted.map(functionDeclaration).join('\n');
}

function constantDeclaration(constant) {
  let type = getType(constant.type);
  if (constant.name === 'DEFAULT_THEME') type = 'string';
  if (constant.name === 'DEVELOP') type = 'boolean';
  if (constant.name === 'PERFORMANCE_MONITORING') type = 'boolean';
  if (constant.name === 'VERSION') type = 'string';
  
  return `${p4}const ${constant.name}: ${type};`;
}

function constantDeclarations(constants) {
  if (!constants) return '';
  const sorted = constants.slice().sort((a,b) => a.name < b.name ? -1 : a.name > b.name ? 1 : 0);
  return sorted.map(constantDeclaration).join('\n');
}

/* typedef -> If properties empty, produce type alias, else produce object type */
function interfaceProp(prop) {
  return `${p8}${checkOptional(prop, checkParam(prop.name))}: ${getTypes(prop.type)};`;
}

function interfaceProps(props) {
  const sorted = props.slice().sort((a,b) => a.name < b.name ? -1 : a.name > b.name ? 1 : 0);
  return sorted.map(interfaceProp).join('\n');
}

function typedefDeclaration(td) {
  if (!td.properties || td.properties.length === 0) {
    return `${p4}type ${td.name} = ${getTypes(td.type)};`;
  }
  return `${p4}type ${td.name} = {\n${interfaceProps(td.properties)}\n    }`;
}

function isFunctionTypedef(td) {
  return (td.params && td.params.length > 0) || (td.type && td.type.includes('function'));
}

function typedefSignature(td) {
  if (td.params && td.params.length > 0) {
     const params = td.params.map(p => {
         const t = getTypes(p.types);
         // Ensure no space around colon to match type-writer's logic for parity
         return `${p.name}:${t}`; 
     }).join(',');
     const ret = functionReturn(td.returns);
     // No spaces around => to match control file style for params
     return `((${params})=>${ret})`;
  } else {
     const ret = functionReturn(td.returns);
     // Spaces around => for empty params
     return `(() => ${ret})`; 
  }
}

function prepareReplacements(topLevel) {
  const map = new Map();
  const funcTypedefs = (topLevel.typedefs || []).filter(isFunctionTypedef);
  
  for (const td of funcTypedefs) {
      map.set(td.name, typedefSignature(td));
      // Also map fullname if different and not global
      if (td.fullName && td.fullName !== td.name) {
         map.set(td.fullName, typedefSignature(td));
      }
  }
  setReplacements(map);
}

function typedefDeclarations(tds) {
  // Exclude function typedefs as they are inlined
  const filtered = (tds || []).filter(td => !isFunctionTypedef(td));
  const sorted = filtered.sort((a,b) => a.name < b.name ? -1 : a.name > b.name ? 1 : 0);
  return sorted.map(typedefDeclaration).join('\n');
}

/* Enums */
function enumField(field) {
  if (field.value !== undefined) {
    if (Number.isInteger(field.value)) {
      return `${p8}${field.name} = ${field.value}`;
    }
    return `${p8}${field.name} = ${JSON.stringify(field.value)}`;
  }
  return `${p8}${field.name}`;
}

function enumDeclaration(e) {
  const sortedFields = e.fields.slice().sort((a,b) => a.name.localeCompare(b.name));
  return `${p4}enum ${e.name} {\n${sortedFields.map(enumField).join(',\n')}\n    }`;
}

/* Classes */
function methodDeclaration(f) {
  return `${p8}${f.name}(${functionParams(f.params)}): ${functionReturn(f.returns)};`;
}

function classDeclaration(cl, topLevel) {
  if (cl.name.indexOf('.') > 0) {
    const [module, name] = cl.name.split(/\.(.+)/); // split to two parts
    const inner = Object.assign({}, cl, { name });
    return `${p4}module ${module} {\n${classDeclaration(inner, topLevel)}\n    }${getEnumsAndTypedefsClass(cl, topLevel)}`;
  }
  const extendsStr = cl.extends && cl.extends.length ? ` extends ${cl.extends.join(', ')}` : '';
  
  // Custom sort: !isMain first, then distance (0=Direct, 1=Parent, etc.), then alphabetical
  const flattened = (cl.methods || []).flatMap(m => m.overrides || m);
  // Filter out unresolved inheritdoc placeholders:
  // Must be marked as inheritdoc, NOT resolved (no parent found), and still have no content params/returns
  const resolved = flattened.filter(m => {
      const hasContent = (m.params && m.params.length > 0) || (m.returns && m.returns.length > 0);
      if (m.isInheritDoc && !m.isResolved && !hasContent) {
          return false;
      }
      return true;
  });
  const sortedMethods = resolved.sort((a, b) => {
    // 1. isMain status (true first)
    if (a.isMain && !b.isMain) return -1;
    if (!a.isMain && b.isMain) return 1;

    // 2. Ancestor distance (lower first)
    const distA = a.ancestorDistance || 0;
    const distB = b.ancestorDistance || 0;
    if (distA !== distB) return distA - distB;

    // 3. Alphabetical
    if (a.name !== b.name) {
       return a.name < b.name ? -1 : 1;
    }
    
    // 4. Original Index (stable sort for overloads)
    return (a.originalIndex || 0) - (b.originalIndex || 0);
  });
  
  const methods = sortedMethods.map(methodDeclaration).join('\n');
  return `${p4}interface ${cl.name}${extendsStr} {\n${methods}\n    }${getEnumsAndTypedefsClass(cl, topLevel)}`;
}

function classDeclarations(topLevel, classes, nsFullName) {
  // Sort classes according to control file order if available
  const order = classOrder[nsFullName] || [];
  const sorted = (classes || []).slice().sort((a, b) => {
    const aIndex = order.indexOf(a.name);
    const bIndex = order.indexOf(b.name);
    // If both are in the order list, use that order
    if (aIndex !== -1 && bIndex !== -1) return aIndex - bIndex;
    // If only one is in the order list, it comes first
    if (aIndex !== -1) return -1;
    if (bIndex !== -1) return 1;
    // Neither in order list - use alphabetical as fallback
    return a.name.localeCompare(b.name);
  });
  return sorted.map(cl => classDeclaration(cl, topLevel)).join('\n');
}

function getEnumsAndTypedefsClass(cl, topLevel) {
  const hasEnums = cl.enums && cl.enums.length > 0;
  const hasTypedefs = cl.typedefs && cl.typedefs.length > 0;
  const hasClasses = cl.classes && cl.classes.length > 0;
  const hasNamespaces = cl.namespaces && cl.namespaces.length > 0;

  if (hasEnums || hasTypedefs || hasClasses || hasNamespaces) {
    const enums = (cl.enums || []).map(name => topLevel.enums.find(e => e.name === name || e.fullName === name)).filter(Boolean).sort((a,b) => a.name < b.name ? -1 : a.name > b.name ? 1 : 0);
    const typedefs = (cl.typedefs || []).map(name => topLevel.typedefs.find(t => t.fullName === name || t.name === name)).filter(Boolean).sort((a,b) => a.name < b.name ? -1 : a.name > b.name ? 1 : 0);
    const classes = (cl.classes || []).map(name => topLevel.classes.find(c => c.fullName === name || c.name === name)).filter(Boolean);
    const namespaces = (cl.namespaces || []).map(name => topLevel.namespaces.find(n => n.fullName === name || n.name === name)).filter(Boolean);

    const enumDecls = enums.map(enumDeclaration).join('\n');
    const typedefDecls = typedefs.map(td => typedefDeclaration(td)).join('\n');
    const classDecls = classDeclarations(topLevel, classes, cl.fullName);
    const nsDecls = namespaces.map(ns => {
        let def = namespaceDefinition(topLevel, ns);
        return def.replace('declare namespace ' + ns.fullName, 'namespace ' + ns.name);
    }).join('\n');

    if (!enumDecls && !typedefDecls && !classDecls && !nsDecls) return '';
    
    // In index.d.ts, some nested items use 'module' instead of 'namespace'
    // but they are mostly equivalent. Let's start with namespace.
    return `\n${p4}namespace ${cl.name} {\n${enumDecls}${typedefDecls}${classDecls}${nsDecls}\n    }`;
  }
  return '';
}

/* Namespaces */
function namespaceDefinition(topLevel, namespace) {
  const constants = constantDeclarations(namespace.constants || []);
  const funcs = functionDeclarations(namespace.functions || []);
  const typedefs = typedefDeclarations((topLevel.typedefs || []).filter(td => (namespace.typedefs || []).includes(td.name) || (namespace.typedefs || []).includes(td.fullName)));
  const enumsData = (topLevel.enums || []).filter(e => (namespace.enums || []).includes(e.name) || (namespace.enums || []).includes(e.fullName));
  const enums = enumsData.sort((a,b) => a.name.localeCompare(b.name)).map(enumDeclaration).join('\n');
  const classes = classDeclarations(topLevel, (namespace.classes || []).map(name => topLevel.classes.find(c => c.fullName === name || c.name === name)).filter(Boolean), namespace.fullName);
  
  let parts = [];
  parts.push(`declare namespace ${namespace.fullName} {`);
  if (constants) { parts.push(constants); }
  if (funcs) { parts.push(funcs); }
  if (typedefs) { parts.push(typedefs); }
  if (enums) { parts.push(enums); }
  if (classes) { parts.push(classes); }
  parts.push('}');

  return parts.join('\n');
}

function addPrefix(data) {
  return '/// <reference path="all.d.ts"/>\n// from https://github.com/teppeis/closure-library.d.ts\n' + data;
}

function generateTS(topLevel) {
  // Exclude anychart.enums namespace
  const namespaces = (topLevel.namespaces || []).filter(ns => ns.fullName !== 'anychart.enums').sort((a,b) => a.fullName.localeCompare(b.fullName));
  return namespaces.map(ns => namespaceDefinition(topLevel, ns)).join('\n\n');
}

function addHeader(ts, versionKey) {
  const vk = versionKey.startsWith('v') ? versionKey : `v${versionKey}`;
  return `// Type definitions for AnyChart JavaScript Charting Library, ${vk}\n// Project: https://www.anychart.com/\n// Definitions by: AnyChart <https://www.anychart.com>\n${ts}`;
}

function generateTSDeclarations(versionKey, topLevel) {
  prepareReplacements(topLevel);
  const ts = addHeader(generateTS(topLevel), versionKey);
  return ts;
}

function generateGraphicsTSDeclarations(versionKey, topLevel) {
  let ts = generateTS(topLevel);
  ts = ts.replace(/anychart\.graphics/g, 'acgraph');
  ts = `// Type definitions for GraphicsJS JavaScript Graphics Library ${versionKey}\n// Project: http://www.graphicsjs.org/\n// Definitions by: AnyChart <https://www.anychart.com>\n${ts}`;
  return ts;
}

export {
  generateTSDeclarations,
  generateGraphicsTSDeclarations
};
