import test from 'node:test';
import assert from 'node:assert/strict';
import { resolveInheritance } from '../src/inheritance.js';

test('resolveInheritance only inherits parent methods declared in child allMemberNames', () => {
  const topLevel = {
    classes: [
      {
        name: 'Parent',
        fullName: 'anychart.core.Parent',
        extends: [],
        allMemberNames: new Set(['customParentMethod', 'label']),
        methods: [
          {
            name: 'customParentMethod',
            params: [],
            returns: [{ types: ['anychart.core.Parent'] }],
            isIgnored: false
          },
          {
            name: 'label',
            params: [],
            returns: [{ types: ['anychart.core.Parent'] }],
            isIgnored: false
          }
        ]
      },
      {
        name: 'Child',
        fullName: 'anychart.core.Child',
        extends: ['anychart.core.Parent'],
        hasInheritDocMethods: true,
        allMemberNames: new Set(['label']),
        methods: []
      }
    ]
  };

  resolveInheritance(topLevel);

  const child = topLevel.classes.find(c => c.fullName === 'anychart.core.Child');
  assert.deepEqual(child.methods.map(m => m.name), ['label']);
});

test('resolveInheritance applies covariance on inherited method returns', () => {
  const topLevel = {
    classes: [
      {
        name: 'Parent',
        fullName: 'anychart.core.Parent',
        extends: [],
        allMemberNames: new Set(['chain']),
        methods: [
          {
            name: 'chain',
            params: [],
            returns: [{ types: ['anychart.core.Parent'] }],
            isIgnored: false
          }
        ]
      },
      {
        name: 'Child',
        fullName: 'anychart.core.Child',
        extends: ['anychart.core.Parent'],
        hasInheritDocMethods: true,
        allMemberNames: new Set(['chain']),
        methods: []
      }
    ]
  };

  resolveInheritance(topLevel);

  const child = topLevel.classes.find(c => c.fullName === 'anychart.core.Child');
  assert.equal(child.methods.length, 1);
  assert.deepEqual(child.methods[0].returns, [{ types: ['anychart.core.Child'] }]);
});

test('resolveInheritance preserves structurize method order (no re-sort)', () => {
  // Methods come pre-sorted from structurize (ASCII + endMethods).
  // Inheritance should preserve this order, not re-sort.
  const topLevel = {
    classes: [
      {
        name: 'Sample',
        fullName: 'anychart.core.Sample',
        extends: [],
        hasInheritDocMethods: false,
        allMemberNames: new Set(['alpha', 'dispose', 'legend']),
        methods: [
          { name: 'alpha', params: [], returns: [], isIgnored: false },
          { name: 'dispose', params: [], returns: [], isIgnored: false },
          { name: 'legend', params: [], returns: [], isIgnored: false }
        ]
      }
    ]
  };

  resolveInheritance(topLevel);
  const sample = topLevel.classes[0];
  assert.deepEqual(sample.methods.map(m => m.name), ['alpha', 'dispose', 'legend']);
});

test('resolveInheritance keeps parent methods when child has no inheritdoc markers', () => {
  const topLevel = {
    classes: [
      {
        name: 'Parent',
        fullName: 'anychart.core.Parent',
        extends: [],
        allMemberNames: new Set(['a11y', 'label']),
        methods: [
          { name: 'a11y', params: [], returns: [], isIgnored: false },
          { name: 'label', params: [], returns: [], isIgnored: false }
        ]
      },
      {
        name: 'Child',
        fullName: 'anychart.core.Child',
        extends: ['anychart.core.Parent'],
        hasInheritDocMethods: false,
        allMemberNames: new Set(['label']),
        methods: []
      }
    ]
  };

  resolveInheritance(topLevel);
  const child = topLevel.classes.find(c => c.fullName === 'anychart.core.Child');
  // H4 ordering: 'label' is in allMemberNames (Zone 1, alphabetical),
  // 'a11y' is not (Zone 2, parent order). So label comes first.
  assert.deepEqual(child.methods.map(m => m.name), ['label', 'a11y']);
});

