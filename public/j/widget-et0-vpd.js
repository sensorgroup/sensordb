(function() {
  var WidgetEt0Vpd;
  var __hasProp = Object.prototype.hasOwnProperty, __extends = function(child, parent) {
    for (var key in parent) { if (__hasProp.call(parent, key)) child[key] = parent[key]; }
    function ctor() { this.constructor = child; }
    ctor.prototype = parent.prototype;
    child.prototype = new ctor;
    child.__super__ = parent.prototype;
    return child;
  };
  WidgetEt0Vpd = (function() {
    __extends(WidgetEt0Vpd, sensordb.Widget);
    function WidgetEt0Vpd(e) {
      WidgetEt0Vpd.__super__.constructor.call(this, e);
    }
    return WidgetEt0Vpd;
  })();
  ws.registerWidget(WidgetEt0Vpd);
}).call(this);
