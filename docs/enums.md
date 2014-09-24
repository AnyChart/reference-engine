# Parsing rules
Экспортим все enum-ы

# Basic definition
```
	/**
	 * description
	 * @enum {values_type}
	 */
	 namespace.EnumName = {
	   KEY: VALUE
	 };
```

# Link to another enum
```
/**
 * Anchor enum. Defines 9 items.
 * @illustration <t>simple</t>
 * var orange = '1 orange 1';
 * var star = stage.star5(stage.width()/2, stage.height()/3, stage.height()/4).fill('yellow', 0.5);
 * var pathBounds = star.getBounds();
 * stage.path().fill('none').stroke(orange)
 *     .moveTo(pathBounds.left, pathBounds.top)
 *     .lineTo(pathBounds.left + pathBounds.width, pathBounds.top)
 *     .lineTo(pathBounds.left + pathBounds.width, pathBounds.top + pathBounds.height)
 *     .lineTo(pathBounds.left, pathBounds.top + pathBounds.height)
 *     .close();
 * stage.text(pathBounds.left - 55, pathBounds.top - 15, 'LEFT_TOP');
 * stage.circle(pathBounds.left, pathBounds.top, 3).fill('blue');
 * stage.text(pathBounds.left - 78, pathBounds.top + pathBounds.height/2 - 8, 'LEFT_CENTER');
 * stage.circle(pathBounds.left, pathBounds.top + pathBounds.height/2, 3).fill('blue');
 * stage.text(pathBounds.left - 80, pathBounds.top + pathBounds.height, 'LEFT_BOTTOM');
 * stage.circle(pathBounds.left, pathBounds.top + pathBounds.height, 3).fill('blue');
 * stage.text(pathBounds.left  + pathBounds.width/2 - 10, pathBounds.top - 18, 'CENTER_TOP');
 * stage.circle(pathBounds.left + pathBounds.width/2, pathBounds.top, 3).fill('blue');
 * stage.text(pathBounds.left + pathBounds.width/2 - 20, pathBounds.top + pathBounds.height/2 - 15, 'CENTER');
 * stage.circle(pathBounds.left + pathBounds.width/2, pathBounds.top + pathBounds.height/2, 3).fill('blue');
 * stage.text(pathBounds.left + pathBounds.width/2 - 23, pathBounds.top + pathBounds.height+ 2, 'CENTER_BOTTOM');
 * stage.circle(pathBounds.left + pathBounds.width/2, pathBounds.top + pathBounds.height, 3).fill('blue');
 * stage.text(pathBounds.left + pathBounds.width + 5, pathBounds.top - 15, 'RIGHT_TOP');
 * stage.circle(pathBounds.left + pathBounds.width, pathBounds.top, 3).fill('blue');
 * stage.text(pathBounds.left + pathBounds.width + 5 , pathBounds.top + pathBounds.height/2 - 8, 'RIGHT_CENTER');
 * stage.circle(pathBounds.left + pathBounds.width, pathBounds.top + pathBounds.height/2, 3).fill('blue');
 * stage.text(pathBounds.left + pathBounds.width + 5, pathBounds.top + pathBounds.height, 'RIGHT_BOTTOM');
 * stage.circle(pathBounds.left + pathBounds.width, pathBounds.top + pathBounds.height, 3).fill('blue');
 * @enum {string}
 */
anychart.enums.Anchor = acgraph.vector.Anchor;
```
Parsing result:
```
{
        "comment": "/**\n * Anchor enum. Defines 9 items.\n * @illustration <t>simple</t>\n * var orange = '1 orange 1';\n * var star = stage.star5(stage.width()/2, stage.height()/3, stage.height()/4).fill('yellow', 0.5);\n * var pathBounds = star.getBounds();\n * stage.path().fill('none').stroke(orange)\n *     .moveTo(pathBounds.left, pathBounds.top)\n *     .lineTo(pathBounds.left + pathBounds.width, pathBounds.top)\n *     .lineTo(pathBounds.left + pathBounds.width, pathBounds.top + pathBounds.height)\n *     .lineTo(pathBounds.left, pathBounds.top + pathBounds.height)\n *     .close();\n * stage.text(pathBounds.left - 55, pathBounds.top - 15, 'LEFT_TOP');\n * stage.circle(pathBounds.left, pathBounds.top, 3).fill('blue');\n * stage.text(pathBounds.left - 78, pathBounds.top + pathBounds.height/2 - 8, 'LEFT_CENTER');\n * stage.circle(pathBounds.left, pathBounds.top + pathBounds.height/2, 3).fill('blue');\n * stage.text(pathBounds.left - 80, pathBounds.top + pathBounds.height, 'LEFT_BOTTOM');\n * stage.circle(pathBounds.left, pathBounds.top + pathBounds.height, 3).fill('blue');\n * stage.text(pathBounds.left  + pathBounds.width/2 - 10, pathBounds.top - 18, 'CENTER_TOP');\n * stage.circle(pathBounds.left + pathBounds.width/2, pathBounds.top, 3).fill('blue');\n * stage.text(pathBounds.left + pathBounds.width/2 - 20, pathBounds.top + pathBounds.height/2 - 15, 'CENTER');\n * stage.circle(pathBounds.left + pathBounds.width/2, pathBounds.top + pathBounds.height/2, 3).fill('blue');\n * stage.text(pathBounds.left + pathBounds.width/2 - 23, pathBounds.top + pathBounds.height+ 2, 'CENTER_BOTTOM');\n * stage.circle(pathBounds.left + pathBounds.width/2, pathBounds.top + pathBounds.height, 3).fill('blue');\n * stage.text(pathBounds.left + pathBounds.width + 5, pathBounds.top - 15, 'RIGHT_TOP');\n * stage.circle(pathBounds.left + pathBounds.width, pathBounds.top, 3).fill('blue');\n * stage.text(pathBounds.left + pathBounds.width + 5 , pathBounds.top + pathBounds.height/2 - 8, 'RIGHT_CENTER');\n * stage.circle(pathBounds.left + pathBounds.width, pathBounds.top + pathBounds.height/2, 3).fill('blue');\n * stage.text(pathBounds.left + pathBounds.width + 5, pathBounds.top + pathBounds.height, 'RIGHT_BOTTOM');\n * stage.circle(pathBounds.left + pathBounds.width, pathBounds.top + pathBounds.height, 3).fill('blue');\n * @enum {string}\n */",
        "meta": {
            "range": [
                2672,
                2717
            ],
            "filename": "enums.js",
            "lineno": 48,
            "path": "/Users/alex/Work/anychart/reference-engine/data/acdvf/repo/src",
            "code": {
                "id": "astnode100000014",
                "name": "anychart.enums.Anchor",
                "type": "MemberExpression",
                "value": "acgraph.vector.Anchor",
                "paramnames": []
            }
        },
        "description": "Anchor enum. Defines 9 items.",
        "tags": [
            {
                "originalTitle": "illustration",
                "title": "illustration",
                "text": "<t>simple</t>\nvar orange = '1 orange 1';\nvar star = stage.star5(stage.width()/2, stage.height()/3, stage.height()/4).fill('yellow', 0.5);\nvar pathBounds = star.getBounds();\nstage.path().fill('none').stroke(orange)\n    .moveTo(pathBounds.left, pathBounds.top)\n    .lineTo(pathBounds.left + pathBounds.width, pathBounds.top)\n    .lineTo(pathBounds.left + pathBounds.width, pathBounds.top + pathBounds.height)\n    .lineTo(pathBounds.left, pathBounds.top + pathBounds.height)\n    .close();\nstage.text(pathBounds.left - 55, pathBounds.top - 15, 'LEFT_TOP');\nstage.circle(pathBounds.left, pathBounds.top, 3).fill('blue');\nstage.text(pathBounds.left - 78, pathBounds.top + pathBounds.height/2 - 8, 'LEFT_CENTER');\nstage.circle(pathBounds.left, pathBounds.top + pathBounds.height/2, 3).fill('blue');\nstage.text(pathBounds.left - 80, pathBounds.top + pathBounds.height, 'LEFT_BOTTOM');\nstage.circle(pathBounds.left, pathBounds.top + pathBounds.height, 3).fill('blue');\nstage.text(pathBounds.left  + pathBounds.width/2 - 10, pathBounds.top - 18, 'CENTER_TOP');\nstage.circle(pathBounds.left + pathBounds.width/2, pathBounds.top, 3).fill('blue');\nstage.text(pathBounds.left + pathBounds.width/2 - 20, pathBounds.top + pathBounds.height/2 - 15, 'CENTER');\nstage.circle(pathBounds.left + pathBounds.width/2, pathBounds.top + pathBounds.height/2, 3).fill('blue');\nstage.text(pathBounds.left + pathBounds.width/2 - 23, pathBounds.top + pathBounds.height+ 2, 'CENTER_BOTTOM');\nstage.circle(pathBounds.left + pathBounds.width/2, pathBounds.top + pathBounds.height, 3).fill('blue');\nstage.text(pathBounds.left + pathBounds.width + 5, pathBounds.top - 15, 'RIGHT_TOP');\nstage.circle(pathBounds.left + pathBounds.width, pathBounds.top, 3).fill('blue');\nstage.text(pathBounds.left + pathBounds.width + 5 , pathBounds.top + pathBounds.height/2 - 8, 'RIGHT_CENTER');\nstage.circle(pathBounds.left + pathBounds.width, pathBounds.top + pathBounds.height/2, 3).fill('blue');\nstage.text(pathBounds.left + pathBounds.width + 5, pathBounds.top + pathBounds.height, 'RIGHT_BOTTOM');\nstage.circle(pathBounds.left + pathBounds.width, pathBounds.top + pathBounds.height, 3).fill('blue');",
                "value": "<t>simple</t>\nvar orange = '1 orange 1';\nvar star = stage.star5(stage.width()/2, stage.height()/3, stage.height()/4).fill('yellow', 0.5);\nvar pathBounds = star.getBounds();\nstage.path().fill('none').stroke(orange)\n    .moveTo(pathBounds.left, pathBounds.top)\n    .lineTo(pathBounds.left + pathBounds.width, pathBounds.top)\n    .lineTo(pathBounds.left + pathBounds.width, pathBounds.top + pathBounds.height)\n    .lineTo(pathBounds.left, pathBounds.top + pathBounds.height)\n    .close();\nstage.text(pathBounds.left - 55, pathBounds.top - 15, 'LEFT_TOP');\nstage.circle(pathBounds.left, pathBounds.top, 3).fill('blue');\nstage.text(pathBounds.left - 78, pathBounds.top + pathBounds.height/2 - 8, 'LEFT_CENTER');\nstage.circle(pathBounds.left, pathBounds.top + pathBounds.height/2, 3).fill('blue');\nstage.text(pathBounds.left - 80, pathBounds.top + pathBounds.height, 'LEFT_BOTTOM');\nstage.circle(pathBounds.left, pathBounds.top + pathBounds.height, 3).fill('blue');\nstage.text(pathBounds.left  + pathBounds.width/2 - 10, pathBounds.top - 18, 'CENTER_TOP');\nstage.circle(pathBounds.left + pathBounds.width/2, pathBounds.top, 3).fill('blue');\nstage.text(pathBounds.left + pathBounds.width/2 - 20, pathBounds.top + pathBounds.height/2 - 15, 'CENTER');\nstage.circle(pathBounds.left + pathBounds.width/2, pathBounds.top + pathBounds.height/2, 3).fill('blue');\nstage.text(pathBounds.left + pathBounds.width/2 - 23, pathBounds.top + pathBounds.height+ 2, 'CENTER_BOTTOM');\nstage.circle(pathBounds.left + pathBounds.width/2, pathBounds.top + pathBounds.height, 3).fill('blue');\nstage.text(pathBounds.left + pathBounds.width + 5, pathBounds.top - 15, 'RIGHT_TOP');\nstage.circle(pathBounds.left + pathBounds.width, pathBounds.top, 3).fill('blue');\nstage.text(pathBounds.left + pathBounds.width + 5 , pathBounds.top + pathBounds.height/2 - 8, 'RIGHT_CENTER');\nstage.circle(pathBounds.left + pathBounds.width, pathBounds.top + pathBounds.height/2, 3).fill('blue');\nstage.text(pathBounds.left + pathBounds.width + 5, pathBounds.top + pathBounds.height, 'RIGHT_BOTTOM');\nstage.circle(pathBounds.left + pathBounds.width, pathBounds.top + pathBounds.height, 3).fill('blue');"
            }
        ],
        "kind": "member",
        "isEnum": true,
        "type": {
            "names": [
                "string"
            ]
        },
        "optional": null,
        "nullable": null,
        "variable": null,
        "defaultvalue": null,
        "name": "Anchor",
        "longname": "anychart.enums.Anchor",
        "memberof": "anychart.enums",
        "scope": "static"
    }
```

