package mil.nga.mapcache.utils;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.ArrayAdapter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.common.api.Response;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataInput;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import mil.nga.mapcache.R;
import mil.nga.mapcache.io.network.HttpClient;
import mil.nga.mapcache.io.network.IResponseHandler;
import mil.nga.mapcache.layersprovider.LayerModel;
import mil.nga.mapcache.layersprovider.LayersProvider;

/**
 * Downloads sample files in JSON format into hashmaps.  Used for sample tile urls and geopackages,
 * retreived from mapcache github
 */
public class SampleDownloader implements IResponseHandler {

    /**
     * The activity used to get back on the main thread.
     */
    private final Activity activity;

    /**
     * True if the retrieve layers step was cancelled by the user.
     */
    private boolean isCancelled = false;

    /**
     * This array adapter should be populated with the results
     */
    ArrayAdapter<String> adapter;

    /**
     * Hashmap to hold results from the download request
     */
    HashMap<String, String> sampleList = new HashMap<>();


    public SampleDownloader(Activity activity, ArrayAdapter<String> adapter) {
        this.activity = activity;
        this.adapter = adapter;
    }

    /**
     * Make a request with the given url to download sample data
     * @param url
     */
    public void getExampleData(String url){
        // Get our sample data from github
        HttpClient.getInstance().sendGet(url, this, this.activity);
    }

    /**
     * Provide a way to cancel in case it's taking too long
     */
    public void cancel() {
        isCancelled = true;
    }

    /**
     * Response handler to parse the json data and populate the adapter
     * @param stream       The response from the server, or null if bad response from server.
     * @param responseCode The http response code from the server.
     */
    @Override
    public void handleResponse(InputStream stream, int responseCode) {
        if (!isCancelled) {
                try {
                    if (stream != null && responseCode == HttpURLConnection.HTTP_OK) {

                        BufferedReader br = new BufferedReader(new InputStreamReader(stream));
                        StringBuilder sb = new StringBuilder();
                        String line;
                        while ((line = br.readLine()) != null) {
                            sb.append(line + "\n");
                        }
                        br.close();
                        JSONObject mainObject = new JSONObject(sb.toString());
                        sampleList.putAll(new Gson().fromJson(sb.toString(), HashMap.class));
                        activity.runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                adapter.addAll(sampleList.keySet());

                            }
                        });
                    }
                } catch (Exception e){
                    Log.e("error", e.toString());
                }

        }



    }

    @Override
    public void handleException(IOException exception) {
        Log.e(SampleDownloader.class.getSimpleName(), "Failed to get sample data: ", exception);
    }

    @Override
    public boolean notCancelled() {
        return false;
    }

    public HashMap<String, String> getSampleList() {
        return sampleList;
    }

    public void setSampleList(HashMap<String, String> sampleList) {
        this.sampleList = sampleList;
    }

    /**
     * Pulls our local geopackage example urls
     */
    public void loadLocalGeoPackageSamples(){
        // Get our local sample data
        HashMap<String, String> map = new HashMap<>();

        // Pull local string resources, shipped with the app
        String[] labels = activity.getResources()
                .getStringArray(
                        R.array.preloaded_geopackage_url_labels);
        String[] urls = activity.getResources()
                .getStringArray(
                        R.array.preloaded_geopackage_urls);
        for(int i=0; i<labels.length; i++){
            sampleList.put(labels[i], urls[i]);
        }
    }
}
