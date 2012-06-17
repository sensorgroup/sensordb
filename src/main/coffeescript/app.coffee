'use strict'


html5mode = ($locationProvider) -> $locationProvider.html5Mode(false)

routes = ($routeProvider)->
	$routeProvider.when('/register', {templateUrl: '/p/registration.html', controller: RegistrationCtrl})
	$routeProvider.when('/data/:username', {templateUrl: '/p/data_page.html', controller: DataPageCtrl})
	$routeProvider.when('/data/:username/:selection_id', {templateUrl: '/p/data_page.html', controller: DataPageCtrl})
	$routeProvider.when('/analysis', {templateUrl: '/p/analysis.html', controller: AnalysisCtrl})
	$routeProvider.when('/create/experiment', {templateUrl: '/p/experiment_create.html', controller: ExperimentCreateCtrl})
	$routeProvider.when('/create/node', {templateUrl: '/p/node_create.html', controller: NodeCreateCtrl})
	$routeProvider.when('/create/stream', {templateUrl: '/p/stream_create.html', controller: StreamCreateCtrl})
	$routeProvider.when('/', {templateUrl: '/p/home.html', controller: HomeCtrl})
	$routeProvider.otherwise({templateUrl: '/p/404_page.html', controller: Err404Ctrl})

angular.module('sensordb', ['ngResource','ngCookies','sensordb.filters', 'sensordb.services', 'sensordb.directives']).
config(['$routeProvider',routes]).
config(['$locationProvider',html5mode])

