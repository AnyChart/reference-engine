# Performance Optimization Design

## Date: 2026-02-21

## Baseline
- **Cold run**: 22.5s (16.1s pipeline + overhead)
- **Bottlenecks**: Doclet extraction (76.6%), TS generation (21.8%)

## Optimizations Implemented

### O1: JSDoc Output Caching
- **File**: `src/jsdoc-runner.js`
- **Approach**: SHA-256 hash of all .adoc source files. Cached raw doclets stored in OS temp dir.
- **Impact**: ~12s saved on repeat runs (largest single optimization)

### O2: In-place String Replacement
- **File**: `src/jsdoc-runner.js`
- **Approach**: Replaced `JSON.stringify → replace → JSON.parse` with recursive in-place property mutation for `acgraph` → `anychart.graphics` and `{{branch-name}}` → version replacements across 22,721 doclets.
- **Impact**: ~500-1000ms saved per run

### O3: Type Parsing Memoization
- **File**: `src/types/type-writer.js`
- **Approach**: `Map`-based cache for `jsdocToTs()`. Many type strings are identical across thousands of methods.
- **Impact**: TS generation 3,510ms → 86ms (40x faster)

### O4: Pre-compiled Typedef Replacement Regexes
- **File**: `src/types/type-writer.js`
- **Approach**: Regex objects compiled once in `setReplacements()` instead of inside the per-type inner loop.
- **Impact**: Contributes to O3's improvement

### O5: Reuse Index TS for Graphics
- **File**: `src/ts-generator.js`
- **Approach**: Cache raw TS string from `generateTSDeclarations()`, reuse in `generateGraphicsTSDeclarations()` with string replacement.
- **Impact**: ~1.5s saved (eliminated duplicate generation)

### O6: Map-based Entity Lookups
- **File**: `src/ts-generator.js`
- **Approach**: Pre-built `Map<name, entity>` for classes, enums, and typedefs. Replaced O(n) `.find()` with O(1) `.get()`.
- **Impact**: ~200ms saved in TS generation

### O7: Memoized Inheritance Depth
- **File**: `src/inheritance.js`
- **Approach**: `Map`-based cache for recursive `getInheritanceDepth()`, accepting classMap for O(1) parent lookups.
- **Impact**: ~50ms saved

### O8: Pre-compiled Peggy Parser
- **Files**: `src/types/type-parser.js`, `src/types/jsdoc-type-parser-compiled.js`
- **Approach**: Generated parser source at build time (37.5 KB), imported directly instead of runtime grammar compilation.
- **Impact**: ~200ms startup time saved

## Results

| Metric | Before | After (cold) | After (warm) | Speedup |
|--------|--------|-------------|-------------|---------|
| Total time | 22.5s | 16.8s | 0.84s | **26.7x** |
| Doclet extraction | 12,341ms | ~12,300ms | 337ms | 36.6x |
| TS Generation | 3,510ms | ~90ms | 86ms | 40.8x |
| Inheritance | 202ms | ~180ms | 180ms | 1.1x |

## Verification
- All 24 tests pass
- Parity diff unchanged at 28 lines
- No functional regressions
