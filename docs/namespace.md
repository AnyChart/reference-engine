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

# Exports (at the bottom)
```
	goog.exportSymbol('name', link);
	namespace['smth'] = link;
```