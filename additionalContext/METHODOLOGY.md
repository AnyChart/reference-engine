# Methodology — Systematic Parity Closure

## Problem Statement

Generate TypeScript `.d.ts` files from AnyChart JSDoc source that are byte-identical to the output of the legacy Clojure reference generator. Current gap: ~1,720 diff lines. Target: <20 diff lines.

---

## The Methodology: Evidence-Driven Iterative Convergence (EDIC)

### Core Principle
Every change must be validated against the reference output. No guessing. No assumptions. Only diff-verified improvements.

### Phase 1: Categorize (Understand the Gap)

**Goal:** Know exactly what types of differences exist and their quantities.

**Steps:**
1. Generate current output with the full pipeline
2. Produce a full parity diff against `index.d.ts`
3. Categorize every diff hunk into one of these buckets:
   - **METHOD_ORDER** — Same methods, different order
   - **METHOD_MISSING** — Method in reference but not in generated
   - **METHOD_EXTRA** — Method in generated but not in reference
   - **TYPE_DIFF** — Same method, different type signature
   - **TYPEDEF_DIFF** — Typedef/enum/constant differences
   - **NAMESPACE_DIFF** — Module vs namespace keyword issues
   - **OTHER** — Anything else
4. Count lines per category
5. Document in `docs/plans/` with exact metrics

**Output:** Category breakdown with line counts. This is the roadmap.

### Phase 2: Isolate (Find Root Causes)

**Goal:** For each category, identify the specific code path responsible.

**Steps:**
1. Pick the category with the most diff lines
2. Extract 3-5 representative diff hunks
3. Trace each hunk back to its origin:
   - Which pipeline stage produces the wrong output?
   - Which function/logic path is responsible?
   - What input data triggers the difference?
4. Identify the root cause (not the symptom)
5. Document root cause in PARITY_PLAN.md

**Key Questions Per Hunk:**
- Is this a structurize issue (wrong model)?
- Is this an inheritance issue (wrong method propagation)?
- Is this a ts-generator issue (wrong rendering)?
- Is this a type-parser issue (wrong type conversion)?

### Phase 3: Fix (Targeted, Gated Changes)

**Goal:** Fix one root cause at a time with strict regression gating.

**Steps:**
1. Write a failing test that captures the current wrong behavior
2. Record baseline metrics: `changed_lines`, `missing_total`, `extra_total`
3. Implement the minimal fix for ONE root cause
4. Run the test — it should pass
5. Run all tests — none should regress
6. Generate output and compute new parity diff
7. Compare metrics against baseline:
   - **Accept** if: `changed_lines` decreased AND `extra_total` did not increase
   - **Reject** if: `changed_lines` increased OR `extra_total` increased
   - **Investigate** if: `missing_total` decreased but `changed_lines` increased slightly
8. If accepted, commit and document the change
9. If rejected, revert and try a different approach

**Strict Gating Rules:**
- Never accept a change that increases `extra_total` (over-generation is worse than under-generation)
- Changed lines budget: each fix should reduce diff by at least its hunk count
- If a fix helps some classes but hurts others, it's too broad — narrow the scope

### Phase 4: Validate (Full Verification)

**Goal:** Confirm the cumulative effect of all fixes.

**Steps:**
1. Run full pipeline from clean state
2. Run all tests
3. Generate complete parity diff
4. Verify metrics match expectations
5. Check for any new categories of differences
6. If new categories appeared, go back to Phase 1

### Phase 5: Converge (Final Polish)

**Goal:** Close the last <100 diff lines.

**Steps:**
1. Remaining diffs are usually edge cases — handle individually
2. For each remaining hunk:
   - Is it worth fixing? (diminishing returns)
   - Can it be fixed without special-casing?
   - Does fixing it require adding to allowlists?
3. Document any intentional differences (if parity is deemed unnecessary for specific cases)
4. Final verification pass

---

## Iteration Template

Each iteration follows this structure:

```
## Iteration N: [Category] — [Specific Issue]

### Baseline
- changed_lines: X
- missing_total: Y
- extra_total: Z

### Root Cause
[Description of what's wrong and why]

### Fix
[Description of the change]

### Result
- changed_lines: X' (delta: ±N)
- missing_total: Y' (delta: ±N)
- extra_total: Z' (delta: ±N)

### Decision: ACCEPT / REJECT / INVESTIGATE
[Reasoning]
```

---

## Tools for Each Phase

| Phase | Tool | Command |
|-------|------|---------|
| Categorize | Parity diff | `git diff --no-index index.d.ts .tmp/8.14.1/index.d.ts` |
| Categorize | Line count | `wc -l < parity.diff` |
| Isolate | Reference search | `grep -n "methodName" index.d.ts` |
| Isolate | Generated search | `grep -n "methodName" .tmp/8.14.1/index.d.ts` |
| Fix | Tests | `node --test tests/*.test.js` |
| Fix | Pipeline | `node src/cli.js --data-dir ./data --version 8.14.1 ...` |
| Validate | Full diff | Save and compare across iterations |

---

## Anti-Patterns to Avoid

1. **Shotgun fixes** — Changing multiple things at once makes it impossible to know what helped
2. **Over-generation** — Adding too many inherited methods is harder to fix than missing methods
3. **Allowlist explosion** — Adding hundreds of entries to ALWAYS_INHERIT_METHODS defeats the purpose
4. **Ignoring metrics** — "It looks better" is not evidence. Diff line counts are evidence
5. **Fixing symptoms** — Changing ts-generator output format when the real issue is in inheritance
6. **Broad inheritance rules** — A rule that helps 10 classes but breaks 2 is net negative

---

## Success Criteria

| Metric | Target |
|--------|--------|
| changed_lines | < 20 |
| missing_total | 0 |
| extra_total | 0 |
| All tests | PASS |
| No debug artifacts | committed |
