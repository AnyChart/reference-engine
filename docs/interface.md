# Definition
```
/**
 * description
 * @interface
 * @extends {type}
 */
namespace.name = function() {};
```
Может наследовать другие интерфейсы, соответственно @inheritDoc работает.

Sample
```
/**
 * A common part between anychart.data.Set and anychart.data.View.
 * @interface
 * @extends {goog.events.Listenable}
 */
anychart.data.IView = function() {
};
```
Parsing result:
```
    {
        "comment": "/**\n * A common part between anychart.data.Set and anychart.data.View.\n * @interface\n * @extends {goog.events.Listenable}\n */",
        "meta": {
            "range": [
                166,
                202
            ],
            "filename": "IView.js",
            "lineno": 10,
            "path": "/Users/alex/Work/anychart/reference-engine/data/acdvf/repo/src/data",
            "code": {
                "id": "astnode100000008",
                "name": "anychart.data.IView",
                "type": "FunctionExpression",
                "value": "function",
                "paramnames": []
            }
        },
        "description": "A common part between anychart.data.Set and anychart.data.View.",
        "tags": [
            {
                "originalTitle": "interface",
                "title": "interface",
                "text": ""
            }
        ],
        "augments": [
            "goog.events.Listenable"
        ],
        "name": "IView",
        "longname": "anychart.data.IView",
        "kind": "function",
        "memberof": "anychart.data",
        "scope": "static"
    }
```

# Methods
Sample:
```
/**
 * Returns the size of the data set (number of rows).
 * @return {number} Number of rows in the set.
 */
anychart.data.IView.prototype.getRowsCount = function() {};
```
Parsing result:
```
    {
        "comment": "/**\n * Returns the size of the data set (number of rows).\n * @return {number} Number of rows in the set.\n */",
        "meta": {
            "range": [
                995,
                1053
            ],
            "filename": "IView.js",
            "lineno": 32,
            "path": "/Users/alex/Work/anychart/reference-engine/data/acdvf/repo/src/data",
            "code": {
                "id": "astnode100000032",
                "name": "anychart.data.IView.prototype.getRowsCount",
                "type": "FunctionExpression",
                "value": "function",
                "paramnames": []
            }
        },
        "description": "Returns the size of the data set (number of rows).",
        "returns": [
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
                "description": "Number of rows in the set."
            }
        ],
        "name": "getRowsCount",
        "longname": "anychart.data.IView#getRowsCount",
        "kind": "function",
        "memberof": "anychart.data.IView",
        "scope": "instance"
    }
```