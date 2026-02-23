# AnyChart DTS Parity Closure Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Close remaining output differences between this JS generator and the legacy reference `index.d.ts`, while keeping repository hygiene clean and reproducible.

**Architecture:** The generator remains a staged pipeline: doclet extraction -> model structurization -> inheritance/typedef transforms -> TS rendering. Parity work should target one stage at a time, verify with generated-output diffs, and avoid cross-stage refactors unless proven necessary.

**Tech Stack:** Node.js (ESM), JSDoc, Peggy parser, fast-glob, fs-extra, execa.

---

### Task 1: Baseline and Diff Harness

**Files:**
- Read: `README.md`
- Read: `PARITY_PLAN.md`
- Read: `src/pipeline.js`
- Read: `src/cli.js`
- Output: `.tmp-verify/` (ignored)

**Step 1: Generate a baseline output with current code**

Run:
```bash
node src/cli.js --data-dir ./data --version 8.14.1 --jsdoc-bin ./node_modules/.bin/jsdoc --max-groups 8 --output-dir ./.tmp-verify
```

Expected: `index.d.ts` and `graphics.d.ts` generated under `.tmp-verify/8.14.1/`.

**Step 2: Produce a reproducible parity diff file**

Run:
```bash
git --no-pager diff --no-index index.d.ts ./.tmp-verify/8.14.1/index.d.ts > ./.tmp-verify/parity.diff
```

Expected: non-empty diff that can be grouped by issue type.

**Step 3: Annotate top mismatch categories**

Write a short note in `docs/plans/` (or update this file) with category counts:
- method ordering/grouping
- inheritance filtering
- typedef expansion
- enum formatting
- namespace/module shape

Why: Prevents mixing unrelated fixes and helps prove impact per change.

---

### Task 2: Inheritance Filtering and Covariance Alignment

**Files:**
- Modify: `src/structurize.js`
- Modify: `src/inheritance.js`
- Reference: `PARITY_PLAN.md`

**Step 1: Implement `all-members` capture in structurization**

Add per-class member name capture based on non-static member/function doclets and expose it in class model.

Why: Reference engine inheritance keeps only methods declared in child `all-members`.

**Step 2: Enforce `all-members` filter during method inheritance**

When inheriting from parent, skip parent method names not present in child `all-members`.

Why: Prevents extra inherited methods such as `a11y()`/`name()` when not declared by child.

**Step 3: Keep covariance updates explicit and deterministic**

Ensure return-type covariance rewrites happen for inherited clones and direct methods consistently.

Why: Parity requires child-return chaining where ancestor type appears.

**Step 4: Verify with focused diff**

Run:
```bash
node src/cli.js --data-dir ./data --version 8.14.1 --jsdoc-bin ./node_modules/.bin/jsdoc --max-groups 8 --output-dir ./.tmp-verify
git --no-pager diff --no-index index.d.ts ./.tmp-verify/8.14.1/index.d.ts > ./.tmp-verify/parity-inheritance.diff
```

Expected: clear reduction in inheritance-related mismatches.

---

### Task 3: Method Grouping/Ordering Parity

**Files:**
- Modify: `src/structurize.js`
- Modify: `src/ts-generator.js`
- Modify: `src/inheritance.js`
- Reference: `src/class-order.json`

**Step 1: Group methods by name with stable overload order**

Ensure method groups represent `name -> overrides[]`, preserving insertion order inside a name group.

Why: Reference output ordering is name-first; overload order must remain stable.

**Step 2: Remove secondary sort dimensions in final emission**

Avoid sort keys that are not present in reference behavior (`isMain`, distance, synthetic tie-breakers), except known required end-method ordering if still validated by parity output.

Why: Over-sorting is a major source of diff churn.

**Step 3: Re-run baseline generation and compare**

Run:
```bash
node src/cli.js --data-dir ./data --version 8.14.1 --jsdoc-bin ./node_modules/.bin/jsdoc --max-groups 8 --output-dir ./.tmp-verify
git --no-pager diff --no-index index.d.ts ./.tmp-verify/8.14.1/index.d.ts > ./.tmp-verify/parity-ordering.diff
```

Expected: substantial drop in ordering-only mismatch blocks.

---

### Task 4: Typedef/Enum/Namespace Edge Cases

**Files:**
- Modify: `src/typedef-builder.js`
- Modify: `src/ts-generator.js`
- Modify: `src/pipeline.js` (if special-case pruning is inserted pre-render)
- Reference: `PARITY_PLAN.md`

**Step 1: Align function-typedef detection and inlining**

Treat only true function typedefs as inline signatures and replace both param and return references consistently.

**Step 2: Align enum field emission**

For string-valued enum entries, emit name-only where reference behavior requires it.

**Step 3: Verify namespace/module forms**

Confirm nested declarations use `module` or `namespace` exactly as expected by reference output contexts.

**Step 4: Add explicit removal for known special case**

If still required by parity, remove `anychart.graphics.math.rect` before final render.

**Step 5: Re-run and diff**

Run:
```bash
node src/cli.js --data-dir ./data --version 8.14.1 --jsdoc-bin ./node_modules/.bin/jsdoc --max-groups 8 --output-dir ./.tmp-verify
git --no-pager diff --no-index index.d.ts ./.tmp-verify/8.14.1/index.d.ts > ./.tmp-verify/parity-edgecases.diff
```

Expected: only residual low-count mismatches remain.

---

### Task 5: Final Verification and Integration

**Files:**
- Read: `AGENTS.md`
- Read: `.gitignore`
- Read: `docs/plans/2026-02-13-parity-closure-implementation-plan.md`

**Step 1: Validate repository hygiene**

Run:
```bash
git status --short
```

Expected: only intentional source/doc updates; no debug artifacts.

**Step 2: Execute final generation + diff snapshot**

Run:
```bash
node src/cli.js --data-dir ./data --version 8.14.1 --jsdoc-bin ./node_modules/.bin/jsdoc --max-groups 8 --output-dir ./.tmp-verify
git --no-pager diff --no-index index.d.ts ./.tmp-verify/8.14.1/index.d.ts > ./.tmp-verify/parity-final.diff
```

Expected: target diff threshold reached (see `PARITY_PLAN.md`).

**Step 3: Prepare review package**

Collect:
- file-level change summary
- before/after diff line counts
- known remaining mismatches and rationale

Why: Reviewers can verify parity progress quickly without re-deriving context.

---

## Reference Links

- Project overview: [`README.md`](../../README.md)
- Existing parity analysis: [`PARITY_PLAN.md`](../../PARITY_PLAN.md)
- Pipeline orchestrator: [`src/pipeline.js`](../../src/pipeline.js)
- Structuring logic: [`src/structurize.js`](../../src/structurize.js)
- Inheritance logic: [`src/inheritance.js`](../../src/inheritance.js)
- TS renderer: [`src/ts-generator.js`](../../src/ts-generator.js)
- Typedef post-processing: [`src/typedef-builder.js`](../../src/typedef-builder.js)
- Agent workflow rules: [`AGENTS.md`](../../AGENTS.md)
