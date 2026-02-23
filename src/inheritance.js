function isAncestor(ancestorName, childName, classMap) {
  if (ancestorName === childName) return true;
  const cl = classMap.get(childName);
  if (!cl || !cl.extends) return false;
  return cl.extends.some(ext => isAncestor(ancestorName, ext, classMap));
}

function updateCovariance(method, childFullName, classMap) {
  if (method.overrides) {
    method.overrides.forEach(o => updateCovariance(o, childFullName, classMap));
  }
  if (method.returns) {
    method.returns = method.returns.map(r => {
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
}

const depthCache = new Map();
function getInheritanceDepth(cl, classes, classMap) {
  const cached = depthCache.get(cl.fullName);
  if (cached !== undefined) return cached;
  if (!cl.extends || cl.extends.length === 0) { depthCache.set(cl.fullName, 0); return 0; }
  const parent = classMap ? classMap.get(cl.extends[0]) : classes.find(c => c.fullName === cl.extends[0]);
  if (!parent) { depthCache.set(cl.fullName, 1); return 1; }
  const depth = 1 + getInheritanceDepth(parent, classes, classMap);
  depthCache.set(cl.fullName, depth);
  return depth;
}

function hasMethodContent(method) {
  const hasParams = Array.isArray(method.params) && method.params.length > 0;
  const hasReturns = Array.isArray(method.returns) && method.returns.length > 0;
  return hasParams || hasReturns;
}

function isUnresolvedInheritDocGroup(method) {
  const overloads = Array.isArray(method.overrides) ? method.overrides : [method];
  if (overloads.length === 0) return false;
  return overloads.every(o => o.isInheritDoc && !hasMethodContent(o));
}

const ALWAYS_INHERIT_METHODS = new Set([
  'listen',
  'listenOnce',
  'unlisten',
  'unlistenByKey',
  'removeAllListeners',
  'container',
  'bounds',
  'parentBounds',
  'print',
  'enabled',
  'width',
  'height',
  'maxHeight',
  'minHeight',
  'maxWidth',
  'minWidth',
  'left',
  'right',
  'top',
  'bottom',
  'zIndex',
  'id',
  'getPixelBounds',
  'dispose',
  'colorScale',
  'selectionMode',
  'getStat',
  'a11y',
  'getPoint',
  'maxPointWidth',
  'minPointLength',
  'seriesType',
  'rendering',
  'markers',
  'xMode',
  'isVertical',
  'pointWidth',
  'getIndex',
  'excludePoint',
  'includePoint',
  'includeAllPoints',
  'keepOnlyPoints',
  'getExcludedPoints',
  'disablePointerEvents',
  'fontFamily',
  'selectable',
  'textIndent',
  'fontDecoration',
  'fontStyle',
  'fontVariant',
  'letterSpacing',
  'lineHeight',
  'textDirection',
  'textOverflow',
  'wordBreak',
  'wordWrap',
  'fontOpacity',
  'textSettings',
  'vAlign',
  'useHtml',
  'fontSize',
  'textShadow',
  'fontWeight',
  'tooltip',
  'maxLabels',
  'minLabels',
  'fontColor',
  'hAlign',
  'animation',
  'position',
  'meta',
  'name',
  'scale',
  'background',
  'clip',
  'interactivity',
  'overlapMode',
  'padding',
  'rotation',
  'stroke',
  'getType',
  'color',
  'allowEdit',
  'select',
  'anchor',
  'error',
  'fill',
  'getSelectedPoints',
  'offsetX',
  'offsetY',
  'getChart',
  'getPlot',
  'hoverGap',
  'xScale',
  'orientation',
  'getRemainingBounds',
  'isHorizontal',
  'getPixelPointWidth',
  'inMarquee',
  'maxFontSize',
  'data',
  'legendItem',
  'minorLabels',
  'ticks',
  'value',
  'minFontSize',
  'adjustFontSize',
  'allowPointsSelect',
  'selectRectangleMarqueeFill',
  'selectRectangleMarqueeStroke',
  'xPointPosition',
  'drawFirstLabel',
  'drawLastLabel',
  'minorTicks',
  'align',
  'axis',
  'endMarker',
  'offset',
  'startMarker',
  'text',
  'transformX',
  'transformY',
  'staggerLines',
  'staggerMaxLines',
  'staggerMode',
  'title',
  'allowRangeChange',
  'autoHide',
  'columnStroke',
  'drawLastLine',
  'edit',
  'horizontalScrollBar',
  'labels',
  'normal',
  'hovered',
  'selected',
  'yScale',
  'lineMarker',
  'leftStroke',
  'isRadial',
  'hoverMode',
  'inverted',
  'holidaysFill',
  'header',
  'hatchFill',
  'groupingTasks',
  'getShapesGroup',
  'getLabelsCount',
  'getLabel',
  'getGauge',
  'format',
  'elements',
  'cropLabels',
  'cornerType',
  'corners',
  'contextMenu',
  'connectorStroke',
  'connectors',
  'bottomStroke',
  'baselines',
  'baselineMilestones',
  'backgroundFill',
  'allowConnectorCaps',
  'alignMinimum',
  'alignMaximum',
  'thumbs',
  'selectedFill',
  'scaleRangeMode',
  'rowSelectedFill',
  'rowOddFill',
  'rowHoverFill',
  'rowFill',
  'rowEvenFill',
  'positionFormatter',
  'outlineStroke',
  'palette',
  'maxLength',
  'margin',
  'legend',
  'layout',
  'allowPointSettings',
  'isMinor',
  'dataIndex',
  'horizontalOffset',
  'hover',
  'hoverCursor',
  'mapAs',
  'reset',
  'unhover',
  'unselect',
  'getMapping',
  'hilo',
  'preview',
  'saveAsSvg',
  'startIndex',
  'style',
  'ticksCount',
  'titleFormat',
  'toSvg',
  'types',
  'y',
  'getCurrentPosition',
  'getCol',
  'getCell',
  'get',
  'fullScreen',
  'fixedColumns',
  'evenFill',
  'endIndex',
  'drawTopLine',
  'drawRightLine',
  'drawOverEnd',
  'drawLeftLine',
  'drawFirstLine',
  'drawBottomLine',
  'draw',
  'drag',
  'direction',
  'descriptions',
  'decoration',
  'createComputer',
  'contents',
  'connectMissingPoints',
  'column',
  'colsWidth',
  'colsMinWidth',
  'colsMaxWidth',
  'colsCount',
  'cellPadding',
  'cellFill',
  'cellBorder',
  'cancelMarquee',
  'borderRadius',
  'border',
  'allowMultiSeriesSelection',
  'advance',
  'zoomOnMouseWheel',
  'workingFill',
  'unselectOnClickOutOfPoint',
  'weekendsFill',
  'topStroke',
  'verticalScrollBar',
  'type',
  'textMarker',
  'spotRadius',
  'multiSelectOnClick',
  'tasks',
  'rightStroke',
  'size',
  'rangeMarker',
  'periods',
  'notWorkingFill',
  'milestones',
  'exports',
  'geoIdField',
  'measureWithText',
  'names',
  'htmlText',
  'iconSize',
  'iconTextSpacing',
  'itemsHAlign',
  'itemsSourceMode',
  'itemsSpacing',
  'length',
  'path',
  'positionMode',
  'rowsCount',
  'rowsHeight',
  'getJpgBase64String',
  'getPdfBase64String',
  'oddFill',
  'from',
  'getPngBase64String',
  'getSvgBase64String',
  'images',
  'items',
  'inverseTransform',
  'shareWithFacebook',
  'transformXY',
  'itemsFormat',
  'itemsFormatter',
  'isFullScreenAvailable',
  'shareWithLinkedIn',
  'itemsLayout',
  'marker',
  'shareWithPinterest',
  'shareWithTwitter',
  'onEditEnd',
  'onEditStart',
]);

const STYLE_GETTER_BACKFILL_METHODS = new Set([
  'fontFamily',
  'fontDecoration',
  'fontStyle',
  'fontVariant',
  'fontOpacity',
  'letterSpacing',
  'lineHeight',
  'textDirection',
  'textIndent',
  'textOverflow',
  'wordBreak',
  'wordWrap',
  'disablePointerEvents',
  'selectable'
]);

const CONTEXT_FORCE_INHERIT_METHODS_BY_CLASS = new Map([
  ['anychart.charts.Gantt', new Set([
    'startSelectRectangleMarquee',
  ])],
  ['anychart.charts.Pareto', new Set([
    'localToGlobal',
  ])],
  ['anychart.charts.Polar', new Set([
    'shareAsJpg',
    'shareAsPdf',
    'shareAsPng',
    'shareAsSvg',
    'toA11yTable',
    'toHtmlTable',
  ])],
  ['anychart.charts.Sankey', new Set([
    'startSelectRectangleMarquee',
  ])],
  ['anychart.charts.Sparkline', new Set([
    'startSelectRectangleMarquee',
  ])],
  ['anychart.charts.Stock', new Set([
    'noData',
  ])],
  ['anychart.charts.TagCloud', new Set([
    'startSelectRectangleMarquee',
  ])],
  ['anychart.charts.TreeMap', new Set([
    'saveAsCsv',
    'saveAsJson',
    'saveAsXlsx',
    'saveAsXml',
  ])],
  ['anychart.charts.Venn', new Set([
    'startSelectRectangleMarquee',
  ])],
  ['anychart.core.SeparateChart', new Set([
    'startSelectRectangleMarquee',
  ])],
  ['anychart.core.axes.LinearGauge', new Set([
    'valueTarget',
  ])],
  ['anychart.core.ui.ColorRange', new Set([
    'valueTarget',
  ])],
  ['anychart.core.utils.DrawingPlanIterator', new Set([
    'getRowsCount',
  ])],
  ['anychart.graphics.vector.vml.Text', new Set([
    'getTextHeight',
    'getTextWidth',
    'opacity',
    'x',
  ])],
  ['anychart.scales.LinearColor', new Set([
    'transform',
  ])],
  ['anychart.standalones.ColorRange', new Set([
    'valueTarget',
  ])],
  ['anychart.standalones.DataGrid', new Set([
    'getVisibleItems',
    'verticalOffset',
  ])],
  ['anychart.standalones.Legend', new Set([
    'paginator',
    'titleSeparator',
  ])],
  ['anychart.standalones.ResourceList', new Set([
    'overlay',
    'tags',
  ])],
  ['anychart.standalones.Table', new Set([
    'getRow',
    'rowsMaxHeight',
    'rowsMinHeight',
    'saveAsJpg',
    'saveAsPdf',
    'saveAsPng',
    'shareAsJpg',
    'shareAsPdf',
    'shareAsPng',
    'shareAsSvg',
  ])],
  ['anychart.standalones.axes.Linear', new Set([
    'valueTarget',
  ])],
  ['anychart.standalones.axisMarkers.Range', new Set([
    'to',
  ])],
]);

function shouldForceContextInheritance(className, methodName) {
  const methods = CONTEXT_FORCE_INHERIT_METHODS_BY_CLASS.get(className);
  return !!(methods && methods.has(methodName));
}

function getOverloads(method) {
  if (Array.isArray(method.overrides) && method.overrides.length > 0) {
    return method.overrides;
  }
  return [method];
}

function methodSignature(method) {
  const params = (method.params || [])
    .map(p => `${p.name || ''}:${(p.types || []).join(',')}:${p.optional ? 'opt' : 'req'}`)
    .join('|');
  const returns = (method.returns || [])
    .map(r => (r.types || []).join(','))
    .join('|');
  return `${method.name}|${params}|${returns}`;
}

function ensureOverridesGroup(method) {
  if (Array.isArray(method.overrides) && method.overrides.length > 0) {
    return method.overrides;
  }
  const base = JSON.parse(JSON.stringify(method));
  delete base.overrides;
  method.overrides = [base];
  return method.overrides;
}

function backfillStyleGetterOverloads(cl, parent, classMap, childAllMembers, shouldFilterByAllMembers) {
  if (!cl.hasInheritDocMethods) return;

  const parentMethodsByName = new Map();
  for (const pm of parent.methods || []) {
    if (pm && pm.name) parentMethodsByName.set(pm.name, pm);
  }

  for (const childMethod of cl.methods) {
    const methodName = childMethod && childMethod.name;
    if (!methodName || !STYLE_GETTER_BACKFILL_METHODS.has(methodName)) continue;
    if (shouldFilterByAllMembers && !childAllMembers.has(methodName)) continue;

    const parentMethod = parentMethodsByName.get(methodName);
    if (!parentMethod) continue;

    const childOverloads = ensureOverridesGroup(childMethod);
    const parentOverloads = getOverloads(parentMethod);
    const childSignatures = new Set(childOverloads.map(methodSignature));
    const childHasSetterLike = childOverloads.some(o => Array.isArray(o.params) && o.params.length > 0);
    if (!childHasSetterLike) continue;

    for (const parentOverload of parentOverloads) {
      const hasZeroParams = !Array.isArray(parentOverload.params) || parentOverload.params.length === 0;
      const hasReturns = Array.isArray(parentOverload.returns) && parentOverload.returns.length > 0;
      if (!hasZeroParams || !hasReturns) continue;

      const sig = methodSignature(parentOverload);
      if (childSignatures.has(sig)) continue;

      const clone = JSON.parse(JSON.stringify(parentOverload));
      delete clone.overrides;
      updateCovariance(clone, cl.fullName, classMap);
      clone.isDirect = false;
      childOverloads.push(clone);
      childSignatures.add(sig);
    }
  }
}

export function resolveInheritance(topLevel) {
  const classMap = new Map();
  topLevel.classes.forEach(cl => classMap.set(cl.fullName, cl));

  // Sort classes by inheritance depth so parents are processed before children
  depthCache.clear();
  const sortedClasses = topLevel.classes.concat().sort((a, b) => {
    return getInheritanceDepth(a, topLevel.classes, classMap) - getInheritanceDepth(b, topLevel.classes, classMap);
  });

  sortedClasses.forEach(cl => {
    // 1. Update covariance for self methods
    cl.methods.forEach(m => updateCovariance(m, cl.fullName, classMap));

    // 2. Inherit methods from parent
    if (cl.extends && cl.extends.length > 0) {
      const parentName = cl.extends[0];
      const parent = classMap.get(parentName);
      if (parent) {
        // Prune explicitly identical methods to force re-inheritance at end
        cl.methods = cl.methods.filter(m => {
          const pm = parent.methods.find(p => p.name === m.name);
          if (!pm) return true;

          const mStr = JSON.stringify(m, (key, val) => {
            if (['isDirect', 'ancestorDistance', 'isMain', 'isResolved', 'isIgnored', 'isInheritDoc', 'originalIndex', 'meta', 'memberof', 'longname', 'undocumented'].includes(key)) return undefined;
            return val;
          });
          const pmStr = JSON.stringify(pm, (key, val) => {
            if (['isDirect', 'ancestorDistance', 'isMain', 'isResolved', 'isIgnored', 'isInheritDoc', 'originalIndex', 'meta', 'memberof', 'longname', 'undocumented'].includes(key)) return undefined;
            return val;
          });

          if (mStr === pmStr) return false;
          return true;
        });

        const childMethodNames = new Set(
          cl.methods
            .filter(m => {
              if (!m.isIgnored) return true;
              return getOverloads(m).some(hasMethodContent);
            })
            .filter(m => !(
              isUnresolvedInheritDocGroup(m) &&
              (
                ALWAYS_INHERIT_METHODS.has(m.name) ||
                shouldForceContextInheritance(cl.fullName, m.name)
              )
            ))
            .map(m => m.name)
        );
        const childAllMembers = cl.allMemberNames instanceof Set
          ? cl.allMemberNames
          : new Set(cl.allMemberNames || []);
        const shouldFilterByAllMembers = !!cl.hasInheritDocMethods;

        // Filter parent methods that are NOT overridden in child by name
        const inherited = parent.methods
          .filter(pm => !childMethodNames.has(pm.name))
          .filter(pm =>
            !shouldFilterByAllMembers ||
            childAllMembers.has(pm.name) ||
            ALWAYS_INHERIT_METHODS.has(pm.name) ||
            shouldForceContextInheritance(cl.fullName, pm.name)
          )
          .map(pm => {
            // Clone and update covariance for the new context
            const clone = JSON.parse(JSON.stringify(pm));
            updateCovariance(clone, cl.fullName, classMap);
            clone.isDirect = false;
            return clone;
          });

        // Append inherited methods to the end
        cl.methods = cl.methods.concat(inherited);

        // Targeted backfill for known missing style getter overloads.
        backfillStyleGetterOverloads(
          cl,
          parent,
          classMap,
          childAllMembers,
          shouldFilterByAllMembers
        );
      }
    }

    // 3. Two-zone sort (H4): own methods alphabetically, inherited methods in parent order.
    // Zone 1: methods documented in this class (allMemberNames) — sorted alphabetically (ASCII)
    // Zone 2: inherited methods not in allMemberNames — preserve parent order (as appended)
    const amn = cl.allMemberNames instanceof Set
      ? cl.allMemberNames
      : new Set(cl.allMemberNames || []);
    const ownMethods = cl.methods.filter(m => amn.has(m.name));
    const inheritedMethods = cl.methods.filter(m => !amn.has(m.name));
    ownMethods.sort((a, b) => a.name < b.name ? -1 : a.name > b.name ? 1 : 0);
    cl.methods = [...ownMethods, ...inheritedMethods];
  });

  return topLevel;
}
