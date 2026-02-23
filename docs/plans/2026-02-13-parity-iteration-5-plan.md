# Parity Iteration 5 Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Reduce the next dominant missing signatures after point/index closure, focusing on text/style getter families and `textSettings` signatures.

**Baseline for this iteration (post-iteration-4):**
- `changed_lines=10518`
- `missing_total=2759`
- `extra_total=15`

---

### Task 1: Lock Text/Style Baseline

**Files:**
- Read: `.tmp-verify/parity-signature-report.txt`
- Read: `docs/plans/2026-02-13-parity-baseline-notes.md`

**Step 1: Freeze top targets**

Prioritize:
- `disablePointerEvents(): boolean;`
- `fontFamily(): string;`
- `selectable(): boolean;`
- `textIndent(): number;`
- `fontDecoration(): ...;`
- `fontStyle(): ...;`
- `fontVariant(): ...;`
- `letterSpacing(): ...;`
- `lineHeight(): ...;`
- `textDirection(): ...;`
- `textOverflow(): ...;`
- `wordBreak(): string;`
- `wordWrap(): string;`
- `fontOpacity(): number;`
- `textSettings(): Object;`
- `textSettings(name?: string): string | number | boolean;`

---

### Task 2: Produce Interface-Level Gap Classification

**Files:**
- Read: `index.d.ts`
- Read: `.tmp-verify/8.14.1/index.d.ts`

**Step 1: Build targeted gap report**

For each signature:
- interfaces where control has it and generated does not;
- whether sibling style/text methods already exist in generated interface.

**Step 2: Classify mismatch**

Mark each as one of:
- missing single overload in existing method group;
- missing full family in inheritdoc class;
- return-type mismatch;
- ordering-only.

---

### Task 3: Add Failing Focused Tests (TDD Red)

**Files:**
- Modify: `tests/inheritance.test.js`

**Step 1: Positive fixture for `textSettings` getter pair**

Synthetic class where parent has both `textSettings()` and `textSettings(name?)`; child keep/setter form only.

**Step 2: Positive fixture for style getter family in inheritdoc class**

Use one representative method from current top list (for example `disablePointerEvents` or `fontFamily`) and assert getter presence after inheritance.

**Step 3: Negative guard**

Confirm non-target methods still follow strict all-members filtering.

---

### Task 4: Implement Narrow Style/Text Family Backfill

**Files:**
- Modify: `src/inheritance.js`

**Step 1: Add explicit text-settings/style method allowlist**

Only include methods proven by Task 2 to be high-impact and safe.

**Step 2: Constrained merge behavior**

- parent-only propagation;
- add missing signatures only in existing child groups, plus explicit allowlist methods where inheritdoc placeholders are unresolved;
- no full ancestor sweep;
- keep all-members safeguards and unresolved-placeholder handling.

**Step 3: Keep deterministic order**

Preserve existing name sort and overload insertion order.

---

### Task 5: Verify and Gate

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

Keep change only if:
- `changed_lines < 10518`;
- `missing_total` decreases;
- `extra_total <= 20`.

If not, rollback iteration-5 inheritance edits and keep tests + diagnostics.

