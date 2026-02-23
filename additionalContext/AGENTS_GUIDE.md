# Agents Guide — Parallel Work Configuration

## Recommended Subagents for dtsGenerator

This project benefits from parallel agent work due to its pipeline architecture. Each pipeline stage is independently testable and modifiable.

---

## Agent 1: Parity Diff Analyst

**Purpose:** Continuously monitor and categorize parity diff changes.
**Trigger:** After any code change to src/ files.
**Tasks:**
- Run the full pipeline: `node src/cli.js --data-dir ./data --version 8.14.1 --jsdoc-bin ./node_modules/.bin/jsdoc --max-groups 8 --output-dir ./.tmp`
- Generate parity diff: `git --no-pager diff --no-index index.d.ts ./.tmp/8.14.1/index.d.ts`
- Categorize diff hunks by type (method ordering, missing methods, extra methods, type differences, namespace issues)
- Report metrics: changed_lines, missing_total, extra_total
- Flag regressions immediately

**Tools:** Bash, Grep, Read
**Independence:** Fully independent — reads output files only.

---

## Agent 2: Inheritance Researcher

**Purpose:** Investigate specific inheritance chain behaviors in the reference vs generated output.
**Trigger:** When parity diff shows missing/extra methods in class declarations.
**Tasks:**
- Trace inheritance chains for specific classes (e.g., `anychart.core.CartesianBase` → children)
- Compare method lists between reference and generated output
- Identify which methods should/shouldn't be inherited
- Check `allMemberNames`, `ALWAYS_INHERIT_METHODS`, and `CONTEXT_FORCE_INHERIT_METHODS_BY_CLASS`
- Propose additions/removals to allowlists

**Tools:** Read, Grep, Bash (for running pipeline)
**Independence:** Independent research, but changes affect inheritance.js.

---

## Agent 3: Test Writer

**Purpose:** Write and maintain tests for each pipeline stage.
**Trigger:** Before or alongside any code change (TDD approach).
**Tasks:**
- Write regression tests for specific parity issues
- Test edge cases in type parsing (jsdoc-type-parser.pegjs)
- Test inheritance resolution for complex class hierarchies
- Test structurize output for specific doclet patterns
- Ensure test coverage for all PARITY_PLAN.md issues

**Tools:** Read, Write, Edit, Bash (for running tests)
**Independence:** Fully independent — writes to tests/ directory only.

---

## Agent 4: Type System Specialist

**Purpose:** Handle type parsing and conversion issues.
**Trigger:** When parity diff shows type syntax differences.
**Tasks:**
- Debug Peggy grammar for edge cases
- Fix type-writer.js conversion logic
- Handle special type patterns (Object.<K,V>, function types, unions)
- Verify typedef inlining replacements
- Compare type output against reference

**Tools:** Read, Edit, Bash
**Independence:** Independent — works on src/types/ subsystem only.

---

## Agent 5: Reference Comparator

**Purpose:** Deep-compare specific sections of reference vs generated output.
**Trigger:** When investigating specific parity failures.
**Tasks:**
- Extract specific namespace/class declarations from both files
- Side-by-side comparison of method signatures
- Identify ordering differences
- Track overload grouping patterns
- Document findings in docs/plans/

**Tools:** Read, Grep, Bash
**Independence:** Fully independent — read-only analysis.

---

## Agent 6: Documentation & Metrics Tracker

**Purpose:** Keep documentation current and track progress metrics.
**Trigger:** After each iteration/significant change.
**Tasks:**
- Update PARITY_PLAN.md with current status of each issue
- Create iteration plan documents in docs/plans/
- Update CLAUDE.md Project Description if scope changes
- Track diff metrics over time
- Maintain SESSION_LOG.md

**Tools:** Read, Write, Edit
**Independence:** Fully independent — writes only to docs.

---

## Parallel Execution Patterns

### Pattern 1: Investigate + Fix + Test (3 agents)
```
Agent 5 (Comparator)  ─→ findings ─→ Developer implements fix
Agent 3 (Test Writer)  ─→ test first ─→ Developer validates
Agent 1 (Diff Analyst) ─→ metrics   ─→ Developer confirms
```

### Pattern 2: Multi-Issue Sprint (2-4 agents)
```
Agent 2 (Inheritance) works on Issue #2 (allMemberNames)
Agent 4 (Type System) works on Issue #6 (typedef inlining)
Agent 3 (Test Writer) writes tests for both
Agent 1 (Diff Analyst) validates after each merge
```

### Pattern 3: Research Phase (2 agents)
```
Agent 5 (Comparator) analyzes reference patterns
Agent 2 (Inheritance) traces class hierarchies
→ Both report findings → Developer plans implementation
```

---

## Dispatch Rules

1. **Never have two agents editing the same file** — coordinate via findings/reports
2. **Agent 1 (Diff Analyst) runs after every change** — it's the validation gate
3. **Agent 3 (Test Writer) runs before implementation** — TDD approach
4. **Agent 5 and Agent 2 are read-only** — safe to run anytime in parallel
5. **Agent 6 runs after each completed iteration** — keeps docs current
