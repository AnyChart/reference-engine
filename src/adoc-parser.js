import { parse as parseComment } from 'comment-parser';

/**
 * Check if outer parens wrap the entire string (balanced).
 */
function hasWrappingParens(s) {
  if (!s.startsWith('(') || !s.endsWith(')')) return false;
  let depth = 0;
  for (let i = 0; i < s.length - 1; i++) {
    if (s[i] === '(') depth++;
    else if (s[i] === ')') depth--;
    if (depth === 0) return false;
  }
  return true;
}

/**
 * Strip JSDoc type modifiers: leading .../? /!, trailing =.
 * Returns { type: cleanedString, optional: boolean }
 */
function stripTypeModifiers(typeStr) {
  if (!typeStr) return { type: '', optional: false };
  let s = typeStr.trim();
  let optional = false;

  // 1. Strip trailing = (optional)
  if (s.endsWith('=')) {
    s = s.slice(0, -1).trim();
    optional = true;
  }
  // 2. Strip leading ... (rest)
  if (s.startsWith('...')) s = s.slice(3).trim();
  // 3. Strip leading ? (nullable) or ! (non-null)
  if (s.startsWith('?') || s.startsWith('!')) s = s.slice(1).trim();
  // 4. Strip outer parens (may appear at any stage)
  if (hasWrappingParens(s)) s = s.slice(1, -1).trim();

  return { type: s, optional };
}

/**
 * Parse type string from JSDoc {type} notation into a names array.
 * Strips JSDoc modifiers (=, ?, !) and splits top-level unions.
 */
