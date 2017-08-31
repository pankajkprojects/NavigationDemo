package com.prvprojects.navigationdemo.datatypes.directionsapi;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Pankaj on 31/08/17.
 */

public class DirectionsBounds {

    @SerializedName("northeast")
    DirectionsLatLng northEast;

    @SerializedName("southwest")
    DirectionsLatLng southWest;

    public DirectionsLatLng getNorthEast() {
        return northEast;
    }

    public void setNorthEast(DirectionsLatLng northEast) {
        this.northEast = northEast;
    }

    public DirectionsLatLng getSouthWest() {
        return southWest;
    }

    public void setSouthWest(DirectionsLatLng southWest) {
        this.southWest = southWest;
    }

    public LatLng getNorthEastLatLng(){
        return new LatLng(northEast.getLat(), northEast.getLng());
    }

    public LatLng getSouthWestLatLng(){
        return new LatLng(southWest.getLat(), southWest.getLng());
    }

}
