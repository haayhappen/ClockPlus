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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
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
    private Long delay =0L;

    ///for later use:
    //IMPORTANT: departure_time uses EPOCH time format -> use Clock time format and convert to epoch seconds
    //get time from alarm TODO HOW ?
    private String departureTime = "";





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

        //get alarm time converted to epoch which is needed for the api
        departureTime = convertToEpoch("Jun 13 2003 23:11:52.454 UTC");


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

            durationLong = Long.parseLong(jsonDuration.get("value").toString());

            //get duration in traffic
            JSONObject jsonDurationInTraffic = new JSONObject(stringBuilder.toString())
                    .getJSONArray("rows")
                    .getJSONObject(0)
                    .getJSONArray ("elements")
                    .getJSONObject(0)
                    .getJSONObject("duration_in_traffic");

            durationInTrafficLong = Long.parseLong(jsonDurationInTraffic.get("value").toString());

            //Get delay if one exists:
            if (durationInTrafficLong > durationLong){
                try{
                    Long del;
                    del = (durationInTrafficLong-durationLong);
                    //del = del/60;
                    //delay in seconds
                    delay = del;
                }
                catch (NumberFormatException nfe){
                    nfe.printStackTrace();
                }
            }
            else{
                //there is no delay
                delay = Long.parseLong(jsonDuration.get("value").toString());
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return delay.toString();
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(String delay) {
        super.onPostExecute(delay);
        Log.d(TAG,"duration calculated: "+ delay);
        long durationInSeconds=0;
        try{
            durationInSeconds= Long.parseLong(delay);
            if(durationInSeconds==0){
                durationInSeconds++;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        asyncResponse.processFinish(durationInSeconds);
    }

    protected String convertToEpoch(String alarmDate){
        SimpleDateFormat df = new SimpleDateFormat("MMM dd yyyy HH:mm:ss.SSS zzz");
        Date date = null;
        try {
            date = df.parse(alarmDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        String epoch = Long.toString(date.getTime());

        return epoch;
    }
}