# Enum with field docs
```
/**
 * Align enumeration.
 * @enum {string}
 */
anychart.enums.Align = {
  /**
   * Center align.
   */
  CENTER: 'center',
  /**
   * Left align.
   */
  LEFT: 'left',
  /**
   * Right align.
   */
  RIGHT: 'right',
  /**
   * Top align.
   */
  TOP: 'top',
  /**
   * Bottom align.
   */
  BOTTOM: 'bottom'
};
```
Parsing result:
```
    {
        "comment": "/**\n * Align enumeration.\n * @enum {string}\n */",
        "meta": {
            "range": [
                7788,
                8050
            ],
            "filename": "enums.js",
            "lineno": 177,
            "path": "/Users/alex/Work/anychart/reference-engine/data/acdvf/repo/src",
            "code": {
                "id": "astnode100000248",
                "name": "anychart.enums.Align",
                "type": "ObjectExpression",
                "value": "{\"CENTER\":\"center\",\"LEFT\":\"left\",\"RIGHT\":\"right\",\"TOP\":\"top\",\"BOTTOM\":\"bottom\"}",
                "paramnames": []
            }
        },
        "description": "Align enumeration.",
        "kind": "member",
        "isEnum": true,
        "type": {
            "names": [
                "string"
            ]
        },
        "optional": null,
        "nullable": null,
        "variable": null,
        "defaultvalue": null,
        "name": "Align",
        "longname": "anychart.enums.Align",
        "memberof": "anychart.enums",
        "scope": "static",
        "properties": [
            {
                "comment": "/**\n   * Center align.\n   */",
                "meta": {
                    "range": [
                        7846,
                        7862
                    ],
                    "filename": "enums.js",
                    "lineno": 181,
                    "path": "/Users/alex/Work/anychart/reference-engine/data/acdvf/repo/src",
                    "code": {
                        "id": "astnode100000255",
                        "name": "CENTER",
                        "type": "Literal",
                        "value": "center"
                    }
                },
                "description": "Center align.",
                "name": "CENTER",
                "longname": "anychart.enums.Align.CENTER",
                "kind": "member",
                "memberof": "anychart.enums.Align",
                "scope": "static",
                "type": "<CircularRef>",
                "defaultvalue": "center"
            },
            {
                "comment": "/**\n   * Left align.\n   */",
                "meta": {
                    "range": [
                        7895,
                        7907
                    ],
                    "filename": "enums.js",
                    "lineno": 185,
                    "path": "/Users/alex/Work/anychart/reference-engine/data/acdvf/repo/src",
                    "code": {
                        "id": "astnode100000257",
                        "name": "LEFT",
                        "type": "Literal",
                        "value": "left"
                    }
                },
                "description": "Left align.",
                "name": "LEFT",
                "longname": "anychart.enums.Align.LEFT",
                "kind": "member",
                "memberof": "anychart.enums.Align",
                "scope": "static",
                "type": "<CircularRef>",
                "defaultvalue": "left"
            },
            {
                "comment": "/**\n   * Right align.\n   */",
                "meta": {
                    "range": [
                        7941,
                        7955
                    ],
                    "filename": "enums.js",
                    "lineno": 189,
                    "path": "/Users/alex/Work/anychart/reference-engine/data/acdvf/repo/src",
                    "code": {
                        "id": "astnode100000259",
                        "name": "RIGHT",
                        "type": "Literal",
                        "value": "right"
                    }
                },
                "description": "Right align.",
                "name": "RIGHT",
                "longname": "anychart.enums.Align.RIGHT",
                "kind": "member",
                "memberof": "anychart.enums.Align",
                "scope": "static",
                "type": "<CircularRef>",
                "defaultvalue": "right"
            },
            {
                "comment": "/**\n   * Top align.\n   */",
                "meta": {
                    "range": [
                        7987,
                        7997
                    ],
                    "filename": "enums.js",
                    "lineno": 193,
                    "path": "/Users/alex/Work/anychart/reference-engine/data/acdvf/repo/src",
                    "code": {
                        "id": "astnode100000261",
                        "name": "TOP",
                        "type": "Literal",
                        "value": "top"
                    }
                },
                "description": "Top align.",
                "name": "TOP",
                "longname": "anychart.enums.Align.TOP",
                "kind": "member",
                "memberof": "anychart.enums.Align",
                "scope": "static",
                "type": "<CircularRef>",
                "defaultvalue": "top"
            },
            {
                "comment": "/**\n   * Bottom align.\n   */",
                "meta": {
                    "range": [
                        8032,
                        8048
                    ],
                    "filename": "enums.js",
                    "lineno": 197,
                    "path": "/Users/alex/Work/anychart/reference-engine/data/acdvf/repo/src",
                    "code": {
                        "id": "astnode100000263",
                        "name": "BOTTOM",
                        "type": "Literal",
                        "value": "bottom"
                    }
                },
                "description": "Bottom align.",
                "name": "BOTTOM",
                "longname": "anychart.enums.Align.BOTTOM",
                "kind": "member",
                "memberof": "anychart.enums.Align",
                "scope": "static",
                "type": "<CircularRef>",
                "defaultvalue": "bottom"
            }
        ]
    },
    {
        "comment": "/**\n   * Center align.\n   */",
        "meta": "<CircularRef>",
        "description": "Center align.",
        "name": "CENTER",
        "longname": "anychart.enums.Align.CENTER",
        "kind": "member",
        "memberof": "anychart.enums.Align",
        "scope": "static",
        "type": "<CircularRef>",
        "defaultvalue": "center"
    },
    {
        "comment": "/**\n   * Left align.\n   */",
        "meta": "<CircularRef>",
        "description": "Left align.",
        "name": "LEFT",
        "longname": "anychart.enums.Align.LEFT",
        "kind": "member",
        "memberof": "anychart.enums.Align",
        "scope": "static",
        "type": "<CircularRef>",
        "defaultvalue": "left"
    },
    {
        "comment": "/**\n   * Right align.\n   */",
        "meta": "<CircularRef>",
        "description": "Right align.",
        "name": "RIGHT",
        "longname": "anychart.enums.Align.RIGHT",
        "kind": "member",
        "memberof": "anychart.enums.Align",
        "scope": "static",
        "type": "<CircularRef>",
        "defaultvalue": "right"
    },
    {
        "comment": "/**\n   * Top align.\n   */",
        "meta": "<CircularRef>",
        "description": "Top align.",
        "name": "TOP",
        "longname": "anychart.enums.Align.TOP",
        "kind": "member",
        "memberof": "anychart.enums.Align",
        "scope": "static",
        "type": "<CircularRef>",
        "defaultvalue": "top"
    },
    {
        "comment": "/**\n   * Bottom align.\n   */",
        "meta": "<CircularRef>",
        "description": "Bottom align.",
        "name": "BOTTOM",
        "longname": "anychart.enums.Align.BOTTOM",
        "kind": "member",
        "memberof": "anychart.enums.Align",
        "scope": "static",
        "type": "<CircularRef>",
        "defaultvalue": "bottom"
    },
```

Хочется показывать key type value description