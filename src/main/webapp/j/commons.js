(function() {
  var alertFallback;
  _.mixin(_.string.exports());
  window.module = function(name, fn) {
    if (!(this[name] != null)) {
      this[name] = {};
    }
    if (!(this[name].module != null)) {
      this[name].module = window.module;
    }
    return fn.apply(this[name], []);
  };
  $LAB.setGlobalDefaults({
    Debug: true
  });
  Date.prototype.utc_format = function() {
    return this.getUTCDate() + "/" + this.getUTCMonth() + "/" + this.getUTCFullYear() + " " + this.getUTCHours() + ":" + this.getUTCMinutes() + ":" + this.getUTCSeconds();
  };
  alertFallback = true;
  if (typeof window.console === "undefined" || typeof window.console.log === "undefined") {
    window.console = {};
    if (alertFallback) {
      window.console.log = function(msg) {
        return alert(msg);
      };
    } else {
      windlow.console.log = function() {};
    }
  }
}).call(this);
