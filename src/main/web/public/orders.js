var app = angular.module('DirectMatchApp', []);

app.config(function($locationProvider) {
  $locationProvider.html5Mode({enabled: true, requireBase: false});
})

app.controller('OrdersController', function($scope, $http, $location) {
  $scope.currentPage = PAGE;
  $scope.params = PARAMS;
  $scope.newestFirst = NEWEST_FIRST;
  $scope.sortKey="";
  $scope.orderByField="";
  $scope.reverseSort=false;
  $scope.tracks = [];
  
  $scope.cancel = function(type, item, field) {
  	  CANCEL($http, type, field ? item[field] : item);
  }

  if ($scope.params!='' && $scope.currentPage!='') { 
	  parseParams(); 
	  getData(); 
  }

  function parseParams() {
    var array = $scope.params.split(';');
    for (var i = 0; i < array.length; i++) {
      var pair = array[i].split(':');
      if (pair[0]=='security') {
        $scope.sec=pair[1];
      }
      if (pair[0]=='contributor') {
        $scope.contributor=pair[1];
      }
      if (pair[0]=='trader') {
        $scope.trader=pair[1];
      }
      if (pair[0]=='account') {
        $scope.account=pair[1];
      }
    }
  }

  function getData() {
    var URL = ORDER_MON_URL + "/orders?params="+$scope.params+"&page="+$scope.currentPage+"&newestFirst="+$scope.newestFirst+"&callback=JSON_CALLBACK";
	console.log('URL: ' + URL);
    $http.jsonp(URL).
        success(function(data, status, headers, config) {
          $scope.selSec = $scope.sec;
          $scope.selContributor = $scope.contributor;
          $scope.selTrader = $scope.trader;
          $scope.selAccount = $scope.account;
          $scope.selNewestFirst = $scope.newestFirst;
          $scope.currentPage = data.payload.page;
          $scope.numPages = data.payload.numPages;
        
  		console.log('numPages: ' + data.payload.numPages);
        $scope.totalItems = data.payload.totalRecordCount;
        angular.copy(data.payload.records, $scope.tracks);
        $scope.warning="Total items: "+$scope.totalItems;
        }).
        error(function(data, status, headers, config) {
        $scope.myData = [{"status": "error"}];
          alert("error");
        });
  }

  $scope.getOrders = function() {
    console.log('trader changed to: ' + $scope.trader);
    console.log('sec changed to: ' + $scope.sec);
    console.log('contributor changed to: ' + $scope.contributor);
    console.log('account changed to: ' + $scope.account);
    $scope.currentPage = 1;
    $scope.warning="";
    var params="";
    var count=0;
    if ($scope.sec ){
      count+=1;
      params+="security:"+$scope.sec;
    }
    if ($scope.contributor ){
      if(count!==0){
        params+=";"
      }
      count+=1;
      params=params+"contributor:"+$scope.contributor;
    }
    if ($scope.trader){
      if(count!==0){
        params+=";"
      }
      count+=1;
      params+="trader:"+$scope.trader;
    }
    if ($scope.account ){
      if(count!==0){
        params+=";"
      }
      count+=1;
      params+="account:"+$scope.account;
    }
    if(count>=1){
      console.log("Getting Data:"+params);
      $scope.params=params;
      $scope.pageChanged();
    }else{
      $scope.params="all";
      $scope.pageChanged();
    }
  }
 
  $scope.pageChanged = function(pageNo) {
    console.log('pageChanged changed to: ' + $scope.currentPage);
    console.log('newestFirst changed to: ' + $scope.newestFirst);
    getData();
    $location.path('/orders/' + CORE + '/' + $scope.params+'/'+$scope.currentPage+'/'+$scope.newestFirst);
  };

  $scope.sortOnClick = function(col) {
    console.log('before sort: key=' + $scope.sortKey + ' orderByField=' + $scope.orderByField + ' reverseSort=' + $scope.reverseSort);
    if ($scope.sortKey == col) {
      if ($scope.reverseSort) {
        $scope.sortKey=""; $scope.orderByField=""; $scope.reverseSort=false;
      } else {
        $scope.orderByField="-"+col; $scope.reverseSort=true;
      }
    } else {
        $scope.sortKey=col; $scope.orderByField=col; $scope.reverseSort=false;
    }
    console.log('after sort: key=' + $scope.sortKey + ' orderByField=' + $scope.orderByField + ' reverseSort=' + $scope.reverseSort);
  }

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
});