package com.haayhappen.clockplus.location;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Fynn on 09.02.2017.
 */

public class DistanceHandler {

    private static final String API_KEY= "AIzaSyBxeG0NzhUtD3aqIoeNqYX4v1is5L2tOYM";

    public static String getDistanceInfo(String originAdress, String destinationAddress) {
        StringBuilder stringBuilder = new StringBuilder();
        String dist="";
        try {
            //            https://maps.googleapis.com/maps/api/distancematrix/json?origins=${origin}&destinations=${destination}&mode=${mode}&departure_time=${departure_time}&traffic_model=${traffic_model}&key=${key}
            //            https://maps.googleapis.com/maps/api/distancematrix/json?origins=Vancouver+BC|Seattle&destinations=San+Francisco|Victoria+BC&mode=bicycling&language=fr-FR
            String url = "https://maps.googleapis.com/maps/api/distancematrix/json?origin=" + originAdress + "&destination=" + destinationAddress + "&mode=driving" +"&key="+API_KEY;
            URL httppost = new URL(url);
            HttpURLConnection urlConnection = (HttpURLConnection) httppost.openConnection();
            InputStream stream = urlConnection.getInputStream();
            int status = urlConnection.getResponseCode();

            stringBuilder = new StringBuilder();

            while ((status = stream.read()) != -1) {
                stringBuilder.append((char) status);
            }

        } catch (ClientProtocolException e) {
        } catch (IOException e) {
        }

        JSONObject jsonObject = new JSONObject();
        try {


            jsonObject = new JSONObject(stringBuilder.toString());

            JSONArray array = jsonObject.getJSONArray("routes");

            JSONObject routes = array.getJSONObject(0);

            JSONArray legs = routes.getJSONArray("legs");

            JSONObject steps = legs.getJSONObject(0);
            JSONObject duration = steps.getJSONObject("duration");

            dist = duration.getString("text");

        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return dist;
    }
}
