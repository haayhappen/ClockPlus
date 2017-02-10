package com.haayhappen.clockplus.location;

import android.os.AsyncTask;
import android.os.StrictMode;
import android.util.Log;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

/**
 * Created by Fynn on 09.02.2017.
 */

public class DistanceHandler extends AsyncTask<String, Void, String> {

    private static final String TAG = "DinstanceHandler";
    private static final String API_KEY= "AIzaSyBxeG0NzhUtD3aqIoeNqYX4v1is5L2tOYM";
    private AsyncResponse asyncResponse;

    public void setAsyncResponse(AsyncResponse asyncResponse) {
        this.asyncResponse = asyncResponse;
    }

    public interface AsyncResponse {
        void processFinish(String output);
    }
    @Override
    protected String doInBackground(String... params) {

        String originAdress = null;
        try {
            originAdress = URLEncoder.encode(params[0],"utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String destinationAddress = null;
        try {
            destinationAddress = URLEncoder.encode(params[1],"utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        StringBuilder stringBuilder = new StringBuilder();
        String result="";
        String url = "https://maps.googleapis.com/maps/api/distancematrix/json?origins="+originAdress+"&destinations="+destinationAddress+"&key="+API_KEY;


        try {
            URL httppost = new URL(url);
            HttpURLConnection urlConnection = (HttpURLConnection) httppost.openConnection();

            try {
                InputStream stream = urlConnection.getInputStream();
                int status = urlConnection.getResponseCode();

                stringBuilder = new StringBuilder();

                while ((status = stream.read()) != -1) {
                    stringBuilder.append((char) status);
                }
            } finally {
                urlConnection.disconnect();
            }

        }catch (MalformedURLException e) {
            e.printStackTrace();
        }
        catch (IOException ex) {
            ex.printStackTrace();
        }

        try {

            JSONObject jsonRespRouteDuration = new JSONObject(stringBuilder.toString())
                    .getJSONArray("rows")
                    .getJSONObject(0)
                    .getJSONArray ("elements")
                    .getJSONObject(0)
                    .getJSONObject("duration");

            result= jsonRespRouteDuration.get("text").toString();

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        Log.d(TAG,"duration calculated; "+result);
        asyncResponse.processFinish(result);
    }
}
