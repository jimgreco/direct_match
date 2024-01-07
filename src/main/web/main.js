/* REQUIRE */
var HTTP = require('http');
var EXPRESS = require('express');
var SERVER = require('http');
var ROUTES = require('./routes.js');

/* Properties */
var port = process.env.PORT || 5000;
console.log('WEB PORT: %d', port);

/* Initializing express */
var app = EXPRESS();
var server = HTTP.Server(app);
app.use(require('body-parser')());

/* routes */
console.log('Initializing routes');
ROUTES(EXPRESS, app);

/* Web port */
console.log('Opening port %d', port);
server.listen(port, '0.0.0.0');

console.log('Initialization complete');
