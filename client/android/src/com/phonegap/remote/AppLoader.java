package com.phonegap.remote;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.ProtocolVersion;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerPNames;
import org.apache.http.conn.params.ConnPerRouteBean;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Base64;

import com.byarger.exchangeit.EasySSLSocketFactory;
import com.phonegap.api.Plugin;
import com.phonegap.api.PluginResult;

public class AppLoader extends Plugin {

  private String callback;
  private String local_path;

  @Override
  public PluginResult execute(String action, JSONArray args, String callbackId) {
    this.callback = callbackId;
    if(action.equals("load")) {
      load(args);
    } else if (action.equals("fetch")) {
      fetch(args);
    } else if (action.equals("remove")) {
      remove(args);
    } else {
      return new PluginResult(PluginResult.Status.INVALID_ACTION);
    }
    PluginResult r = new PluginResult(PluginResult.Status.NO_RESULT);
    r.setKeepCallback(true);
    return r;
  }

  // Loads a locally-saved app into the WebView.
  private void load(JSONArray args) {
    try {
      local_path = "/data/data/" + ctx.getPackageName() + "/remote_app/" + args.getString(1) + "/";
      this.success(new PluginResult(PluginResult.Status.OK, "file://" + local_path + "index.html"), this.callback);
    } catch (JSONException e) {
      this.error(new PluginResult(PluginResult.Status.ERROR, "JSON exception during argument parsing; make sure the app ID was passed as an argument."), this.callback);
    }
  }

  // Grabs assets off the intarwebz and saves them to a local store/jail for hydration.
  private void fetch(JSONArray args) {
    String url;
    String username;
    String password;
    String id;
    try {
      id = args.getString(1);
      url = args.getString(2);
      username = args.getString(3);
      password = args.getString(4);
      
      // Create directory for app.
      local_path = "/data/data/" + ctx.getPackageName() + "/remote_app/" + id + "/";
      File fp = new File(local_path);
      fp.mkdirs();
      
      if(fetchApp(url, username, password)) {
        this.success(new PluginResult(PluginResult.Status.OK, "file://" + local_path + "index.html"), this.callback);
      } else {
    	  this.error(new PluginResult(PluginResult.Status.ERROR, "Error during app saving or fetching; protocol or IO error likely."), this.callback);
      }
    } catch (JSONException e) {
      this.error(new PluginResult(PluginResult.Status.JSON_EXCEPTION, "JSON exception during argument parsing; make sure the app ID, URL, username and password were passed as an argument."), this.callback);
    }
  }

  // Removes locally-stored app(s).
  private void remove(JSONArray args) {
    try {
      local_path = "/data/data/" + ctx.getPackageName() + "/remote_app/" + args.getString(1) + "/";
      deleteDirectory(new File(local_path));
    } catch (JSONException e) {
      this.error(new PluginResult(PluginResult.Status.ERROR, "JSON exception during argument parsing; make sure the app ID was passed as an argument."), this.callback);
    }
  }
  private boolean deleteDirectory(File path) {
	  File[] files = path.listFiles();
	  for (int i = 0; i < files.length; i++) {
		  File f = files[i];
		  if (f.isFile()) {
			  if (!f.delete()) {
				  return false;
			  }
		  } else {
			  if (!deleteDirectory(f)) {
				  return false;
			  }
		  }
	  }
	  if (path.delete()) {
		  return true;
	  } else {
		  return false;
	  }
  }
  private boolean fetchApp(String url, String username, String password) throws JSONException 
  {
    try {
    	if (username == "null") {
    		username = null;
    	}
    	if (password == "null") {
    		password = null;
    	}
      HttpResponse response = makeRequest(url, username, password);
      StatusLine sl = response.getStatusLine();
      int code = sl.getStatusCode();
      HttpEntity entity = response.getEntity();
      InputStream content = entity.getContent();
      if (code != 200) {
        return false;
      } else {
        ZipInputStream data = new ZipInputStream(content);
        return saveAndVerify(data);
      }
    } catch (ClientProtocolException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      return false;
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      return false;
    }
  }

  private boolean saveAndVerify(ZipInputStream data) throws IOException  {
    try {
      ZipEntry ze;
      while ((ze = data.getNextEntry()) != null) {
        // Filename + reference to file.
        String filename = ze.getName();
        File output = new File(local_path + filename);

        if(filename.endsWith("/")) {
          output.mkdirs();
        } else {
          if (output.exists()) {
        	  // Delete the file if it already exists.
        	  if (!output.delete()) {
        		  return false;
        	  }
          }
          if (output.createNewFile()) {
            FileOutputStream out = new FileOutputStream(output);
            byte[] buffer = new byte[1024];
            int count;
            while ((count = data.read(buffer)) != -1) {
              out.write(buffer, 0, count);
            }
          } else {
        	  return false;
          }
        }
      }
    } catch(Exception e) {
      e.printStackTrace();
      return false;
    } finally {
      data.close();
    }
    return true; 
  }
  private HttpResponse makeRequest(String url) throws ClientProtocolException, IOException {
	  return makeRequest(url, null, null);
  }
  private HttpResponse makeRequest(String url, String username, String password) throws ClientProtocolException, IOException {
	  SchemeRegistry schemeRegistry = new SchemeRegistry();
    schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
    schemeRegistry.register(new Scheme("https", new EasySSLSocketFactory(), 443));
     
    HttpParams params = new BasicHttpParams();
    params.setParameter(ConnManagerPNames.MAX_TOTAL_CONNECTIONS, 30);
    params.setParameter(ConnManagerPNames.MAX_CONNECTIONS_PER_ROUTE, new ConnPerRouteBean(30));
    params.setParameter(HttpProtocolParams.USE_EXPECT_CONTINUE, false);
    HttpProtocolParams.setVersion(params, new ProtocolVersion("HTTP", 1, 0));
     
    ClientConnectionManager cm = new SingleClientConnManager(params, schemeRegistry);
    DefaultHttpClient httpclient = new DefaultHttpClient(cm, params);
    
    HttpGet httpget = new HttpGet(url);
    if (username != null && password != null) {
      httpget.setHeader("Authorization", "Basic " + Base64.encodeToString((username + ":" + password).getBytes(), Base64.DEFAULT));
    }
    return httpclient.execute(httpget);
  }
}