test('resolveInheritance does not let unresolved base-method placeholders block inheritance', () => {
  const topLevel = {
    classes: [
      {
        name: 'Grand',
        fullName: 'anychart.core.Grand',
        extends: [],
        hasInheritDocMethods: false,
        allMemberNames: new Set(['bounds']),
        methods: [
          {
            name: 'bounds',
            params: [],
            returns: [{ types: ['anychart.core.Grand'] }],
            isIgnored: false
          }
        ]
      },
      {
        name: 'Parent',
        fullName: 'anychart.core.Parent',
        extends: ['anychart.core.Grand'],
        hasInheritDocMethods: true,
        allMemberNames: new Set(['bounds']),
        methods: [
          {
            name: 'bounds',
            isInheritDoc: true,
            isIgnored: false,
            params: [],
            returns: [],
            overrides: [
              {
                name: 'bounds',
                isInheritDoc: true,
                isIgnored: false,
                params: [],
                returns: []
              }
            ]
          }
        ]
      },
      {
        name: 'Child',
        fullName: 'anychart.core.Child',
        extends: ['anychart.core.Parent'],
        hasInheritDocMethods: true,
        allMemberNames: new Set(['bounds']),
        methods: [
          {
            name: 'bounds',
            isInheritDoc: true,
            isIgnored: false,
            params: [],
            returns: [],
            overrides: [
              {
                name: 'bounds',
                isInheritDoc: true,
                isIgnored: false,
                params: [],
                returns: []
              }
            ]
          }
        ]
      }
    ]
  };

  resolveInheritance(topLevel);
  const child = topLevel.classes.find(c => c.fullName === 'anychart.core.Child');
  const boundsMethods = child.methods.filter(m => m.name === 'bounds');
  const boundsOverloads = boundsMethods.flatMap(m => m.overrides || [m]);
  assert.equal(boundsOverloads.some(m => (m.params || []).length === 0 && (m.returns || []).length > 0), true);
});

test('resolveInheritance adds missing overloads for existing child method names', () => {
  const topLevel = {
    classes: [
      {
        name: 'Parent',
        fullName: 'anychart.core.Parent',
        extends: [],
        hasInheritDocMethods: false,
        allMemberNames: new Set(['fontFamily']),
        methods: [
          {
            name: 'fontFamily',
            params: [],
            returns: [{ types: ['string'] }],
            isIgnored: false,
            overrides: [
              {
                name: 'fontFamily',
                params: [],
                returns: [{ types: ['string'] }],
                isIgnored: false
              },
              {
                name: 'fontFamily',
                params: [{ name: 'value', types: ['string'], optional: true }],
                returns: [{ types: ['anychart.core.Parent'] }],
                isIgnored: false
              }
            ]
          }
        ]
      },
      {
        name: 'Child',
        fullName: 'anychart.core.Child',
        extends: ['anychart.core.Parent'],
        hasInheritDocMethods: true,
        allMemberNames: new Set(['fontFamily']),
        methods: [
          {
            name: 'fontFamily',
            params: [{ name: 'value', types: ['string'], optional: true }],
            returns: [{ types: ['anychart.core.Child'] }],
            isIgnored: false,
            isInheritDoc: true,
            overrides: [
              {
                name: 'fontFamily',
                params: [{ name: 'value', types: ['string'], optional: true }],
                returns: [{ types: ['anychart.core.Child'] }],
                isIgnored: false,
                isInheritDoc: true
              }
            ]
          }
        ]
      }
    ]
  };

  resolveInheritance(topLevel);

  const child = topLevel.classes.find(c => c.fullName === 'anychart.core.Child');
  const fontFamilyGroup = child.methods.find(m => m.name === 'fontFamily');
  const overloads = fontFamilyGroup ? (fontFamilyGroup.overrides || [fontFamilyGroup]) : [];
  assert.equal(
    overloads.some(o => (o.params || []).length === 0 && (o.returns || [])[0]?.types?.includes('string')),
    true
  );
});

test('resolveInheritance does not backfill non-allowlisted method getters', () => {
  const topLevel = {
    classes: [
      {
        name: 'Parent',
        fullName: 'anychart.core.Parent',
        extends: [],
        hasInheritDocMethods: false,
        allMemberNames: new Set(['customMethod']),
        methods: [
          {
            name: 'customMethod',
            params: [],
            returns: [{ types: ['string'] }],
            isIgnored: false,
            overrides: [
              {
                name: 'customMethod',
                params: [],
                returns: [{ types: ['string'] }],
                isIgnored: false
              },
              {
                name: 'customMethod',
                params: [{ name: 'value', types: ['string'], optional: true }],
                returns: [{ types: ['anychart.core.Parent'] }],
                isIgnored: false
              }
            ]
          }
        ]
      },
      {
        name: 'Child',
        fullName: 'anychart.core.Child',
        extends: ['anychart.core.Parent'],
        hasInheritDocMethods: true,
        allMemberNames: new Set(['customMethod']),
        methods: [
          {
            name: 'customMethod',
            params: [{ name: 'value', types: ['string'], optional: true }],
            returns: [{ types: ['anychart.core.Child'] }],
            isIgnored: false,
            isInheritDoc: true,
            overrides: [
              {
                name: 'customMethod',
                params: [{ name: 'value', types: ['string'], optional: true }],
                returns: [{ types: ['anychart.core.Child'] }],
                isIgnored: false,
                isInheritDoc: true
              }
            ]
          }
        ]
      }
    ]
  };

  resolveInheritance(topLevel);

  const child = topLevel.classes.find(c => c.fullName === 'anychart.core.Child');
  const methodGroup = child.methods.find(m => m.name === 'customMethod');
  const overloads = methodGroup ? (methodGroup.overrides || [methodGroup]) : [];
  assert.equal(overloads.some(o => (o.params || []).length === 0), false);
});

