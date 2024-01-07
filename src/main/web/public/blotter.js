var app = angular.module('DirectMatchApp', []);

var TIMEOUT = 10000;

app.config(function($locationProvider) {
  $locationProvider.html5Mode({enabled: true, requireBase: false});
})

app.controller('BlotterController', function($scope, $http,  $location) {
	$scope.currentPage = PAGE;
	$scope.data = [];

	$scope.pageChanged = function(pageNo) {
        $scope.getData();
	    console.log('pageChanged changed to: ' + $scope.currentPage);
	    $location.path('/blotter/' + CORE + '/' + $scope.currentPage);
    };

    $scope.firstPage = function() {
      if ($scope.currentPage <= 1) { return; }
      $scope.currentPage = 1;
      $scope.pageChanged();
    }

    $scope.lastPage = function() {
      if ($scope.currentPage >= $scope.numPages) { return; }
      $scope.currentPage = $scope.numPages;
      $scope.pageChanged();
    }

    $scope.prevPage = function() {
    if ($scope.currentPage <= 1) { return; }
      $scope.currentPage--;
      $scope.pageChanged();
    }

    $scope.nextPage = function() {
      if ($scope.currentPage >= $scope.numPages) { return; }
      $scope.currentPage++;
      $scope.pageChanged();
    }
	
	$scope.getData =function() {	
		$http
		.jsonp(CORE_URL + '&page=' + $scope.currentPage)
		.success(function(data, status, headers, config) {
			if (data.status == "ok") {
				$scope.data = [];
				for (var i=0; i<data.payload.records.length; i++) {
					$scope.data.push(data.payload.records[i]);
				}
	            $scope.selNewestFirst = $scope.newestFirst;
	            $scope.currentPage = data.payload.page;
	            $scope.numPages = data.payload.numPages;
			}
			setTimeout($scope.getData, TIMEOUT);
    	})
		.error(function(data, status, headers, config) {
			alert('ERROR!');
			setTimeout($scope.getData, TIMEOUT);
    	});
	}
	$scope.getData();
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

app.filter('qty', function($filter) {
	return function(value) {
		return value / 1000.0;
	}
})