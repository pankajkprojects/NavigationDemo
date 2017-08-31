package com.prvprojects.navigationdemo.datatypes.directionsapi;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Pankaj on 31/08/17.
 */

public class DirectionsLatLng {

    double lat;

    double lng;

    public DirectionsLatLng(){}

    public DirectionsLatLng(LatLng latLng) {

        if(latLng!=null) {
            lat = latLng.latitude;
            lng = latLng.longitude;
        }

    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    @Override
    public String toString() {
        return String.valueOf(lat)+","+String.valueOf(lng);
    }
}
