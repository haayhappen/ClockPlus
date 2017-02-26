package com.haayhappen.clockplus.location;

import android.os.AsyncTask;
import android.util.Log;

import com.haayhappen.clockplus.alarms.Alarm;

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

import static com.haayhappen.clockplus.R.string.minute;

/**
 * Created by Fynn on 09.02.2017.
 */

public class DistanceHandler extends AsyncTask<String, Void, Long> {

    private static final String TAG = "DinstanceHandler";
    private static final String API_KEY= "AIzaSyBxeG0NzhUtD3aqIoeNqYX4v1is5L2tOYM";
    private AsyncResponse asyncResponse;
    private String duration = null;
    private String durationInTraffic = null;
    private Long durationLong = null;
    private Long durationInTrafficLong = null;
    private String language = Locale.getDefault().toString();
    private Long delay =0L;
    private Alarm alarm;
    private String departureTime = "";





    private String trafficModel = "best_guess";
    ///

    public interface AsyncResponse {
        void processFinish(long durationInSeconds);
    }

    public DistanceHandler(Alarm alarm, AsyncResponse asyncResponse) {
        this.asyncResponse = asyncResponse;
        this.alarm= alarm;
    }

    @Override
    protected Long doInBackground(String... params) {

        String origin = null;
        String destination = null;

        //get ring time in epoch for api
        Long epoch = alarm.ringsAt()/1000;
        departureTime = epoch.toString();

        try {
            origin = URLEncoder.encode(this.alarm.origin().getLatLng(),"utf-8");
            destination = URLEncoder.encode(this.alarm.destination().getLatLng(),"utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        StringBuilder stringBuilder = new StringBuilder();
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
                    //TODO Philipp : subtract delay time from alarm ringtime, so alarm rings earlier -how the fuck
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
        return delay;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(Long delay) {
        super.onPostExecute(delay);
        Log.d(TAG,"duration calculated: "+ delay);
        asyncResponse.processFinish(delay);
    }
}
