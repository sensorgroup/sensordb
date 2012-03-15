(function() {
  var WidgetTemperatureHumidityRange;
  var __hasProp = Object.prototype.hasOwnProperty, __extends = function(child, parent) {
    for (var key in parent) { if (__hasProp.call(parent, key)) child[key] = parent[key]; }
    function ctor() { this.constructor = child; }
    ctor.prototype = parent.prototype;
    child.prototype = new ctor;
    child.__super__ = parent.prototype;
    return child;
  };
  WidgetTemperatureHumidityRange = (function() {
    __extends(WidgetTemperatureHumidityRange, sensordb.Widget);
    function WidgetTemperatureHumidityRange(elem) {}
    return WidgetTemperatureHumidityRange;
  })();
  ws.registerWidget(WidgetTemperatureHumidityRange);
}).call(this);
