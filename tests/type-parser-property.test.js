import { describe, it } from 'node:test';
import assert from 'node:assert/strict';
import fc from 'fast-check';
import { parse } from '../src/types/type-parser.js';
import { jsdocToTs, setReplacements } from '../src/types/type-writer.js';

// Reset replacements to empty before tests
setReplacements(new Map());

// --- Arbitraries for generating valid JSDoc type strings ---

const simpleTypeName = fc.oneof(
  fc.constant('string'),
  fc.constant('number'),
  fc.constant('boolean'),
  fc.constant('void'),
  fc.constant('any'),
  fc.constant('null'),
  fc.constant('undefined'),
  fc.constant('Object'),
  fc.constant('Array'),
  fc.constant('Function'),
  fc.constant('Date'),
  fc.constant('RegExp'),
  fc.constant('Element'),
  fc.constant('Node'),
  fc.constant('Event'),
  fc.constant('*'),
  // Qualified names like anychart.charts.Bullet
  fc.stringMatching(/^[a-zA-Z][a-zA-Z0-9]*(\.[a-zA-Z][a-zA-Z0-9]*){0,3}$/)
);

// A simple type (leaf node in the AST)
const simpleType = simpleTypeName;

// Array type: Array<T> or Array.<T>
const arrayType = fc.tuple(
  simpleType,
  fc.boolean() // whether to use dot notation
).map(([inner, dot]) => dot ? `Array.<${inner}>` : `Array<${inner}>`);

// Object type: Object<K,V> or Object.<K,V>
const objectType = fc.tuple(
  simpleType,
  simpleType,
  fc.boolean()
).map(([k, v, dot]) => dot ? `Object.<${k},${v}>` : `Object<${k},${v}>`);

// Union type: T1|T2|T3
const unionType = fc.array(simpleType, { minLength: 2, maxLength: 4 })
  .map(types => types.join('|'));

// Parenthesized union: (T1|T2)
const parenUnionType = unionType.map(u => `(${u})`);

// JS function type: function(a:T1,b:T2):T3
const jsFuncType = fc.tuple(
  fc.array(
    fc.tuple(
      fc.stringMatching(/^[a-zA-Z][a-zA-Z0-9]{0,5}$/),
      simpleType
    ),
    { minLength: 0, maxLength: 3 }
  ),
  simpleType
).map(([params, ret]) => {
  const paramStr = params.map(([n, t]) => `${n}:${t}`).join(',');
  return `function(${paramStr}):${ret}`;
});

// TS arrow function type: (a:T1,b:T2)=>T3
const tsArrowType = fc.tuple(
  fc.array(
    fc.tuple(
      fc.stringMatching(/^[a-zA-Z][a-zA-Z0-9]{0,5}$/),
      simpleType
    ),
    { minLength: 0, maxLength: 3 }
  ),
  simpleType
).map(([params, ret]) => {
  const paramStr = params.map(([n, t]) => `${n}:${t}`).join(',');
  return `(${paramStr})=>${ret}`;
});

// Props/object literal: {key1:T1,key2:T2}
const propsType = fc.array(
  fc.tuple(
    fc.stringMatching(/^[a-zA-Z][a-zA-Z0-9]{0,5}$/),
    simpleType
  ),
  { minLength: 1, maxLength: 3 }
).map(kvs => {
  return '{' + kvs.map(([k, v]) => `${k}:${v}`).join(',') + '}';
});

// Any valid type expression (weighted toward common patterns)
const anyValidType = fc.oneof(
  { weight: 5, arbitrary: simpleType },
  { weight: 2, arbitrary: arrayType },
  { weight: 2, arbitrary: objectType },
  { weight: 2, arbitrary: unionType },
  { weight: 1, arbitrary: parenUnionType },
  { weight: 1, arbitrary: jsFuncType },
  { weight: 1, arbitrary: tsArrowType },
  { weight: 1, arbitrary: propsType }
);

// --- Property-based tests ---

