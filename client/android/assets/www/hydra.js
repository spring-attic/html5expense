(function() {
    if (!('localStorage' in window && window['localStorage'] !== null))
        alert("No support for localStorage.");
  
    // Dom helpers
    $ = function(s) {
        return document.getElementById(s);
    }
    function showModal(msg) {
        $('modal').innerHTML = msg || 'Loading';
        var h = window.innerHeight;
        var topOffset = Math.floor(h / 2) - 50; // 50 = half the height of modal dialog
        var leftOffset = Math.floor(window.innerWidth / 2) - 100; // 100 = half the width of the modal dialog
        document.body.style.height = h + 'px';
        document.body.style.overflow = 'hidden';
        var m = $('modal');
        m.style.top = topOffset + 'px';
        m.style.left = leftOffset + 'px';
        m.style.display = '';
        $('backdrop').style.display = '';
    }
    function hideModal() {
        document.body.style.height = '';
        document.body.style.overflow = '';
        $('backdrop').style.display = 'none';
        $('modal').style.display = 'none';
    }

    // Extend the String object for simple templating
    String.prototype.format = function() {
        var args = arguments;
        obj = (args.length == 1 && (typeof args[0] == 'object')) ? args[0] : args;
        return this.replace(/\{(\w+)\}/g, function(m, i) {
            return obj[i];
        });
    }

    // helper for XHR
    function xhr(url, options) {
        var xhr = new XMLHttpRequest();
        var async = (options && options.async ? options.async : true);

        console.log('xhr url: ' + url);
        console.log('xhr options: ' + options);
                
        xhr.open("GET", url, async);

        if (options && options.headers) {
            // Lifted from xui source; github.com/xui/xui/blob/master/src/js/xhr.js
            for (key in options.headers) {
                if (options.headers.hasOwnProperty(key)) {
                    xhr.setRequestHeader(key, options.headers[key]);
                }
            }
        }

        xhr.setRequestHeader("Accept", "application/json");

        xhr.onreadystatechange = function() {
            if (xhr.readyState == 4) {
                if (xhr.status == 200 || xhr.status == 0) {
                    options.callback.call(xhr);
                } else {
                    alert('XHR error, status: ' + xhr.status);
                }
            }
        };
        xhr.send((options && options.data ? options.data : null));
    }

    // plugin error handler
    function pluginError(msg) {
        alert('Hydration plugin error!' + msg);
        hideModal();
    }

    // loads an app
    loadApp = function() {
        var url = 'http://html5expense-assets.cloudfoundry.com/version.json';
        var installed = window.localStorage.getItem('installed');
        var localVersion = window.localStorage.getItem('version');

        xhr(url, {
            callback : function() {
                console.log('xhr callback');
                console.log(this);
                console.log(this.responseText);
                eval('var response = ' + this.responseText + ';');
                console.log("local version: " + localVersion);
                console.log("remote version: " + response.version);
                var version = response.version;
                
                if (version.error) {
                    alert("html5expense-assets.cloudfoundry.com error: " + version.error);
                    hideModal();
                } else {
                    
                    // Check if the app is installed
                    if (installed == true) {

                        // Check if there is a newer version of the app
                        if (localVersion < version) {
                            
                            console.log('found new version of app, update it');
                            showModal('Downloading application update...');
                            window.plugins.remoteApp.fetch(function(loc) {
                                console.log('new version app fetch plugin success!');
                                window.localStorage.setItem('version', version);
                                window.plugins.remoteApp.load(function(loc) {
                                    console.log('app load plugin success!');
                                    window.location = loc;
                                }, pluginError);
                            }, pluginError);
                        } else {
                            console.log('same version of app, dont update, just load it');
                            showModal('Loading application...');
                            window.plugins.remoteApp.load(function(loc) {
                                console.log('app load plugin success!');
                                window.location = loc;
                            }, pluginError);
                        }
                    } else {
                        // Couldn't find the app in local storage, so fetch it.
                        showModal('Downloading application...');
                        console.log('fetching app for first time');
                        window.plugins.remoteApp.fetch(function(loc) {
                            console.log('app fetch plugin success!');
                            window.localStorage.setItem('installed', true);
                            window.plugins.remoteApp.load(function(loc) {
                                console.log('app load plugin success!');
                                window.location = loc;
                            }, pluginError);
                        }, pluginError);
                    }
                }
            },
            async : true
        });
    }

    hydra = function() {
        showModal('Talking to html5expense-assets.cloudfoundry.com...');
        loadApp();
    }

    document.addEventListener('deviceready', function() {

        console.log('deviceready');
        document.getElementById('action').style.display = 'block';
        
        if (window.localStorage && window.localStorage.getItem('installed')) {
            $('app_list').innerHTML = '<li><a href="#" onclick="loadApp();">Load App</a></li>';
            list.style.display = '';
        }
    }, false);

})();


// ***************************************
// Home
// ***************************************

$('#hydra-home').live('pageshow', function(event) {

    var content = '';
    if (window.localStorage && window.localStorage.getItem('installed')) {
        content += '<li><a href="#create-new-purpose">Run App</a></li>';
    } else {
        content += '<li><a href="#sign-in">Install</a></li>';
    }

    $('#hydra-home-menu-items').html(content).listview('refresh');
});


