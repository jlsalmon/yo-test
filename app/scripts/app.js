'use strict';

/**
 * @ngdoc overview
 * @name verity
 * @description
 * # verity
 *
 * Main module of the application.
 */
var app = angular
  .module('verity', [
    'ngAnimate',
    'ngCookies',
    'ngResource',
    'ngRoute',
    'ngSanitize',
    'ngTouch',
    'ui.bootstrap',
    'ngTable',
    'restangular'
  ]);


app.config(function($routeProvider, $locationProvider) {
  // Configure routes
  $routeProvider
  .when('/new', {
    templateUrl: 'views/new_request.html',
    controller: 'NewRequestController'
  })
  .when('/requests', {
    templateUrl: 'views/user_requests.html',
    controller: 'UserRequestsController'
  })
  .when('/requests/:id', {
    templateUrl: 'views/request.html',
    controller: 'RequestController'
  })
  .when('/about', {
    templateUrl: 'views/about.html',
    controller: 'AboutCtrl'
  })
  .when('/404', {
    templateUrl: 'views/errors/404.html',
    //controller: 'PageNotFoundController'
  })
  .otherwise({
    redirectTo: '/'
  });
});


app.config(function(RestangularProvider) {
  // Set the base URL
  RestangularProvider.setBaseUrl('http://localhost:8080/');
  
  // Enable access to the response headers
  RestangularProvider.setFullResponse(true);

  // Add a response interceptor
  RestangularProvider.addResponseInterceptor(function(data, operation, what, url, response, deferred) {
    var extractedData;

    if (operation === "getList") {
      extractedData = data._embedded.requests;
    } else {
      extractedData = data;
    }
    return extractedData;
  });

  // Set the self link
  RestangularProvider.setRestangularFields({
    selfLink : "_links.self.href"
  });
});