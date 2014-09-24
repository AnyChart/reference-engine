# Parsing rules
Проверяем, есть ли в эксопртах что-то из этого namespace, и если есть - оставляем.

# Definition
```
	/**
	 * description
	 * @namespace
	 * @name namespace-name
	 */
	 no code
```
Sample:
```
/**
 * Core space for all anychart components.
 * @namespace
 * @name anychart
 */
```
Parsing result:
```
    {
        "comment": "/**\n * Core space for all anychart components.\n * @namespace\n * @name anychart\n */",
        "description": "Core space for all anychart components.",
        "kind": "namespace",
        "name": "anychart",
        "longname": "anychart"
    }
```

# Constants
```
	/**
	 * description
	 * @define {type} short description
	 */
	 namespace.constant = value;
```
Sample:
```
/**
 * Current version of the framework, replaced on compile time.
 * @define {string} Current version of the framework.
 */
anychart.VERSION = '';
```
Parsing result:
```
    {
        "comment": "/**\n * Current version of the framework, replaced on compile time.\n * @define {string} Current version of the framework.\n */",
        "description": "Current version of the framework, replaced on compile time.",
        "tags": [
            {
                "originalTitle": "define",
                "title": "define",
                "text": "{string} Current version of the framework.",
                "value": "{string} Current version of the framework."
            }
        ],
        "name": "VERSION",
        "longname": "anychart.VERSION",
        "kind": "member",
        "memberof": "anychart",
        "scope": "static"
    }
```

# Properties
В коде вроде как не наблюдается
```
	/**
	 * description
	 * @type {type}
	 */
	 namespace.property = default_value;
```
Sample:
```
/**
 * If the globalLock is locked.
 * @type {number}
 */
anychart.globalLock.locked = 0;
```
Parsing result:
```
    {
        "comment": "/**\n * If the globalLock is locked.\n * @type {number}\n */",
        "meta": {
            "range": [
                1281,
                1311
            ],
            "filename": "anychart.js",
            "lineno": 52,
            "path": "/Users/alex/Work/anychart/reference-engine/data/acdvf/repo/src",
            "code": {
                "id": "astnode100000064",
                "name": "anychart.globalLock.locked",
                "type": "Literal",
                "value": "0",
                "paramnames": []
            }
        },
        "description": "If the globalLock is locked.",
        "type": {
            "names": [
                "number"
            ]
        },
        "optional": null,
        "nullable": null,
        "variable": null,
        "defaultvalue": null,
        "name": "locked",
        "longname": "anychart.globalLock.locked",
        "kind": "member",
        "memberof": "anychart.globalLock",
        "scope": "static"
    }
```

# Methods
```
	/**
	 * description
	 * @param {type} name description
	 * @return {type} description
	 */
	 namespace.method = function(...)
```
Sample:
```
/**
 * Please be watchful and careful with this method.
 * Callback is invoked prior to full page LOAD, which means you
 * have no access to CSS and other elemnents outside page head and async loaded elements
 *
 * Add callback for document load event.
 * @param {Function} func Function which will called on document load event.
 * @param {*=} opt_scope Function call context.
 */
anychart.onDocumentReady = function(func, opt_scope) {
```
Parsing result:
```
    {
        "comment": "/**\n * Please be watchful and careful with this method.\n * Callback is invoked prior to full page LOAD, which means you\n * have no access to CSS and other elemnents outside page head and async loaded elements\n *\n * Add callback for document load event.\n * @param {Function} func Function which will called on document load event.\n * @param {*=} opt_scope Function call context.\n */",
        "description": "Please be watchful and careful with this method.\nCallback is invoked prior to full page LOAD, which means you\nhave no access to CSS and other elemnents outside page head and async loaded elements\n\nAdd callback for document load event.",
        "params": [
            {
                "type": {
                    "names": [
                        "function"
                    ]
                },
                "optional": null,
                "nullable": null,
                "variable": null,
                "defaultvalue": null,
                "description": "Function which will called on document load event.",
                "name": "func"
            },
            {
                "type": {
                    "names": [
                        "*"
                    ]
                },
                "optional": true,
                "nullable": null,
                "variable": null,
                "defaultvalue": null,
                "description": "Function call context.",
                "name": "opt_scope"
            }
        ],
        "name": "onDocumentReady",
        "longname": "anychart.onDocumentReady",
        "kind": "function",
        "memberof": "anychart",
        "scope": "static"
    }
```

