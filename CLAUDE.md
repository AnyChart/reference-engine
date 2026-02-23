# CLAUDE.md — AnyChart DTS Generator

## Project Description

**Name:** dtsGenerator
**Purpose:** Node.js pipeline that generates TypeScript declaration files (`.d.ts`) from AnyChart's JSDoc-annotated `.adoc` source files. Replaces a legacy Clojure-based generator.
**Primary Goal:** Achieve byte-level parity with the Clojure reference output (`index.d.ts`).
**Status:** ~1,720 diff lines remaining (down from 12,220). Target: <20 diff lines.

## Environment

- **OS:** Windows 11 Pro
- **Runtime:** Node.js v22.13.0, npm 10.9.2
- **Shell:** bash (Git Bash on Windows — use Unix syntax, forward slashes)
- **Package Manager:** npm
- **Build System:** Node.js scripts (no bundler)
- **Deployment:** Local CLI tool + GitHub Actions CI
- **Test Runner:** `node --test` (built-in Node.js test runner)

## Architecture & Pipeline

```
.adoc files → jsdoc-runner → structurize → inheritance → typedef-builder → ts-generator → .d.ts
```

### Stage Details

| Stage | File | Purpose |
|-------|------|---------|
| Entry | `src/cli.js` | CLI arg parsing, launches pipeline |
| Orchestrator | `src/pipeline.js` | Sequences all stages, writes output files |
| Extraction | `src/jsdoc-runner.js` | Converts .adoc→.js, runs JSDoc in parallel, normalizes doclets |
| Structurization | `src/structurize.js` | Flat doclets → hierarchical model (namespaces, classes, methods) |
| Inheritance | `src/inheritance.js` | Resolves class hierarchy, propagates methods, covariant returns |
| Typedef | `src/typedef-builder.js` | Normalizes typedefs for TS output |
| Generation | `src/ts-generator.js` | Renders final .d.ts using Peggy-based type parser |
| Type Parsing | `src/types/type-parser.js` | JSDoc type string → AST |
| Type Writing | `src/types/type-writer.js` | AST → TypeScript type syntax |
| Grammar | `src/types/jsdoc-type-parser.pegjs` | Peggy grammar for JSDoc types |
| Class Order | `src/class-order.json` | Deterministic class ordering (from reference) |

### Key Data Flow

- **Input:** `data/versions/<version>/*.adoc` files
- **Intermediate:** Flat doclets → structured topLevel object → inherited model
- **Output:** `index.d.ts`, `graphics.d.ts`, `index-<version>.d.ts`

## Commands

```bash
# Install dependencies
npm install

# Generate declarations (main workflow)
node src/cli.js --data-dir ./data --version 8.14.1 --jsdoc-bin ./node_modules/.bin/jsdoc --max-groups 8 --output-dir ./.tmp

# Run all tests
node --test tests/*.test.js

# Run single test file
node --test tests/inheritance.test.js

# Compare output against reference (parity check)
git --no-pager diff --no-index index.d.ts ./.tmp/8.14.1/index.d.ts | wc -l

# Full parity diff to file
git --no-pager diff --no-index index.d.ts ./.tmp/8.14.1/index.d.ts > parity.diff
```

## Project Structure

```
├── src/                    # Core source code
│   ├── cli.js              # CLI entry point
│   ├── pipeline.js         # Pipeline orchestrator
│   ├── jsdoc-runner.js     # JSDoc extraction (parallel)
│   ├── structurize.js      # Doclet → hierarchy
│   ├── inheritance.js      # Inheritance resolution
│   ├── typedef-builder.js  # Typedef normalization
│   ├── ts-generator.js     # .d.ts rendering
│   ├── class-order.json    # Deterministic class ordering
│   └── types/              # Type parsing subsystem
├── tests/                  # Node.js test runner tests
├── data/versions/          # Input .adoc source files
├── docs/plans/             # Parity iteration plans
├── additionalContext/      # Methodology, agent guides, reference docs
├── index.d.ts              # Reference baseline (~2.4MB, ~50k lines)
├── PARITY_PLAN.md          # Root cause analysis of 9 parity issues
├── AGENTS.md               # Agent workflow rules
└── .github/workflows/      # CI pipeline
```

## Core Concepts

### Parity
The output must match the Clojure reference `index.d.ts` byte-for-byte. Every change is validated by diff comparison. The parity diff is the single source of truth for progress.

### allMemberNames
Each class tracks which method names are explicitly documented. During inheritance, only methods whose names appear in the child's `allMemberNames` set are inherited (plus methods in `ALWAYS_INHERIT_METHODS`).

### Covariance
When a child class inherits a method that returns the parent type, the return type is rewritten to the child type (for method chaining).

### Deterministic Output
- Classes ordered by `class-order.json` (extracted from reference)
- Methods sorted alphabetically by name
- Overloads preserved in original order within name groups
- Constants, enums, typedefs all sorted alphabetically

### Function Typedef Inlining
Function-type typedefs are not emitted as standalone declarations. Instead, they are inlined at usage sites via a replacement map in `type-writer.js`.

## Important Files for Parity Work

