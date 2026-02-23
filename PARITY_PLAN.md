# Parity Plan: Achieving Byte-for-Byte Match with `index.d.ts`

**Current State:** ~1,720 diff lines
**Target State:** < 20 diff lines (ideally 0)

---

## Root Cause Analysis

After comparing our JS generator (`dtsGenerator/src/`) with the original Clojure reference-engine (`reference-engine/src/reference/adoc/`), the following **critical differences in logic** were identified:

---

## ISSUE 1: Method Sorting — `sort-by :name` (CRITICAL — ~500+ diff lines)

### Reference Engine (Clojure)
**File:** `inheritance.clj:51`
```clojure
all-methods (sort-by :name (concat class-methods inherited-methods))
```
The original sorts ALL methods (local + inherited) **purely by `:name`** using Clojure's default `sort-by`, which is a **stable sort**. There is NO concept of `isMain`, `ancestorDistance`, or `originalIndex` in the sort. Methods with the same name preserve their insertion order (locals first, then inherited).

### Our Generator (JavaScript)
**File:** `ts-generator.js:217-234`
```javascript
sortedMethods.sort((a, b) => {
    if (a.isMain && !b.isMain) return -1;  // isMain first
    if (!a.isMain && b.isMain) return 1;
    const distA = a.ancestorDistance || 0;  // then distance
    ...
    if (a.name !== b.name) return a.name < b.name ? -1 : 1;  // then alpha
    return (a.originalIndex || 0) - (b.originalIndex || 0);  // then index
});
```
We sort by `isMain`, then `ancestorDistance`, then alphabetical, then `originalIndex`.

### Fix
**Remove `isMain` and `ancestorDistance` from the sort.** Sort purely by `name` (alphabetical). For same-name overloads, preserve insertion order (stable sort).

---

## ISSUE 2: `a11y()` and `name()` Methods Are Extra (~57 diff lines)

### Reference Engine (Clojure)
**File:** `inheritance.clj:48-50`
```clojure
inherited-methods (filter
    (fn [m] (some #(= (:name m) (:name %)) (:all-members class)))
    parent-class-methods)
```
The reference engine filters inherited methods against `:all-members` of the child class. Only methods whose names appear in the child's **`:all-members`** list (all JSDoc `member` kind doclets for that class) are kept. Methods like `a11y()` or `name()` that exist in a parent but are NOT declared as members (even as `@inheritDoc` stubs) in the child class are **excluded**.

### Our Generator (JavaScript)
**File:** `inheritance.js:111-142`
```javascript
// NEW: Copy methods from parent that are NOT in child at all
for (const [name, parentMethods] of parentMethodsByName) {
    if (!childMethodsByName.has(name)) {
        // ... copies ALL parent methods not already in child
    }
}
```
We copy **ALL** parent methods that are missing from the child. This is too aggressive — it adds methods like `a11y()` and `name()` that the child class never declared even as stubs.

### Fix
**Only inherit methods that appear in the child's `all-members` list** (i.e., methods that have at least an `@inheritDoc` stub or explicit declaration in the child's JSDoc). This requires:
1. In `structurize.js`: Capture ALL member doclets (including those with `inheritdoc`) as an `allMembers` name-set on each class.
2. In `inheritance.js`: When copying parent methods NOT in child, check if the method name exists in `allMembers`. If not, skip it.

---

## ISSUE 3: Missing `update-classes-methods` / Return Type Covariance Step (~30+ diff lines)

### Reference Engine (Clojure)
**File:** `core.clj:91` and `tree.clj:26-32, 44-55`
```clojure
replaced-top-level-ts (tree-ts/update-classes-methods top-level-ts :add-parent-methods true)
```
After inheritance, the reference engine runs `update-classes-methods` which:
1. **Replaces parent return types with child class types** (covariant returns). E.g., if `title()` returns `anychart.core.Chart` in parent, but we're generating for `anychart.charts.Pie`, it replaces the return type with `anychart.charts.Pie`.
2. **Adds inherited methods from parents** that aren't already present.

This is done by walking the full parent chain and collecting all ancestor names, then replacing any return type matching an ancestor with the current class name.

### Our Generator (JavaScript)
**File:** `inheritance.js:13-26`
We have `getCovariantReturns()` but it only replaces **direct ancestors** via `isAncestor()` check. The reference engine's approach is simpler and more aggressive: it collects ALL parent names up the chain, and replaces ANY return type matching them.

### Fix
Verify our `getCovariantReturns` produces the same output as `tree.clj:update-self-methods`. The logic should be equivalent, but edge cases with multiple inheritance chains may differ.

---

## ISSUE 4: `module` vs `namespace` keyword (~10 diff lines)

### Reference Engine (Clojure)
**File:** `typescript.clj:217`
```clojure
(str p4 "module " module " {\n" ...)
```
For nested classes (e.g., `anychart.core.series.RenderingSettings`), the reference engine uses `module` keyword.

### Our Generator (JavaScript)
**File:** `ts-generator.js:202`
```javascript
return `${p4}module ${module} {\n${classDeclaration(inner, topLevel)}\n    }...`;
```
We already use `module` for nested classes. But for the companion namespace (enums/typedefs), we use `namespace`. The reference engine also uses `namespace` there (`typescript.clj:204`). Need to verify exact alignment.

### Fix
Verify the `module` vs `namespace` usage matches exactly. Check for `RenderingSettings` specifically (`module RenderingSettings` vs `namespace RenderingSettings`).

