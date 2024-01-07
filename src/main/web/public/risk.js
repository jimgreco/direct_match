var app = angular.module('DirectMatchApp', []);

app.controller('AccountDataController', function($rootScope, $http) {
	var TIMEOUT = 10000;

	$rootScope.cancel = function(type, item, field) {
		CANCEL($http, type, item ? item[field] : undefined);
	}
	$rootScope.enable = function(type, item, field) {
		ENABLE($http, type, item[field]);
	}
	$rootScope.disable = function(type, item, field) {
		DISABLE($http, type, item[field]);
	}
	
    $rootScope.getData = function() {
		$http.jsonp(CORE_URL).
	        success(function(data, status, headers, config) {
	            if (data.payload.traders) {
					$rootScope.data = data.payload.traders;
				}	
				if (data.payload.accounts) {
					$rootScope.data = data.payload.accounts;
				}
				if (data.payload.contribs) {
					$rootScope.data = data.payload.contribs;
				}
				if (data.payload.ficcdv01 !== undefined) {
					$rootScope.FICC = data.payload.ficcdv01;
				}
	            setTimeout($rootScope.getData, TIMEOUT);
	        }).error(function(data, status, headers, config) {
	            $rootScope.data = [];
	            alert( "Failed" );	
	            setTimeout($rootScope.getData, TIMEOUT);
	        });
    };
	
	$rootScope.getData();
});
