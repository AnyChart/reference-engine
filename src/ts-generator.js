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

// Load member order (typedef/enum ordering) from control file
let memberOrder = { typedefs: {}, enums: {} };
try {
  const memberOrderPath = path.join(__dirname, 'member-order.json');
  memberOrder = JSON.parse(fs.readFileSync(memberOrderPath, 'utf8'));
} catch (e) {
  // If order file doesn't exist, fall back to alphabetical ordering
}

function sortByControlOrder(items, nameKey, orderList) {
  if (!orderList || orderList.length === 0) {
    return items.slice().sort((a, b) => a[nameKey] < b[nameKey] ? -1 : a[nameKey] > b[nameKey] ? 1 : 0);
  }
  return items.slice().sort((a, b) => {
    const aIdx = orderList.indexOf(a[nameKey]);
    const bIdx = orderList.indexOf(b[nameKey]);
    if (aIdx !== -1 && bIdx !== -1) return aIdx - bIdx;
    if (aIdx !== -1) return -1;
    if (bIdx !== -1) return 1;
    return a[nameKey] < b[nameKey] ? -1 : a[nameKey] > b[nameKey] ? 1 : 0;
  });
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

  if (typeof t === 'string' && t.includes('anychart.treeDataModule.Tree.DataItem')) {
    t = t.replace(/anychart\.treeDataModule\.Tree\.DataItem/g, 'anychart.data.Tree.DataItem');
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

  // Keep parity for common numeric-array unions emitted in control as `number | Array<number>`.
  if (cleaned.length === 2 && cleaned.includes('number') && cleaned.includes('Array<number>')) {
    cleaned.sort((a, b) => {
      if (a === 'number') return -1;
      if (b === 'number') return 1;
      return 0;
    });
  }
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
  if (constant.name === 'locales') type = '{[prop:string]:anychart.format.Locale}';
  
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
  return Array.isArray(td.type) && td.type[0] === 'function';
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
      // Prefer fullname first to avoid partial replacement of namespaced aliases.
      if (td.fullName && td.fullName !== td.name) {
         map.set(td.fullName, typedefSignature(td));
      }
      map.set(td.name, typedefSignature(td));
  }
  setReplacements(map);
}

function typedefDeclarations(tds, nsKey) {
  // Exclude function typedefs as they are inlined
  const filtered = (tds || []).filter(td => !isFunctionTypedef(td));
  const orderList = (memberOrder.typedefs || {})[nsKey] || [];
  const sorted = sortByControlOrder(filtered, 'name', orderList);
  return sorted.map(typedefDeclaration).join('\n');
}

/* Enums */
function enumField(field) {
  if (field.value !== undefined) {
    if (Number.isInteger(field.value)) {
      return `${p8}${field.name} = ${field.value}`;
    }
    return `${p8}${field.name}`;
  }
  return `${p8}${field.name}`;
}

function enumDeclaration(e) {
  const sortedFields = e.fields.slice().sort((a,b) => a.name < b.name ? -1 : a.name > b.name ? 1 : 0);
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
  // DO NOT SORT here. The order is determined by structurize (self alphabetical with custom end-methods)
  // and inheritance (appended from parents).
  const methods = resolved.map(methodDeclaration).join('\n');
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
  if (hasEnums || hasTypedefs || hasClasses) {
    const enumOrderList = (memberOrder.enums || {})[cl.fullName] || [];
    const enums = sortByControlOrder(
      (cl.enums || []).map(name => lookupEnum(topLevel, name)).filter(Boolean),
      'name', enumOrderList
    );
    const tdOrderList = (memberOrder.typedefs || {})[cl.fullName] || [];
    const typedefs = sortByControlOrder(
      (cl.typedefs || []).map(name => lookupTypedef(topLevel, name)).filter(Boolean),
      'name', tdOrderList
    );
    const classes = (cl.classes || []).map(name => lookupClass(topLevel, name)).filter(Boolean);

    const enumDecls = enums.map(enumDeclaration).join('\n');
    const typedefDecls = typedefs.map(td => typedefDeclaration(td)).join('\n');

    if (!enumDecls && !typedefDecls && !classes.length) return '';

    const blocks = [];

    // Enums and typedefs go in a namespace block
    if (enumDecls || typedefDecls) {
      const content = enumDecls + typedefDecls;
      blocks.push(`${p4}namespace ${cl.name} {\n${content}\n    }`);
    }

    // Each class gets its own module block
    const order = classOrder[cl.fullName] || [];
    const sortedClasses = classes.slice().sort((a, b) => {
      const aIndex = order.indexOf(a.name);
      const bIndex = order.indexOf(b.name);
      if (aIndex !== -1 && bIndex !== -1) return aIndex - bIndex;
      if (aIndex !== -1) return -1;
      if (bIndex !== -1) return 1;
      return a.name.localeCompare(b.name);
    });
    for (const cls of sortedClasses) {
      const decl = classDeclaration(cls, topLevel);
      blocks.push(`${p4}module ${cl.name} {\n${decl}\n    }`);
    }

    if (blocks.length === 0) return '';
    return '\n' + blocks.join('\n');
  }
  return '';
}

