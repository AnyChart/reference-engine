# Parity Iteration 3 Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Reduce the remaining high-count missing getter signatures without reintroducing broad inheritance over-generation.

**Architecture:** Keep the current inheritance flow intact and introduce only narrow, evidence-based overload backfills for specific method families where control has getter+setter parity and generated output systematically misses the getter form.

**Tech Stack:** Node.js (ESM), JSDoc, custom inheritance/model pipeline, TypeScript text emitter.

---

### Task 1: Freeze Iteration Baseline

**Files:**
- Read: `docs/plans/2026-02-13-parity-baseline-notes.md`
- Read: `.tmp-verify/parity-final.diff`
- Read: `.tmp-verify/parity-signature-report.txt`

**Step 1: Record baseline metrics for this loop**

Use current rollback-stable snapshot:
- `changed_lines=10545`
- `hunks=1073`
- `missing_total=2913`
- `extra_total=15`

**Step 2: Lock top missing families**

Capture top signatures and group into families:
- text/style getters (`fontFamily`, `fontStyle`, `fontDecoration`, `textIndent`, `lineHeight`, etc.);
- point/index getters (`getIndex`);
- selectability getters (`disablePointerEvents`, `selectable`).

---

### Task 2: Build Targeted Repro Fixtures (No Broad Changes Yet)

**Files:**
- Modify: `tests/inheritance.test.js`
- Optional create: `tests/parity-overload-backfill.test.js`

**Step 1: Convert diagnostic skip tests into active fixtures behind a strict method allowlist context**

Add focused tests proving:
- getter+setter pair expected when child has only setter inheritdoc form;
- covariance remains correct for any propagated chainable setter signatures.

**Step 2: Add negative fixture**

Add a counter-test that verifies unrelated method families are **not** backfilled.

Why: Prevent repeating the iteration-2 over-generation regression.

---

### Task 3: Implement Narrow Getter Backfill Rule

**Files:**
- Modify: `src/inheritance.js`

**Step 1: Add explicit `STYLE_GETTER_BACKFILL_METHODS` allowlist**

Start with method names from top missing signatures only (no speculative additions).

**Step 2: Add constrained merge logic**

For classes with `hasInheritDocMethods`:
- if child already has method group name from allowlist;
- if child group has at least one setter-style overload (`params.length > 0`);
- if parent group has a zero-param getter-style overload (`params.length === 0`);
- append only that missing getter signature;
- apply covariance rewrite to appended overloads;
- do not add new method names.

**Step 3: Keep deterministic ordering**

After backfill, keep existing name-based sort and preserve overload insertion order.

---

### Task 4: Verify Against Baseline and Decide

**Files:**
- Read: `.tmp-verify/parity-final.diff`
- Modify: `docs/plans/2026-02-13-parity-review-package.md`

**Step 1: Run focused tests**

```bash
node --test tests/inheritance.test.js tests/structurize.test.js
```

**Step 2: Run full parity validation**

```bash
node src/cli.js --data-dir ./data --version 8.14.1 --jsdoc-bin ./node_modules/.bin/jsdoc --max-groups 8 --output-dir ./.tmp-verify
git --no-pager diff --no-index index.d.ts ./.tmp-verify/8.14.1/index.d.ts > ./.tmp-verify/parity-final.diff
```

Collect:
- changed lines;
- hunk count;
- missing/extra multiset totals;
- top 20 missing signatures.

**Step 3: Gate criteria**

Keep changes only if:
- `changed_lines` decreases from `10545`, and
- `extra_total` stays close to baseline (`<= 25`), and
- top missing getter family counts decrease.

If gate fails:
- rollback only iteration-3 inheritance edits;
- keep tests and notes.

---

### Task 5: Prepare Next Loop Entry

**Files:**
- Modify: `docs/plans/2026-02-13-parity-review-package.md`
- Optional create: `docs/plans/2026-02-13-parity-iteration-4-plan.md`

**Step 1: Document “what is in diff / why in diff” with concrete signatures**

Use signature counts and 2-3 representative interface hunks.

**Step 2: Write next constrained plan**

Target only the next highest-impact residual family.

**Step 3: Verify hygiene**

```bash
git status --short --ignored
```

---

## Reference Links

- Baseline notes: [`docs/plans/2026-02-13-parity-baseline-notes.md`](./2026-02-13-parity-baseline-notes.md)
- Current review package: [`docs/plans/2026-02-13-parity-review-package.md`](./2026-02-13-parity-review-package.md)
- Previous plan: [`docs/plans/2026-02-13-parity-iteration-2-plan.md`](./2026-02-13-parity-iteration-2-plan.md)
- Inheritance implementation: [`src/inheritance.js`](../../src/inheritance.js)
- Structurization implementation: [`src/structurize.js`](../../src/structurize.js)
