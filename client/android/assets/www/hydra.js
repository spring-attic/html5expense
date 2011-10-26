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
    
    function setAppInfo(appInfo) {
        if (appInfo != null) {
            localStorage.setItem('AppInfo', JSON.stringify(appInfo));
        }
    }

    function getAppInfo() {
        var t = localStorage.getItem('AppInfo');
        return t && JSON.parse(t);
    }

    // loads an app
    loadApp = function() {
        var url = 'http://html5expense-assets.cloudfoundry.com/version.json';
        
        var appInfo = getAppInfo();

        xhr(url, {
            callback : function() {
                console.log('xhr callback');
                console.log(this);
                console.log(this.responseText);
                eval('var response = ' + this.responseText + ';');
                console.log("local version: " + appInfo.version);
                console.log("remote version: " + response.version);
                var remoteVersion = response.version;
                
                if (remoteVersion.error) {
                    alert("Error determining remote version: " + remoteVersion.error);
                    hideModal();
                } else {
                    
                    // Check if the app is installed
                    if (appInfo.installed == true) {

                        // Check if there is a newer version of the app
                        if (appInfo.version < remoteVersion) {
                            
                            console.log('found new version of app, update it');
                            showModal('Downloading application update...');
                            window.plugins.remoteApp.fetch(function(loc) {
                                console.log('new version app fetch plugin success!');
                                appInfo.version = remoteVersion;
                                setAppInfo(appInfo);
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
                            appInfo.installed = true;
                            setAppInfo(appInfo);
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
        var networkState = detectNetwork();
        if (networkState == Connection.UNKNOWN || networkState == Connection.NONE) {
            alert('No network detected!');
        } else {
            loadApp();
        }
    }
    
    document.addEventListener("deviceready", onDeviceReady, false);
    
    function onDeviceReady() {
        console.log('deviceready');
        
        initializeAppInfo();
        
        document.addEventListener("pause", onPause, false);
        document.addEventListener("resume", onResume, false);
    }
    
    function onPause() {
//        alert('pausing...');
    }
    
    function onResume() {
//        alert('resuming...');
//        window.location = 'index.html';
        hydra();
    }
    
    function initializeAppInfo() {
        var appInfo = getAppInfo();
        if (appInfo == null) {
            console.log("initializing app info");
            appInfo = {
                installed : false,
                version : 0
            };
            setAppInfo(appInfo);
        }
    }
    
    function detectNetwork() {
        // TODO: also check the offline/online events for enabling
        var networkState = navigator.network.connection.type;
        var states = {};
        states[Connection.UNKNOWN] = 'Unknown connection';
        states[Connection.ETHERNET] = 'Ethernet connection';
        states[Connection.WIFI] = 'WiFi connection';
        states[Connection.CELL_2G] = 'Cell 2G connection';
        states[Connection.CELL_3G] = 'Cell 3G connection';
        states[Connection.CELL_4G] = 'Cell 4G connection';
        states[Connection.NONE] = 'No network connection';
        console.log('Connection type: ' + states[networkState]);
        return networkState;
    }

})();