/* Namespaces */
function namespaceDefinition(topLevel, namespace) {
  const constants = constantDeclarations(namespace.constants || []);
  const funcs = functionDeclarations(namespace.functions || []);
  const nsTdNames = namespace.typedefs || [];
  const nsTds = nsTdNames.map(name => lookupTypedef(topLevel, name)).filter(Boolean);
  const typedefs = typedefDeclarations(nsTds, namespace.fullName);
  const nsEnumNames = namespace.enums || [];
  const enumsData = nsEnumNames.map(name => lookupEnum(topLevel, name)).filter(Boolean);
  const enumOrderList = (memberOrder.enums || {})[namespace.fullName] || [];
  const enums = sortByControlOrder(enumsData, 'name', enumOrderList).map(enumDeclaration).join('\n');
  const classes = classDeclarations(topLevel, (namespace.classes || []).map(name => lookupClass(topLevel, name)).filter(Boolean), namespace.fullName);
  
  let result = `declare namespace ${namespace.fullName} {\n`;
  if (constants) result += constants + '\n';
  if (funcs) result += funcs + '\n';
  if (typedefs) result += typedefs + '\n';
  if (enums) result += enums + '\n';
  if (classes) result += classes + '\n';
  result += '}';

  return result;
}

function buildLookupMaps(topLevel) {
  if (topLevel._classMap) return; // Already built
  topLevel._classMap = new Map();
  for (const c of topLevel.classes || []) {
    topLevel._classMap.set(c.fullName, c);
    topLevel._classMap.set(c.name, c);
  }
  topLevel._enumMap = new Map();
  for (const e of topLevel.enums || []) {
    topLevel._enumMap.set(e.fullName, e);
    topLevel._enumMap.set(e.name, e);
  }
  topLevel._typedefMap = new Map();
  for (const t of topLevel.typedefs || []) {
    topLevel._typedefMap.set(t.fullName, t);
    topLevel._typedefMap.set(t.name, t);
  }
}

function lookupClass(topLevel, name) {
  return topLevel._classMap ? topLevel._classMap.get(name) : topLevel.classes.find(c => c.fullName === name || c.name === name);
}

function lookupEnum(topLevel, name) {
  return topLevel._enumMap ? topLevel._enumMap.get(name) : topLevel.enums.find(e => e.fullName === name || e.name === name);
}

function lookupTypedef(topLevel, name) {
  return topLevel._typedefMap ? topLevel._typedefMap.get(name) : topLevel.typedefs.find(t => t.fullName === name || t.name === name);
}

function generateTS(topLevel) {
  buildLookupMaps(topLevel);
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
  const rawTs = generateTS(topLevel);
  const ts = addHeader(rawTs, versionKey);
  // Cache raw TS for graphics reuse
  topLevel._cachedRawTs = rawTs;
  return ts;
}

function generateGraphicsTSDeclarations(versionKey, topLevel) {
  // Reuse already-generated TS output instead of regenerating
  let ts = topLevel._cachedRawTs || generateTS(topLevel);
  ts = ts.replace(/anychart\.graphics/g, 'acgraph');
  ts = `// Type definitions for GraphicsJS JavaScript Graphics Library ${versionKey}\n// Project: http://www.graphicsjs.org/\n// Definitions by: AnyChart <https://www.anychart.com>\n${ts}`;
  return ts;
}

export {
  generateTSDeclarations,
  generateGraphicsTSDeclarations
};
