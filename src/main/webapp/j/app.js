(function() {
  'use strict';
  var html5mode, routes;
  html5mode = function($locationProvider) {
    return $locationProvider.html5Mode(false);
  };
  routes = function($routeProvider) {
    $routeProvider.when('/register', {
      template: '/p/registration.html',
      controller: RegistrationCtrl
    });
    $routeProvider.when('/data/:username', {
      template: '/p/data_page.html',
      controller: DataPageCtrl
    });
    $routeProvider.when('/analysis', {
      template: '/p/analysis.html',
      controller: AnalysisCtrl
    });
    $routeProvider.when('/create/experiment', {
      template: '/p/experiment_create.html',
      controller: ExperimentCreateCtrl
    });
    $routeProvider.when('/create/node', {
      template: '/p/node_create.html',
      controller: NodeCreateCtrl
    });
    $routeProvider.when('/create/stream', {
      template: '/p/stream_create.html',
      controller: StreamCreateCtrl
    });
    $routeProvider.when('/', {
      template: '/p/home.html',
      controller: HomeCtrl
    });
    return $routeProvider.otherwise({
      template: '/p/404_page.html',
      controller: Err404Ctrl
    });
  };
  angular.module('sensordb', ['ngResource', 'ngCookies', 'sensordb.filters', 'sensordb.services', 'sensordb.directives']).config(['$routeProvider', routes]).config(['$locationProvider', html5mode]);
}).call(this);
