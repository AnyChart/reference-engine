import test from 'node:test';
import assert from 'node:assert/strict';
import { removeIndexRectSpecialCase } from '../src/pipeline.js';

test('removeIndexRectSpecialCase removes anychart.graphics.math.rect from namespace functions', () => {
  const topLevel = {
    namespaces: [
      {
        fullName: 'anychart.graphics.math',
        name: 'math',
        constants: [],
        functions: [
          { name: 'rect' },
          { name: 'round' }
        ],
        typedefs: [],
        enums: [],
        classes: []
      },
      {
        fullName: 'anychart.graphics.vector',
        name: 'vector',
        constants: [],
        functions: [
          { name: 'rect' }
        ],
        typedefs: [],
        enums: [],
        classes: []
      }
    ],
    classes: [],
    typedefs: [],
    enums: []
  };

  const result = removeIndexRectSpecialCase(topLevel);
  const math = result.namespaces.find(ns => ns.fullName === 'anychart.graphics.math');
  const vector = result.namespaces.find(ns => ns.fullName === 'anychart.graphics.vector');

  assert.deepEqual(math.functions.map(f => f.name), ['round']);
  assert.deepEqual(vector.functions.map(f => f.name), ['rect']);
});
