CANCEL = function($http, type, id) {
    var url = CANCEL_URL + '/cancel' + type + '?callback=JSON_CALLBACK&';
	if (type == 'Account') {
		url += 'accountName=' + id;
	}
	else if (type == 'Trader') {
		url += 'traderName=' + id;
	}
	else if (type == 'Contributor') {
		url += 'contributorName=' + id;
	}
	else if (type == 'Security') {
		url += 'securityName=' + id;
	}
	else if (type == 'Order') {
		url += 'orderID=' + id;
	}
	else if (type == 'All') {
		url += 'cancelAll';
	}
	console.log(url);
	$http.jsonp(url).success(function(data, status, headers, config) {
		if (data.status == "ok") {
			alert('Canceled');
		} 
		else {
			alert(data.payload);
		}
    }).
    error(function(data, status, headers, config, more) {
		alert(status);
    });
}

ENABLE = function($http, type, id) {
	ENDIS($http, 'enable', type, id);
}

DISABLE = function($http, type, id) {
	ENDIS($http, 'disable', type, id);
}

var ENDIS = function($http, endis, type, id) {
    var url = CONTROL_URL + '/' + endis + type + '?callback=JSON_CALLBACK&';
	if (type == 'Account') {
		url += 'accountName=' + id;
	}
	else if (type == 'Trader') {
		url += 'traderName=' + id;
	}
	else if (type == 'Contributor') {
		url += 'contributorName=' + id;
	}
	else if (type == 'Security') {
		url += 'securityName=' + id;
	}
	else if (type == 'Order') {
		url += 'id=' + id;
	}
	console.log(url);
	$http.jsonp(url).success(function(data, status, headers, config) {
		if (data.status == "ok") {
			alert(endis + type + ":" + id);
		} 
		else {
			alert(data.payload);
		}
    }).
    error(function(data, status, headers, config) {
		alert(status);
    });
}
