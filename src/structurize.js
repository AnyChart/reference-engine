function paramHasDefault(param) {
  return param.description && /^\s*\[[^\]]*\]\s*/.test(param.description);
}

function reduceToParamDefault(description) {
  let state = null;
  let cnt = 0;
  let def = '';
  let desc = '';
  for (let i=0;i<description.length;i++) {
    const c = description[i];
    if (state === null && /\s/.test(c)) continue;
    if (state === null && c === '[') {
      state = 'start-default';
      cnt = 1;
      def = '';
      desc = '';
      continue;
    }
    if (state === 'start-default' && c === ']') {
      if (cnt === 1) state = 'end-default';
      else { def += c; cnt--; }
      continue;
    }
    if (state === 'start-default' && c === '[') { def += c; cnt++; continue; }
    if (state === 'start-default') { def += c; continue; }
    desc += c;
  }
  return { default: def, description: desc };
}

function parseFunctionParam(param) {
  const result = { 
    name: param.name ? param.name.replace(/^opt_/, '') : undefined, 
    types: (param.type && param.type.names) || [] 
  };
  
  if (paramHasDefault(param)) {
    const d = reduceToParamDefault(param.description);
    result.description = d.description;
    result.default = d.default;
  } else {
    result.description = param.description;
  }
  
  if (param.optional || (param.name && param.name.startsWith('opt_'))) {
    result.optional = true;
  }
  return result;
}

function parseFunctionParams(params) {
  if (!params) return [];
  return params.map(parseFunctionParam);
}

function isStaticDoclet(doclet) {
  return doclet && doclet.scope === 'static';
}

