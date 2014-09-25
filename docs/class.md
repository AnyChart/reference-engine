# Class definition
```
/**
 * description
 * @constructor
 * @extends {classname}
 * @implements {classname}
 */
namespace.ClassName = function(params) {...
```
В целом совпадает с определением метода.
Sample:
```
/**
 * Special view to use as the terminating view with Ordinal scales.
 * @param {!anychart.data.IView} parentView Parent view. The last view is a mapping.
 * @param {string} fieldName Field name to make ordinal mask by.
 * @param {!Array} categories A set of categories to fit to.
 * @constructor
 * @extends {anychart.data.View}
 */
anychart.data.OrdinalView = function(parentView, fieldName, categories) {
  goog.base(this, parentView);
```
Parsing result:
```
    {
        "comment": "/**\n * Special view to use as the terminating view with Ordinal scales.\n * @param {!anychart.data.IView} parentView Parent view. The last view is a mapping.\n * @param {string} fieldName Field name to make ordinal mask by.\n * @param {!Array} categories A set of categories to fit to.\n * @constructor\n * @extends {anychart.data.View}\n */",
        "meta": {
            "range": [
                479,
                1086
            ],
            "filename": "OrdinalView.js",
            "lineno": 17,
            "path": "/Users/alex/Work/anychart/reference-engine/data/acdvf/repo/src/data",
            "code": {
                "id": "astnode100000026",
                "name": "anychart.data.OrdinalView",
                "type": "FunctionExpression",
                "value": "function",
                "paramnames": [
                    "parentView",
                    "fieldName",
                    "categories"
                ]
            },
            "vars": {
                "this.fieldName_": null,
                "this.categoriesMap_": null,
                "i": null
            }
        },
        "description": "Special view to use as the terminating view with Ordinal scales.",
        "params": [
            {
                "type": {
                    "names": [
                        "anychart.data.IView"
                    ]
                },
                "optional": null,
                "nullable": false,
                "variable": null,
                "defaultvalue": null,
                "description": "Parent view. The last view is a mapping.",
                "name": "parentView"
            },
            {
                "type": {
                    "names": [
                        "string"
                    ]
                },
                "optional": null,
                "nullable": null,
                "variable": null,
                "defaultvalue": null,
                "description": "Field name to make ordinal mask by.",
                "name": "fieldName"
            },
            {
                "type": {
                    "names": [
                        "Array"
                    ]
                },
                "optional": null,
                "nullable": false,
                "variable": null,
                "defaultvalue": null,
                "description": "A set of categories to fit to.",
                "name": "categories"
            }
        ],
        "kind": "class",
        "augments": [
            "anychart.data.View"
        ],
        "name": "OrdinalView",
        "longname": "anychart.data.OrdinalView",
        "memberof": "anychart.data",
        "scope": "static"
    },
```

