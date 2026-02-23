import test from 'node:test';
import assert from 'node:assert/strict';
import path from 'node:path';
import os from 'node:os';
import fs from 'fs-extra';
import { createJsdocConfig, runJsdocOnGroup } from '../src/jsdoc-runner.js';
import { normalizeAndFilterDoclets } from '../src/jsdoc-runner.js';

test('createJsdocConfig stores source includes', () => {
  const files = ['C:/tmp/a.js', 'C:/tmp/b.js'];
  const cfg = createJsdocConfig(files);
  assert.deepEqual(cfg, { source: { include: files } });
});

test('runJsdocOnGroup parses doclets using config-based invocation', async () => {
  const tmpDir = await fs.mkdtemp(path.join(os.tmpdir(), 'dts-jsdoc-test-'));
  try {
    const file = path.join(tmpDir, 'sample.js');
    await fs.writeFile(
      file,
      [
        '/**',
        ' * @namespace demo.ns',
        ' */',
        'var demo = demo || {};',
      ].join('\n'),
      'utf8'
    );

    const jsdocBin = process.platform === 'win32'
      ? path.resolve('node_modules/.bin/jsdoc.cmd')
      : path.resolve('node_modules/.bin/jsdoc');

    const doclets = await runJsdocOnGroup(jsdocBin, [file]);
    assert.equal(Array.isArray(doclets), true);
    assert.equal(doclets.some(d => d.longname === 'demo.ns' && d.kind === 'namespace'), true);
  } finally {
    await fs.remove(tmpDir);
  }
});

test('normalizeAndFilterDoclets drops inherited and private doclets', () => {
  const input = [
    { name: 'kept', access: 'public', inherited: true, longname: 'x#kept' },
    { name: 'removedPrivate', access: 'private', inherited: false, longname: 'x#removedPrivate' },
    { access: 'public', inherited: false, longname: 'x#removedNoName' },
    { name: 'keptProtectedFlagFalse', access: undefined, inherited: false, longname: 'x#kept2' }
  ];

  const result = normalizeAndFilterDoclets(input, '8.14.1');
  assert.equal(result.some(d => d.name === 'kept' && d.inherited === true), false);
  assert.equal(result.some(d => d.name === 'removedPrivate'), false);
  assert.equal(result.some(d => d.name === 'removedNoName'), false);
  assert.equal(result.some(d => d.name === 'keptProtectedFlagFalse'), true);
});
