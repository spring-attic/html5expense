(function() {
  if (!('localStorage' in window && window['localStorage'] !== null)) alert("No support for localStorage.");

  // Dom helpers
  $ = function(s) { return document.getElementById(s); }
  function showModal(msg) {
    $('modal').innerHTML = msg || 'Loading';
    var h = window.innerHeight;
    var topOffset = Math.floor(h/2) - 50; // 50 = half the height of modal dialog
    var leftOffset = Math.floor(window.innerWidth/2) - 100; // 100 = half the width of the modal dialog
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
  String.prototype.format = function(){
      var args = arguments;
      obj = (args.length == 1 && (typeof args[0] == 'object')) ? args[0] : args;
      return this.replace(/\{(\w+)\}/g, function(m, i){
          return obj[i];
      });
  }

  function openXHR(url, options, xhr) {
    // differentiate, we need this function because iOS can only handle basic auth credentials in the URL itself
	var isiOS = (navigator.userAgent.match(/iPhone/i) != null) || (navigator.userAgent.match(/iPad/i) != null);
 
	var async = (options && options.async ? options.async : true);
	var username = (options && options.username ? options.username : null );
	var password = (options && options.password ? options.password : null );
	var newUrl = url;
	
	if (isiOS && username && password) {
		var schemeSeparator = "://";
		var extractToIndex = url.indexOf(schemeSeparator) + schemeSeparator.length;
		var scheme = url.substring(0, extractToIndex);
		var rhs = url.substring(extractToIndex);
		
		// re-assemble
		newUrl = scheme + encodeURIComponent(username) + ":" + encodeURIComponent(password) + "@" + rhs;
		username = null;
		password = null;
	}
	
	if (isiOS) {
		// this is important, we can't even pass nulls for username and password on iOS, it hangs
    	xhr.open("GET", newUrl, async);
	} else {
    	xhr.open("GET", newUrl, async, username, password);
	}
  }

  // helper for XHR
  function xhr(url, options) {
    var xhr = new XMLHttpRequest();

	openXHR(url, options, xhr);

    if (options && options.headers) {
      // Lifted from xui source; github.com/xui/xui/blob/master/src/js/xhr.js
      for (key in options.headers) {
          if (options.headers.hasOwnProperty(key)) {
            xhr.setRequestHeader(key, options.headers[key]);
          }
      }
    }

    xhr.setRequestHeader("Accept", "application/json");

    xhr.onreadystatechange = function(){
      if ( xhr.readyState == 4 ) {
        if ( xhr.status == 200 || xhr.status == 0) {
          options.callback.call(xhr);
        } else {
          alert('XHR error, status: ' + xhr.status);
        }
      }
    };
    xhr.send((options && options.data? options.data : null));
  }

  // plugin error handler
  function pluginError(msg) {
    alert('Hydration plugin error!' + msg);
    hideModal();
  }

  // saves app information to localstorage and loads app into current webview
  function saveAppInfoAndLoad(key, id, app) {
    var apps = window.localStorage.getItem('apps');
    if (apps == null) {
      apps = {};
    } else {
      apps = JSON.parse(apps);
    }
    apps['app' + id] = app;
    window.localStorage.setItem('apps', JSON.stringify(apps));
    console.log('loading ' + app.location);
 
	window.plugins.remoteApp.load(function(loc) {
								  window.location = loc;
							   },  pluginError, key, id);
  }

  // loads an app
  loadApp = function(id, username, password) {
    var url = 'https://build.phonegap.com/api/v1/apps/' + id + '/hydrate';
    var apps = window.localStorage.getItem('apps');

    // Check the last updated timestamp on build.
    xhr(url, {
      callback:function() {
        console.log('xhr callback');
        console.log(this);
        console.log(this.responseText);
        eval('var json = ' + this.responseText + ';');
        if (json.error) {
          alert("build.phonegap.com error: " + json.error);
          hideModal();
        } else {
          // We get an S3 url, updated_at time stamp and app title.
          var sthree = json['s3_url'].replace(/&amp;/gi, '&'),
              updatedAt = json['updated_at'],
              title = json['title'],
              key = json['key'];
          
          // save credentials
          saveCredentials();
        
          // Weird JSON.parse error in Android browser: can't parse null, it'll throw an exception.
          if (apps != null) apps = JSON.parse(apps);

          // Check if we've already stored this app.
          if (apps && typeof apps['app' + id] != 'undefined') {
            var app = apps['app' + id];

            // Update its data.
            app.title = title;
            app.username = username;
            app.password = password;
			app.id = id;

            // Check if the app was updated on build.phonegap.com
            if (app.updatedAt != updatedAt) {
              console.log('new version of app, update this shit!');
              app.updatedAt = updatedAt;
              showModal('Downloading application update...');
              window.plugins.remoteApp.fetch(function(loc) {
                console.log('new version app fetch plugin success!');
                app.location = loc;
                saveAppInfoAndLoad(key, id, app);
              }, pluginError, key, id, sthree, null, null);
            } else {
              console.log('same version of app, dont update, just load it');
              showModal('Loading application...');
			  window.plugins.remoteApp.load(function(loc) {
											  window.location = loc;
										},  pluginError, key, id);
            }
          } else {
            // Couldn't find the app in local storage, fetch it yo.
            showModal('Downloading application...');
            console.log('fresh app, fetching it for first time');
            window.plugins.remoteApp.fetch(function(loc) {
              var app = {
                title:title,
                location:loc,
                username:username,
                password:password,
                updatedAt:updatedAt,
                key:key
              };
              console.log('fresh app fetch plugin success!');
              saveAppInfoAndLoad(key, id, app);
            }, pluginError, key, id, sthree, null, null);
          }
        }
      },
      async:true,
      username:username,
      password:password
    });
  }
 
 var saveCredentials = function(){
 try {
    if (!$('rememberme').checked) 
    {
        // clear credentials
        localStorage.removeItem('hydra_id');
        localStorage.removeItem('hydra_username');
        localStorage.removeItem('hydra_password');
        localStorage.removeItem('hydra_rememberme');
 
        return;
     }
 
     // save credentials
     localStorage.hydra_id = $('app_id').value;
     localStorage.hydra_username = $('username').value;
     localStorage.hydra_password = $('password').value;
     localStorage.hydra_rememberme = true;
 } catch (e) {
    alert(e);
 }
 }

 var loadCredentials = function(){
 try {
     if (!(localStorage.hydra_rememberme)) {
        return;
     }
 
     if (localStorage.hydra_id) $('app_id').value = localStorage.hydra_id;
     if (localStorage.hydra_username) $('username').value = localStorage.hydra_username;
     if (localStorage.hydra_password) $('password').value = localStorage.hydra_password;
     $('rememberme').checked = true;
 } catch(e) {
 alert(e);
 }
 }

  // Hydrate action
  hydra = function() {
    var id = $('app_id').value;
    var username = $('username').value;
    var password = $('password').value;

    showModal('Talking to build.phonegap.com...');
    loadApp(id, username, password);
  }
 
  document.addEventListener('deviceready', function() {

    loadCredentials();
                            
    console.log('deviceready');
    document.getElementById('action').style.display = 'block';

    // Load existing apps.
    if (window.localStorage && window.localStorage.getItem('apps')) {
      console.log('loading existing apps into dom');
      var apps = JSON.parse(window.localStorage.getItem('apps')),
          template = '<li><a href="#" onclick="loadApp(\'{appId}\', \'{username}\', \'{password}\');">{name}</a><span>(Last updated at {updatedAt})</span></li>',
          html = [];
      for (var app_id in apps) {
        if (apps.hasOwnProperty(app_id)) {
          var app = apps[app_id];
          if (app.updatedAt && app.location) {
            html.push(template.format({
              appId:app_id.substr(3),
              name:app.title,
              updatedAt:prettyDate(app.updatedAt),
              username:app.username,
              password:app.password
            }));
          }
        }
      }
      if (html.length > 0) {
        var list = $('app_list');
        list.innerHTML = html.join('');
        list.style.display = '';
      }
    }
  }, false);
 
})();
