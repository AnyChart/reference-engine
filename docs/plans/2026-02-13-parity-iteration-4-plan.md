# Parity Iteration 4 Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Reduce the next highest-impact missing signatures after iteration 3, focusing on `getIndex()` and point-selection API families.

**Baseline for this iteration:** `changed_lines=10528`, `missing_total=2882`, `extra_total=15`.

---

### Task 1: Lock Targeted Signature Baseline

**Files:**
- Read: `.tmp-verify/parity-signature-report.txt`
- Read: `docs/plans/2026-02-13-parity-baseline-notes.md`

**Step 1: Freeze top target signatures**

Primary targets:
- `getIndex(): number;`
- `excludePoint(indexes: number | Array<number>): boolean;`
- `includePoint(indexes: number | Array<number>): boolean;`
- `includeAllPoints(): boolean;`
- `keepOnlyPoints(indexes: number | Array<number>): void;`
- `getExcludedPoints(): Array<anychart.core.Point>;`

---

### Task 2: Identify Exact Interface-Hunk Gaps

**Files:**
- Read: `index.d.ts`
- Read: `.tmp-verify/8.14.1/index.d.ts`

**Step 1: Sample 3-5 concrete interfaces for each target signature**

For each signature, capture:
- where present in control;
- where absent in generated;
- whether child class has sibling methods from same family.

**Step 2: Classify each mismatch**

Use one of:
- missing full method family;
- missing single overload;
- wrong return type/covariance;
- ordering-only.

---

### Task 3: Add Focused Inheritance Tests

**Files:**
- Modify: `tests/inheritance.test.js`

**Step 1: Add positive fixture for point-selection family propagation**

Synthetic chain where child has inheritdoc placeholder(s) but is missing one or more point-selection methods from parent.

**Step 2: Add positive fixture for `getIndex()` propagation**

Ensure child receives `getIndex(): number;` when expected by inheritance rules.

**Step 3: Add negative guard**

Ensure no unrelated method families are added when target family is absent from child/member context.

---

### Task 4: Implement Narrow Family Backfill

**Files:**
- Modify: `src/inheritance.js`

**Step 1: Add targeted allowlist for point/index method family**

Use exact names from Task 1 only; avoid broad additions.

**Step 2: Implement constrained merge rules**

Rules:
- operate parent-only (no full ancestor sweep);
- add only missing signatures in existing child groups, except for explicit family methods where child has inheritdoc placeholder-only group;
- preserve all-members filtering and unresolved-placeholder safeguards.

**Step 3: Keep deterministic ordering**

Retain existing name-based ordering and overload insertion order.

---

### Task 5: Verify and Decide

**Files:**
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

**Step 3: Keep/rollback gate**

Keep only if:
- `changed_lines < 10528`;
- `missing_total` decreases;
- `extra_total <= 20`.

If not, rollback only iteration-4 inheritance edits and keep tests/notes.

