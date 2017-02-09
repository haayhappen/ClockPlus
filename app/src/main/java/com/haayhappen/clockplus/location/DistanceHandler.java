package com.haayhappen.clockplus.location;

import android.os.StrictMode;
import android.util.Log;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Fynn on 09.02.2017.
 */

public class DistanceHandler {

    private static final String TAG = "DinstanceHandler";
    private static final String API_KEY= "AIzaSyBxeG0NzhUtD3aqIoeNqYX4v1is5L2tOYM";

    public static String getDistanceInfo(String originAdress, String destinationAddress) {

        StringBuilder stringBuilder = new StringBuilder();
        String dist="";
        String url = "https://maps.googleapis.com/maps/api/distancematrix/json?origins="+originAdress+"&destinations="+destinationAddress+"&key="+API_KEY;

        //TODO REALLY BAD PRACTISE to do network operations on main ui thread
        //TODO add asynk task! and remove the two lines below after
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
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

            dist= jsonRespRouteDuration.get("text").toString();

        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return dist;
    }
}
