// Code goes here
var cache = {};
var secType = [];


var EighthFractionDisplay = ["0", "1", "2", "3", "+", "5", "6", "7"];
var ThirtySecondDisplay = ["-00", "-01", "-02", "-03", "-04", "-05", "-06", "-07", "-08", "-09",
            "-10", "-11", "-12", "-13", "-14", "-15", "-16", "-17", "-18", "-19",
            "-20", "-21", "-22", "-23", "-24", "-25", "-26", "-27", "-28", "-29",
            "-30", "-31"];

var length = 10;

send();

function send(){
    //var url = 'http://10.9.14.200:3210';
    //var url = 'http://localhost:3210';
    
    var socket = io(DATA_URL);

    // socket.on('connection', function() {
        console.log("Connected");

        //socket.emit ('login', JSON.stringify({'token':token}));

        socket.on( 'disconnect', function( data ) {
            console.log("Received Socket disconnect from GUI");
        });

        socket.on('error', function (exc) {
            console.log("Socket ERROR: " + exc);
        });

        socket.on( 'data', function(data) {
            //console.log(JSON.stringify(data) );
        });

        socket.on('security', function(data){
            if(secType.indexOf(data.sec) != -1){
                console.log("we have these data");
            }else{
                secType.push(data.sec);
                changeSec(data.sec);
            }
        });


        socket.on( 'pricelevel', function(data) {
            //console.log(data);
            if(!cache[data.side + data.sec]){
                cache[data.side + data.sec] = [];
            }

            cache[data.side + data.sec][data.pos] = data;
            
            if(data.sec=="2Y"||data.sec=="3Y"||data.sec=="5Y"||data.sec=="7Y"||data.sec=="10Y"||data.sec == "30Y" ){
            	cache[data.side + data.sec][data.pos].px = ConvertTo32ndPrice(data.px);
        	}else{
                cache[data.side + data.sec][data.pos].px = data.px / 1000000000 ;
            }
            cache[data.side + data.sec][data.pos].qty = data.qty / 1000000;
        });

        socket.on('blotter', function (data) {
            console.log(JSON.stringify(data))
        });
    // });
}


function ConvertTo32ndPrice(price){
    var str = ""; 

    if(price == 0) return str;

    var price = price / (1000 * 1000 * 1000);
    
    str += Math.floor(price);

    var thrity = (price - Math.floor(price)) * 32;

    str += ThirtySecondDisplay[Math.floor(thrity)];

    var eight = (thrity - Math.floor(thrity)) * 8;

    str += EighthFractionDisplay[Math.floor(eight)];

    return str;
}


function changeSec(sec){
    $(".dropdown-menu").append("<li><a class =" + sec+ "> sec : " +  sec +"</a></li>");
}


function getError(){
    $("button").text("not Connected");
}

function clearError(){
    $("button").text(sec + " Years");
}