- [PARITY_PLAN.md](PARITY_PLAN.md) — 9 identified root causes with impact estimates
- [docs/plans/](docs/plans/) — Iteration-by-iteration plans and metrics
- [index.d.ts](index.d.ts) — Reference baseline (ground truth)
- [src/inheritance.js](src/inheritance.js) — Most complex module, handles method propagation
- [src/ts-generator.js](src/ts-generator.js) — Final rendering, type conversion

## Working Rules

### 1. Parity First
Every code change must reduce (or at minimum not increase) the parity diff. Validate with:
```bash
node src/cli.js --data-dir ./data --version 8.14.1 --jsdoc-bin ./node_modules/.bin/jsdoc --max-groups 8 --output-dir ./.tmp && git --no-pager diff --no-index index.d.ts ./.tmp/8.14.1/index.d.ts | wc -l
```

### 2. Test Before and After
Run `node --test tests/*.test.js` before and after changes. Never commit with failing tests.

### 3. Never Guess or Assume
- **Always use Context7 MCP** or official docs before writing code that uses any library/framework/API.
- **Always use web search** when uncertain about best practices or current standards.
- **Always ask the user** when requirements are ambiguous or multiple valid approaches exist.
- **Never rely on training data** for API signatures, config options, or syntax — it may be outdated.
- If Context7 is unavailable, fetch official docs via web search.
- Goal: zero bugs from outdated API usage or deprecated patterns.

### 4. Use Superpowers — Always
The obra/superpowers plugin is mandatory. Before each significant task:
- Check available skills and invoke relevant ones
- Use brainstorming before creative/feature work
- Use systematic-debugging before fixing bugs
- Use test-driven-development before writing implementation
- Use verification-before-completion before claiming done
- Use requesting-code-review after major features
- Delegate to subagents when tasks can be parallelized

### 5. Research Latest Best Practices
Before choosing a technology, library, pattern, or approach:
- Search for current best practices (2024-2026)
- Check if better-maintained or more performant alternatives exist
- Verify the chosen technology is actively maintained and not deprecated
- Mention what alternatives were considered and why

### 6. Understand the Environment First
At the start of a new session, inspect:
- OS, runtime versions, package manager
- Project structure, dependencies, build system
- Current deployment target
- **Ask the user** where they plan to deploy before making architectural decisions

### 7. Document Everything in This File
- This CLAUDE.md is the single source of truth for the project.
- When project description, logic, architecture, or scope changes — **rewrite the Project Description section**.
- When session-significant events happen — **append to SESSION_LOG.md**.
- Any new session reading this file should fully understand the project state.

### 8. Log Every User Prompt (in PROMPT_LOG.md)
- Every user prompt gets appended to `PROMPT_LOG.md` with sequential number, timestamp, and text.
- If `PROMPT_LOG.md` doesn't exist, create it.
- Do this BEFORE starting work on the request.

### 9. Always Use Context7 MCP or Latest Documentation
Before writing or modifying code that uses any library, framework, or API:
- Look up latest docs via Context7 MCP or official sources
- Do NOT rely on training data for syntax, API signatures, or configuration
- Goal: zero bugs from outdated API usage

### 10. Use Playwright for Web UI Testing After UI Changes
(Not currently applicable — this is a CLI tool. Include if UI is added later.)

### 11. Always Use Superpowers — Add Relevant Subagents and Skills
- Evaluate which superpowers skills/subagents are relevant before each task
- Use subagents for parallel work (test-writing, review, exploration)
- Don't just have superpowers installed — actively use them

### 12. Research Latest Best Practices Before Tech Decisions
- Before choosing technology, library, architecture, or approach — search for current best practices
- Check for newer, better-maintained, more performant alternatives
- Verify technology is actively maintained and not deprecated
- Use Context7 MCP, official docs, or web search to verify

## Verification Checklist (Before Claiming Done)

1. `node --test tests/*.test.js` — all tests pass
2. Generate output and run parity diff — diff lines did not increase
3. No debug artifacts committed (debug_*, .tmp*, etc.)
4. Changes documented in docs/plans/ if significant

## Agent Parallel Work Guide

See [additionalContext/AGENTS_GUIDE.md](additionalContext/AGENTS_GUIDE.md) for recommended subagent configurations for parallel development work.

## Methodology

See [additionalContext/METHODOLOGY.md](additionalContext/METHODOLOGY.md) for the systematic approach to closing parity gaps.

## Current Plan

See [additionalContext/PARITY_CLOSURE_PLAN.md](additionalContext/PARITY_CLOSURE_PLAN.md) for the active implementation plan.

## Dependencies

| Package | Purpose |
|---------|---------|
| execa | Subprocess execution (JSDoc) |
| fast-glob | File pattern matching |
| fs-extra | Extended file system ops |
| jsdoc | Doclet extraction from source |
| minimist | CLI argument parsing |
| p-map | Concurrency-limited parallel mapping |
| peggy | PEG parser generator (type grammar) |
| typescript | Type validation |

## Git Workflow

- **Main branch:** `main` (PRs target here)
- **Development:** `master` (current work)
- **Artifacts excluded:** `.tmp*`, `dist*`, `data/`, debug files
- **CI:** GitHub Actions runs generation on push to `main`
