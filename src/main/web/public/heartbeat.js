var app = angular.module('DirectMatchApp', ['ngAnimate', 'ngCookies']);
//var app = angular.module('DirectMatchApp', []);

app.controller('cookiesController', ['$rootScope','$cookies','$window', function($rootScope,$cookies,$window) {
  $cookies.userName = 'DirectMatch';
  
  $rootScope.platformCookie = $cookies.userName;

  var flag = $cookies.getObject('flag');

  $rootScope.flag = {};
  console.log(flag);

  if(flag !== undefined){
  	console.log("I am here");
  	$rootScope.flag = flag;
  }
  
  $rootScope.add = function(key, flag){
  	console.log(key);	
  	console.log(flag);
  	if(key == 'all'){
  		flag = {};
  	}else{
  		flag[key] = true;
  	}
  	$rootScope.flag = flag;
  	$cookies.putObject('flag', flag);
  };

  $rootScope.remove = function(key, flag){
  	flag[key] = false;
  	$rootScope.flag = flag;
  	$cookies.putObject('flag', flag);
  };

}])

.controller('hbController',["$rootScope", "$http", function($rootScope,$http) {
	var TIMEOUT = 1000;
	

	//$rootScope.flag = [];
	//$rootScope.query = [];
	//$rootScope.selected = {};
	
	
	$rootScope.getData =function(){
		$http.jsonp(CORE_URL).
			success(function(data, status, headers, config) {
	        	$rootScope.myData = data;
				var keys = ['Active'];			
				for(var i = 0;i<data.length;i++)
				{
					Object.keys(data[i]).forEach(function(key){
						if(keys.indexOf(key) == -1){
							keys.push(key);
						}
					});
				}
				keys.splice(0,1);
				$rootScope.keys = keys; 
				
				setTimeout($rootScope.getData, TIMEOUT);
	    	}).
	    	error(function(data, status, headers, config) {
	        	$rootScope.myData = [{"status": "error"}];

				setTimeout($rootScope.getData, TIMEOUT);
	    	});

	}
		
	$rootScope.getData();
}]);

app.directive('animateOnChange', ANIMATION_FUNCTION);

app.filter('range', function() {
  return function(input, myData) {
	if(typeof myData == 'undefined'){
		return input;
	}
    var length = myData.length;
	
    for (var i=0; i< length; i++) {
    	if (myData[i]['Active'] != null) {
    		input.push(i);
    	}
    	//input.push(i); 
    }
    return input;
  };
});







