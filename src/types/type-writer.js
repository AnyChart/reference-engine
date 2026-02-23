import { parse } from './type-parser.js';

function write(node) {
  if (node == null) return "void";
  if (typeof node === 'string') return node;

  const tag = node[0];
  switch (tag) {
    case 'types': {
      // node = ['types', t1, t2, ...]
      return node.slice(1).map(write).join('|');
    }
    case 'array': {
      return `Array<${write(node[1])}>`;
    }
    case 'object': {
      // either ['object', ['proptype', prop], typesOrProps] or ['object', typesOrProps]
      if (node[1] && node[1][0] === 'proptype') {
        const proptype = node[1][1];
        const value = node[2];
        return `{[prop:${write(proptype)}]:${write(value)}}`;
      } else {
        // Closure Object.<T> is usually a string-keyed dictionary.
        // Keep plain object literal payloads (Object.<{...}>) as-is.
        const value = node[node.length - 1];
        const isSinglePropsType =
          Array.isArray(value) &&
          (
            value[0] === 'props' ||
            (value[0] === 'types' && value.length === 2 && Array.isArray(value[1]) && value[1][0] === 'props')
          );
        if (isSinglePropsType) {
          return write(value);
        }
        return `{[prop:string]:${write(value)}}`;
      }
    }
    case 'props': {
      // ['props', ['kv', key, types], ...]
      const kvs = node.slice(1);
      return `{${kvs.map(write).join(',')}}`;
    }
    case 'kv': {
      const key = node[1];
      const typesNode = node[2];
      const value = write(typesNode);
      if (key === 'format' && value === '(() => void)') {
        return `${key}:Function`;
      }
      return `${key}:${value}`;
    }
    case 'tsfunc': {
      // ['tsfunc', kv?, returnTypes]
      // kvs are like ['kv', key, types]...
      const parts = node.slice(1);
      const returnType = parts[parts.length - 1]; // last is return types
      const kvs = parts.slice(0, -1);
      const params = kvs.map(kv => {
        // kv: ['kv', 'a', typesnode]
        return `${kv[1]}:${write(kv[2])}`;
      }).join(',');
      
      const isVoid = returnType && returnType[0] === 'simple' && returnType[1] === 'void';
      const arrow = (params === '' && isVoid) ? ' => ' : '=>';
      return `((${params})${arrow}${write(returnType)})`;
    }
    case 'jsfunc': {
      // ['jsfunc', [param...], returnTypes?]
      const paramsArr = node[1] || [];
      const returnNode = node[2] || ['types', ['simple', 'void']];
      const params = paramsArr.map(p => {
        // p: ['jsfuncparam', name, types]
        if (!p) return '';
        return `${p[1]}:${write(p[2])}`;
      }).join(',');
      
      const isVoid = (!node[2]) || (returnNode[0] === 'types' && returnNode[1] && returnNode[1][0] === 'simple' && returnNode[1][1] === 'void') || (returnNode[0] === 'simple' && returnNode[1] === 'void');
      const arrow = (params === '' && isVoid) ? ' => ' : '=>';
      return `((${params})${arrow}${write(returnNode)})`;
    }
    case 'jsfuncparam': {
      // ['jsfuncparam', name, types]
      return `${node[1]}:${write(node[2])}`;
    }
    case 'simple': {
      const s = node[1];
      switch (s) {
        case 'Array': return 'Array<any>';
        case 'function()':
        case 'function':
        case 'Function':
          return '(() => void)';
        case '*': return 'any';
        default:
          return s;
      }
    }
    default:
      return String(node);
  }
}

// High level conversion: jsdoc string -> ts-string (like ts.type-parser.jsdoc->ts)
let replacementsMap = new Map();
let compiledReplacements = []; // Pre-compiled regex/value pairs

function setReplacements(map) {
  replacementsMap = map;
  // Pre-compile replacement regexes sorted by key length (longest first)
  const entries = Array.from(map.entries()).sort((a, b) => b[0].length - a[0].length);
  compiledReplacements = entries.map(([key, value]) => ({
    regex: new RegExp(`\\b${key.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')}\\b`, 'g'),
    value
  }));
  // Clear memoization cache when replacements change
  jsdocToTsCache.clear();
}

// Memoization cache for parsed type strings
const jsdocToTsCache = new Map();

function jsdocToTs(s) {
  if (s == null) return s;

  // Check cache first
  const cached = jsdocToTsCache.get(s);
  if (cached !== undefined) return cached;

  let t = s.replace(/\s+/g, '')
           .replace(/\bscope\b/g, 'any')
           .replace(/!/g, '')
           .replace(/\|null/g, '')
           .replace(/\|undefined/g, '');

  for (const { regex, value } of compiledReplacements) {
    regex.lastIndex = 0;
    t = t.replace(regex, value);
  }

  const ast = parse(t);
  const result = ast ? write(ast) : t;
  jsdocToTsCache.set(s, result);
  return result;
}

export { jsdocToTs, setReplacements };
