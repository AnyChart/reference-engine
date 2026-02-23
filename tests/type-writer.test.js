import test from 'node:test';
import assert from 'node:assert/strict';
import { jsdocToTs } from '../src/types/type-writer.js';

test('Object.<T> is rendered as string-keyed map type', () => {
  assert.equal(jsdocToTs('Object.<string>'), '{[prop:string]:string}');
  assert.equal(
    jsdocToTs('Object.<Array.<(number|string)>>'),
    '{[prop:string]:Array<number|string>}'
  );
});

test('Object.<T> keeps union/object payload as map value', () => {
  assert.equal(
    jsdocToTs('Object.<{column:(number|string),type:(string|string),weights:(number|string)}|number|string>'),
    '{[prop:string]:{column:number|string,type:string|string,weights:number|string}|number|string}'
  );
  assert.equal(
    jsdocToTs('Object.<string,{column:(number|string),type:(string|string),weights:(number|string)}|number|string>'),
    '{[prop:string]:{column:number|string,type:string|string,weights:number|string}|number|string}'
  );
});

test('Object.<{...}> keeps plain object payload without map wrapping', () => {
  assert.equal(
    jsdocToTs('Object.<{enabled:boolean}>'),
    '{enabled:boolean}'
  );
});

test('format object property keeps Function token for parity', () => {
  assert.equal(
    jsdocToTs('Object.<{format:Function}>'),
    '{format:Function}'
  );
});
