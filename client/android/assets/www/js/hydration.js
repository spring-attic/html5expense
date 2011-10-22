/* iOS PhoneGap does not have this function*/
if (!PhoneGap.addPlugin) {
	if (!window.plugins) {
	    window.plugins = {};
	}	
	
	PhoneGap.addPlugin = function(name, obj) {
	    if (!window.plugins[name]) {
	        window.plugins[name] = obj;
	    }
	    else {
	        console.log("Error: Plugin "+name+" already exists.");
	    }
	};
}

var Hydration = function() {}

Hydration.prototype = {
	load:function(win, fail, key, id){
	    return PhoneGap.exec(win, fail, 'AppLoader', 'load', [ id ]);
	},
	fetch:function(win, fail, key, id, url, username, password){
	    return PhoneGap.exec(win, fail, 'AppLoader', 'fetch', [ id, url, username, password ]);
	},
	remove:function(win, fail, key, id){
	    return PhoneGap.exec(win, fail, 'AppLoader', 'remove', [ id ]);
	}
}

PhoneGap.addConstructor(function() {
  	PhoneGap.addPlugin('remoteApp', new Hydration());
  var name = 'AppLoader',
      package = 'com.phonegap.remote.AppLoader';

  // we really need to fix this plugin shit man
  if (navigator && navigator.app && typeof navigator.app.addService != 'undefined') {
    navigator.app.addService(name, package);
  } else if (window.phonegap){
    phonegap.PluginManager.addPlugin(name, package);
  }
});
