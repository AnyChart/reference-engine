# CLAUDE.md — AnyChart DTS Generator

## Project Description

**Name:** dtsGenerator
**Purpose:** Node.js pipeline that generates TypeScript declaration files (`.d.ts`) from AnyChart's JSDoc-annotated `.adoc` source files.
**Status:** Complete. Generates valid TypeScript declarations that pass `tsc --strict`.

## Environment

- **Runtime:** Node.js v22+ (uses `util.parseArgs`, `node:fs/promises`)
- **Shell:** bash (Git Bash on Windows — use Unix syntax, forward slashes)
- **Build System:** npm scripts
- **Test Runner:** `node --test` (built-in)

## Architecture

```
.adoc files → adoc-parser → jsdoc-runner → structurize → inheritance → typedef-builder → ts-generator → .d.ts
```

| Stage | File | Purpose |
|-------|------|---------|
| Entry | `src/cli.js` | CLI arg parsing via `util.parseArgs` |
| Orchestrator | `src/pipeline.js` | Sequences stages, writes output, runs tsc validation |
| Parsing | `src/adoc-parser.js` | Parses `.adoc` files directly using `comment-parser` |
| Extraction | `src/jsdoc-runner.js` | File discovery, caching, doclet normalization |
| Structurization | `src/structurize.js` | Flat doclets → hierarchical model |
| Inheritance | `src/inheritance.js` | Class hierarchy, method propagation, covariant returns |
| Typedef | `src/typedef-builder.js` | Typedef normalization |
| Generation | `src/ts-generator.js` | Renders `.d.ts` using Peggy-based type parser |
| Type Parsing | `src/types/type-parser.js` | JSDoc type string → AST |
| Type Writing | `src/types/type-writer.js` | AST → TypeScript syntax |
| Grammar | `src/types/jsdoc-type-parser.pegjs` | Peggy grammar (compiled to `-compiled.js`) |

## Commands

```bash
npm install            # Install dependencies
npm run build          # Generate declarations + tsc validation
npm test               # Run all tests
npm run generate       # Generate only (no validation)
npm run validate       # tsc validation only
npm run compile-grammar  # Recompile Peggy grammar after .pegjs changes
```

## Project Structure

```
├── src/                    # Core source code
│   ├── cli.js              # CLI entry point
│   ├── pipeline.js         # Pipeline orchestrator
│   ├── adoc-parser.js      # .adoc file parser (comment-parser based)
│   ├── jsdoc-runner.js     # Doclet extraction + caching
│   ├── structurize.js      # Doclet → hierarchy
│   ├── inheritance.js      # Inheritance resolution
│   ├── typedef-builder.js  # Typedef normalization
│   ├── ts-generator.js     # .d.ts rendering
│   └── types/              # Type parsing subsystem
│       ├── type-parser.js
│       ├── type-writer.js
│       ├── jsdoc-type-parser.pegjs
│       └── jsdoc-type-parser-compiled.js
├── tests/                  # Node.js test runner tests
├── data/versions/          # Input .adoc source files
├── index.d.ts              # Generated output (~32k lines)
├── graphics.d.ts           # Generated output (graphics subset)
└── .github/workflows/      # CI pipeline
```

## Core Concepts

### allMemberNames
Each class tracks which method names are explicitly documented. During inheritance, only methods in the child's `allMemberNames` set are inherited (plus `ALWAYS_INHERIT_METHODS`).

### Covariance
When a child class inherits a method returning the parent type, the return type is rewritten to the child type (method chaining).

### Function Typedef Inlining
Function-type typedefs are inlined at usage sites via a replacement map in `type-writer.js`, not emitted as standalone declarations.

### Deterministic Output
- Classes sorted alphabetically within namespaces
- Methods sorted alphabetically by name
- Overloads preserved in original order within name groups
- Constants, enums, typedefs sorted alphabetically

## Dependencies

| Package | Type | Purpose |
|---------|------|---------|
| comment-parser | prod | JSDoc comment block parsing |
| peggy | dev | PEG grammar → parser compilation |
| typescript | dev | Output validation via `tsc --strict` |
| fast-check | dev | Property-based testing |

## Working Rules

### 1. Validate After Changes
Run `npm run build` — this generates output AND validates with tsc. If tsc passes, the output is valid.

### 2. Test Before and After
Run `npm test` before and after changes. Never commit with failing tests.

### 3. Grammar Changes
After editing `jsdoc-type-parser.pegjs`, run `npm run compile-grammar` to regenerate the compiled parser.

## Git Workflow

- **Main branch:** `main` (PRs target here)
- **Development:** `master` (current work)
- **CI:** GitHub Actions runs tests + build on push to `main`