# Typedefs
```
/**
 * Description
 * @typedef {base-type} name
 * @property {type} name description
 * ...
 *//**
 @ignoreDoc
 @typedef {{
   field_name: type
 }}
 */
namespace.name;
```
Sample:
```
/**
 Text segment.
 @includeDoc
 @typedef {Object} acgraph.vector.TextSegmentStyle
 @property {(string|undefined)} fontStyle Font style. More at {@link acgraph.vector.Text.FontStyle}.
 @property {(string|undefined)} fontVariant Font variant. More at {@link acgraph.vector.Text.FontVariant}.
 @property {(string|undefined)} fontFamily Font family. {@link http://www.w3schools.com/cssref/pr_font_font-family.asp}.
 @property {(string|number|undefined)} fontSize Font size.
 @property {(number|string|undefined)} fontWeight Font weight. {@link http://www.w3schools.com/cssref/pr_font_weight.asp}.
 @property {(string|undefined)} letterSpacing Letter spacing.
 @property {(string|undefined)} decoration Decoration. More at {@link acgraph.vector.Text.Decoration}.
 @property {(string|goog.color.names|undefined)} color Color. {@link http://www.w3schools.com/html/html_colors.asp}.
 @property {(number|undefined)} opacity Color opacity (0 to 1).
 *//**
 @ignoreDoc
 @typedef {{
    fontStyle: (string|undefined),
    fontVariant: (string|undefined),
    fontFamily: (string|undefined),
    fontSize: (string|number|undefined),
    fontWeight: (number|string|undefined),
    color: (string|goog.color.names|undefined),
    letterSpacing: (string|undefined),
    decoration: (string|undefined),
    opacity: (number|undefined)
 }}
 */
acgraph.vector.TextSegmentStyle;
```
Parsing result:
```
    {
        "comment": "/**\n Text segment.\n @includeDoc\n @typedef {Object} acgraph.vector.TextSegmentStyle\n @property {(string|undefined)} fontStyle Font style. More at {@link acgraph.vector.Text.FontStyle}.\n @property {(string|undefined)} fontVariant Font variant. More at {@link acgraph.vector.Text.FontVariant}.\n @property {(string|undefined)} fontFamily Font family. {@link http://www.w3schools.com/cssref/pr_font_font-family.asp}.\n @property {(string|number|undefined)} fontSize Font size.\n @property {(number|string|undefined)} fontWeight Font weight. {@link http://www.w3schools.com/cssref/pr_font_weight.asp}.\n @property {(string|undefined)} letterSpacing Letter spacing.\n @property {(string|undefined)} decoration Decoration. More at {@link acgraph.vector.Text.Decoration}.\n @property {(string|goog.color.names|undefined)} color Color. {@link http://www.w3schools.com/html/html_colors.asp}.\n @property {(number|undefined)} opacity Color opacity (0 to 1).\n @also\n @ignoreDoc\n @typedef {{\n    fontStyle: (string|undefined),\n    fontVariant: (string|undefined),\n    fontFamily: (string|undefined),\n    fontSize: (string|number|undefined),\n    fontWeight: (number|string|undefined),\n    color: (string|goog.color.names|undefined),\n    letterSpacing: (string|undefined),\n    decoration: (string|undefined),\n    opacity: (number|undefined)\n }}\n */",
        "description": "Text segment.",
        "tags": [
            {
                "originalTitle": "includeDoc",
                "title": "includedoc",
                "text": ""
            },
            {
                "originalTitle": "ignoreDoc",
                "title": "ignoredoc",
                "text": ""
            }
        ],
        "kind": "typedef",
        "name": "TextSegmentStyle",
        "type": {
            "names": [
                "Object"
            ]
        },
        "optional": null,
        "nullable": null,
        "variable": null,
        "defaultvalue": null,
        "properties": [
            {
                "type": {
                    "names": [
                        "string",
                        "undefined"
                    ]
                },
                "optional": null,
                "nullable": null,
                "variable": null,
                "defaultvalue": null,
                "description": "Font style. More at {@link acgraph.vector.Text.FontStyle}.",
                "name": "fontStyle"
            },
            {
                "type": {
                    "names": [
                        "string",
                        "undefined"
                    ]
                },
                "optional": null,
                "nullable": null,
                "variable": null,
                "defaultvalue": null,
                "description": "Font variant. More at {@link acgraph.vector.Text.FontVariant}.",
                "name": "fontVariant"
            },
            {
                "type": {
                    "names": [
                        "string",
                        "undefined"
                    ]
                },
                "optional": null,
                "nullable": null,
                "variable": null,
                "defaultvalue": null,
                "description": "Font family. {@link http://www.w3schools.com/cssref/pr_font_font-family.asp}.",
                "name": "fontFamily"
            },
            {
                "type": {
                    "names": [
                        "string",
                        "number",
                        "undefined"
                    ]
                },
                "optional": null,
                "nullable": null,
                "variable": null,
                "defaultvalue": null,
                "description": "Font size.",
                "name": "fontSize"
            },
            {
                "type": {
                    "names": [
                        "number",
                        "string",
                        "undefined"
                    ]
                },
                "optional": null,
                "nullable": null,
                "variable": null,
                "defaultvalue": null,
                "description": "Font weight. {@link http://www.w3schools.com/cssref/pr_font_weight.asp}.",
                "name": "fontWeight"
            },
            {
                "type": {
                    "names": [
                        "string",
                        "undefined"
                    ]
                },
                "optional": null,
                "nullable": null,
                "variable": null,
                "defaultvalue": null,
                "description": "Letter spacing.",
                "name": "letterSpacing"
            },
            {
                "type": {
                    "names": [
                        "string",
                        "undefined"
                    ]
                },
                "optional": null,
                "nullable": null,
                "variable": null,
                "defaultvalue": null,
                "description": "Decoration. More at {@link acgraph.vector.Text.Decoration}.",
                "name": "decoration"
            },
            {
                "type": {
                    "names": [
                        "string",
                        "goog.color.names",
                        "undefined"
                    ]
                },
                "optional": null,
                "nullable": null,
                "variable": null,
                "defaultvalue": null,
                "description": "Color. {@link http://www.w3schools.com/html/html_colors.asp}.",
                "name": "color"
            },
            {
                "type": {
                    "names": [
                        "number",
                        "undefined"
                    ]
                },
                "optional": null,
                "nullable": null,
                "variable": null,
                "defaultvalue": null,
                "description": "Color opacity (0 to 1).",
                "name": "opacity"
            }
        ],
        "memberof": "acgraph.vector",
        "longname": "acgraph.vector.TextSegmentStyle",
        "scope": "static"
    },
    {
        "comment": "",
        "undocumented": true,
        "name": "TextSegmentStyle",
        "longname": "acgraph.vector.TextSegmentStyle",
        "kind": "member",
        "memberof": "acgraph.vector",
        "scope": "static"
    },
    {
        "comment": "\n @ignoreDoc\n @typedef {{\n    fontStyle: (string|undefined),\n    fontVariant: (string|undefined),\n    fontFamily: (string|undefined),\n    fontSize: (string|number|undefined),\n    fontWeight: (number|string|undefined),\n    color: (string|goog.color.names|undefined),\n    letterSpacing: (string|undefined),\n    decoration: (string|undefined),\n    opacity: (number|undefined)\n }}\n */",
        "tags": [
            {
                "originalTitle": "ignoreDoc",
                "title": "ignoredoc",
                "text": ""
            }
        ],
        "kind": "typedef",
        "type": {
            "names": [
                "Object"
            ]
        },
        "optional": null,
        "nullable": null,
        "variable": null,
        "defaultvalue": null,
        "name": "TextSegmentStyle",
        "longname": "acgraph.vector.TextSegmentStyle",
        "memberof": "acgraph.vector",
        "scope": "static"
    }
```
Надо игнорировать 2 последних записи при парсинге.

# Exports (at the bottom)
```
	goog.exportSymbol('name', link);
	namespace['smth'] = link;
```