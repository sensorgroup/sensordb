'use strict';

angular.module('sensordb.directives', []).directive('appVersion', ['version', (version)->
	(scope, elm, attrs)->
		elm.text(version)
])
