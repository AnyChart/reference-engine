import test from 'node:test';
import assert from 'node:assert/strict';
import { structurize } from '../src/structurize.js';

test('structurize orders class methods alphabetically by name', () => {
  const doclets = [
    {
      kind: 'class',
      name: 'Sample',
      longname: 'anychart.Sample'
    },
    {
      kind: 'function',
      name: 'legend',
      longname: 'anychart.Sample#legend',
      memberof: 'anychart.Sample',
      params: [],
      returns: [{ type: { names: ['anychart.Sample'] } }]
    },
    {
      kind: 'function',
      name: 'dispose',
      longname: 'anychart.Sample#dispose',
      memberof: 'anychart.Sample',
      params: [],
      returns: [{ type: { names: ['anychart.Sample'] } }]
    },
    {
      kind: 'function',
      name: 'alpha',
      longname: 'anychart.Sample#alpha',
      memberof: 'anychart.Sample',
      params: [],
      returns: [{ type: { names: ['anychart.Sample'] } }]
    }
  ];

  const topLevel = structurize(doclets);
  const sample = topLevel.classes.find(c => c.fullName === 'anychart.Sample');
  assert.deepEqual(sample.methods.map(m => m.name), ['alpha', 'dispose', 'legend']);
});