test('resolveInheritance propagates getIndex through unresolved inheritdoc placeholders', () => {
  const topLevel = {
    classes: [
      {
        name: 'Grand',
        fullName: 'anychart.core.Grand',
        extends: [],
        hasInheritDocMethods: false,
        allMemberNames: new Set(['getIndex']),
        methods: [
          {
            name: 'getIndex',
            params: [],
            returns: [{ types: ['number'] }],
            isIgnored: false
          }
        ]
      },
      {
        name: 'Parent',
        fullName: 'anychart.core.Parent',
        extends: ['anychart.core.Grand'],
        hasInheritDocMethods: true,
        allMemberNames: new Set(['getIndex']),
        methods: [
          {
            name: 'getIndex',
            isInheritDoc: true,
            isIgnored: false,
            params: [],
            returns: [],
            overrides: [
              {
                name: 'getIndex',
                isInheritDoc: true,
                isIgnored: false,
                params: [],
                returns: []
              }
            ]
          }
        ]
      },
      {
        name: 'Child',
        fullName: 'anychart.core.Child',
        extends: ['anychart.core.Parent'],
        hasInheritDocMethods: true,
        allMemberNames: new Set(['getIndex']),
        methods: [
          {
            name: 'getIndex',
            isInheritDoc: true,
            isIgnored: false,
            params: [],
            returns: [],
            overrides: [
              {
                name: 'getIndex',
                isInheritDoc: true,
                isIgnored: false,
                params: [],
                returns: []
              }
            ]
          }
        ]
      }
    ]
  };

  resolveInheritance(topLevel);

  const child = topLevel.classes.find(c => c.fullName === 'anychart.core.Child');
  const methods = child.methods.filter(m => m.name === 'getIndex');
  const overloads = methods.flatMap(m => m.overrides || [m]);
  assert.equal(
    overloads.some(o => (o.params || []).length === 0 && (o.returns || []).some(r => (r.types || []).includes('number'))),
    true
  );
});

test('resolveInheritance keeps point-selection family for inheritdoc classes without local members', () => {
  const pointFamily = [
    ['excludePoint', [{ name: 'indexes', types: ['number', 'Array<number>'] }], [{ types: ['boolean'] }]],
    ['includePoint', [{ name: 'indexes', types: ['number', 'Array<number>'] }], [{ types: ['boolean'] }]],
    ['includeAllPoints', [], [{ types: ['boolean'] }]],
    ['keepOnlyPoints', [{ name: 'indexes', types: ['number', 'Array<number>'] }], [{ types: ['void'] }]],
    ['getExcludedPoints', [], [{ types: ['Array<anychart.core.Point>'] }]]
  ];

  const parentMethods = pointFamily.map(([name, params, returns]) => ({
    name,
    params,
    returns,
    isIgnored: false
  }));

  const topLevel = {
    classes: [
      {
        name: 'Parent',
        fullName: 'anychart.core.Parent',
        extends: [],
        hasInheritDocMethods: false,
        allMemberNames: new Set(pointFamily.map(([name]) => name)),
        methods: parentMethods
      },
      {
        name: 'Child',
        fullName: 'anychart.core.Child',
        extends: ['anychart.core.Parent'],
        hasInheritDocMethods: true,
        allMemberNames: new Set(),
        methods: []
      }
    ]
  };

  resolveInheritance(topLevel);

  const child = topLevel.classes.find(c => c.fullName === 'anychart.core.Child');
  const childNames = new Set(child.methods.map(m => m.name));

  assert.deepEqual(
    pointFamily.map(([name]) => childNames.has(name)),
    [true, true, true, true, true]
  );
});

