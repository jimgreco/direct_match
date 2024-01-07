var app = angular.module('DirectMatchApp', ['ngAnimate']);

app.controller('MarketDataController', function($rootScope, $http) {
	var url = MARKET_DATA_URL;

	function maturityDateSort(a, b) {
		return a.maturityDate > b.maturityDate;
	}

	$rootScope.getData =function(){$http.jsonp(url).
    	success(function(data, status, headers, config) {
			if (data.status == "ok") {
				var data = data.payload;	
				data.sort(maturityDateSort);
				$rootScope.d = data;
			}
			setTimeout($rootScope.getData, 1000);
    	}).
    	error(function(data, status, headers, config) {
			setTimeout($rootScope.getData, 1000);
    	});
	}

	$rootScope.getData();
});

app.directive('animateOnChange', ANIMATION_FUNCTION);

app.filter('HHMMSSsss', function($filter) {
	return function(value) {
		if (value === undefined) return "";
		if (value.indexOf('.') > -1) {
			var split = value.split('.');
			return split[0] + '.' + (split[1] + '000').slice(0, 3);
		}
		return value;
	};
});

app.filter('MMYY', function($filter) {
	return function(value) {
		if (value === undefined) return "";
		var YY = Math.round(value / 10000) % 100;
		var MM = Math.round(value / 100) % 100;
		return MM + '/' + YY;
	};
});