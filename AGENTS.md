# AGENTS.md

## Project Mission

This repository generates AnyChart TypeScript declaration files from AnyChart `.adoc` API sources.  
Primary success criteria:
1. Deterministic output.
2. Behavioral parity with the legacy Clojure generator.
3. Clean separation between source logic and generated/debug artifacts.

## Core Functionality (Do Not Treat as Artifacts)

- `src/cli.js` - CLI entrypoint.
- `src/pipeline.js` - Pipeline orchestration.
- `src/jsdoc-runner.js` - JSDoc extraction and normalization.
- `src/structurize.js` - Doclet-to-model transformation.
- `src/inheritance.js` - Inheritance/covariance resolution.
- `src/typedef-builder.js` - Typedef post-processing.
- `src/ts-generator.js` - TypeScript declaration rendering.
- `src/types/*` - JSDoc type parsing/writing.
- `src/class-order.json` - Control ordering for class output.
- `README.md` - Project usage and architecture.
- `.github/workflows/generate.yml` - CI generation workflow.
- `index.d.ts` - Reference baseline for parity comparison.
- `docs/plans/*` - Implementation plans and design docs.

## Non-Core Artifacts (Ignore/Clean)

Generated and scratch files must not be committed:
- debug scripts and dumps: `debug_*.js`, `debug_*.log`, `dump_*.log`
- parity snapshots: `sunburst_*.txt`, `sunburst_*.json`, `diff*.txt`
- generated output directories: `.tmp*`, `dist*`, `out/`
- local datasets and temp version copies: `data/`, `data/versions-tmp*/`

## Current Engineering Context

Active objective is parity closure with the Clojure reference implementation.  
See:
- `PARITY_PLAN.md`
- `docs/plans/2026-02-13-parity-closure-implementation-plan.md`

High-impact parity areas:
1. Class method ordering/grouping.
2. `all-members` filtering in inheritance.
3. Typedef inlining equivalence.
4. Namespace/module emission parity.
5. Known special-case removals (for example `anychart.graphics.math.rect`).

## MCP and Research Rules

Use local repository evidence first.  
Use external tools only when local sources are insufficient.

1. `shell_command`:
   - Prefer `rg`/`rg --files` for search.
   - Parallelize independent reads/diffs.
2. Context7:
   - Required for external library/framework API clarification.
   - Call `resolve-library-id` before `query-docs`.
   - Ask specific, scoped questions tied to the current file/function.
3. MCP resources:
   - Check `list_mcp_resources` / `list_mcp_resource_templates` before web search when structured context might exist.
4. Playwright MCP:
   - Use only for browser automation/flow debugging tasks.
5. Web search:
   - Use for unstable facts or when explicitly requested.
   - Prefer primary sources (official docs/repos/specs).

## Superpowers / Skill Policy

Follow these skills when triggered by task type:

1. `using-superpowers` at conversation start.
2. `brainstorming` before creative design/behavioral modifications.
3. `writing-plans` for multi-step requirements before implementation.
4. `systematic-debugging` for bugs, regressions, or failing tests.
5. `test-driven-development` for feature/bugfix implementation.
6. `verification-before-completion` before claiming success.
7. `requesting-code-review` before merge when major changes are complete.
8. `finishing-a-development-branch` when implementation and verification are done.
9. `gh-fix-ci` / `gh-address-comments` / `yeet` only when explicitly requested or clearly applicable.

## Working Rules for This Repo

1. Preserve parity-first behavior; do not do broad refactors during parity fixes.
2. Keep output deterministic (ordering changes require parity evidence).
3. Validate with concrete diff checks, not assumptions.
4. Keep temporary analysis artifacts out of git.
5. Document major decisions in `docs/plans/`.

## Verification Checklist

Run before closing a task:

1. `git status --short`
2. `node src/cli.js --data-dir ./data --version <version> --jsdoc-bin ./node_modules/.bin/jsdoc --max-groups 8 --output-dir ./.tmp-verify`
3. Compare generated output against control/reference as required by task.
4. Ensure no artifact files are staged unless explicitly requested.