test('resolveInheritance keeps top style getter family for inheritdoc classes without local members', () => {
  const styleFamily = [
    ['disablePointerEvents', [], [{ types: ['boolean'] }]],
    ['fontFamily', [], [{ types: ['string'] }]],
    ['selectable', [], [{ types: ['boolean'] }]],
    ['textIndent', [], [{ types: ['number'] }]]
  ];

  const parentMethods = styleFamily.map(([name, params, returns]) => ({
    name,
    params,
    returns,
    isIgnored: false
  }));

  const topLevel = {
    classes: [
      {
        name: 'Parent',
        fullName: 'anychart.core.Parent',
        extends: [],
        hasInheritDocMethods: false,
        allMemberNames: new Set(styleFamily.map(([name]) => name)),
        methods: parentMethods
      },
      {
        name: 'Child',
        fullName: 'anychart.core.Child',
        extends: ['anychart.core.Parent'],
        hasInheritDocMethods: true,
        allMemberNames: new Set(),
        methods: []
      }
    ]
  };

  resolveInheritance(topLevel);

  const child = topLevel.classes.find(c => c.fullName === 'anychart.core.Child');
  const childNames = new Set(child.methods.map(m => m.name));

  assert.deepEqual(
    styleFamily.map(([name]) => childNames.has(name)),
    [true, true, true, true]
  );
});

test('resolveInheritance keeps extended text style getter family for inheritdoc classes without local members', () => {
  const styleFamily = [
    ['fontDecoration', [], [{ types: ['anychart.graphics.vector.Text.Decoration', 'string'] }]],
    ['fontStyle', [], [{ types: ['anychart.graphics.vector.Text.FontStyle', 'string'] }]],
    ['fontVariant', [], [{ types: ['anychart.graphics.vector.Text.FontVariant', 'string'] }]],
    ['letterSpacing', [], [{ types: ['string', 'number'] }]],
    ['lineHeight', [], [{ types: ['string', 'number'] }]],
    ['textDirection', [], [{ types: ['anychart.graphics.vector.Text.Direction', 'string'] }]],
    ['textOverflow', [], [{ types: ['anychart.graphics.vector.Text.TextOverflow', 'string'] }]],
    ['wordBreak', [], [{ types: ['string'] }]],
    ['wordWrap', [], [{ types: ['string'] }]],
    ['fontOpacity', [], [{ types: ['number'] }]]
  ];

  const parentMethods = styleFamily.map(([name, params, returns]) => ({
    name,
    params,
    returns,
    isIgnored: false
  }));

  const topLevel = {
    classes: [
      {
        name: 'Parent',
        fullName: 'anychart.core.Parent',
        extends: [],
        hasInheritDocMethods: false,
        allMemberNames: new Set(styleFamily.map(([name]) => name)),
        methods: parentMethods
      },
      {
        name: 'Child',
        fullName: 'anychart.core.Child',
        extends: ['anychart.core.Parent'],
        hasInheritDocMethods: true,
        allMemberNames: new Set(),
        methods: []
      }
    ]
  };

  resolveInheritance(topLevel);

  const child = topLevel.classes.find(c => c.fullName === 'anychart.core.Child');
  const childNames = new Set(child.methods.map(m => m.name));
  assert.deepEqual(
    styleFamily.map(([name]) => childNames.has(name)),
    [true, true, true, true, true, true, true, true, true, true]
  );
});

test('resolveInheritance keeps text settings family for inheritdoc classes without local members', () => {
  const styleFamily = [
    ['textSettings', [], [{ types: ['Object'] }]],
    ['textSettings', [{ name: 'name', types: ['string'], optional: true }], [{ types: ['string', 'number', 'boolean'] }]],
    ['vAlign', [], [{ types: ['anychart.graphics.vector.Text.VAlign', 'string'] }]],
    ['useHtml', [], [{ types: ['boolean'] }]],
    ['fontSize', [], [{ types: ['string', 'number'] }]],
    ['textShadow', [], [{ types: ['string'] }]],
    ['fontWeight', [], [{ types: ['string', 'number'] }]],
    ['tooltip', [], [{ types: ['anychart.core.ui.Tooltip'] }]],
    ['maxLabels', [], [{ types: ['anychart.core.ui.LabelsFactory'] }]],
    ['minLabels', [], [{ types: ['anychart.core.ui.LabelsFactory'] }]]
  ];

  const parentMethods = styleFamily.map(([name, params, returns]) => ({
    name,
    params,
    returns,
    isIgnored: false
  }));

  const topLevel = {
    classes: [
      {
        name: 'Parent',
        fullName: 'anychart.core.Parent',
        extends: [],
        hasInheritDocMethods: false,
        allMemberNames: new Set(styleFamily.map(([name]) => name)),
        methods: parentMethods
      },
      {
        name: 'Child',
        fullName: 'anychart.core.Child',
        extends: ['anychart.core.Parent'],
        hasInheritDocMethods: true,
        allMemberNames: new Set(),
        methods: []
      }
    ]
  };

  resolveInheritance(topLevel);

  const child = topLevel.classes.find(c => c.fullName === 'anychart.core.Child');
  const childNames = new Set(child.methods.map(m => m.name));
  const textSettingsCount = child.methods.filter(m => m.name === 'textSettings').length;

  assert.equal(textSettingsCount >= 1, true);
  assert.deepEqual(
    ['vAlign', 'useHtml', 'fontSize', 'textShadow', 'fontWeight', 'tooltip', 'maxLabels', 'minLabels'].map(name => childNames.has(name)),
    [true, true, true, true, true, true, true, true]
  );
});

