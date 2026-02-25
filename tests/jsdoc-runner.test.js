import test from 'node:test';
import assert from 'node:assert/strict';
import { parseAdocFile, splitTypeNames, stripTypeModifiers, parseIdentifier } from '../src/adoc-parser.js';
import { normalizeAndFilterDoclets } from '../src/jsdoc-runner.js';

test('parseAdocFile extracts doclets from comment blocks', () => {
  const content = `
/**
 * A chart class.
 * @constructor
 * @extends {anychart.core.Chart}
 */
anychart.charts.Bullet;

/**
 * Getter for data.
 * @return {anychart.data.View} Current data.
 */
anychart.charts.Bullet.prototype.data;
`;
  const doclets = parseAdocFile(content, 'Bullet.adoc');
  assert.equal(doclets.length, 2);
  assert.equal(doclets[0].kind, 'class');
  assert.equal(doclets[0].name, 'Bullet');
  assert.deepEqual(doclets[0].augments, ['anychart.core.Chart']);
  assert.equal(doclets[1].kind, 'function');
  assert.equal(doclets[1].name, 'data');
  assert.equal(doclets[1].scope, 'instance');
});

test('splitTypeNames strips modifiers and splits unions', () => {
  assert.deepEqual(splitTypeNames('string|number'), ['string', 'number']);
  assert.deepEqual(splitTypeNames('...number'), ['number']);
  assert.deepEqual(splitTypeNames('?string'), ['string']);
  assert.deepEqual(splitTypeNames('string='), ['string']);
  assert.deepEqual(splitTypeNames('Array.<number>|...number'), ['Array.<number>', 'number']);
  assert.deepEqual(splitTypeNames('function(number):number'), ['function']);
});

test('parseIdentifier handles prototype and assignment syntax', () => {
  const inst = parseIdentifier('anychart.charts.Bullet.prototype.data;');
  assert.equal(inst.name, 'data');
  assert.equal(inst.memberof, 'anychart.charts.Bullet');
  assert.equal(inst.scope, 'instance');

  const assigned = parseIdentifier('anychart.editor.Editor.prototype.addClassName = function(){};');
  assert.equal(assigned.name, 'addClassName');
  assert.equal(assigned.scope, 'instance');
});

test('parseAdocFile handles @name tag without identifier line', () => {
  const content = `
/**
 * A callback type.
 * @typedef {function}
 * @name MyCallback
 * @param {string} data The data.
 */
`;
  const doclets = parseAdocFile(content, 'test.adoc');
  assert.equal(doclets.length, 1);
  assert.equal(doclets[0].name, 'MyCallback');
  assert.equal(doclets[0].kind, 'typedef');
});

test('parseAdocFile infers missing return types from sibling overloads', () => {
  const content = `
/**
 * Getter.
 * @return {anychart.core.utils.Margin} Margin.
 */
anychart.charts.Radar.prototype.margin;

/**
 * Setter single arg.
 * @param {Object} opt_margin Value.
 * @return {anychart.charts.Radar} Self.
 */
anychart.charts.Radar.prototype.margin;

/**
 * Setter multi arg (no @return).
 * @param {string} opt_value1 Top.
 * @param {string} opt_value2 Right.
 */
anychart.charts.Radar.prototype.margin;
`;
  const doclets = parseAdocFile(content, 'Radar.adoc');
  const setters = doclets.filter(d => d.params && d.params.length > 0);
  // Both setters should have returns (the 2nd one inferred from the 1st)
  assert.equal(setters.length, 2);
  assert.ok(setters[0].returns);
  assert.ok(setters[1].returns);
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
