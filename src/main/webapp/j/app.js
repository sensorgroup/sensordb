(function() {
  'use strict';
  var html5mode, routes;
  html5mode = function($locationProvider) {
    return $locationProvider.html5Mode(false);
  };
  angular.module('SharedServices', []).config(function($httpProvider) {
    var spinnerFunction;
    $httpProvider.responseInterceptors.push('myHttpInterceptor');
    spinnerFunction = function(data, headersGetter) {
      $('.ajax-loading img').show();
      return data;
    };
    return $httpProvider.defaults.transformRequest.push(spinnerFunction);
  }).factory('myHttpInterceptor', (function($q, $window) {
    return function(promise) {
      return promise.then(function(response) {
        $('.ajax-loading img').hide();
        return response;
      });
    };
  }), (function(response) {
    $('.ajax-loading img').hide();
    return $q.reject(response);
  }));
  routes = function($routeProvider) {
    $routeProvider.when('/register', {
      templateUrl: '/p/registration.html',
      controller: RegistrationCtrl
    });
    $routeProvider.when('/data/:username', {
      templateUrl: '/p/data_page.html',
      controller: DataPageCtrl
    });
    $routeProvider.when('/explore/:username', {
      templateUrl: '/p/data_explorer.html',
      controller: DataExplorerCtrl
    });
    $routeProvider.when('/data/:username/:selection_id', {
      templateUrl: '/p/data_page.html',
      controller: DataPageCtrl
    });
    $routeProvider.when('/analysis', {
      templateUrl: '/p/analysis.html',
      controller: AnalysisCtrl
    });
    $routeProvider.when('/create/experiment', {
      templateUrl: '/p/experiment_create.html',
      controller: ExperimentCreateCtrl
    });
    $routeProvider.when('/create/node', {
      templateUrl: '/p/node_create.html',
      controller: NodeCreateCtrl
    });
    $routeProvider.when('/create/stream', {
      templateUrl: '/p/stream_create.html',
      controller: StreamCreateCtrl
    });
    $routeProvider.when('/', {
      templateUrl: '/p/home.html',
      controller: HomeCtrl
    });
    return $routeProvider.otherwise({
      templateUrl: '/p/404_page.html',
      controller: Err404Ctrl
    });
  };
  angular.module('sensordb', ['ngResource', 'ngCookies', 'SharedServices', 'sensordb.filters', 'sensordb.services', 'sensordb.directives']).config(['$routeProvider', routes]).config(['$locationProvider', html5mode]);
}).call(this);
