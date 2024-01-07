module.exports = function(hostPort, loggerinfo, listener) {
    var host = hostPort.split(':')[0];
    var port = parseInt(hostPort.split(':')[1]);

    var net = require('net');
    var partial = '';
    var msgs = 0;
    var connectTimer;
    var client = new net.Socket();

    client.connect(port, host, function() {
        partial = '';
        loggerinfo.info("Node initiated connection with " + host + ":" + port);
    });
    client.on('timeout', function() {
        partial = '';
        loggerinfo.info("Received timeout on connection " + host + ":" + port);
    });
    client.on('end', function() {
        partial = '';
        loggerinfo.info("Received end on connection " + host + ":" + port);
    });
    client.on('close', function() {
        partial = '';
        listener.reset();
        loggerinfo.info("Received close event on connection " + host + ":" + port);
        setTimeout(connect, 15000);
    });
    client.on('connect', function() {
        clearTimeout(connectTimer);
        partial = '';
        listener.reset();
        loggerinfo.info("Received connect  event  on connection " + host + ":" + port);
    });
    client.on('drain', function() {
        partial = '';
        loggerinfo.info("Received drain event  on connection " + host + ":" + port);
    });
    client.on('lookup', function() {
        //loggerinfo.info("Received lookup on connection " + host + ":" + port);
        // Swallow this event
    });

    client.on('data', function(data) {
        clearTimeout(connectTimer);
        data = partial + data;
        partial = '';
        var rows = data.split('\n');
        for (var i = 0; i < rows.length; i++) {
            var row = rows[i];
            if (i == rows.length - 1) {
                if (row) {
                    partial = row;
                }
            } else {
                if (row) {
                    var e;
                    try {
                        e = JSON.parse(row);

                    } catch (e) {
                        loggerinfo.info('Bad json:' + row);
                        return;
                    }
                    listener.message(e);

                    if (++msgs % 100 == 0) {
                        loggerinfo.info('Market Data Messages: %d', msgs);
                    }
                }
            }
        }
    });
    client.on('error', function(e) {
        loggerinfo.info('Error connecting to %s: %s', hostPort, e.toString());

    });


    var connect = this.connect = function() {
        connectTimer = setTimeout(connect, 5000);

    }

    this.connect();
}