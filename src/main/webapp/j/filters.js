(function() {
  'use strict';  angular.module('sensordb.filters', []).filter('interpolate', [
    'version', function(version) {
      return function(text) {
        return String(text).replace(/\%VERSION\%/mg, version);
      };
    }
  ]);
}).call(this);
