package com.haayhappen.clockplus.location;

/**
 * Created by Philipp on 10.02.2017.
 */

public class Location {

    private String adress;
    private double latitude;
    private double longitude;

    public Location(String adress, double latitude, double longitude) {
        this.adress = adress;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getAdress() {
        return adress;
    }

    public void setAdress(String adress) {
        this.adress = adress;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getLatLng(){
        return latitude+","+longitude;
    }
}
