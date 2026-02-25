# AnyChart TypeScript Definition Generator

Generates TypeScript declaration files (`.d.ts`) from AnyChart's JSDoc-annotated `.adoc` source files.

## Quick Start

```bash
npm install
npm run build    # generate + validate with tsc --strict
```

## Commands

| Command | Description |
|---------|-------------|
| `npm run build` | Generate declarations and validate with TypeScript |
| `npm run generate` | Generate declarations only |
| `npm run validate` | Run `tsc --strict` on generated files |
| `npm test` | Run all tests |
| `npm run compile-grammar` | Recompile Peggy type grammar after `.pegjs` changes |

## Pipeline

```
.adoc files → adoc-parser → structurize → inheritance → typedef-builder → ts-generator → .d.ts
```

1. **Parsing** — `adoc-parser.js` reads `.adoc` files directly using `comment-parser`, extracting doclets from JSDoc comment blocks and their associated identifier lines.
2. **Structuring** — `structurize.js` organizes flat doclets into a hierarchy of namespaces, classes, enums, and typedefs.
3. **Inheritance** — `inheritance.js` resolves class hierarchies, propagates inherited methods, and applies covariant return types for method chaining.
4. **Generation** — `ts-generator.js` renders the model as `.d.ts` code. `type-writer.js` uses a Peggy grammar to convert JSDoc type strings into TypeScript syntax.
5. **Validation** — `tsc --strict` validates the generated output.

## Project Structure

| File | Description |
|------|-------------|
| `src/cli.js` | CLI entry point (`--data-dir`, `--version`, `--output-dir`, `--flat`) |
| `src/pipeline.js` | Orchestrates all stages, writes output, runs tsc validation |
| `src/adoc-parser.js` | Parses `.adoc` files into doclets using `comment-parser` |
| `src/jsdoc-runner.js` | File discovery, caching, doclet normalization |
| `src/structurize.js` | Flat doclets → hierarchical namespace/class/method model |
| `src/inheritance.js` | Class hierarchy resolution, covariant returns |
| `src/typedef-builder.js` | Typedef normalization |
| `src/ts-generator.js` | Final `.d.ts` rendering |
| `src/types/jsdoc-type-parser.pegjs` | Peggy grammar for JSDoc type syntax |
| `src/types/type-parser.js` | Wraps compiled Peggy parser |
| `src/types/type-writer.js` | Type AST → TypeScript syntax conversion |

## CLI Usage

```bash
node src/cli.js --data-dir ./data --version 8.14.1 --output-dir . --flat
```

| Flag | Description | Default |
|------|-------------|---------|
| `--data-dir` | Path to folder containing `versions/<version>/*.adoc` | `./data` |
| `--version` | Version subdirectory to process | `latest` |
| `--output-dir` | Where to write generated `.d.ts` files | `./.tmp` |
| `--flat` | Output directly to `output-dir` (no version subdirectory) | `false` |

## Output

- `index.d.ts` — Main AnyChart type declarations
- `graphics.d.ts` — GraphicsJS engine declarations

## Dependencies

- **Production:** `comment-parser` (JSDoc comment block parsing)
- **Dev:** `peggy` (grammar compilation), `typescript` (output validation), `fast-check` (property-based testing)
