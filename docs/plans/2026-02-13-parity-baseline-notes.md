# 2026-02-13 Parity Baseline Notes

Baseline generated with:

```bash
node src/cli.js --data-dir ./data --version 8.14.1 --jsdoc-bin ./node_modules/.bin/jsdoc --max-groups 8 --output-dir ./.tmp-verify
git --no-pager diff --no-index index.d.ts ./.tmp-verify/8.14.1/index.d.ts > ./.tmp-verify/parity.diff
```

Quick metrics:
- Diff changed lines (added+removed): `12220`
- Diff total lines (with context): `25452`

Heuristic mismatch category counts (hunk-level):
- method ordering/grouping: `508`
- inheritance filtering: `9`
- typedef expansion: `519`
- enum formatting: `2`
- namespace/module shape: `16`
- other: `4`

Note:
- Category counts are automated heuristics from `.tmp-verify/parity.diff`.
- They are directional guidance for prioritization, not exact semantic totals.

---

Iteration check (after inherited-doclet retention change):
- `parity_final_changed_lines`: `11577`
- `parity_final_hunks`: `868`

Signature multiset comparison (`index.d.ts` vs generated):
- Missing signatures: `8648`
- Extra signatures: `34`

Top missing signatures include inherited base APIs:
- `listen(type: string, listener: ((e:Object)=>void), useCapture?: boolean, listenerScope?: Object): Object;` (249)
- `listenOnce(type: string, listener: ((e:Object)=>void), useCapture?: boolean, listenerScope?: Object): Object;` (249)
- `unlisten(type: string, listener: ((e:Object)=>boolean), useCapture?: boolean, listenerScope?: Object): boolean;` (249)
- `removeAllListeners(type?: string): number;` (249)
- `parentBounds(): anychart.math.Rect;` (210)

Root-cause evidence:
- Unfiltered raw JSDoc contains inherited members such as `parentBounds`/`dispose` for chart classes.
- Missing-signature concentration indicates remaining mismatch is dominated by inheritance propagation semantics, not enum or typedef formatting.

---

Iteration 2 baseline refresh (2026-02-13, pre-task-3 implementation validation):

Commands:

```bash
node src/cli.js --data-dir ./data --version 8.14.1 --jsdoc-bin ./node_modules/.bin/jsdoc --max-groups 8 --output-dir ./.tmp-verify
git --no-pager diff --no-index index.d.ts ./.tmp-verify/8.14.1/index.d.ts > ./.tmp-verify/parity-final.diff
```

Fresh diff metrics:
- `parity_final_changed_lines`: `10522`
- `parity_final_hunks`: `1073`

Method-signature multiset snapshot (`;`-terminated lines with `(`):
- Missing signatures: `2902`
- Extra signatures: `15`

Top missing signatures (count x signature):
- `24` x `getIndex(): number;`
- `20` x `disablePointerEvents(): boolean;`
- `20` x `excludePoint(indexes: number | Array<number>): boolean;`
- `20` x `fontFamily(): string;`
- `20` x `getExcludedPoints(): Array<anychart.core.Point>;`
- `20` x `includeAllPoints(): boolean;`
- `20` x `includePoint(indexes: number | Array<number>): boolean;`
- `20` x `keepOnlyPoints(indexes: number | Array<number>): void;`
- `20` x `selectable(): boolean;`
- `20` x `textIndent(): number;`
- `19` x `fontDecoration(): anychart.graphics.vector.Text.Decoration | string;`
- `19` x `fontStyle(): anychart.graphics.vector.Text.FontStyle | string;`
- `19` x `fontVariant(): anychart.graphics.vector.Text.FontVariant | string;`
- `19` x `letterSpacing(): string | number;`
- `19` x `lineHeight(): string | number;`
- `19` x `textDirection(): anychart.graphics.vector.Text.Direction | string;`
- `19` x `textOverflow(): anychart.graphics.vector.Text.TextOverflow | string;`
- `19` x `wordBreak(): string;`
- `19` x `wordWrap(): string;`
- `18` x `fontOpacity(): number;`

---

Iteration 3 targeted getter-backfill check (2026-02-13):

- Change scope:
  - Added a constrained parent-only getter backfill for selected style methods in `src/inheritance.js`.
  - Added positive/negative guard tests in `tests/inheritance.test.js`.

- Post-change parity snapshot:
  - `parity_final_changed_lines`: `10528` (improved from `10545`)
  - `parity_final_hunks`: `1075` (slightly up from `1073`)
  - Missing signatures: `2882` (improved from `2913`)
  - Extra signatures: `15` (unchanged)

- Outcome:
  - Keep change (small net improvement with no extra-signature regression).
  - Remaining dominant missing families are still `getIndex`, text/style getters, and point-selection methods.

---

Iteration 4 point/index-family pass (2026-02-13):

- Task-1 baseline captured at loop start:
  - `parity_final_changed_lines`: `10575`
  - `parity_final_hunks`: `1065`
  - Missing signatures: `2979`
  - Extra signatures: `15`

- Root-cause signal from interface-gap report:
  - `getIndex()` missing in interfaces that already contain sibling point-family methods (`Area`, `Candlestick`, `Marker`, etc.).
  - Full point-selection family missing in several interfaces (`Choropleth`, `Connector`, `Moment`, `Polygon`, `Polyline`, `Range`).

- Change applied:
  - Added targeted point/index family methods to inheritance allowlist in `src/inheritance.js`:
    - `getIndex`, `excludePoint`, `includePoint`, `includeAllPoints`, `keepOnlyPoints`, `getExcludedPoints`.
  - Added focused inheritance tests for:
    - unresolved inheritdoc propagation of `getIndex`;
    - full point-family retention for inheritdoc classes without local members.

- Post-change verification (repeat run):
  - `parity_final_changed_lines`: `10518`
  - `parity_final_hunks`: `1088`
  - Missing signatures: `2759`
  - Extra signatures: `15`

- Outcome:
  - Keep change (improves changed lines and missing signatures; no extra-signature regression).
  - `getIndex` and point-selection methods are no longer in top-missing signatures after this pass.