---

## ISSUE 5: `group-functions` Logic — Method Overload Grouping (~100+ diff lines)

### Reference Engine (Clojure)
**File:** `structs.clj:186-202`
```clojure
(defn- group-functions [functions]
    (sort-by :name
        (map (fn [[name methods]]
               {:name name :overrides methods ...})
             (reduce ... {} functions))))
```
The reference engine:
1. Groups functions by name into `{name: [overrides]}` structure.
2. Then **sorts groups by name**.
3. Then during output (`typescript.clj:224`): `(mapcat :overrides (:methods class))` — flattens overrides in group order.

This means the output order is: **sorted by method name, then within each name, overrides appear in their original insertion order**.

### Our Generator (JavaScript)
We use `methodsBySig` (Map by signature), which deduplicates by signature. Methods with the same name but different signatures are stored separately. Then in `ts-generator.js` we flatten all overrides and sort them.

### Fix
Our `structurize.js` should group methods by **name** (not by signature), creating an `overrides` array per method name — exactly like the Clojure `group-functions`. Then sort groups by name. Then in `ts-generator.js`, just `flatMap(m => m.overrides)` and output in order (NO re-sorting).

---

## ISSUE 6: Typedef Inline Expansion — Function Typedefs (~50+ diff lines)

### Reference Engine (Clojure)
**File:** `typedef_builder.clj:57-99`
The reference engine runs `fix-typedef` AFTER inheritance, which:
1. Separates typedefs into "function typedefs" (those with `type = "function"`) and "normal typedefs".
2. Replaces ALL occurrences of function typedef names in method params/returns with their inline expansion: `((param:type)=>returnType)`.
3. Removes function typedefs from the typedef list (they are inlined).

### Our Generator (JavaScript)
**File:** `typedef-builder.js` + `ts-generator.js:135-168`
We have similar logic but it may differ in:
- **Detection:** We check `td.params.length > 0 || td.type.includes('function')` vs Clojure's `(= "function" (first (:type typedef)))`.
- **Replacement scope:** Need to verify we transform BOTH `params` AND `returns` in BOTH `classes` AND `namespaces`.

### Fix
Align detection logic. Ensure function typedef detection matches exactly: `type[0] === "function"`.

---

## ISSUE 7: `anychart.graphics.math.rect` Removal (~5 diff lines)

### Reference Engine (Clojure)
**File:** `core.clj:139-146`
```clojure
anychart-top-level (setval [:namespaces ALL
    #(= "anychart.graphics.math" (:full-name %))
    :functions ALL #(= "rect" (:name %))]
    NONE replaced-top-level-ts)
```
The reference engine explicitly **removes** the `rect` function from `anychart.graphics.math` namespace before generating `index.d.ts`.

### Our Generator
We do NOT have this removal step.

### Fix
Add explicit removal of `anychart.graphics.math.rect` function in the pipeline before TS generation.

---

## ISSUE 8: Enum Fields — No Values for String Enums (~20 diff lines)

### Reference Engine (Clojure)
**File:** `typescript.clj:163-167`
```clojure
(defn enum-field [field]
  (if (integer? (:value field))
    (str p8 (:name field) " = " (:value field))
    (str p8 (:name field))))
```
String enum fields output **only the field name** (no `= "value"` assignment).

### Our Generator
**File:** `ts-generator.js:178-186`
```javascript
if (Number.isInteger(field.value)) {
    return `${p8}${field.name} = ${field.value}`;
}
return `${p8}${field.name}`;
```
Our logic appears similar but may differ for fields with explicit string values (we use `JSON.stringify`).

### Fix
Ensure string enum fields omit the `= "value"` part entirely.

---

## ISSUE 9: `all-members` Filtering in Inheritance (~100+ diff lines)

### Reference Engine (Clojure)
**File:** `structs.clj:394-398, 425`
```clojure
(defn- get-all-members [doclets class]
    (filter #(and (= (:kind %) "member")
                  (= (:longname class) (:memberof %))
                  (not (is-static %))) doclets))
```
Each class stores `:all-members` — a list of ALL non-static `member` kind doclets. This is used during inheritance to filter which parent methods to keep (only those whose name matches an `all-members` entry).

### Our Generator
We do NOT track `allMembers`. We track `isInheritDoc` on individual methods, but don't have the full member list for filtering.

### Fix
In `structurize.js`, for each class, capture `allMembers: members.filter(m => m.kind === 'member' && !isStatic(m)).map(m => m.name)`. Pass this to `inheritance.js` and use it to filter inherited methods.

---

## Implementation Order (Priority)

| # | Issue | Est. Impact | Difficulty |
|---|-------|-------------|------------|
| 1 | **Method Sorting** (pure name sort) | ~500 lines | Easy |
| 2 | **`all-members` filtering** (stop inheriting `a11y`, `name`) | ~150 lines | Medium |
| 3 | **Method Grouping** (group by name, not signature) | ~200 lines | Medium |
| 4 | **`rect` removal** | ~5 lines | Trivial |
| 5 | **Enum field values** | ~20 lines | Easy |
| 6 | **`module` vs `namespace`** | ~10 lines | Easy |
| 7 | **Typedef inline expansion** alignment | ~50 lines | Medium |
| 8 | **Return type covariance** edge cases | ~30 lines | Medium |

**Expected result after all fixes:** < 20 diff lines.
