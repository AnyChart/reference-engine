# Parity Iteration 2 Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Reduce the dominant missing-method parity gap by aligning inheritance propagation behavior with the reference flow (including the post-inheritance class-method update behavior).

**Architecture:** Keep the current pipeline stages, but refine inheritance semantics in two passes: (1) strict name/all-members matching, then (2) explicit ancestor-method propagation and covariance update. This avoids ad-hoc filtering and gives testable checkpoints for each stage.

**Tech Stack:** Node.js (ESM), JSDoc, fast-glob, execa, custom TS emitter.

---

### Task 1: Lock the Diff Baseline for This Iteration

**Files:**
- Read: `.tmp-verify/parity-final.diff`
- Modify: `docs/plans/2026-02-13-parity-baseline-notes.md`

**Step 1: Record current parity numbers**

Run:
```bash
node src/cli.js --data-dir ./data --version 8.14.1 --jsdoc-bin ./node_modules/.bin/jsdoc --max-groups 8 --output-dir ./.tmp-verify
git --no-pager diff --no-index index.d.ts ./.tmp-verify/8.14.1/index.d.ts > ./.tmp-verify/parity-final.diff
```

Expected:
- `parity-final.diff` exists and is reproducible.

**Step 2: Extract high-impact missing signatures**

Run scripts that count missing line multiplicities (multiset comparison) and keep top 20.

Expected:
- Dominant missing lines include event/listener and base container/bounds APIs.

**Step 3: Save snapshot in notes**

Append exact counts and top missing signatures to `docs/plans/2026-02-13-parity-baseline-notes.md`.

---

### Task 2: Add Failing Regression Tests for Missing Base Methods

**Files:**
- Modify: `tests/inheritance.test.js`
- Optional create: `tests/parity-inheritance-regression.test.js`

**Step 1: Add test for inherited base methods on annotation/ui classes**

Write failing test with synthetic class chain proving that child class receives parent methods (`listen`, `listenOnce`, `unlisten`, `removeAllListeners`, `container`, `parentBounds`) after inheritance/update pass.

**Step 2: Add test for covariance in propagated ancestor methods**

Ensure propagated methods returning ancestor class names are rewritten to child class name.

**Step 3: Run tests and confirm red state**

Run:
```bash
node --test tests/inheritance.test.js
```

Expected:
- New tests fail before implementation.

---

### Task 3: Implement Reference-Style Post-Inheritance Method Update Pass

**Files:**
- Modify: `src/inheritance.js`
- Reference: `PARITY_PLAN.md`

**Step 1: Add ancestor-chain utility**

Implement helper that returns full ordered ancestor chain for a class.

**Step 2: Add explicit `updateClassesMethods` pass**

After existing inheritance merge, add a pass that:
- collects ancestor methods through full chain;
- adds missing methods to child where absent by method signature/group semantics;
- reapplies covariance for appended methods.

**Step 3: Keep deterministic order**

After update pass, sort methods stably by name and preserve overload insertion order.

**Step 4: Verify unit tests**

Run:
```bash
node --test tests/inheritance.test.js tests/structurize.test.js
```

Expected:
- All inheritance-related tests pass.

---

### Task 4: Validate with Full Parity Diff and Compare Against Baseline

**Files:**
- Read: `.tmp-verify/parity-final.diff`
- Modify: `docs/plans/2026-02-13-parity-review-package.md`

**Step 1: Regenerate and diff**

Run:
```bash
node src/cli.js --data-dir ./data --version 8.14.1 --jsdoc-bin ./node_modules/.bin/jsdoc --max-groups 8 --output-dir ./.tmp-verify
git --no-pager diff --no-index index.d.ts ./.tmp-verify/8.14.1/index.d.ts > ./.tmp-verify/parity-final.diff
```

**Step 2: Measure before/after**

Collect:
- changed lines count;
- hunk count;
- top missing signature counts.

**Step 3: Decide iteration outcome**

If metrics improve:
- keep changes and proceed to next root-cause loop.

If metrics worsen:
- rollback only the iteration-specific inheritance changes and retain test improvements for diagnosis.

---

### Task 5: Prepare Next Loop Entry

**Files:**
- Modify: `docs/plans/2026-02-13-parity-review-package.md`
- Create: `docs/plans/2026-02-13-parity-iteration-3-plan.md` (if needed)

**Step 1: Document “what is in diff / why in diff”**

Summarize remaining top categories with concrete signature examples.

**Step 2: Write next fix plan**

Focus only on the next highest-impact root cause after this iteration.

**Step 3: Verify repo hygiene**

Run:
```bash
git status --short --ignored
```

Expected:
- only intentional source/tests/docs changes;
- no leaked artifacts staged.

---

## Reference Links

- Baseline notes: [`docs/plans/2026-02-13-parity-baseline-notes.md`](./2026-02-13-parity-baseline-notes.md)
- Current review package: [`docs/plans/2026-02-13-parity-review-package.md`](./2026-02-13-parity-review-package.md)
- Prior execution plan: [`docs/plans/2026-02-13-parity-closure-implementation-plan.md`](./2026-02-13-parity-closure-implementation-plan.md)
- Inheritance implementation: [`src/inheritance.js`](../../src/inheritance.js)
- Structurization implementation: [`src/structurize.js`](../../src/structurize.js)
- Doclet ingestion: [`src/jsdoc-runner.js`](../../src/jsdoc-runner.js)
