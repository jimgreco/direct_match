var app = angular.module('DirectMatchApp', []); 

app.controller('HistoryController', function($rootScope, $http, $window) {
	$rootScope.data = [];
	$rootScope.order = {};
	$rootScope.cancelmsg = '';
	
	var url = CORE_URL + '&id=' + ORDER_ID;
	console.log(url);
	$http.jsonp(url)
		.success(function(data, status, headers, config) {
			if (data.status == "ok") {
				if (data.payload.order) {
					$rootScope.order = data.payload.order;
				}
				if (data.payload.records) {
					$rootScope.data = data.payload.records;
				}
			}
		})
		.error(function(data, status, headers, config) {
			alert('Error Connecting to server');
		});
		
		$rootScope.submit = function(new_id) {
			$window.location.href = new_id;
		}

		$rootScope.cancel = function(id) {
            if (!confirm("OK to cancel order #"+id+"?")) { return; }
			var URL = CANCEL_URL + "/cancelOrder?orderID="+id+"&callback=JSON_CALLBACK";
        	$http.jsonp(URL).
        	success(function(data, status, headers, config) {
			if (data.status == "ok") {
        		console.log("Order "+id+" cancelled");
        		$rootScope.cancelmsg='Cancel successful!';
        		$window.location.href = id;
        	} else {
        		$rootScope.cancelmsg='Error: '+data.payload;
        	}
        	}).
    error(function(data, status, headers, config) {
      $rootScope.cancelmsg='Error: '+status;
    });
		}
});

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