var fs = require('fs');
var vash = require('vash');

var urls_cancel = {
	dev: '10.9.11.27:8003/CANCEL01A',
	uat: '10.9.11.137:8006/CANCEL01A',
	prod: '10.9.14.69:8001/CANCEL01A',
	local: 'localhost:8001/CANCEL01A'
};

var urls_control = {
	dev: '10.9.11.26:8001/SEQ01A',
	uat: '10.9.11.136:8001/SEQ01A',
	prod: '10.9.14.69:8001/SEQ01A',
	local: 'localhost:8001/SEQ01A'
};

var urls_risk = {
	dev: "10.9.11.27:8003/ACCTMON01A",
	uat: "10.9.11.137:8006/ACCTMON01A",
	prod: '10.9.14.73:8004/ACCTMON01A',
	local: "127.0.0.1:8001/ACCTMON01A"
}

var urls_order = {
	dev: '10.9.11.27:8003/ORDMON01A',
	uat: '10.9.11.137:8006/ORDMON01A',
	prod: '10.9.14.73:8004/ORDMON01A',
	local: 'localhost:8001/ORDMON01A'
}

function getp(core, monitor) {
	return {
		core: core,
		monitor: monitor,
		monitors: ['market-data', 'accounts', 'traders', 'contribs', 'blotter', 'orders', 'history', 'heartbeat', 'market'],
		cores: ['local', 'dev', 'uat', 'prod'],
		cancelUrl: 'http://' + urls_cancel[core],
		controlUrl: 'http://' + urls_control[core]
	};
}

module.exports = function(express, app, userRepo, crypto, streamingUsers) {	
	app.set('view engine', 'vash');
	app.use(express.static('public'));
	
	app.get('/', function(req, res) {
		res.redirect('/heartbeat/prod');
	});

	app.get('/market/:core', function(req,res){
		var urls = {
            uat: '10.9.14.200:3210',
            dev: '10.9.14.200:3211',
            prod: '10.9.14.200:3212'
		}
		var p = getp(req.params.core, 'market');
		p.url = 'http://' + urls[p.core];
		res.render(p.monitor, p);
	});

	app.get('/market-data/:core', function(req, res) {
		var urls = {
			dev: '10.9.11.27:8003/MDMON01A',
			uat: '10.9.11.137:8006/MDMON01A',
			prod: '10.9.14.73:8004/MDMON01A'
		}
		var p = getp(req.params.core, 'market-data');
		p.url = 'http://' + urls[p.core] + '/bbo?callback=JSON_CALLBACK';
		res.render(p.monitor, p);
	});
	
	app.get('/heartbeat/:core' ,function(req, res) {
		var urls = {
			dev: "10.9.11.25:10001",
			uat: "10.9.11.136:10001",
			prod: "10.9.14.77:10001",
			local: "127.0.0.1:10001"
		}
	    var p = getp(req.params.core, 'heartbeat');
		p.url = 'http://' + urls[p.core] + '/vm?callback=JSON_CALLBACK';
		res.render(p.monitor, p);
	});
	
	app.get('/blotter/:core', function(req, res) {
		res.redirect('/blotter/'+req.params.core+'/1');
	})
	
	app.get('/blotter/:core/:page', function(req, res) {
		var p = getp(req.params.core, 'blotter');
		p.url = 'http://' + urls_order[p.core] + '/blotter?callback=JSON_CALLBACK';
		p.page = req.params.page;
		res.render(p.monitor, p);
	});
	
	app.get('/history/:core', function(req, res) {
		res.redirect('/history/' + req.params.core + '/1');
	});
	
	app.get('/history/:core/:id', function(req, res) {
		var p = getp(req.params.core, 'history');
		p.url = 'http://' + urls_order[p.core] + '/history?callback=JSON_CALLBACK';
		p.id = req.params.id;
		res.render(p.monitor, p);
	});

	app.get('/accounts/:core' ,function(req, res) {
	    var p = getp(req.params.core, 'accounts');
		p.url = 'http://' + urls_risk[p.core] + '/accounts?callback=JSON_CALLBACK';
		res.render(p.monitor, p);
	});
	
	app.get('/traders/:core' ,function(req, res) {
	    var p = getp(req.params.core, 'traders');
		p.url = 'http://' + urls_risk[p.core] + '/traders?callback=JSON_CALLBACK';
		res.render(p.monitor, p);
	});

	app.get('/contribs/:core' ,function(req, res) {
	    var p = getp(req.params.core, 'contribs');
		p.url = 'http://' + urls_risk[p.core] + '/contribs?callback=JSON_CALLBACK';
		res.render(p.monitor, p);
	});	

	app.get('/orders/:core/', function(req, res) {
		res.redirect('/orders/' + req.params.core + '/all/1/true');
	});

	app.get('/orders/:core/:params/:page/:newestFirst', function(req, res) {
		var p = getp(req.params.core, 'orders');
		p.url = 'http://' + urls_order[p.core];
		p.params = req.params.params;
		p.page = req.params.page;
		p.newestFirst = req.params.newestFirst;
		res.render(p.monitor, p);
	});
	
	app.get('/ping', function(req, res) {
		return res.send('Welcome to Direct Match!');
	});
}

