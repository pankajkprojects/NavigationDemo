package com.prvprojects.navigationdemo.datatypes.directionsapi;

/**
 * Created by Pankaj on 31/08/17.
 */

public class DirectionsWayPoint {

    private DirectionsGeoCoderStatus geoCoderStatus;

    private String placeId;

    // Not implementing place types

    public DirectionsWayPoint(){}

    public DirectionsGeoCoderStatus getGeoCoderStatus() {
        return geoCoderStatus;
    }

    public void setGeoCoderStatus(DirectionsGeoCoderStatus geoCoderStatus) {
        this.geoCoderStatus = geoCoderStatus;
    }

    public String getPlaceId() {
        return placeId;
    }

    public void setPlaceId(String placeId) {
        this.placeId = placeId;
    }
}
