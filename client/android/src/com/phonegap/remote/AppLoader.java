package com.phonegap.remote;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
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

import com.byarger.exchangeit.EasySSLSocketFactory;
import com.phonegap.api.Plugin;
import com.phonegap.api.PluginResult;

public class AppLoader extends Plugin {

    private String callback;
    private String local_path;

    @Override
    public PluginResult execute(String action, JSONArray args, String callbackId) {
        this.callback = callbackId;
        if (action.equals("load")) {
            load();
        } else if (action.equals("fetch")) {
            fetch();
        } else if (action.equals("remove")) {
            remove();
        } else {
            return new PluginResult(PluginResult.Status.INVALID_ACTION);
        }
        PluginResult r = new PluginResult(PluginResult.Status.NO_RESULT);
        r.setKeepCallback(true);
        return r;
    }

    // Loads a locally-saved app into the WebView.
    private void load() {
            local_path = "/data/data/" + ctx.getPackageName() + "/remote_app/";
            this.success(new PluginResult(PluginResult.Status.OK, "file://" + local_path + "index.html"), this.callback);
    }

    // Grabs assets off the intarwebz and saves them to a local store/jail for hydration.
    private void fetch() {
        try {
            // Create directory for app.
            local_path = "/data/data/" + ctx.getPackageName() + "/remote_app/";
            File fp = new File(local_path);
            fp.mkdirs();

            if (fetchApp("http://html5expense-assets.cloudfoundry.com/client/app.zip")) {
                this.success(new PluginResult(PluginResult.Status.OK, "file://" + local_path + "index.html"), this.callback);
            } else {
                this.error(new PluginResult(PluginResult.Status.ERROR, "Error during app saving or fetching; protocol or IO error likely."), this.callback);
            }
        } catch (JSONException e) {
            this.error(new PluginResult(PluginResult.Status.JSON_EXCEPTION, "JSON exception during argument parsing"), this.callback);
        }
    }

    // Removes locally-stored app.
    private void remove() {
        local_path = "/data/data/" + ctx.getPackageName() + "/remote_app/";
        deleteDirectory(new File(local_path));
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

    private boolean fetchApp(String url) throws JSONException {
        try {
            HttpResponse response = makeRequest(url);
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
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean saveAndVerify(ZipInputStream data) throws IOException {
        try {
            ZipEntry ze;
            while ((ze = data.getNextEntry()) != null) {
                // Filename + reference to file.
                String filename = ze.getName();
                File output = new File(local_path + filename);

                if (filename.endsWith("/")) {
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
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            data.close();
        }
        return true;
    }

    private HttpResponse makeRequest(String url) throws ClientProtocolException, IOException {
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

        HttpGet httpGet = new HttpGet(url);
        return httpclient.execute(httpGet);
    }
}
