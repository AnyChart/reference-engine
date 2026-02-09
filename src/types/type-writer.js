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
        // fallback: write inner (last element)
        return write(node[node.length - 1]);
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
      return `${key}:${write(typesNode)}`;
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

function setReplacements(map) {
  replacementsMap = map;
}

function jsdocToTs(s) {
  if (s == null) return s;
  // Strip spaces but be careful not to break function signatures if we had a better parser.
  // Actually, the current parser handles stripped strings best.
  let t = s.replace(/\s+/g, '')
           .replace(/\bscope\b/g, 'any')
           .replace(/!/g, '')
           .replace(/\|null/g, '')
           .replace(/\|undefined/g, '');

  if (replacementsMap.size > 0) {
    for (const [key, value] of replacementsMap) {
       // Replace whole word matches
       const regex = new RegExp(`\\b${key}\\b`, 'g');
       t = t.replace(regex, value);
    }
  }

  const ast = parse(t);
  if (!ast) return t;
  return write(ast);
}

export { parse, write, jsdocToTs, setReplacements };
