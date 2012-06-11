'use strict';

angular.module('sensordb.filters', []).filter('interpolate', ['version', (version)->
	(text)->
		String(text).replace(/\%VERSION\%/mg, version)
])