describe('type-parser property-based tests', () => {

  it('parse never throws on valid type strings', () => {
    fc.assert(
      fc.property(anyValidType, (typeStr) => {
        // parse should not throw; it returns AST or fallback string
        const result = parse(typeStr);
        assert.ok(result !== undefined, `parse returned undefined for: ${typeStr}`);
      }),
      { numRuns: 500 }
    );
  });

  it('parse returns non-null for non-empty type strings', () => {
    fc.assert(
      fc.property(anyValidType, (typeStr) => {
        const result = parse(typeStr);
        assert.ok(result !== null, `parse returned null for: ${typeStr}`);
      }),
      { numRuns: 500 }
    );
  });

  it('parse returns array AST for simple types', () => {
    fc.assert(
      fc.property(simpleType, (typeStr) => {
        const result = parse(typeStr);
        // Should be ['types', ['simple', ...]] or fallback string
        if (Array.isArray(result)) {
          assert.equal(result[0], 'types');
        }
      }),
      { numRuns: 200 }
    );
  });

  it('jsdocToTs never throws on valid type strings', () => {
    fc.assert(
      fc.property(anyValidType, (typeStr) => {
        // jsdocToTs should not throw
        const result = jsdocToTs(typeStr);
        assert.ok(result !== undefined, `jsdocToTs returned undefined for: ${typeStr}`);
      }),
      { numRuns: 500 }
    );
  });

  it('jsdocToTs returns a string for valid type strings', () => {
    fc.assert(
      fc.property(anyValidType, (typeStr) => {
        const result = jsdocToTs(typeStr);
        assert.equal(typeof result, 'string', `jsdocToTs did not return string for: ${typeStr}`);
      }),
      { numRuns: 500 }
    );
  });

  it('jsdocToTs converges: applying twice stabilizes the output', () => {
    // jsdocToTs is a one-way JSDoc→TS transform. Types like Function → (() => void)
    // may change whitespace on re-parse. But the output must stabilize after 2 passes.
    fc.assert(
      fc.property(anyValidType, (typeStr) => {
        const pass1 = jsdocToTs(typeStr);
        const pass2 = jsdocToTs(pass1);
        const pass3 = jsdocToTs(pass2);
        assert.equal(pass2, pass3,
          `Did not converge for: ${typeStr} -> ${pass1} -> ${pass2} -> ${pass3}`);
      }),
      { numRuns: 500 }
    );
  });

  it('jsdocToTs is idempotent for non-special simple types', () => {
    // Types that don't get transformed (no Function, Array, *) should be truly idempotent
    const nonSpecialType = fc.oneof(
      fc.constant('string'),
      fc.constant('number'),
      fc.constant('boolean'),
      fc.constant('void'),
      fc.constant('Date'),
      fc.constant('RegExp'),
      fc.constant('Element'),
      fc.constant('Node'),
      fc.stringMatching(/^[a-zA-Z][a-zA-Z0-9]*(\.[a-zA-Z][a-zA-Z0-9]*){0,3}$/)
    );
    fc.assert(
      fc.property(nonSpecialType, (typeStr) => {
        const once = jsdocToTs(typeStr);
        const twice = jsdocToTs(once);
        assert.equal(once, twice, `Not idempotent for: ${typeStr} -> ${once} -> ${twice}`);
      }),
      { numRuns: 300 }
    );
  });

  it('parse preserves union members count', () => {
    fc.assert(
      fc.property(
        fc.array(simpleTypeName, { minLength: 2, maxLength: 5 }),
        (types) => {
          const typeStr = types.join('|');
          const result = parse(typeStr);
          if (Array.isArray(result) && result[0] === 'types') {
            // Number of union members should match (AST is ['types', m1, m2, ...])
            assert.equal(result.length - 1, types.length,
              `Union member count mismatch for: ${typeStr}`);
          }
        }
      ),
      { numRuns: 200 }
    );
  });

  it('Array<T> parses to array AST node', () => {
    fc.assert(
      fc.property(simpleTypeName, (inner) => {
        const typeStr = `Array<${inner}>`;
        const result = parse(typeStr);
        if (Array.isArray(result) && result[0] === 'types') {
          const innerNode = result[1];
          assert.ok(Array.isArray(innerNode) && innerNode[0] === 'array',
            `Expected array AST for: ${typeStr}, got: ${JSON.stringify(result)}`);
        }
      }),
      { numRuns: 200 }
    );
  });

  it('Object<K,V> parses to object AST node', () => {
    fc.assert(
      fc.property(simpleTypeName, simpleTypeName, (k, v) => {
        const typeStr = `Object<${k},${v}>`;
        const result = parse(typeStr);
        if (Array.isArray(result) && result[0] === 'types') {
          const innerNode = result[1];
          assert.ok(Array.isArray(innerNode) && innerNode[0] === 'object',
            `Expected object AST for: ${typeStr}, got: ${JSON.stringify(result)}`);
        }
      }),
      { numRuns: 200 }
    );
  });

  it('function types parse without error', () => {
    fc.assert(
      fc.property(jsFuncType, (typeStr) => {
        const result = parse(typeStr);
        assert.ok(result !== null && result !== undefined,
          `Failed to parse function type: ${typeStr}`);
      }),
      { numRuns: 200 }
    );
  });

  it('TS arrow function types parse without error', () => {
    fc.assert(
      fc.property(tsArrowType, (typeStr) => {
        const result = parse(typeStr);
        assert.ok(result !== null && result !== undefined,
          `Failed to parse arrow type: ${typeStr}`);
      }),
      { numRuns: 200 }
    );
  });

  it('props/object literal types parse without error', () => {
    fc.assert(
      fc.property(propsType, (typeStr) => {
        const result = parse(typeStr);
        assert.ok(result !== null && result !== undefined,
          `Failed to parse props type: ${typeStr}`);
      }),
      { numRuns: 200 }
    );
  });

  it('jsdocToTs output does not contain JSDoc-specific syntax', () => {
    fc.assert(
      fc.property(anyValidType, (typeStr) => {
        const result = jsdocToTs(typeStr);
        // Should not contain JSDoc dot notation for generics (Array.<T>)
        assert.ok(!result.includes('.<'),
          `Output contains JSDoc dot notation: ${result} (from: ${typeStr})`);
      }),
      { numRuns: 500 }
    );
  });

  it('null input returns null from jsdocToTs', () => {
    assert.equal(jsdocToTs(null), null);
    assert.equal(jsdocToTs(undefined), undefined);
  });

  it('empty string returns empty from parse', () => {
    assert.equal(parse(''), null);
    assert.equal(parse('  '), null);
  });
});