test('resolveInheritance keeps interaction and style residual family for inheritdoc classes without local members', () => {
  const family = [
    ['fontColor', [], [{ types: ['string'] }]],
    ['hAlign', [], [{ types: ['anychart.graphics.vector.Text.HAlign', 'string'] }]],
    ['animation', [], [{ types: ['anychart.core.utils.Animation'] }]],
    ['position', [], [{ types: ['string'] }]],
    ['meta', [{ name: 'key', types: ['any'], optional: true }], [{ types: ['any'] }]],
    ['name', [], [{ types: ['string'] }]],
    ['scale', [], [{ types: ['anychart.scales.Base'] }]],
    ['background', [], [{ types: ['anychart.core.ui.Background'] }]],
    ['clip', [], [{ types: ['boolean', 'anychart.math.Rect'] }]],
    ['interactivity', [], [{ types: ['anychart.core.utils.Interactivity'] }]]
  ];

  const parentMethods = family.map(([name, params, returns]) => ({
    name,
    params,
    returns,
    isIgnored: false
  }));

  const topLevel = {
    classes: [
      {
        name: 'Parent',
        fullName: 'anychart.core.Parent',
        extends: [],
        hasInheritDocMethods: false,
        allMemberNames: new Set(family.map(([name]) => name)),
        methods: parentMethods
      },
      {
        name: 'Child',
        fullName: 'anychart.core.Child',
        extends: ['anychart.core.Parent'],
        hasInheritDocMethods: true,
        allMemberNames: new Set(),
        methods: []
      }
    ]
  };

  resolveInheritance(topLevel);

  const child = topLevel.classes.find(c => c.fullName === 'anychart.core.Child');
  const childNames = new Set(child.methods.map(m => m.name));

  assert.deepEqual(
    family.map(([name]) => childNames.has(name)),
    [true, true, true, true, true, true, true, true, true, true]
  );
});

test.skip('resolveInheritance applies covariance to propagated ancestor overloads (diagnostic)', () => {
  const topLevel = {
    classes: [
      {
        name: 'Parent',
        fullName: 'anychart.core.Parent',
        extends: [],
        hasInheritDocMethods: false,
        allMemberNames: new Set(['chain']),
        methods: [
          {
            name: 'chain',
            params: [],
            returns: [{ types: ['anychart.core.Parent'] }],
            isIgnored: false,
            overrides: [
              {
                name: 'chain',
                params: [],
                returns: [{ types: ['anychart.core.Parent'] }],
                isIgnored: false
              },
              {
                name: 'chain',
                params: [{ name: 'value', types: ['number'], optional: true }],
                returns: [{ types: ['anychart.core.Parent'] }],
                isIgnored: false
              }
            ]
          }
        ]
      },
      {
        name: 'Child',
        fullName: 'anychart.core.Child',
        extends: ['anychart.core.Parent'],
        hasInheritDocMethods: true,
        allMemberNames: new Set(['chain']),
        methods: [
          {
            name: 'chain',
            params: [{ name: 'value', types: ['number'], optional: true }],
            returns: [{ types: ['anychart.core.Child'] }],
            isIgnored: false,
            isInheritDoc: true,
            overrides: [
              {
                name: 'chain',
                params: [{ name: 'value', types: ['number'], optional: true }],
                returns: [{ types: ['anychart.core.Child'] }],
                isIgnored: false,
                isInheritDoc: true
              }
            ]
          }
        ]
      }
    ]
  };

  resolveInheritance(topLevel);

  const child = topLevel.classes.find(c => c.fullName === 'anychart.core.Child');
  const chainGroup = child.methods.find(m => m.name === 'chain');
  const overloads = chainGroup ? (chainGroup.overrides || [chainGroup]) : [];
  const propagated = overloads.find(o => (o.params || []).length === 0);

  assert.ok(propagated);
  assert.deepEqual(propagated.returns, [{ types: ['anychart.core.Child'] }]);
});