function structurize(doclets) {
  const topLevel = {
    namespaces: [],
    classes: [],
    typedefs: [],
    enums: [],
    constants: []
  };

  const docletsByMemberOf = {};
  doclets.forEach(d => {

    if (d.memberof) {
      if (!docletsByMemberOf[d.memberof]) docletsByMemberOf[d.memberof] = [];
      docletsByMemberOf[d.memberof].push(d);
    }
  });

  const allNsNames = new Set(doclets.filter(d => d.kind === 'namespace').map(d => d.longname));
  const allClassNames = new Set(doclets.filter(d => d.kind === 'class').map(d => d.longname));

  const namespacesByLongname = new Map();
  doclets.filter(d => d.kind === 'namespace').forEach(ns => {
    if (!namespacesByLongname.has(ns.longname)) {
      namespacesByLongname.set(ns.longname, ns);
    }
  });

  namespacesByLongname.forEach(ns => {
    const fullName = ns.longname;
    const nsObj = {
      fullName: fullName,
      name: ns.name,
      constants: [],
      functions: [],
      typedefs: [],
      enums: [],
      classes: [],
    };

    const members = docletsByMemberOf[fullName] || [];
    const functionsBySig = new Map();
    members.forEach((m, i) => {
      const isFunction = m.kind === 'function' || (m.kind === 'member' && (!!m.params || !!m.returns || m.inheritdoc !== undefined));
      const hasOtherDocumented = members.some(other => other !== m && other.name === m.name && !other.undocumented);
      const isIgnored = !!(m.ignore || (m.undocumented && hasOtherDocumented) || m.tags?.some(t => t.title === 'ignore'));
      
      // Skip if it's actually a nested class or namespace (handled by their own doclets)
      if (!isFunction && (allNsNames.has(m.longname) || allClassNames.has(m.longname))) return;
      
      if (isFunction) {
        const params = parseFunctionParams(m.params);
        const returns = m.returns ? m.returns.map(r => ({ types: (r.type && r.type.names) || [] })) : [];
        const sig = m.name + "|" + 
                    params.map(p => (p.types || []).join(',')).join(';') + "|" + 
                    returns.map(r => r.types.join(',')).join(';');
        
        const method = {
          name: m.name,
          params: params,
          returns: returns,
          description: m.description,
          isDirect: true,
          ancestorDistance: 0,
          isIgnored: isIgnored,
          isInheritDoc: m.inheritdoc !== undefined,
          originalIndex: i
        };

        if (!functionsBySig.has(sig)) {
          functionsBySig.set(sig, method);
        } else {
          const existing = functionsBySig.get(sig);
          if (existing.isIgnored && !method.isIgnored) {
            existing.isIgnored = false;
            if (method.description) existing.description = method.description;
          } else if (!existing.description && method.description) {
            existing.description = method.description;
          }
        }
      } else if (m.isEnum) {
        // JSDoc marks enums as kind:'member' with isEnum:true
        // Enum fields are separate doclets with memberof set to the enum's longname
        const enumFields = (docletsByMemberOf[m.longname] || []).filter(f => f.kind === 'member');
        const e = {
          name: m.name,
          fullName: m.longname,
          fields: enumFields.map(p => ({
            name: p.name,
            value: p.defaultvalue
          })),
          description: m.description
        };
        topLevel.enums.push(e);
        nsObj.enums.push(e.fullName);
      } else if (m.kind === 'member' || m.kind === 'constant' || m.tags?.some(t => t.title === 'define')) {
        if (m.undocumented && hasOtherDocumented) return;

        // Handle members with properties (like MouseEvent) as typedefs
        if (m.properties && m.properties.length > 0) {
           const td = {
             name: m.name,
             fullName: m.longname,
             type: m.type && m.type.names ? m.type.names : ['Object'],
             properties: m.properties.map(p => ({
               name: p.name,
               type: p.type && p.type.names ? p.type.names : [],
               optional: p.optional
             })),
             description: m.description
           };
           topLevel.typedefs.push(td);
           nsObj.typedefs.push(td.fullName);
        } else {
          nsObj.constants.push({
            name: m.name,
            type: m.type && m.type.names ? m.type.names[0] : 'any',
            description: m.description
          });
        }
      } else if (m.kind === 'typedef') {
        const td = {
          name: m.name,
          fullName: m.longname,
          type: m.type && m.type.names ? m.type.names : [],
          params: parseFunctionParams(m.params),
          returns: m.returns ? m.returns.map(r => ({ types: (r.type && r.type.names) || [] })) : [],
          properties: m.properties ? m.properties.map(p => ({
            name: p.name,
            type: p.type && p.type.names ? p.type.names : [],
            optional: p.optional
          })) : [],
          description: m.description
        };
        topLevel.typedefs.push(td);
        nsObj.typedefs.push(td.fullName); // Use fullName to ensure match in ts-generator
      } else if (m.kind === 'enum') {
        const e = {
          name: m.name,
          fullName: m.longname,
          fields: m.properties ? m.properties.map(p => ({
            name: p.name,
            value: p.defaultvalue
          })) : [],
          description: m.description
        };
        topLevel.enums.push(e);
        nsObj.enums.push(e.fullName); // Use fullName to ensure match in ts-generator
      }
    });
    nsObj.functions = Array.from(functionsBySig.values());

    topLevel.namespaces.push(nsObj);
  });

  const classes = doclets.filter(d => d.kind === 'class');
  classes.forEach(cl => {

    const members = docletsByMemberOf[cl.longname] || [];
    const clObj = {
      name: cl.name,
      fullName: cl.longname,
      extends: cl.augments || [],
      allMemberNames: new Set(
        members
          .filter(m => !isStaticDoclet(m) && (m.kind === 'member' || m.kind === 'function'))
          .map(m => m.name)
          .filter(Boolean)
      ),
      hasInheritDocMethods: members.some(
        m =>
          !isStaticDoclet(m) &&
          (m.kind === 'member' || m.kind === 'function') &&
          m.inheritdoc !== undefined
      ),
      methods: [],
      typedefs: [],
      enums: [],
      classes: [],
      namespaces: []
    };

    const methodsByName = new Map();
    members.forEach((m, i) => {
      // Skip inherited members to let inheritance logic handle them matches index.d.ts structure
      if (m.inherited) return;

      // Handle isEnum members (JSDoc marks enums as kind:'member' with isEnum:true)
      if (m.isEnum) {
        const enumFields = (docletsByMemberOf[m.longname] || []).filter(f => f.kind === 'member');
        const e = {
          name: m.name,
          fullName: m.longname,
          fields: enumFields.map(p => ({
            name: p.name,
            value: p.defaultvalue
          })),
          description: m.description
        };
        topLevel.enums.push(e);
        clObj.enums.push(e.fullName);
        return;
      }

      const isFunction = m.kind === 'function' || (m.kind === 'member' && (!!m.params || !!m.returns || m.inheritdoc !== undefined));
      const hasOtherDocumented = members.some(other => other !== m && other.name === m.name && !other.undocumented);
      const isIgnored = !!(m.ignore || (m.undocumented && hasOtherDocumented) || m.tags?.some(t => t.title === 'ignore'));

      if (isFunction) {
        const params = parseFunctionParams(m.params);
        const returns = m.returns ? m.returns.map(r => ({ types: (r.type && r.type.names) || [] })) : [];
        const method = {
          name: m.name,
          params: params,
          returns: returns,
          description: m.description,
          isDirect: true,
          ancestorDistance: 0,
          isIgnored: isIgnored,
          isInheritDoc: m.inheritdoc !== undefined,
          originalIndex: i,
          sourceFile: m.meta && m.meta.filename
        };
        if (!methodsByName.has(m.name)) {
          methodsByName.set(m.name, []);
        }
        methodsByName.get(m.name).push(method);
      } else if (m.kind === 'typedef') {
        const td = {
          name: m.name,
          fullName: m.longname,
          type: m.type && m.type.names ? m.type.names : [],
          params: parseFunctionParams(m.params),
          returns: m.returns ? m.returns.map(r => ({ types: (r.type && r.type.names) || [] })) : [],
          properties: m.properties ? m.properties.map(p => ({
            name: p.name,
            type: p.type && p.type.names ? p.type.names : [],
            optional: p.optional
          })) : [],
          description: m.description
        };
        topLevel.typedefs.push(td);
        clObj.typedefs.push(td.fullName);
      } else if (m.kind === 'class') {
        clObj.classes.push(m.longname);
      } else if (m.kind === 'namespace') {
        clObj.namespaces.push(m.longname);
      } else if (m.kind === 'enum') {
        const e = {
          name: m.name,
          fullName: m.longname,
          fields: m.properties ? m.properties.map(p => ({
            name: p.name,
            value: p.defaultvalue
          })) : [],
          description: m.description
        };
        topLevel.enums.push(e);
        clObj.enums.push(e.fullName);
      }
    });
    const classFile = cl.meta && cl.meta.filename;
    clObj.methods = Array.from(methodsByName.keys()).sort((a, b) => {
      return a < b ? -1 : a > b ? 1 : 0;
    }).map(name => {
      const overloads = methodsByName.get(name);
      // Sort cross-file overloads: those returning the same class go first,
      // those returning a foreign class go last (preserving relative order within each group)
      if (classFile) {
        const foreignSameReturn = [];
        const own = [];
        const foreignDiffReturn = [];
        for (const m of overloads) {
          if (!m.sourceFile || m.sourceFile === classFile) {
            own.push(m);
          } else {
            // Check if return type matches this class
            const retTypes = (m.returns || []).flatMap(r => r.types || []);
            const returnsSameClass = retTypes.some(t => t === cl.longname);
            if (returnsSameClass) {
              foreignSameReturn.push(m);
            } else {
              foreignDiffReturn.push(m);
            }
          }
        }
        if (foreignSameReturn.length > 0 || foreignDiffReturn.length > 0) {
          overloads.length = 0;
          overloads.push(...foreignSameReturn, ...own, ...foreignDiffReturn);
        }
      }
      const all = overloads;
      const main = all.find(m => !m.isIgnored) || all[0];
      return {
        ...main,
        overrides: all
      };
    });

    topLevel.classes.push(clObj);
    
    // Add class to its namespace if applicable
    if (cl.memberof) {
      const ns = topLevel.namespaces.find(n => n.fullName === cl.memberof);
      if (ns) {
        ns.classes.push(cl.longname);
      }
    }
  });

  // Capture global typedefs
  const globalTypedefs = doclets.filter(d => d.kind === 'typedef' && !d.memberof);
  globalTypedefs.forEach(m => {
    const isIgnored = !!(m.ignore || m.undocumented || m.tags?.some(t => t.title === 'ignore'));
    if (isIgnored) return;

    const td = {
      name: m.name,
      fullName: m.name, // Global, so fullName is name
      type: m.type && m.type.names ? m.type.names : [],
      params: parseFunctionParams(m.params),
      returns: m.returns ? m.returns.map(r => ({ types: (r.type && r.type.names) || [] })) : [],
      properties: m.properties ? m.properties.map(p => ({
        name: p.name,
        type: p.type && p.type.names ? p.type.names : [],
        optional: p.optional
      })) : [],
      description: m.description
    };
    topLevel.typedefs.push(td);
  });

  return topLevel;
}

export { structurize };
