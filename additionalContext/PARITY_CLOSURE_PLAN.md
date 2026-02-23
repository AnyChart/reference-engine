# Parity Closure Plan — Using EDIC Methodology

## Current State

- **Diff baseline:** ~1,720 changed lines
- **Missing signatures:** ~24
- **Extra signatures:** 0
- **Issues fixed:** 5 of 9 from PARITY_PLAN.md
- **Issues remaining:** 4 (method sorting refinements, covariance edge cases, overload grouping, typedef alignment)

---

## Plan Overview

This plan follows the Evidence-Driven Iterative Convergence (EDIC) methodology.
Each task has: categorize → isolate → fix → validate gates.

---

## Task 1: Diff Categorization Refresh

**Phase:** Categorize
**Estimated effort:** Low
**Parallel:** Yes — can dispatch Diff Analyst agent

### Steps
1. Run full pipeline with current code
2. Generate fresh parity diff
3. Categorize all remaining ~1,720 lines into buckets:
   - METHOD_ORDER, METHOD_MISSING, METHOD_EXTRA, TYPE_DIFF, TYPEDEF_DIFF, NAMESPACE_DIFF, OTHER
4. Count lines per category
5. Document in `docs/plans/current-gap-analysis.md`

### Success Gate
- All diff lines categorized with accurate counts
- Roadmap for Tasks 2-6 is clear

---

## Task 2: Method Ordering Alignment

**Phase:** Isolate → Fix
**PARITY_PLAN issue:** #1 (Method Sorting ~500 lines)
**Priority:** CRITICAL — largest diff category
**Parallel:** Reference Comparator agent for research

### Steps
1. Extract 10 representative method-ordering diff hunks
2. Compare sort logic in inheritance.js vs reference behavior
3. Identify: is it sort key differences, overload order, or group boundaries?
4. Write test capturing a specific ordering failure
5. Fix the sort comparator (likely: pure alphabetical by name, stable for overloads)
6. Validate: changed_lines must decrease by ≥200

### Success Gate
- changed_lines decreased by ≥200
- No new extra_total entries
- All tests pass

---

## Task 3: Overload Grouping Refinement

**Phase:** Isolate → Fix
**PARITY_PLAN issue:** #5 (Method Overload Grouping ~200 lines)
**Priority:** HIGH
**Parallel:** Type System Specialist agent

### Steps
1. Find classes where overload order differs from reference
2. Check: is it getter/setter ordering? Parameter count ordering?
3. Write test for specific overload sequence
4. Fix grouping logic in structurize.js or inheritance.js
5. Validate: overload sequences match reference

### Success Gate
- Overload-related diff hunks eliminated
- No regression in other categories

---

## Task 4: Return Type Covariance Edge Cases

**Phase:** Isolate → Fix
**PARITY_PLAN issue:** #3 (Covariance ~30 lines)
**Priority:** MEDIUM
**Parallel:** Inheritance Researcher agent

### Steps
1. Identify remaining covariance mismatches in diff
2. Trace each to a specific class/method
3. Check: is updateCovariance missing a case? Wrong ancestor detection?
4. Write test for each edge case
5. Fix covariance logic in inheritance.js
6. Validate: type signatures match reference

### Success Gate
- All return type differences resolved
- changed_lines decreased

---

## Task 5: Remaining Missing Signatures (24)

**Phase:** Isolate → Fix
**Priority:** MEDIUM
**Parallel:** Inheritance Researcher + Test Writer agents

### Steps
1. List all 24 missing signatures with their classes
2. For each, determine: is it an inheritance filter issue or a structurize issue?
3. Group by root cause
4. Fix in priority order (most impactful first)
5. Use narrow fixes (class-specific allowlists, not broad rules)
6. Validate each fix independently

### Success Gate
- missing_total = 0
- No increase in extra_total

---

## Task 6: Typedef and Namespace Polish

**Phase:** Isolate → Fix
**PARITY_PLAN issues:** #4 (module vs namespace), #6 (typedef inlining)
**Priority:** LOW (small diff impact)
**Parallel:** Type System Specialist agent

### Steps
1. Identify remaining typedef/namespace diff hunks
2. Fix module vs namespace keyword where needed
3. Verify typedef inlining scope matches reference
4. Validate: these diff categories reach 0

### Success Gate
- TYPEDEF_DIFF and NAMESPACE_DIFF categories at 0

---

## Task 7: Final Convergence

**Phase:** Validate → Converge
**Priority:** Final
**Parallel:** All agents for verification

### Steps
1. Run full pipeline from clean state
2. Run all tests
3. Generate final parity diff
4. If <20 lines: document any intentional differences
5. If >20 lines: go back to Task 1 with refreshed categorization
6. Clean up debug artifacts
7. Update CLAUDE.md status
8. Prepare review package in docs/plans/

### Success Gate
- changed_lines < 20
- missing_total = 0
- extra_total = 0
- All tests pass
- No debug artifacts

---

## Execution Order

```
Task 1 (Categorize) ────────────────────────────┐
                                                  ↓
Task 2 (Method Ordering) ──┐                 [parallel]
Task 3 (Overload Grouping) ┤── can run in parallel
Task 4 (Covariance)        ┘  after Task 1
                                                  ↓
Task 5 (Missing Signatures) ── after Tasks 2-4 ──┤
Task 6 (Typedef/Namespace) ── after Tasks 2-4 ──┘
                                                  ↓
Task 7 (Final Convergence) ── after all above ────┘
```

## Agent Dispatch for This Plan

| Task | Agent(s) | Mode |
|------|----------|------|
| 1 | Diff Analyst | Foreground |
| 2 | Reference Comparator + Test Writer | Parallel |
| 3 | Type System Specialist + Test Writer | Parallel |
| 4 | Inheritance Researcher + Test Writer | Parallel |
| 5 | Inheritance Researcher + Diff Analyst | Sequential |
| 6 | Type System Specialist | Foreground |
| 7 | All agents | Validation |
