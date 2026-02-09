# AnyChart TypeScript Definition Generator

This Node.js tool automates the generation of TypeScript declaration files (`.d.ts`) from AnyChart's source API documentation (`.adoc` files). It replicates the behavior of the original Clojure-based generator to ensure backward compatibility and output parity.

## Core Process

The generation pipeline follows these steps:

1.  **Extraction**: 
    - Copies source `.adoc` files to a temporary workspace.
    - Pre-processes them (converting `@define` to `@const`, replacing `@ignoreDoc` with `@doc`) to make them JSDoc-compliant.
    - Runs `jsdoc` in parallel groups to extract "doclets" (JSON representations of the comments).
2.  **Structuring**:
    - `structurize.js` organizes the flat list of doclets into a hierarchical tree of Namespaces, Classes, Enums, and Typedefs.
    - Captures top-level constants like `VERSION` and `DEFAULT_THEME`.
3.  **Processing**:
    - `typedef-builder.js` refines type definitions.
    - *Note*: Explicit inheritance flattening is disabled to rely on TypeScript's native `extends` capability, matching the legacy output format.
4.  **Generation**:
    - `ts-generator.js` walks the tree and emits TypeScript code.
    - `type-writer.js` uses a Peggy grammar to parse complex JSDoc type strings (e.g., `Object.<string, Array<number>>`) and convert them into valid TypeScript syntax (e.g., `{[key: string]: number[]}`).
    - Applies strict alphabetical sorting to ensures deterministic, parity-compliant output.

## Project Structure

### Source Code (`src/`)

| File | Description |
|------|-------------|
| **`cli.js`** | The command-line entry point. Handles argument parsing (`--data-dir`, `--version`, etc.) and initiates the pipeline. |
| **`pipeline.js`** | The main orchestrator. Calls the extraction, structuring, and generation steps sequentially. |
| **`jsdoc-runner.js`** | Handles the interaction with JSDoc. Manages file pre-processing, parallel execution groups, and doclet extraction. |
| **`structurize.js`** | Transforms the raw JSDoc JSON output into a structured object model (Namespaces -> Classes -> Methods). Handles logic for identifying constants vs. members. |
| **`typedef-builder.js`** | Post-processes structurized data to refine complex typedefs. |
| **`ts-generator.js`** | The final rendering layer. Converts the internal object model into valid `.d.ts` source code, including formatting and indentation. |
| **`types/jsdoc-type-parser.pegjs`** | A Peggy grammar file defining how to parse AnyChart's specific JSDoc type syntax. |
| **`types/type-parser.js`** | The parser generated from the `.pegjs` grammar. |
| **`types/type-writer.js`** | Converts the parsed type AST into TypeScript type strings. Handles specific mapping rules (e.g., `function()` -> `(() => void)`). |

### Root Files

| File | Description |
|------|-------------|
| **`package.json`** | Defines project dependencies (chiefly `jsdoc`, `peggy`, `fs-extra`) and scripts. |
| **`.gitignore`** | Ensures temporary build artifacts (like `.tmp-*` directories) are not committed. |
| **`data/`** | Contains the versioned API source files (e.g., `8.14.1/`) used as input for generation. |

## Usage

1.  **Install Dependencies**:
    ```bash
    npm install
    ```

2.  **Run Generator**:
    ```bash
    node src/cli.js --data-dir ./data --version 8.14.1 --max-groups 32 --output-dir ./dist
    ```

    *   `--data-dir`: Path to the root folder containing API versions (e.g., `./data`).
    *   `--version`: The version subdirectory to process (e.g., `8.14.1`).
    *   `--max-groups`: Controls parallelism for JSDoc. Higher numbers equal faster runs but higher memory usage.
    *   `--output-dir`: Where the final `.d.ts` files will be saved.

## Output

The tool generates the following files in the output directory:
- `index.d.ts`: Main AnyChart definitions.
- `graphics.d.ts`: Definitions for the GraphicsJS engine (mapped from `acgraph` namespace).
