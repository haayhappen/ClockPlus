package com.haayhappen.clockplus.location;

import android.os.AsyncTask;
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
import java.util.Locale;

/**
 * Created by Fynn on 09.02.2017.
 */

public class DistanceHandler extends AsyncTask<String, Void, String> {

    private static final String TAG = "DinstanceHandler";
    private static final String API_KEY= "AIzaSyBxeG0NzhUtD3aqIoeNqYX4v1is5L2tOYM";
    private AsyncResponse asyncResponse;
    private Location origin;
    private Location destination;
    private String duration = null;
    private String durationInTraffic = null;
    private Long durationLong = null;
    private Long durationInTrafficLong = null;
    private String language = Locale.getDefault().toString();
    private String delay ="";

    ///for later use:
    //IMPORTANT: departure_time uses EPOCH time format -> use Clock time format and convert to epoch seconds
    private String departureTime = "now";
    private String trafficModel = "best_guess";
    ///

    public interface AsyncResponse {
        void processFinish(long durationInSeconds);
    }

    public DistanceHandler(Location origin, Location destination,AsyncResponse asyncResponse) {
        this.asyncResponse = asyncResponse;
        this.origin = origin;
        this.destination=destination;
    }

    @Override
    protected String doInBackground(String... params) {

        String origin = null;
        String destination = null;


        try {
            origin = URLEncoder.encode(this.origin.getLatLng(),"utf-8");
            destination = URLEncoder.encode(this.destination.getLatLng(),"utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        StringBuilder stringBuilder = new StringBuilder();



        //DEPRECATED Nur Duration
        //String url = "https://maps.googleapis.com/maps/api/distancematrix/json?origins="+origin+"&destinations="+destination+"&key="+API_KEY;

        //Get Duration and duration in Traffic
        //TODO set the departure time related to how early the alarm will be triggered
                    //https://maps.googleapis.com/maps/api/distancematrix/json?origins=${origins}/&destinations=${destinations}&mode=driving&departure_time=now&traffic_model=best_guess&language=de-DE&key=AIzaSyBxeG0NzhUtD3aqIoeNqYX4v1is5L2tOYM
        String url = "https://maps.googleapis.com/maps/api/distancematrix/json?origins="+origin+"&destinations="+destination+"&mode=driving&departure_time="+departureTime+"&traffic_model="+trafficModel+"&language="+language+"&key="+API_KEY;


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

            //get normal duration
            JSONObject jsonDuration = new JSONObject(stringBuilder.toString())
                    .getJSONArray("rows")
                    .getJSONObject(0)
                    .getJSONArray ("elements")
                    .getJSONObject(0)
                    .getJSONObject("duration");

            durationLong = (Long)jsonDuration.get("value");

            //get duration in traffic
            JSONObject jsonDurationInTraffic = new JSONObject(stringBuilder.toString())
                    .getJSONArray("rows")
                    .getJSONObject(0)
                    .getJSONArray ("elements")
                    .getJSONObject(0)
                    .getJSONObject("duration_in_traffic");

            durationInTrafficLong = (Long)jsonDurationInTraffic.get("value");

            //Get delay if one exists:
            if (durationInTrafficLong > durationLong){
                try{
                    Long del;
                    del = (durationInTrafficLong-durationLong);
                    del = del/60;
                    //delay in minutes
                    delay = del+" Minutes delay";
                }
                catch (NumberFormatException nfe){
                    nfe.printStackTrace();
                }
            }
            else{
                //there is no delay
                delay = jsonDuration.get("value").toString();
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return delay;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        Log.d(TAG,"duration calculated; "+result);
        long durationInSeconds=0;
        try{
            durationInSeconds= Long.parseLong(result);
            if(durationInSeconds==0){
                durationInSeconds++;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        asyncResponse.processFinish(durationInSeconds);
    }
}
