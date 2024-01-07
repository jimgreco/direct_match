var netHost = process.env.NETHOST || '10.9.11.27';
var netPort = process.env.NETPORT || 6000;

var net = require('net');

var express = require('express');
var app = express();
var server = require('http').createServer(app);
var io = require('socket.io')(server);
var port = process.env.PORT || 3210;

var received = "";

var secType = [];
var cache = {};
var connected = 0;

var client = new net.Socket();

client.connect(netPort, netHost, function(){
	secType = [];
	cache = {};

	client.write('Hello, server! Love, Client.');
});

client.on('data', function(data) {
	//console.log('Received: ' + data);
	//console.log(connected);
	processData(data);

});

client.on('close', function() {
	console.log('Connection closed');

	cache = {priceThirtytwoBid:[], priceThirtytwoAsk:[], priceBid:[], priceAsk:[]};
	io.sockets.emit("disconnect","");
});

client.on('error', function (exc) {
	console.log("Net ERROR: " + exc);
});

server.listen(port, function(){
	console.log('server listening at port %d', port);
});

io.on('connection', function(socket){
	console.log("Hello I am in ");
	connected++;
	//console.log(cache);
	//var client = new net.Socket();

	notifyThis(socket);


	socket.on('error', function (exc) {
		console.log("Socket ERROR: " + exc);
	});

	socket.on('disconnect', function () {
   	console.log("one user has logged out");
		connected--;
 	});
});


var processData = function(data){
	received += data.toString();
	var currentIndex = 0;
	var nextNewLine = -1;

	while((nextNewLine = received.indexOf('\n', currentIndex)) != -1 ){
		var line = received.substring(currentIndex, nextNewLine);
		processLine(line);
		currentIndex = nextNewLine + 1;
	}
	received = received.substring(currentIndex);
};

var notifyThis = function(socket){
	if(!cache || typeof cache !== 'object' || cache == null){
		socket.emit("disconnect","");
	}

	for(var i = 0; i < secType.length; i++){
		//console.log("security   " + secType[i]);
		socket.emit("security", JSON.parse('{"sec" : "'+ secType[i] +'" }'))
	}

	for(var key in cache){
		//console.log(key);

		for(var i = 0; i < cache[key].length ; i++){
			//console.log(cache[key][i]);
			socket.emit("pricelevel", cache[key][i]);
		}
	}
}

var processLine = function(line){
	var json = JSON.parse(line);

	if(json.type == "security"){

		if(secType.indexOf(json.sec) == -1){
			secType.push(json.sec);
			if(connected > 0 ) {
				io.emit("security" + json);
			}
		}

	}else if(json.type == "price"){
		
		//console.log(json);
		if(connected > 0 ) {
			io.emit("pricelevel", json);
		}
		if(!cache[json.side + json.sec]){
			cache[json.side + json.sec] = [];
		}
		cache[json.side + json.sec][parseInt(json.pos)] = json; 
	}else{
		//socket.emit("data", json);
	}
}