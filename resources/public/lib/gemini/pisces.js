(function (global, factory) {
	typeof exports === 'object' && typeof module !== 'undefined' ? module.exports = factory() :
	typeof define === 'function' && define.amd ? define(factory) :
	(global.Pisces = factory());
}(this, (function () { 'use strict';

var getScrollingElement = function () {
  if ('scrollingElement' in document) {
    return document.scrollingElement;
  }

  var html = document.documentElement;
  var start = html.scrollTop;
  var end;

  html.scrollTop = start + 1;

  end = html.scrollTop;

  html.scrollTop = start;

  if (end > start) {
    return html;
  }

  return document.body;
};

var BODY = document.body;
var relativeValueReg = new RegExp(/^(\-|\+)\d/);
var numberReg = new RegExp(/^\d*\.?\d*$/);

function assign(target) {
  var sources = [], len = arguments.length - 1;
  while ( len-- > 0 ) sources[ len ] = arguments[ len + 1 ];

  [].concat( sources ).map(function (source) {
    return Object.keys(source).map(function (propertyName) {
      target[propertyName] = source[propertyName];
    });
  });
  return target;
}

function isElement(el) {
  return (el instanceof HTMLElement);
}

function isString(value) {
  return (typeof value === 'string');
}

function isNull(value) {
  return value === null;
}

function isUndefined(value) {
  return (typeof value === 'undefined');
}

function isNumber(value) {
  return ((typeof value === 'number') || numberReg.test(value));
}

function isObject(value) {
  return (typeof value === 'object');
}

function isFunction(value) {
  return (typeof value === 'function');
}

function isBody(el) {
  return (el === BODY);
}

function isRelativeValue(value) {
  if (!isString(value)) {
    return false;
  }

  return relativeValueReg.test(value);
}

var version = "0.0.19";

var Pisces = function Pisces(scrollingBox, options) {
  if ( scrollingBox === void 0 ) scrollingBox = getScrollingElement();
  if ( options === void 0 ) options = {};

  this.scrollingBox = scrollingBox;
  this.options = assign({}, Pisces.defaults(), options);
};

var prototypeAccessors = { start: {},max: {} };

Pisces.defaults = function defaults () {
  var duration = 600;
  var easing = function (t) { return Math.sqrt(1 - (--t * t)); };
  var onComplete = null;
  return { duration: duration, easing: easing, onComplete: onComplete };
};

prototypeAccessors.start.get = function () {
  var ref = this.scrollingBox;
    var scrollLeft = ref.scrollLeft;
    var scrollTop = ref.scrollTop;
  return { x: scrollLeft, y: scrollTop };
};

prototypeAccessors.max.get = function () {
  var el = this.scrollingBox;
  var x;
  var y;
  if (isBody(el)) {
    x = (el.scrollWidth - window.innerWidth);
    y = (el.scrollHeight - window.innerHeight);
  } else {
    x = (el.scrollWidth - el.clientWidth);
    y = (el.scrollHeight - el.clientHeight);
  }

  return { x: x, y: y };
};

Pisces.prototype._animate = function _animate (coords, options) {
    if ( options === void 0 ) options = {};

  var _this = this;
  var _options = assign({}, _this.options, options);

  var start = performance.now();
  var step = function (timestamp) {
    var elapsed = Math.abs(timestamp - start);
    var progress = _options.easing(elapsed / _options.duration);
    _this.scrollingBox.scrollTop = (coords.start.y + coords.end.y * progress);
    _this.scrollingBox.scrollLeft = (coords.start.x + coords.end.x * progress);
    if (elapsed > _options.duration) { _this._completed(coords, _options); }
    else { _this._RAF = requestAnimationFrame(step); }
  };

  _this.cancel();
  _this._RAF = requestAnimationFrame(step);
  return this;
};

Pisces.prototype._completed = function _completed (coords, options) {
  this.cancel();
  this.scrollingBox.scrollTop = (coords.start.y + coords.end.y);
  this.scrollingBox.scrollLeft = (coords.start.x + coords.end.x);
  if (isFunction(options.onComplete)) { options.onComplete(); }
};

Pisces.prototype._getEndCoordinateValue = function _getEndCoordinateValue (coord, start, max) {
  if (isNumber(coord)) {
    if (coord > max) { coord = max; }
    return (coord - start);
  }

  if (isRelativeValue(coord)) {
    var value = (start - (start - ~~coord));
    if ((start + value) > max) { return (max - start); }
    else if ((start + value) < 0) { return -start; }
    return value;
  }

  return 0;
};

Pisces.prototype.scrollTo = function scrollTo (target, options) {
    if ( target === void 0 ) target = null;

  var ERROR_MESSAGE = 'target param should be a HTMLElement or and ' +
    'object formatted as: {x: Number, y: Number}';

  if (isNull(target) || isUndefined(target)) {
    return console.error('target param is required');
  } else if (!isObject(target) && !isString(target)) {
    return console.error(ERROR_MESSAGE);
  }

  if (isString(target)) {
    var element = this.scrollingBox.querySelector(target);
    if (isElement(element)) {
      return this.scrollToElement(element, options);
    }

    return console.error(ERROR_MESSAGE);
  }

  if (isElement(target)) {
    return this.scrollToElement(target, options);
  }

  return this.scrollToPosition(target, options);
};

Pisces.prototype.scrollToElement = function scrollToElement (el, options) {
  var start = this.start;
  var end = this.getElementOffset(el);
  if (!end) { return; }
  return this._animate({ start: start, end: end }, options);
};

Pisces.prototype.scrollToPosition = function scrollToPosition (coords, options) {
  var start = this.start;
  var max = this.max;
  var x = (coords.hasOwnProperty('x')) ? coords.x : start.x;
  var y = (coords.hasOwnProperty('y')) ? coords.y : start.y;
  x = this._getEndCoordinateValue(x, start.x, max.x);
  y = this._getEndCoordinateValue(y, start.y, max.y);
  var end = { x: x, y: y };
  return this._animate({ start: start, end: end }, options);
};

Pisces.prototype.scrollToTop = function scrollToTop (options) {
  var start = this.start;
  var end = { x: 0, y: -(start.y) };
  return this._animate({ start: start, end: end }, options);
};

Pisces.prototype.scrollToBottom = function scrollToBottom (options) {
  var start = this.start;
  var max = this.max;
  var end ={ x: 0, y: (max.y - start.y) };
  return this._animate({ start: start, end: end }, options);
};

Pisces.prototype.scrollToLeft = function scrollToLeft (options) {
  var start = this.start;
  var end ={ x: -(start.x), y: 0 };
  return this._animate({ start: start, end: end }, options);
};

Pisces.prototype.scrollToRight = function scrollToRight (options) {
  var start = this.start;
  var max = this.max;
  var end ={ x: (max.x - start.x), y: 0 };
  return this._animate({ start: start, end: end }, options);
};

Pisces.prototype.set = function set (key, value) {
  this.options[key] = value;
  return this;
};

Pisces.prototype.cancel = function cancel () {
  this._RAF = cancelAnimationFrame(this._RAF);
  return this;
};

Pisces.prototype.getElementOffset = function getElementOffset (el) {
  if (!isBody(el) && !this.scrollingBox.contains(el)) {
    console.error('scrollingBox does not contains element');
    return false;
  }

  var start = this.start;
  var max = this.max;
  var e = el;
  var _top = 0;
  var _left = 0;
  var x = 0;
  var y = 0;

  do {
    _left += e.offsetLeft;
    _top += e.offsetTop;
    e = e.parentElement;
  } while (e !== this.scrollingBox);

  x = (_left - start.x);
  y = (_top - start.y);

  if (x > max.x) { x = max.x; }
  if (y > max.y) { y = max.y; }

  return { x: x, y: y };
};

Object.defineProperties( Pisces.prototype, prototypeAccessors );



Pisces.VERSION = version;

return Pisces;

})));
