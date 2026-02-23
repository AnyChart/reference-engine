import test from 'node:test';
import assert from 'node:assert/strict';
import { generateTSDeclarations } from '../src/ts-generator.js';

test('string enum fields are emitted without explicit value assignment', () => {
  const topLevel = {
    namespaces: [
      {
        fullName: 'anychart.test',
        name: 'test',
        constants: [],
        functions: [],
        typedefs: [],
        enums: ['anychart.test.Mode'],
        classes: []
      }
    ],
    classes: [],
    typedefs: [],
    enums: [
      {
        name: 'Mode',
        fullName: 'anychart.test.Mode',
        fields: [
          { name: 'AUTO', value: 'auto' },
          { name: 'COUNT', value: 2 }
        ]
      }
    ]
  };

  const out = generateTSDeclarations('8.14.1', topLevel);
  assert.equal(out.includes('AUTO = "auto"'), false);
  assert.equal(out.includes('AUTO'), true);
  assert.equal(out.includes('COUNT = 2'), true);
});

test('only typedefs with type[0] === function are treated as function typedefs', () => {
  const topLevel = {
    namespaces: [
      {
        fullName: 'anychart.test',
        name: 'test',
        constants: [],
        functions: [
          {
            name: 'use',
            params: [{ name: 'cb', types: ['anychart.test.Fn'] }],
            returns: []
          }
        ],
        typedefs: ['anychart.test.Fn', 'anychart.test.Pseudo'],
        enums: [],
        classes: []
      }
    ],
    classes: [],
    typedefs: [
      {
        name: 'Fn',
        fullName: 'anychart.test.Fn',
        type: ['function'],
        params: [{ name: 'a', types: ['number'] }],
        returns: [{ types: ['string'] }],
        properties: []
      },
      {
        name: 'Pseudo',
        fullName: 'anychart.test.Pseudo',
        type: ['Object'],
        params: [{ name: 'x', types: ['number'] }],
        returns: [{ types: ['number'] }],
        properties: []
      }
    ],
    enums: []
  };

  const out = generateTSDeclarations('8.14.1', topLevel);
  assert.equal(out.includes('function use(cb: ((a:number)=>string)): void;'), true);
  assert.equal(out.includes('type Pseudo = Object;'), true);
});
