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

# Constants
```
	/**
	 * description
	 * @define {type} short description
	 */
	 namespace.constant = value;
```

# Properties
```
	/**
	 * description
	 * @type {type}
	 */
	 namespace.property = default_value;
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

# Exports (at the bottom)
```
	goog.exportSymbol('name', link);
	namespace['smth'] = link;
```