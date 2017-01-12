package edu.vt.scm.groupsafe;

import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.util.HashMap;
import java.util.Map;

public class NetWorker {

    public interface VolleyCallback {
        void onSuccess(String result);
        void onFailure(String result);
    }

    private static RequestQueue RequestQueue;
    private static NetWorker sInstance = null;
    private static CookieManager cookieManager; //uses HTTPUrlConnection for Androids post 2010

    //private constructor-  use getSInstance for singleton behavior
    private NetWorker() {
        cookieManager = new CookieManager();
        CookieHandler.setDefault(cookieManager);
        RequestQueue = Volley.newRequestQueue(MyApplication.getAppContext());
    }


    public static NetWorker getSInstance() {
        if (sInstance == null)
        {
            sInstance = new NetWorker();
            Log.d("singleton", "making singleton");
        }
        return sInstance;
    }

    public void get(String url, final VolleyCallback callback) {

        StringRequest strReq = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        Log.d("RESPONSE_nw", "nw got the response");
                        callback.onSuccess(response);
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("RESPONSE_nw", "pickles!!!!!!");
                callback.onFailure(error.toString());
            }
        });
        RequestQueue.add(strReq);
    }

    public void put(String url, final Map<String, String> params,
                    final VolleyCallback callback) {

        StringRequest strReq = new StringRequest(Request.Method.PUT,
                url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("RESPONSE_nw", "the post response is " + response);
                callback.onSuccess(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                callback.onFailure(error.toString());
            }
        })
        {
            @Override
            protected Map<String, String> getParams()
            {
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError
            {
                Map<String,String> params = new HashMap<String, String>();
                params.put("Content-Type","application/x-www-form-urlencoded");
                return params;
            }
        };
        RequestQueue.add(strReq);
    }

    public void post(String url, final Map<String,
            String> params, final VolleyCallback callback) {

        StringRequest strReq = new StringRequest(Request.Method.POST,
                url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("RESPONSE_nw", "the post response is " + response);
                callback.onSuccess(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                callback.onFailure(error.toString());
            }
        })
        {
            @Override
            protected Map<String, String> getParams()
            {
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError
            {
                Map<String,String> params = new HashMap<String, String>();
                params.put("Content-Type","application/x-www-form-urlencoded");
                return params;
            }
        };
        RequestQueue.add(strReq);
    }

    public void delete(String url, final VolleyCallback callback) {

        StringRequest strReq = new StringRequest(Request.Method.DELETE, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        Log.d("RESPONSE_nw", "nw got the response");
                        callback.onSuccess(response);
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {

                Log.d("RESPONSE_nw", "pickles!!!!!!");
                callback.onFailure(error.toString());
            }
        });
        RequestQueue.add(strReq);
    }
}
