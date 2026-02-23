# Current Gap Analysis — 2026-02-20

## Baseline Metrics
- **Total diff lines:** 20,583
- **Total hunks:** 1,229
- **Removed lines (-):** 4,451
- **Added lines (+):** 4,232
- **missing_total:** 24 method signatures
- **extra_total:** 0 method signatures (8 enum→const + 1 type degradation)

## Category Breakdown

| Category | Lines | Hunks | % of Diff |
|----------|-------|-------|-----------|
| METHOD_ORDER | ~7,800 | ~1,100 | ~95% |
| METHOD_MISSING | ~24 sigs | ~15 | <1% |
| METHOD_EXTRA | 0 | 0 | 0% |
| TYPE_DIFF | ~4 | 1 | <1% |
| TYPEDEF_DIFF | ~80 | ~15 | ~1% |
| NAMESPACE_DIFF | ~280 | ~35 | ~3% |
| OVERLOAD_ORDER | ~6 | 2 | <1% |
| OTHER | ~6 | 3 | <1% |

## METHOD_ORDER Root Causes (3 sub-issues)

### A. Tail Methods (~60-70% of hunks)
Inherited methods from base classes should be at the END of each class, not alphabetically sorted into the body. The reference places these after `zIndex()`:
- `container()`, `parentBounds()`, `print()`, `listen()`, `listenOnce()`, `removeAllListeners()`, `unlisten()`, `unlistenByKey()` (from VisualBase)
- `dispose()`, `startSelectRectangleMarquee()`, `animation()`, `getSelectedPoints()`, `getStat()`, `legend()`, `tooltip()`, etc. (from SeparateChart/Chart)

### B. localeCompare vs ASCII (~10-15% of hunks)
The generator uses `localeCompare` (case-insensitive) while the reference uses case-sensitive ASCII comparison (`<` operator). This causes:
- `tooltip` sorts before `toSvg` (should be after with ASCII)
- `selected` sorts before `selectRectangleMarqueeFill` (should be after with ASCII)

### C. selected vs selectRectangle (~5-10% of hunks)
Subcase of B. Every chart class has this swap.

## Fix: Change inheritance.js sort from:
```js
cl.methods.sort((a, b) => a.name.localeCompare(b.name));
```
To: Two-zone sort with ASCII comparison. Zone 1 (isDirect) alphabetical by ASCII. Zone 2 (inherited tail) appended at end.

## METHOD_MISSING (24 signatures)

| Class | Missing Methods | Root Cause |
|-------|----------------|-----------|
| anychart.charts.Gantt/Sparkline/TagCloud/Venn | startSelectRectangleMarquee | inheritance filter |
| anychart.charts.Polar | shareAsJpg/Pdf/Png/Svg, toA11yTable, toHtmlTable | inheritance filter |
| anychart.charts.Stock | noData (2 overloads) | inheritance filter |
| anychart.charts.TreeMap | saveAsCsv/Json/Xlsx/Xml | inheritance filter |
| anychart.charts.Pareto | localToGlobal | inheritance filter |
| anychart.graphics.vector.vml.Text | getTextHeight/Width | inheritance filter |
| anychart.standalones.DataGrid | getVisibleItems | inheritance filter |
| anychart.standalones.Table | saveAsJpg/Pdf/Png | inheritance filter |
| anychart.core.utils.DrawingPlanIterator | getRowsCount | inheritance filter |

## NAMESPACE_DIFF (280 lines)
- 12 instances: `module` keyword → `namespace` keyword
- 21 enums: replaced with `const X: string` instead of proper enum
- 3 collapsed brace issues

## TYPE_DIFF (4 lines)
- `anychart.format.locales: any` → should be `{[prop:string]:anychart.format.Locale}`

## TYPEDEF_DIFF (80 lines)
- Type alias reordering (alphabetical vs reference order)
