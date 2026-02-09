{
  // helper to flatten arrays where needed
  function flatten(arr) { return [].concat.apply([], arr); }
}

/* top-level */
types
  = _ "(" _ t:types_ _ ")" _ { return t; }
  / types_

types_
  = head:type tail:(_ "|" _ type)* {
      if (tail.length === 0) return ["types", head];
      return ["types", head].concat(tail.map(t => t[3]));
  }

type
  = _ "(" _ t:type_ _ ")" _ { return t; }
  / _ t:type_ { return t; }

type_
  = array
  / object
  / tsfunc
  / jsfunc
  / simple

/* ts function notation: (a:b,c:d)=>type */
tsfunc
  = "(" _ kvs:kvs? _ ")" _ "=>" _ t:types {
      var kvsArr = kvs || [];
      return ["tsfunc"].concat(kvsArr).concat([t]);
  }

/* js function notation: function(a:b,c:d):type  or function(a:b,c:d) */
jsfunc
  = "function(" jsfuncparams:jsfuncparams ")" _ ret:(":" _ t:types { return t; })? {
      var returnType = ret ? ret : null;
      return ["jsfunc", jsfuncparams, returnType];
  }

jsfuncparams
  = head:jsfuncparam tail:(_ "," _ jsfuncparam)* {
      var rest = tail.map(x => x[3]);
      return [head].concat(rest);
  }
  / _ { return []; }

jsfuncparam
  = name:jsfuncparamname ":" _ t:types {
      return ["jsfuncparam", name, t];
  }

jsfuncparamname
  = [a-zA-Z0-9_.]+ { return text(); }

/* Array.<T> */
/* Array.<T> or Array<T> */
array
  = "Array" "."? "<" t:types ">" { return ["array", t]; }

/* Object.<...> or props or Object<...> */
object
  = object_with_prefix
  / props

object_with_prefix
  = "Object" "."? "<" t1:(types / props) _ "," _ t2:(types / props) ">" {
      return ["object", ["proptype", t1], t2];
  }
  / "Object" "."? "<" t1:(types / props) ">" {
      return ["object", t1];
  }

proptype
  = types

props
  = "{" _ k:kvs _ "}" { return ["props"].concat(k); }
  / "(" "{" _ k:kvs _ "}" ")" { return ["props"].concat(k); }

kvs
  = head:kv tail:(_ "," _ kv)* {
      var arr = [head];
      for (var i=0;i<tail.length;i++) arr.push(tail[i][3]);
      return arr;
  }

kv
  = key:[a-zA-Z0-9_.]+ ":" _ t:types {
      return ["kv", key.join(""), t];
  }

/* a simple type */
simple
  = "'" s:[^']+ "'" { return ["simple", "'" + s.join("") + "'"]; }
  / [a-zA-Z0-9_.*]+ { return ["simple", text()]; }
  / "function()" { return ["simple", "function()"]; }

/* whitespace helper */
_ = [ \t\r\n]*
