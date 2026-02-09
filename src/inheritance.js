
function shouldInherit(m) {
  return (!m.params || m.params.length === 0) && (!m.returns || m.returns.length === 0);
}

function isAncestor(ancestorName, childName, classMap) {
  if (ancestorName === childName) return true;
  const cl = classMap.get(childName);
  if (!cl || !cl.extends) return false;
  return cl.extends.some(ext => isAncestor(ancestorName, ext, classMap));
}

function getCovariantReturns(returns, childFullName, classMap) {
  if (!returns) return returns;
  return returns.map(r => {
    if (!r.types) return r;
    const newTypes = r.types.map(t => {
      // If it's a known class and an ancestor of current class, return self for chaining
      if (classMap.has(t) && isAncestor(t, childFullName, classMap)) {
        return childFullName;
      }
      return t;
    });
    return { ...r, types: newTypes };
  });
}

function copyDocs(target, source, parentName, childName, classMap) {
  target.params = source.params;
  target.returns = getCovariantReturns(source.returns, childName, classMap);
  if (!target.description) target.description = source.description;
}

function resolveClass(cl, classMap, visited) {
  if (visited.has(cl.fullName)) return;
  visited.add(cl.fullName);

  if (!cl.extends || cl.extends.length === 0) {
    // Initial class with no parents: isMain is just !isIgnored
    cl.methods.forEach(m => {
        if (m.overrides) {
            m.overrides.forEach(o => o.isMain = !o.isIgnored);
            m.isMain = m.overrides.some(o => o.isMain); // Helper for grouping
        } else {
            m.isMain = !m.isIgnored;
        }
    });
    return;
  }

  for (const parentName of cl.extends) {
    const parent = classMap.get(parentName);
    if (!parent) continue;

    // Resolve parent first
    resolveClass(parent, classMap, visited);

    // Group parent methods by name, flattening overrides
    const parentMethodsByName = new Map();
    parent.methods.forEach(pm => {
      const methods = pm.overrides || [pm];
      methods.forEach(m => {
          if (!parentMethodsByName.has(m.name)) parentMethodsByName.set(m.name, []);
          parentMethodsByName.get(m.name).push(m);
      });
    });

    // Group child methods by name to track index
    const childMethodsByName = new Map();
    cl.methods.forEach(m => {
       if (!childMethodsByName.has(m.name)) childMethodsByName.set(m.name, []);
       childMethodsByName.get(m.name).push(m);
    });

     // Inherit methods
    for (const [name, childMethods] of childMethodsByName) {
      const parentMethods = parentMethodsByName.get(name) || [];
      
      const isSingleInheritDoc = childMethods.length === 1 && shouldInherit(childMethods[0]);
      
      if (isSingleInheritDoc && parentMethods.length > 1) {
          // Special case: Child has 1 method, parent has N. 
          const childPlaceholder = childMethods[0];
          childPlaceholder.overrides = parentMethods.map(pm => {
              const clone = { ...pm }; 
              copyDocs(clone, pm, parent.fullName, cl.fullName, classMap);
              clone.overrides = undefined;
              clone.ancestorDistance = childPlaceholder.ancestorDistance;
              // isMain propagates from parent
              clone.isMain = pm.isMain;
              clone.isResolved = true;
              clone.ancestorDistance = childPlaceholder.ancestorDistance;
              return clone;
          });
          childPlaceholder.isMain = childPlaceholder.overrides.some(o => o.isMain);
          childPlaceholder.isResolved = true;
      } else {
          // Normal matching
          childMethods.forEach((m, index) => {
            const parentMethod = parentMethods[index] || parentMethods[0];
            if (parentMethod && shouldInherit(m)) {
                copyDocs(m, parentMethod, parent.fullName, cl.fullName, classMap);
                m.isResolved = true;
            }
            // isMain: true if child not ignored OR parent was main
            m.isMain = (!m.isIgnored) || (parentMethod ? parentMethod.isMain : false);
          });
      }
    }

    // NEW: Copy methods from parent that are NOT in child at all
    for (const [name, parentMethods] of parentMethodsByName) {
      if (!childMethodsByName.has(name)) {
        if (parentMethods.length === 1) {
          const pm = parentMethods[0];
          const clone = { ...pm };
          clone.isDirect = false;
          clone.ancestorDistance = (pm.ancestorDistance || 0) + 1;
          clone.returns = getCovariantReturns(pm.returns, cl.fullName, classMap);
          // isMain propagates from parent
          clone.isMain = pm.isMain;
          cl.methods.push(clone);
        } else {
          const holder = {
            name: name,
            isDirect: false,
            ancestorDistance: (parentMethods[0].ancestorDistance || 0) + 1,
            isMain: parentMethods.some(pm => pm.isMain),
            overrides: parentMethods.map(pm => {
              const clone = { ...pm };
              clone.isDirect = false;
              clone.ancestorDistance = (pm.ancestorDistance || 0) + 1;
              clone.returns = getCovariantReturns(pm.returns, cl.fullName, classMap);
              clone.isMain = pm.isMain;
              return clone;
            })
          };
          cl.methods.push(holder);
        }
        childMethodsByName.set(name, []);
      }
    }
  }
}

export function resolveInheritance(topLevel) {
  const classMap = new Map();
  topLevel.classes.forEach(cl => classMap.set(cl.fullName, cl));
  
  const visited = new Set();
  for (const cl of topLevel.classes) {
    resolveClass(cl, classMap, visited);
  }

  // Final pass for all classes to ensure ALL methods have proper covariance
  // Use a second loop to avoid modifying methods while they are being inherited
  for (const cl of topLevel.classes) {
    cl.methods.forEach(m => {
       if (m.overrides) {
         m.overrides.forEach(o => {
           o.returns = getCovariantReturns(o.returns, cl.fullName, classMap);
         });
       } else {
         m.returns = getCovariantReturns(m.returns, cl.fullName, classMap);
       }
    });
  }
  
  return topLevel;
}
