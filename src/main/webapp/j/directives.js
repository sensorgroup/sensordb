(function() {
  'use strict';  angular.module('sensordb.directives', []).directive('appVersion', [
    'version', function(version) {
      return function(scope, elm, attrs) {
        return elm.text(version);
      };
    }
  ]);
}).call(this);
