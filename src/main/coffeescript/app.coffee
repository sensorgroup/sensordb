'use strict'

html5mode = ($locationProvider) -> $locationProvider.html5Mode(false)

angular.module('SharedServices', []).config(($httpProvider) ->
	$httpProvider.responseInterceptors.push('myHttpInterceptor')
	spinnerFunction =  (data, headersGetter) ->
		# Start the spinner here
		$('.ajax-loading img').show()
		data;
	$httpProvider.defaults.transformRequest.push(spinnerFunction)
).factory('myHttpInterceptor',  (($q, $window) -> # Register the interceptor as a service, intercepts ALL angular ajax http calls
	(promise) -> promise.then (response) ->
		$('.ajax-loading img').hide() # do something on success,e.g., hide the spinner
		response
),((response) ->
	$('.ajax-loading img').hide() # do something on error
	$q.reject(response)
))

routes = ($routeProvider)->
	$routeProvider.when('/register', {templateUrl: '/p/registration.html', controller: RegistrationCtrl})
	$routeProvider.when('/data/:username', {templateUrl: '/p/data_page.html', controller: DataPageCtrl})
	$routeProvider.when('/explore/:username', {templateUrl: '/p/data_explorer.html', controller: DataExplorerCtrl})
	$routeProvider.when('/data/:username/:selection_id', {templateUrl: '/p/data_page.html', controller: DataPageCtrl})
	$routeProvider.when('/analysis', {templateUrl: '/p/analysis.html', controller: AnalysisCtrl})
	$routeProvider.when('/create/experiment', {templateUrl: '/p/experiment_create.html', controller: ExperimentCreateCtrl})
	$routeProvider.when('/create/node', {templateUrl: '/p/node_create.html', controller: NodeCreateCtrl})
	$routeProvider.when('/create/stream', {templateUrl: '/p/stream_create.html', controller: StreamCreateCtrl})
	$routeProvider.when('/', {templateUrl: '/p/home.html', controller: HomeCtrl})
	$routeProvider.otherwise({templateUrl: '/p/404_page.html', controller: Err404Ctrl})

angular.module('sensordb', ['ngResource','ngCookies','SharedServices','sensordb.filters', 'sensordb.services', 'sensordb.directives']).
config(['$routeProvider',routes]).
config(['$locationProvider',html5mode])