function splitTypeNames(typeStr) {
  if (!typeStr) return [];
  const { type: cleaned } = stripTypeModifiers(typeStr);
  if (!cleaned) return [];

  // Split on | but not inside <>, (), {}
  const names = [];
  let depth = 0;
  let current = '';
  for (let i = 0; i < cleaned.length; i++) {
    const c = cleaned[i];
    if (c === '<' || c === '(' || c === '{') depth++;
    else if (c === '>' || c === ')' || c === '}') depth--;
    if (c === '|' && depth === 0) {
      const trimmed = current.trim();
      if (trimmed) names.push(trimmed);
      current = '';
    } else {
      current += c;
    }
  }
  const trimmed = current.trim();
  if (trimmed) names.push(trimmed);

  // Post-process each individual union member
  return names.map(n => {
    const { type: clean } = stripTypeModifiers(n);
    let result = clean || n;
    // Normalize Closure-style function types to just "function"
    // e.g. "function(number):number" → "function", "function(?):?" → "function"
    // JSDoc does this normalization; param details come from @param tags instead
    if (/^function\(/.test(result)) result = 'function';
    // Normalize JSDoc record types containing function() to "Object"
    // e.g. "{handleEvent:(function(?):?)}" → "Object"
    if (result.startsWith('{') && result.includes('function(')) result = 'Object';
    return result;
  });
}

/**
 * Derive longname, memberof, name, scope from an identifier line.
 * e.g. "anychart.charts.Bullet.prototype.data;" →
 *   longname: "anychart.charts.Bullet#data"
 *   memberof: "anychart.charts.Bullet"
 *   name: "data"
 *   scope: "instance"
 */
function parseIdentifier(line) {
  let cleaned = line.replace(/;/g, '').trim();
  if (!cleaned) return null;

  // Strip assignment expressions: "foo.bar = function(){}" → "foo.bar"
  const eqIdx = cleaned.indexOf(' = ');
  if (eqIdx !== -1) cleaned = cleaned.substring(0, eqIdx).trim();

  const protoIdx = cleaned.indexOf('.prototype.');
  if (protoIdx !== -1) {
    const memberof = cleaned.substring(0, protoIdx);
    const name = cleaned.substring(protoIdx + '.prototype.'.length);
    return {
      longname: memberof + '#' + name,
      memberof,
      name,
      scope: 'instance'
    };
  }

  // Static member or namespace: last dot separates memberof from name
  const lastDot = cleaned.lastIndexOf('.');
  if (lastDot === -1) {
    return { longname: cleaned, memberof: '', name: cleaned, scope: undefined };
  }

  return {
    longname: cleaned,
    memberof: cleaned.substring(0, lastDot),
    name: cleaned.substring(lastDot + 1),
    scope: 'static'
  };
}

/**
 * Determine the doclet `kind` from parsed tags.
 */
function determineKind(tags, hasParams, hasReturns) {
  for (const t of tags) {
    if (t.tag === 'constructor' || t.tag === 'class') return 'class';
    if (t.tag === 'namespace') return 'namespace';
    if (t.tag === 'typedef') return 'typedef';
    if (t.tag === 'enum') return 'enum';
  }
  if (hasParams || hasReturns) return 'function';
  return 'member';
}

/**
 * Parse a single .adoc file content into an array of doclet-like objects.
 * The output matches the shape expected by structurize.js.
 */
function parseAdocFile(content, filePath) {
  const doclets = [];
  const fileName = filePath ? filePath.replace(/\\/g, '/').split('/').pop() : undefined;

  // Find all /** ... */ blocks with their following identifier lines.
  // We use a regex that matches the comment block, then captures the next non-empty line.
  const blockRegex = /\/\*\*[\s\S]*?\*\//g;
  let match;

  while ((match = blockRegex.exec(content)) !== null) {
    const commentBlock = match[0];
    const afterIdx = match.index + commentBlock.length;

    // Find the identifier line after the comment (skip blank lines and // comments)
    let identLine = '';
    let lineno = 0;
    const rest = content.substring(afterIdx);
    const lines = rest.split('\n');
    // Count lines up to the comment start for lineno
    const beforeComment = content.substring(0, match.index);
    const commentLineNo = beforeComment.split('\n').length;

    for (let i = 0; i < lines.length; i++) {
      const trimmed = lines[i].trim();
      if (trimmed === '' || trimmed.startsWith('//')) continue;
      identLine = trimmed;
      lineno = commentLineNo + content.substring(match.index, afterIdx).split('\n').length + i;
      break;
    }

    // Parse the comment block with comment-parser
    const parsed = parseComment(commentBlock, { spacing: 'preserve' });
    if (!parsed || parsed.length === 0) continue;
    const block = parsed[0];
    const tags = block.tags || [];

    // If no identifier line, try to derive name from @name or @typedef tags
    let ident;
    if (identLine) {
      ident = parseIdentifier(identLine);
    }
    if (!ident) {
      const nameTag = tags.find(t => t.tag === 'name' && t.name);
      const typedefTag = tags.find(t => t.tag === 'typedef' && t.name);
      const tagName = nameTag?.name || typedefTag?.name;
      if (tagName) {
        const lastDot = tagName.lastIndexOf('.');
        ident = lastDot !== -1
          ? { longname: tagName, memberof: tagName.substring(0, lastDot), name: tagName.substring(lastDot + 1), scope: 'static' }
          : { longname: tagName, memberof: '', name: tagName, scope: undefined };
        lineno = commentLineNo;
      }
    }
    if (!ident) continue;

    // Extract tag data
    const paramTags = tags.filter(t => t.tag === 'param');
    const returnTags = tags.filter(t => t.tag === 'return' || t.tag === 'returns');
    const extendsTags = tags.filter(t => t.tag === 'extends');
    const typeTags = tags.filter(t => t.tag === 'type');
    const propertyTags = tags.filter(t => t.tag === 'property' || t.tag === 'prop');
    const accessTags = tags.filter(t => t.tag === 'access');
    const inheritDocTags = tags.filter(t => t.tag === 'inheritDoc' || t.tag === 'inheritdoc');
    const ignoreTags = tags.filter(t => t.tag === 'ignore' || t.tag === 'ignoreDoc' || t.tag === 'ignoredoc');
    const defineTags = tags.filter(t => t.tag === 'define' || t.tag === 'const');
    const enumTags = tags.filter(t => t.tag === 'enum');

    const hasParams = paramTags.length > 0;
    const hasReturns = returnTags.length > 0;
    const kind = determineKind(tags, hasParams, hasReturns);

    // Build params array
    const params = paramTags.map(t => {
      const { optional: typeOptional } = stripTypeModifiers(t.type);
      const typeNames = splitTypeNames(t.type);
      const isOptional = t.optional || typeOptional || (t.name && t.name.startsWith('opt_'));
      return {
        name: t.name || undefined,
        type: typeNames.length > 0 ? { names: typeNames } : undefined,
        optional: isOptional || undefined,
        description: t.description || undefined
      };
    });

    // Build returns array
    const returns = returnTags.map(t => {
      const typeNames = splitTypeNames(t.type);
      return {
        type: typeNames.length > 0 ? { names: typeNames } : undefined,
        description: t.description || undefined
      };
    });

    // Build properties array
    const properties = propertyTags.map(t => {
      const typeNames = splitTypeNames(t.type);
      return {
        name: t.name || undefined,
        type: typeNames.length > 0 ? { names: typeNames } : undefined,
        optional: t.optional || undefined,
        defaultvalue: t.default || undefined
      };
    });

    // For @enum tags, if enum has a type, extract its fields from properties
    // But enum fields in .adoc files are typically separate doclets with memberof = enum longname
    let isEnum = kind === 'enum' || enumTags.length > 0;

    // For @define/@const, store as tags for downstream detection
    const docletTags = [];
    if (ignoreTags.length > 0) {
      docletTags.push({ title: 'ignore', text: '' });
    }
    if (defineTags.length > 0) {
      docletTags.push({ title: 'define', text: defineTags[0].type || '' });
    }

    // Determine access
    let access = undefined;
    if (accessTags.length > 0) {
      access = accessTags[0].name || accessTags[0].description;
    }
    // Check for @private, @protected tags directly
    if (tags.some(t => t.tag === 'private')) access = 'private';
    if (tags.some(t => t.tag === 'protected')) access = 'protected';

    // Type from @type tag
    let type = undefined;
    if (typeTags.length > 0) {
      type = { names: splitTypeNames(typeTags[0].type) };
    }
    // For @typedef, the type comes from the typedef tag itself
    const typedefTags = tags.filter(t => t.tag === 'typedef');
    if (typedefTags.length > 0 && typedefTags[0].type && !type) {
      type = { names: splitTypeNames(typedefTags[0].type) };
    }
    // For @enum, the type comes from the enum tag itself
    if (enumTags.length > 0 && enumTags[0].type) {
      type = { names: splitTypeNames(enumTags[0].type) };
    }
    // For @define/@const, the type comes from the define tag
    if (defineTags.length > 0 && defineTags[0].type && !type) {
      type = { names: splitTypeNames(defineTags[0].type) };
    }

    // Build augments array from @extends
    const augments = extendsTags.map(t => {
      // @extends {Type} — type is in t.type or t.name
      return t.type || t.name || '';
    }).filter(Boolean);

    // Override name if @name tag is present
    let name = ident.name;
    let longname = ident.longname;
    let memberof = ident.memberof;
    const nameTags = tags.filter(t => t.tag === 'name');
    if (nameTags.length > 0 && nameTags[0].name) {
      // @name provides the full qualified name
      const explicitName = nameTags[0].name;
      const lastDot = explicitName.lastIndexOf('.');
      if (lastDot !== -1) {
        name = explicitName.substring(lastDot + 1);
        memberof = explicitName.substring(0, lastDot);
        longname = explicitName;
      } else {
        name = explicitName;
        longname = explicitName;
      }
    }

    const doclet = {
      kind,
      name,
      longname,
      memberof,
      scope: ident.scope,
      description: block.description || undefined,
      meta: { filename: fileName, lineno }
    };

    if (params.length > 0) doclet.params = params;
    if (returns.length > 0) doclet.returns = returns;
    if (augments.length > 0) doclet.augments = augments;
    if (type) doclet.type = type;
    if (properties.length > 0) doclet.properties = properties;
    if (access) doclet.access = access;
    if (isEnum) doclet.isEnum = true;
    if (inheritDocTags.length > 0) doclet.inheritdoc = '';
    if (docletTags.length > 0) doclet.tags = docletTags;

    doclets.push(doclet);
  }

  // Post-process: infer missing return types from sibling overloads.
  // When a setter method has @param but no @return, copy the return type
  // from another overload of the same method that does have @return.
  // This handles a common documentation pattern where @return is omitted on
  // some overloads (e.g. margin 4-arg setter missing @return on Radar.adoc).
  const methodGroups = new Map();
  for (const d of doclets) {
    if (d.kind === 'function' && d.memberof) {
      const key = d.memberof + '#' + d.name;
      if (!methodGroups.has(key)) methodGroups.set(key, []);
      methodGroups.get(key).push(d);
    }
  }
  for (const [, group] of methodGroups) {
    // Find a setter sibling (has both params AND returns) to copy return type from.
    // Prefer one that returns the class type (for method chaining).
    const setterSiblings = group.filter(d => d.params && d.params.length > 0 && d.returns && d.returns.length > 0);
    if (setterSiblings.length === 0) continue;
    const donor = setterSiblings[0];
    for (const d of group) {
      if (d.params && d.params.length > 0 && !d.returns) {
        d.returns = donor.returns;
      }
    }
  }

  return doclets;
}

export { parseAdocFile, splitTypeNames, stripTypeModifiers, parseIdentifier };
