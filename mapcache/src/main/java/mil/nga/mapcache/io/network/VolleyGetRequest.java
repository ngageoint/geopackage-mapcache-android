package mil.nga.mapcache.io.network;

import android.util.Base64;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class VolleyGetRequest extends StringRequest {

    public VolleyGetRequest(String url, IResponseHandler handler) {
        super(Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                InputStream targetStream = new ByteArrayInputStream(response.getBytes());
                handler.handleResponse(targetStream, 200);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if(error.networkResponse != null) {
                    handler.handleResponse(null, error.networkResponse.statusCode);
                } else {
                    handler.handleException(new IOException(error.getCause()));
                }
            }
        });
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        HashMap<String, String> params = new HashMap<String, String>();
        String creds = String.format("%s:%s","username","password");
        String auth = "Basic " + Base64.encodeToString(creds.getBytes(), Base64.DEFAULT);
        params.put("Authorization", auth);
        return params;
    }
}