# Class fields
Sample:
```
/**
 * Returns current fill.
 * @return {!acgraph.vector.Fill} Current fill settings (empty fill is always 'none').
 *//**
 * Sets fill settings using an object or a string.<br/>
 * Accepts:
 * <ul>
 * <li>{@link acgraph.vector.LinearGradientFill}</li>
 * <li>{@link acgraph.vector.RadialGradientFill}</li>
 * <li>{@link acgraph.vector.Fill}</li>
 * <li>{@link acgraph.vector.ImageFill}</li>
 * </ul>
 * or a color as a string, along with opacity, if needed, format is '<b>Color Opacity</b>',
 * e.g. 'red .5'.
 * @shortDescription Sets fill settings using an object or a string.
 * @example <c>Solid Fill</c><t>simple-h100</t>
 * var bg = anychart.elements.background();
 * // Set fill
 *   bg.fill('red 0.1');
 * // the same
 * // bg.fill('#ff0000 0.1');
 * // or
 * // bg.fill({color: 'red', opacity: 0.1});
 * // or
 * // bg.fill('#ff0000 0.1');
 * // then draw
 * bg.container(stage)
 *   .bounds( anychart.math.rect(10, 10, stage.width()-20, stage.height()-20) )
 *   .draw();
 * @example <c>Gradient Fill</c><t>simple-h100</t>
 * var bg = anychart.elements.background();
 * // Set fill
 *   bg.fill({keys:['red .1', 'orange'], mode: true, angle: 45});
 * bg.container(stage)
 *   .bounds( anychart.math.rect(10, 10, stage.width()-20, stage.height()-20) )
 *   .draw();
 * @example <c>Image Fill</c><t>simple-h100</t>
 * anychart.elements.background()
 *    .bounds( anychart.math.rect(10, 10, stage.width()-20, stage.height() - 20) )
 *    .stroke('#000 2').fill({
 *        src: 'http://static.anychart.com/rainbow.png',
 *        mode: acgraph.vector.ImageFillMode.TILE
 *     }).container(stage).draw();
 * @param {acgraph.vector.Fill} value ['#000 0.5'] Fill as an object or a string.
 * @return {!acgraph.vector.Shape} {@link acgraph.vector.Shape} instance for method chaining.
 * *//**
 * Fill as a color with opacity.<br/>
 * <b>Note:</b> If color is set as a string (e.g. 'red .5') it has a priority over opt_opacity, which
 * means: <b>fill</b> set like this <b>rect.fill('red 0.3', 0.7)</b> will have 0.3 opacity.
 * @shortDescription Fill as a string or an object.
 * @example <t>simple-h100</t>
 * var bg = anychart.elements.background();
 * // Set fill
 *   bg.fill('red', 0.1);
 * bg.container(stage)
 *   .bounds( anychart.math.rect(10, 10, stage.width()-20, stage.height()-20) )
 *   .draw();
 * @param {string} color Fill as a string.
 * @param {number=} opt_opacity Fill opacity.
 * @return {!anychart.elements.Background} {@link anychart.elements.Background} instance for method chaining.
 *//**
 * Linear gradient fill.<br/>
 * There are three modes:
 * <ul>
 *  <li>ObjectBoundingBox preserving an angle</li>
 *  <li>ObjectBoundingBox no angle preservation</li>
 *  <li>UserSpaceOnUse</li>
 * </ul>
 * <h4>Modes:</h4>
 * <p><b>ObjectBoundingBox preserving an angle</b><br/>
 * If boolean is passed it says how gradient behaves, specificaly
 * how gradient line angle behaves. If true - it is ObjectBoundingBox
 * with angle preservation. If angle is preserved, in any shape angle looks as one expects it to see.<br/>
 * <b>Note:</b> By default gradient vector for any shape, which sides are not in 1:1 proportions, will not
 * look as expected, because browser transforms this angle.</p>
 * <p><b>ObjectBoundingBox no angle preservation</b><br/>
 * If false is passed - that's ObjectBoundingBox no angle preservation. In this case default
 * behaviour comes up - gradient vector is calculated for a shape with 1:1 side proportions.</p>
 * <p><b>UserSpaceOnUse</b><br/>
 * If acgraph.math.Rect is passed - that'sUserSpaceOnUse mode.
 * In this mode gradient gets its own size and coordinates. Shapes with such gradient will be colored
 * only in those parts, which are covered by this custom gradient. Read more about this mode at
 * <a href='http://www.w3.org/TR/SVG/pservers.html#LinearGradientElementGradientUnitsAttribute'>
 * gradientUnits</a>. Angle is always preserved in this mode.</p>
 * @shortDescription Linear gradient fill.
 * @illustration <t>simple</t>
 * stage.text(0*stage.width()/6+3, 0, 'a');
 * anychart.elements.background()
 *   .fill(['0.2 black', 'white'], 45)
 *   .bounds( anychart.math.rect(0*stage.width()/6+3, 13, stage.width()/7-6, stage.height()-20) )
 *   .container(stage).draw();
 * stage.text(1*stage.width()/6-5, 0, 'b');
 * anychart.elements.background()
 *   .fill(['0.2 black', 'white'], 45, true)
 *   .bounds( anychart.math.rect(1*stage.width()/6-5, 13, stage.width()/7-6, stage.height()-20) )
 *   .container(stage).draw();
 * stage.text(2*stage.width()/6+3, 0, 'c');
 * anychart.elements.background()
 *   .fill(['red', 'blue'], 45, {left: 10, top: 20, width: 100, height: 100})
 *   .bounds( anychart.math.rect(2*stage.width()/6+3, 13, stage.width()/7-6, stage.height()-20) )
 *   .container(stage).draw();
 * anychart.elements.background()
 *   .fill(['red', 'blue'], 45, anychart.math.rect(10, 20, 100, 100))
 *   .bounds( anychart.math.rect(3*stage.width()/6-5, 13, stage.width()/7-6, stage.height()-20) )
 *   .container(stage).draw();
 * stage.text(4*stage.width()/6+3, 0, 'd');
 * anychart.elements.background()
 *   .fill(['red 0.1', 'orange', 'red 0.1'])
 *   .bounds( anychart.math.rect(4*stage.width()/6+3, 13, stage.width()/7-6, stage.height()-20) )
 *   .container(stage).draw();
 * anychart.elements.background()
 *   .fill(['red', {offset: 0.3, color: 'orange'}, 'red 0.1'])
 *   .bounds( anychart.math.rect(5*stage.width()/6-5, 13, stage.width()/7-6, stage.height()-20) )
 *   .container(stage).draw();
 * @illustrationDesc
 *  a) ObjectBoundingBox no angle preservation.<br/>
 *  b) ObjectBoundingBox preserving an angle.<br/>
 *  c) UserSpaceOnUse.<br/>
 *  d) Three step gradients.<br/>
 * @param {!Array.<(acgraph.vector.GradientKey|string)>} keys Gradient keys.
 * @param {number=} opt_angle Gradient angle.
 * @param {(boolean|!acgraph.vector.Rect|!{left:number,top:number,width:number,height:number})=} opt_mode Gradient mode.
 * @param {number=} opt_opacity Gradient opacity.
 * @return {!anychart.elements.Background} {@link anychart.elements.Background} instance for method chaining.
 *//**
 * Radial gradient fill.
 * @example <t>simple-h100</t>
 * var bg = anychart.elements.background();
 * // set fill
 *   bg.fill(['black', 'white'], .5, .5, null, .9, 0.3, 0.81)
 * bg.container(stage)
 *   .bounds( anychart.math.rect(10, 10, 90, 90) )
 *   .draw();
 * @param {!Array.<(acgraph.vector.GradientKey|string)>} keys Color-stop gradient keys.
 * @param {number} cx X ratio of center radial gradient.
 * @param {number} cy Y ratio of center radial gradient.
 * @param {acgraph.math.Rect=} opt_mode If defined then userSpaceOnUse mode, else objectBoundingBox.
 * @param {number=} opt_opacity Opacity of the gradient.
 * @param {number=} opt_fx X ratio of focal point.
 * @param {number=} opt_fy Y ratio of focal point.
 * @return {!anychart.elements.Background} {@link anychart.elements.Background} instance for method chaining.
 *//**
 * @ignoreDoc
 * @param {(!acgraph.vector.Fill|!Array.<(acgraph.vector.GradientKey|string)>|null)=} opt_fillOrColorOrKeys .
 * @param {number=} opt_opacityOrAngleOrCx .
 * @param {(number|boolean|!acgraph.math.Rect|!{left:number,top:number,width:number,height:number})=} opt_modeOrCy .
 * @param {(number|!acgraph.math.Rect|!{left:number,top:number,width:number,height:number}|null)=} opt_opacityOrMode .
 * @param {number=} opt_opacity .
 * @param {number=} opt_fx .
 * @param {number=} opt_fy .
 * @return {!(acgraph.vector.Fill|anychart.elements.Background)} .
 */
anychart.elements.Background.prototype.fill = function(opt_fillOrColorOrKeys, opt_opacityOrAngleOrCx, opt_modeOrCy, opt_opacityOrMode, opt_opacity, opt_fx, opt_fy) {
```
Parse results:
```
    {
        "comment": "/**\n * Returns current fill.\n * @return {!acgraph.vector.Fill} Current fill settings (empty fill is always 'none').\n ",
        "meta": {
            "range": [
                14778,
                15292
            ],
            "filename": "Background.js",
            "lineno": 356,
            "path": "/Users/alex/Work/anychart/reference-engine/data/acdvf/repo/src/elements",
            "code": {
                "id": "astnode100000402",
                "name": "anychart.elements.Background.prototype.fill",
                "type": "FunctionExpression",
                "value": "function",
                "paramnames": [
                    "opt_fillOrColorOrKeys",
                    "opt_opacityOrAngleOrCx",
                    "opt_modeOrCy",
                    "opt_opacityOrMode",
                    "opt_opacity",
                    "opt_fx",
                    "opt_fy"
                ]
            }
        },
        "description": "Returns current fill.",
        "returns": [
            {
                "type": {
                    "names": [
                        "acgraph.vector.Fill"
                    ]
                },
                "optional": null,
                "nullable": false,
                "variable": null,
                "defaultvalue": null,
                "description": "Current fill settings (empty fill is always 'none')."
            }
        ],
        "name": "fill",
        "longname": "anychart.elements.Background#fill",
        "kind": "function",
        "memberof": "anychart.elements.Background",
        "scope": "instance"
    },
    {
        "comment": "\n * Sets fill settings using an object or a string.<br/>\n * Accepts:\n * <ul>\n * <li>{@link acgraph.vector.LinearGradientFill}</li>\n * <li>{@link acgraph.vector.RadialGradientFill}</li>\n * <li>{@link acgraph.vector.Fill}</li>\n * <li>{@link acgraph.vector.ImageFill}</li>\n * </ul>\n * or a color as a string, along with opacity, if needed, format is '<b>Color Opacity</b>',\n * e.g. 'red .5'.\n * @shortDescription Sets fill settings using an object or a string.\n * @example <c>Solid Fill</c><t>simple-h100</t>\n * var bg = anychart.elements.background();\n * // Set fill\n *   bg.fill('red 0.1');\n * // the same\n * // bg.fill('#ff0000 0.1');\n * // or\n * // bg.fill({color: 'red', opacity: 0.1});\n * // or\n * // bg.fill('#ff0000 0.1');\n * // then draw\n * bg.container(stage)\n *   .bounds( anychart.math.rect(10, 10, stage.width()-20, stage.height()-20) )\n *   .draw();\n * @example <c>Gradient Fill</c><t>simple-h100</t>\n * var bg = anychart.elements.background();\n * // Set fill\n *   bg.fill({keys:['red .1', 'orange'], mode: true, angle: 45});\n * bg.container(stage)\n *   .bounds( anychart.math.rect(10, 10, stage.width()-20, stage.height()-20) )\n *   .draw();\n * @example <c>Image Fill</c><t>simple-h100</t>\n * anychart.elements.background()\n *    .bounds( anychart.math.rect(10, 10, stage.width()-20, stage.height() - 20) )\n *    .stroke('#000 2').fill({\n *        src: 'http://static.anychart.com/rainbow.png',\n *        mode: acgraph.vector.ImageFillMode.TILE\n *     }).container(stage).draw();\n * @param {acgraph.vector.Fill} value ['#000 0.5'] Fill as an object or a string.\n * @return {!acgraph.vector.Shape} {@link acgraph.vector.Shape} instance for method chaining.\n * ",
        "meta": {
            "range": [
                14778,
                15292
            ],
            "filename": "Background.js",
            "lineno": 356,
            "path": "/Users/alex/Work/anychart/reference-engine/data/acdvf/repo/src/elements",
            "code": {
                "id": "astnode100000402",
                "name": "anychart.elements.Background.prototype.fill",
                "type": "FunctionExpression",
                "value": "function",
                "paramnames": [
                    "opt_fillOrColorOrKeys",
                    "opt_opacityOrAngleOrCx",
                    "opt_modeOrCy",
                    "opt_opacityOrMode",
                    "opt_opacity",
                    "opt_fx",
                    "opt_fy"
                ]
            }
        },
        "description": "Sets fill settings using an object or a string.<br/>\nAccepts:\n<ul>\n<li>{@link acgraph.vector.LinearGradientFill}</li>\n<li>{@link acgraph.vector.RadialGradientFill}</li>\n<li>{@link acgraph.vector.Fill}</li>\n<li>{@link acgraph.vector.ImageFill}</li>\n</ul>\nor a color as a string, along with opacity, if needed, format is '<b>Color Opacity</b>',\ne.g. 'red .5'.",
        "tags": [
            {
                "originalTitle": "shortDescription",
                "title": "shortdescription",
                "text": "Sets fill settings using an object or a string.",
                "value": "Sets fill settings using an object or a string."
            }
        ],
        "examples": [
            "<c>Solid Fill</c><t>simple-h100</t>\nvar bg = anychart.elements.background();\n// Set fill\n bg.fill('red 0.1');\n// the same\n// bg.fill('#ff0000 0.1');\n// or\n// bg.fill({color: 'red', opacity: 0.1});\n// or\n// bg.fill('#ff0000 0.1');\n// then draw\nbg.container(stage)\n .bounds( anychart.math.rect(10, 10, stage.width()-20, stage.height()-20) )\n .draw();",
            "<c>Gradient Fill</c><t>simple-h100</t>\nvar bg = anychart.elements.background();\n// Set fill\n bg.fill({keys:['red .1', 'orange'], mode: true, angle: 45});\nbg.container(stage)\n .bounds( anychart.math.rect(10, 10, stage.width()-20, stage.height()-20) )\n .draw();",
            "<c>Image Fill</c><t>simple-h100</t>\nanychart.elements.background()\n  .bounds( anychart.math.rect(10, 10, stage.width()-20, stage.height() - 20) )\n  .stroke('#000 2').fill({\n      src: 'http://static.anychart.com/rainbow.png',\n      mode: acgraph.vector.ImageFillMode.TILE\n   }).container(stage).draw();"
        ],
        "params": [
            {
                "type": {
                    "names": [
                        "acgraph.vector.Fill"
                    ]
                },
                "optional": null,
                "nullable": null,
                "variable": null,
                "defaultvalue": null,
                "description": "['#000 0.5'] Fill as an object or a string.",
                "name": "value"
            }
        ],
        "returns": [
            {
                "type": {
                    "names": [
                        "acgraph.vector.Shape"
                    ]
                },
                "optional": null,
                "nullable": false,
                "variable": null,
                "defaultvalue": null,
                "description": "{@link acgraph.vector.Shape} instance for method chaining."
            }
        ],
        "name": "fill",
        "longname": "anychart.elements.Background#fill",
        "kind": "function",
        "memberof": "anychart.elements.Background",
        "scope": "instance"
    },
    {
        "comment": "\n * Fill as a color with opacity.<br/>\n * <b>Note:</b> If color is set as a string (e.g. 'red .5') it has a priority over opt_opacity, which\n * means: <b>fill</b> set like this <b>rect.fill('red 0.3', 0.7)</b> will have 0.3 opacity.\n * @shortDescription Fill as a string or an object.\n * @example <t>simple-h100</t>\n * var bg = anychart.elements.background();\n * // Set fill\n *   bg.fill('red', 0.1);\n * bg.container(stage)\n *   .bounds( anychart.math.rect(10, 10, stage.width()-20, stage.height()-20) )\n *   .draw();\n * @param {string} color Fill as a string.\n * @param {number=} opt_opacity Fill opacity.\n * @return {!anychart.elements.Background} {@link anychart.elements.Background} instance for method chaining.\n ",
        "meta": {
            "range": [
                14778,
                15292
            ],
            "filename": "Background.js",
            "lineno": 356,
            "path": "/Users/alex/Work/anychart/reference-engine/data/acdvf/repo/src/elements",
            "code": {
                "id": "astnode100000402",
                "name": "anychart.elements.Background.prototype.fill",
                "type": "FunctionExpression",
                "value": "function",
                "paramnames": [
                    "opt_fillOrColorOrKeys",
                    "opt_opacityOrAngleOrCx",
                    "opt_modeOrCy",
                    "opt_opacityOrMode",
                    "opt_opacity",
                    "opt_fx",
                    "opt_fy"
                ]
            }
        },
        "description": "Fill as a color with opacity.<br/>\n<b>Note:</b> If color is set as a string (e.g. 'red .5') it has a priority over opt_opacity, which\nmeans: <b>fill</b> set like this <b>rect.fill('red 0.3', 0.7)</b> will have 0.3 opacity.",
        "tags": [
            {
                "originalTitle": "shortDescription",
                "title": "shortdescription",
                "text": "Fill as a string or an object.",
                "value": "Fill as a string or an object."
            }
        ],
        "examples": [
            "<t>simple-h100</t>\nvar bg = anychart.elements.background();\n// Set fill\n bg.fill('red', 0.1);\nbg.container(stage)\n .bounds( anychart.math.rect(10, 10, stage.width()-20, stage.height()-20) )\n .draw();"
        ],
        "params": [
            {
                "type": {
                    "names": [
                        "string"
                    ]
                },
                "optional": null,
                "nullable": null,
                "variable": null,
                "defaultvalue": null,
                "description": "Fill as a string.",
                "name": "color"
            },
            {
                "type": {
                    "names": [
                        "number"
                    ]
                },
                "optional": true,
                "nullable": null,
                "variable": null,
                "defaultvalue": null,
                "description": "Fill opacity.",
                "name": "opt_opacity"
            }
        ],
        "returns": [
            {
                "type": {
                    "names": [
                        "anychart.elements.Background"
                    ]
                },
                "optional": null,
                "nullable": false,
                "variable": null,
                "defaultvalue": null,
                "description": "{@link anychart.elements.Background} instance for method chaining."
            }
        ],
        "name": "fill",
        "longname": "anychart.elements.Background#fill",
        "kind": "function",
        "memberof": "anychart.elements.Background",
        "scope": "instance"
    },
    {
        "comment": "\n * Linear gradient fill.<br/>\n * There are three modes:\n * <ul>\n *  <li>ObjectBoundingBox preserving an angle</li>\n *  <li>ObjectBoundingBox no angle preservation</li>\n *  <li>UserSpaceOnUse</li>\n * </ul>\n * <h4>Modes:</h4>\n * <p><b>ObjectBoundingBox preserving an angle</b><br/>\n * If boolean is passed it says how gradient behaves, specificaly\n * how gradient line angle behaves. If true - it is ObjectBoundingBox\n * with angle preservation. If angle is preserved, in any shape angle looks as one expects it to see.<br/>\n * <b>Note:</b> By default gradient vector for any shape, which sides are not in 1:1 proportions, will not\n * look as expected, because browser transforms this angle.</p>\n * <p><b>ObjectBoundingBox no angle preservation</b><br/>\n * If false is passed - that's ObjectBoundingBox no angle preservation. In this case default\n * behaviour comes up - gradient vector is calculated for a shape with 1:1 side proportions.</p>\n * <p><b>UserSpaceOnUse</b><br/>\n * If acgraph.math.Rect is passed - that'sUserSpaceOnUse mode.\n * In this mode gradient gets its own size and coordinates. Shapes with such gradient will be colored\n * only in those parts, which are covered by this custom gradient. Read more about this mode at\n * <a href='http://www.w3.org/TR/SVG/pservers.html#LinearGradientElementGradientUnitsAttribute'>\n * gradientUnits</a>. Angle is always preserved in this mode.</p>\n * @shortDescription Linear gradient fill.\n * @illustration <t>simple</t>\n * stage.text(0*stage.width()/6+3, 0, 'a');\n * anychart.elements.background()\n *   .fill(['0.2 black', 'white'], 45)\n *   .bounds( anychart.math.rect(0*stage.width()/6+3, 13, stage.width()/7-6, stage.height()-20) )\n *   .container(stage).draw();\n * stage.text(1*stage.width()/6-5, 0, 'b');\n * anychart.elements.background()\n *   .fill(['0.2 black', 'white'], 45, true)\n *   .bounds( anychart.math.rect(1*stage.width()/6-5, 13, stage.width()/7-6, stage.height()-20) )\n *   .container(stage).draw();\n * stage.text(2*stage.width()/6+3, 0, 'c');\n * anychart.elements.background()\n *   .fill(['red', 'blue'], 45, {left: 10, top: 20, width: 100, height: 100})\n *   .bounds( anychart.math.rect(2*stage.width()/6+3, 13, stage.width()/7-6, stage.height()-20) )\n *   .container(stage).draw();\n * anychart.elements.background()\n *   .fill(['red', 'blue'], 45, anychart.math.rect(10, 20, 100, 100))\n *   .bounds( anychart.math.rect(3*stage.width()/6-5, 13, stage.width()/7-6, stage.height()-20) )\n *   .container(stage).draw();\n * stage.text(4*stage.width()/6+3, 0, 'd');\n * anychart.elements.background()\n *   .fill(['red 0.1', 'orange', 'red 0.1'])\n *   .bounds( anychart.math.rect(4*stage.width()/6+3, 13, stage.width()/7-6, stage.height()-20) )\n *   .container(stage).draw();\n * anychart.elements.background()\n *   .fill(['red', {offset: 0.3, color: 'orange'}, 'red 0.1'])\n *   .bounds( anychart.math.rect(5*stage.width()/6-5, 13, stage.width()/7-6, stage.height()-20) )\n *   .container(stage).draw();\n * @illustrationDesc\n *  a) ObjectBoundingBox no angle preservation.<br/>\n *  b) ObjectBoundingBox preserving an angle.<br/>\n *  c) UserSpaceOnUse.<br/>\n *  d) Three step gradients.<br/>\n * @param {!Array.<(acgraph.vector.GradientKey|string)>} keys Gradient keys.\n * @param {number=} opt_angle Gradient angle.\n * @param {(boolean|!acgraph.vector.Rect|!{left:number,top:number,width:number,height:number})=} opt_mode Gradient mode.\n * @param {number=} opt_opacity Gradient opacity.\n * @return {!anychart.elements.Background} {@link anychart.elements.Background} instance for method chaining.\n ",
        "meta": {
            "range": [
                14778,
                15292
            ],
            "filename": "Background.js",
            "lineno": 356,
            "path": "/Users/alex/Work/anychart/reference-engine/data/acdvf/repo/src/elements",
            "code": {
                "id": "astnode100000402",
                "name": "anychart.elements.Background.prototype.fill",
                "type": "FunctionExpression",
                "value": "function",
                "paramnames": [
                    "opt_fillOrColorOrKeys",
                    "opt_opacityOrAngleOrCx",
                    "opt_modeOrCy",
                    "opt_opacityOrMode",
                    "opt_opacity",
                    "opt_fx",
                    "opt_fy"
                ]
            }
        },
        "description": "Linear gradient fill.<br/>\nThere are three modes:\n<ul>\n <li>ObjectBoundingBox preserving an angle</li>\n <li>ObjectBoundingBox no angle preservation</li>\n <li>UserSpaceOnUse</li>\n</ul>\n<h4>Modes:</h4>\n<p><b>ObjectBoundingBox preserving an angle</b><br/>\nIf boolean is passed it says how gradient behaves, specificaly\nhow gradient line angle behaves. If true - it is ObjectBoundingBox\nwith angle preservation. If angle is preserved, in any shape angle looks as one expects it to see.<br/>\n<b>Note:</b> By default gradient vector for any shape, which sides are not in 1:1 proportions, will not\nlook as expected, because browser transforms this angle.</p>\n<p><b>ObjectBoundingBox no angle preservation</b><br/>\nIf false is passed - that's ObjectBoundingBox no angle preservation. In this case default\nbehaviour comes up - gradient vector is calculated for a shape with 1:1 side proportions.</p>\n<p><b>UserSpaceOnUse</b><br/>\nIf acgraph.math.Rect is passed - that'sUserSpaceOnUse mode.\nIn this mode gradient gets its own size and coordinates. Shapes with such gradient will be colored\nonly in those parts, which are covered by this custom gradient. Read more about this mode at\n<a href='http://www.w3.org/TR/SVG/pservers.html#LinearGradientElementGradientUnitsAttribute'>\ngradientUnits</a>. Angle is always preserved in this mode.</p>",
        "tags": [
            {
                "originalTitle": "shortDescription",
                "title": "shortdescription",
                "text": "Linear gradient fill.",
                "value": "Linear gradient fill."
            },
            {
                "originalTitle": "illustration",
                "title": "illustration",
                "text": "<t>simple</t>\nstage.text(0*stage.width()/6+3, 0, 'a');\nanychart.elements.background()\n  .fill(['0.2 black', 'white'], 45)\n  .bounds( anychart.math.rect(0*stage.width()/6+3, 13, stage.width()/7-6, stage.height()-20) )\n  .container(stage).draw();\nstage.text(1*stage.width()/6-5, 0, 'b');\nanychart.elements.background()\n  .fill(['0.2 black', 'white'], 45, true)\n  .bounds( anychart.math.rect(1*stage.width()/6-5, 13, stage.width()/7-6, stage.height()-20) )\n  .container(stage).draw();\nstage.text(2*stage.width()/6+3, 0, 'c');\nanychart.elements.background()\n  .fill(['red', 'blue'], 45, {left: 10, top: 20, width: 100, height: 100})\n  .bounds( anychart.math.rect(2*stage.width()/6+3, 13, stage.width()/7-6, stage.height()-20) )\n  .container(stage).draw();\nanychart.elements.background()\n  .fill(['red', 'blue'], 45, anychart.math.rect(10, 20, 100, 100))\n  .bounds( anychart.math.rect(3*stage.width()/6-5, 13, stage.width()/7-6, stage.height()-20) )\n  .container(stage).draw();\nstage.text(4*stage.width()/6+3, 0, 'd');\nanychart.elements.background()\n  .fill(['red 0.1', 'orange', 'red 0.1'])\n  .bounds( anychart.math.rect(4*stage.width()/6+3, 13, stage.width()/7-6, stage.height()-20) )\n  .container(stage).draw();\nanychart.elements.background()\n  .fill(['red', {offset: 0.3, color: 'orange'}, 'red 0.1'])\n  .bounds( anychart.math.rect(5*stage.width()/6-5, 13, stage.width()/7-6, stage.height()-20) )\n  .container(stage).draw();",
                "value": "<t>simple</t>\nstage.text(0*stage.width()/6+3, 0, 'a');\nanychart.elements.background()\n  .fill(['0.2 black', 'white'], 45)\n  .bounds( anychart.math.rect(0*stage.width()/6+3, 13, stage.width()/7-6, stage.height()-20) )\n  .container(stage).draw();\nstage.text(1*stage.width()/6-5, 0, 'b');\nanychart.elements.background()\n  .fill(['0.2 black', 'white'], 45, true)\n  .bounds( anychart.math.rect(1*stage.width()/6-5, 13, stage.width()/7-6, stage.height()-20) )\n  .container(stage).draw();\nstage.text(2*stage.width()/6+3, 0, 'c');\nanychart.elements.background()\n  .fill(['red', 'blue'], 45, {left: 10, top: 20, width: 100, height: 100})\n  .bounds( anychart.math.rect(2*stage.width()/6+3, 13, stage.width()/7-6, stage.height()-20) )\n  .container(stage).draw();\nanychart.elements.background()\n  .fill(['red', 'blue'], 45, anychart.math.rect(10, 20, 100, 100))\n  .bounds( anychart.math.rect(3*stage.width()/6-5, 13, stage.width()/7-6, stage.height()-20) )\n  .container(stage).draw();\nstage.text(4*stage.width()/6+3, 0, 'd');\nanychart.elements.background()\n  .fill(['red 0.1', 'orange', 'red 0.1'])\n  .bounds( anychart.math.rect(4*stage.width()/6+3, 13, stage.width()/7-6, stage.height()-20) )\n  .container(stage).draw();\nanychart.elements.background()\n  .fill(['red', {offset: 0.3, color: 'orange'}, 'red 0.1'])\n  .bounds( anychart.math.rect(5*stage.width()/6-5, 13, stage.width()/7-6, stage.height()-20) )\n  .container(stage).draw();"
            },
            {
                "originalTitle": "illustrationDesc",
                "title": "illustrationdesc",
                "text": "a) ObjectBoundingBox no angle preservation.<br/>\n b) ObjectBoundingBox preserving an angle.<br/>\n c) UserSpaceOnUse.<br/>\n d) Three step gradients.<br/>",
                "value": "a) ObjectBoundingBox no angle preservation.<br/>\n b) ObjectBoundingBox preserving an angle.<br/>\n c) UserSpaceOnUse.<br/>\n d) Three step gradients.<br/>"
            }
        ],
        "params": [
            {
                "type": {
                    "names": [
                        "Array.<acgraph.vector.GradientKey,string>"
                    ]
                },
                "optional": null,
                "nullable": false,
                "variable": null,
                "defaultvalue": null,
                "description": "Gradient keys.",
                "name": "keys"
            },
            {
                "type": {
                    "names": [
                        "number"
                    ]
                },
                "optional": true,
                "nullable": null,
                "variable": null,
                "defaultvalue": null,
                "description": "Gradient angle.",
                "name": "opt_angle"
            },
            {
                "type": {
                    "names": [
                        "boolean",
                        "acgraph.vector.Rect",
                        "Object"
                    ]
                },
                "optional": true,
                "nullable": null,
                "variable": null,
                "defaultvalue": null,
                "description": "Gradient mode.",
                "name": "opt_mode"
            },
            {
                "type": {
                    "names": [
                        "number"
                    ]
                },
                "optional": true,
                "nullable": null,
                "variable": null,
                "defaultvalue": null,
                "description": "Gradient opacity.",
                "name": "opt_opacity"
            }
        ],
        "returns": [
            {
                "type": {
                    "names": [
                        "anychart.elements.Background"
                    ]
                },
                "optional": null,
                "nullable": false,
                "variable": null,
                "defaultvalue": null,
                "description": "{@link anychart.elements.Background} instance for method chaining."
            }
        ],
        "name": "fill",
        "longname": "anychart.elements.Background#fill",
        "kind": "function",
        "memberof": "anychart.elements.Background",
        "scope": "instance"
    },
    {
        "comment": "\n * Radial gradient fill.\n * @example <t>simple-h100</t>\n * var bg = anychart.elements.background();\n * // set fill\n *   bg.fill(['black', 'white'], .5, .5, null, .9, 0.3, 0.81)\n * bg.container(stage)\n *   .bounds( anychart.math.rect(10, 10, 90, 90) )\n *   .draw();\n * @param {!Array.<(acgraph.vector.GradientKey|string)>} keys Color-stop gradient keys.\n * @param {number} cx X ratio of center radial gradient.\n * @param {number} cy Y ratio of center radial gradient.\n * @param {acgraph.math.Rect=} opt_mode If defined then userSpaceOnUse mode, else objectBoundingBox.\n * @param {number=} opt_opacity Opacity of the gradient.\n * @param {number=} opt_fx X ratio of focal point.\n * @param {number=} opt_fy Y ratio of focal point.\n * @return {!anychart.elements.Background} {@link anychart.elements.Background} instance for method chaining.\n ",
        "meta": {
            "range": [
                14778,
                15292
            ],
            "filename": "Background.js",
            "lineno": 356,
            "path": "/Users/alex/Work/anychart/reference-engine/data/acdvf/repo/src/elements",
            "code": {
                "id": "astnode100000402",
                "name": "anychart.elements.Background.prototype.fill",
                "type": "FunctionExpression",
                "value": "function",
                "paramnames": [
                    "opt_fillOrColorOrKeys",
                    "opt_opacityOrAngleOrCx",
                    "opt_modeOrCy",
                    "opt_opacityOrMode",
                    "opt_opacity",
                    "opt_fx",
                    "opt_fy"
                ]
            }
        },
        "description": "Radial gradient fill.",
        "examples": [
            "<t>simple-h100</t>\nvar bg = anychart.elements.background();\n// set fill\n bg.fill(['black', 'white'], .5, .5, null, .9, 0.3, 0.81)\nbg.container(stage)\n .bounds( anychart.math.rect(10, 10, 90, 90) )\n .draw();"
        ],
        "params": [
            {
                "type": {
                    "names": [
                        "Array.<acgraph.vector.GradientKey,string>"
                    ]
                },
                "optional": null,
                "nullable": false,
                "variable": null,
                "defaultvalue": null,
                "description": "Color-stop gradient keys.",
                "name": "keys"
            },
            {
                "type": {
                    "names": [
                        "number"
                    ]
                },
                "optional": null,
                "nullable": null,
                "variable": null,
                "defaultvalue": null,
                "description": "X ratio of center radial gradient.",
                "name": "cx"
            },
            {
                "type": {
                    "names": [
                        "number"
                    ]
                },
                "optional": null,
                "nullable": null,
                "variable": null,
                "defaultvalue": null,
                "description": "Y ratio of center radial gradient.",
                "name": "cy"
            },
            {
                "type": {
                    "names": [
                        "acgraph.math.Rect"
                    ]
                },
                "optional": true,
                "nullable": null,
                "variable": null,
                "defaultvalue": null,
                "description": "If defined then userSpaceOnUse mode, else objectBoundingBox.",
                "name": "opt_mode"
            },
            {
                "type": {
                    "names": [
                        "number"
                    ]
                },
                "optional": true,
                "nullable": null,
                "variable": null,
                "defaultvalue": null,
                "description": "Opacity of the gradient.",
                "name": "opt_opacity"
            },
            {
                "type": {
                    "names": [
                        "number"
                    ]
                },
                "optional": true,
                "nullable": null,
                "variable": null,
                "defaultvalue": null,
                "description": "X ratio of focal point.",
                "name": "opt_fx"
            },
            {
                "type": {
                    "names": [
                        "number"
                    ]
                },
                "optional": true,
                "nullable": null,
                "variable": null,
                "defaultvalue": null,
                "description": "Y ratio of focal point.",
                "name": "opt_fy"
            }
        ],
        "returns": [
            {
                "type": {
                    "names": [
                        "anychart.elements.Background"
                    ]
                },
                "optional": null,
                "nullable": false,
                "variable": null,
                "defaultvalue": null,
                "description": "{@link anychart.elements.Background} instance for method chaining."
            }
        ],
        "name": "fill",
        "longname": "anychart.elements.Background#fill",
        "kind": "function",
        "memberof": "anychart.elements.Background",
        "scope": "instance"
    },
    {
        "comment": "\n * @ignoreDoc\n * @param {(!acgraph.vector.Fill|!Array.<(acgraph.vector.GradientKey|string)>|null)=} opt_fillOrColorOrKeys .\n * @param {number=} opt_opacityOrAngleOrCx .\n * @param {(number|boolean|!acgraph.math.Rect|!{left:number,top:number,width:number,height:number})=} opt_modeOrCy .\n * @param {(number|!acgraph.math.Rect|!{left:number,top:number,width:number,height:number}|null)=} opt_opacityOrMode .\n * @param {number=} opt_opacity .\n * @param {number=} opt_fx .\n * @param {number=} opt_fy .\n * @return {!(acgraph.vector.Fill|anychart.elements.Background)} .\n */",
        "meta": {
            "range": [
                14778,
                15292
            ],
            "filename": "Background.js",
            "lineno": 356,
            "path": "/Users/alex/Work/anychart/reference-engine/data/acdvf/repo/src/elements",
            "code": {
                "id": "astnode100000402",
                "name": "anychart.elements.Background.prototype.fill",
                "type": "FunctionExpression",
                "value": "function",
                "paramnames": [
                    "opt_fillOrColorOrKeys",
                    "opt_opacityOrAngleOrCx",
                    "opt_modeOrCy",
                    "opt_opacityOrMode",
                    "opt_opacity",
                    "opt_fx",
                    "opt_fy"
                ]
            },
            "vars": {
                "val": null,
                "this.fill_": null
            }
        },
        "tags": [
            {
                "originalTitle": "ignoreDoc",
                "title": "ignoredoc",
                "text": ""
            }
        ],
        "params": [
            {
                "type": {
                    "names": [
                        "acgraph.vector.Fill",
                        "!Array.<(acgraph.vector.GradientKey|string)>",
                        "null"
                    ]
                },
                "optional": true,
                "nullable": null,
                "variable": null,
                "defaultvalue": null,
                "description": ".",
                "name": "opt_fillOrColorOrKeys"
            },
            {
                "type": {
                    "names": [
                        "number"
                    ]
                },
                "optional": true,
                "nullable": null,
                "variable": null,
                "defaultvalue": null,
                "description": ".",
                "name": "opt_opacityOrAngleOrCx"
            },
            {
                "type": {
                    "names": [
                        "number",
                        "boolean",
                        "acgraph.math.Rect",
                        "Object"
                    ]
                },
                "optional": true,
                "nullable": null,
                "variable": null,
                "defaultvalue": null,
                "description": ".",
                "name": "opt_modeOrCy"
            },
            {
                "type": {
                    "names": [
                        "number",
                        "acgraph.math.Rect",
                        "Object",
                        "null"
                    ]
                },
                "optional": true,
                "nullable": null,
                "variable": null,
                "defaultvalue": null,
                "description": ".",
                "name": "opt_opacityOrMode"
            },
            {
                "type": {
                    "names": [
                        "number"
                    ]
                },
                "optional": true,
                "nullable": null,
                "variable": null,
                "defaultvalue": null,
                "description": ".",
                "name": "opt_opacity"
            },
            {
                "type": {
                    "names": [
                        "number"
                    ]
                },
                "optional": true,
                "nullable": null,
                "variable": null,
                "defaultvalue": null,
                "description": ".",
                "name": "opt_fx"
            },
            {
                "type": {
                    "names": [
                        "number"
                    ]
                },
                "optional": true,
                "nullable": null,
                "variable": null,
                "defaultvalue": null,
                "description": ".",
                "name": "opt_fy"
            }
        ],
        "returns": [
            {
                "type": {
                    "names": [
                        "acgraph.vector.Fill",
                        "anychart.elements.Background"
                    ]
                },
                "optional": null,
                "nullable": false,
                "variable": null,
                "defaultvalue": null,
                "description": "."
            }
        ],
        "name": "fill",
        "longname": "anychart.elements.Background#fill",
        "kind": "function",
        "memberof": "anychart.elements.Background",
        "scope": "instance"
    },
    {
        "comment": "",
        "meta": {
            "range": [
                14995,
                15052
            ],
            "filename": "Background.js",
            "lineno": 358,
            "path": "/Users/alex/Work/anychart/reference-engine/data/acdvf/repo/src/elements",
            "code": {
                "id": "astnode100000429",
                "name": "val",
                "type": "CallExpression"
            }
        },
        "undocumented": true,
        "name": "val",
        "longname": "anychart.elements.Background#fill~val",
        "kind": "member",
        "memberof": "anychart.elements.Background#fill",
        "scope": "inner"
    },
    {
        "comment": "",
        "meta": {
            "range": [
                15111,
                15127
            ],
            "filename": "Background.js",
            "lineno": 360,
            "path": "/Users/alex/Work/anychart/reference-engine/data/acdvf/repo/src/elements",
            "code": {
                "id": "astnode100000455",
                "name": "this.fill_",
                "type": "Identifier",
                "value": "val",
                "paramnames": []
            }
        },
        "undocumented": true,
        "name": "fill_",
        "longname": "anychart.elements.Background#fill_",
        "kind": "member",
        "memberof": "anychart.elements.Background",
        "scope": "instance"
    },
```

# Method
Sample:
```
```

# Exports
Все проверяется в exports!