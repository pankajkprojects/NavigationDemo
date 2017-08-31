package com.prvprojects.navigationdemo.datatypes.directionsapi;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Pankaj on 31/08/17.
 */

public class DirectionsRouteLeg {

    @SerializedName("distance")
    DirectionsLegDistance legDistance;

    @SerializedName("duration")
    DirectionsLegDuration legDuration;

    @SerializedName("end_address")
    String endAddress;

    @SerializedName("end_location")
    DirectionsLatLng endLocation;

    @SerializedName("start_address")
    String startAddress;

    @SerializedName("start_location")
    DirectionsLatLng startLocation;

    @SerializedName("steps")
    DirectionsRouteSteps routeSteps;

    public DirectionsRouteLeg(){}

    public DirectionsLegDistance getLegDistance() {
        return legDistance;
    }

    public void setLegDistance(DirectionsLegDistance legDistance) {
        this.legDistance = legDistance;
    }

    public DirectionsLegDuration getLegDuration() {
        return legDuration;
    }

    public void setLegDuration(DirectionsLegDuration legDuration) {
        this.legDuration = legDuration;
    }

    public String getEndAddress() {
        return endAddress;
    }

    public void setEndAddress(String endAddress) {
        this.endAddress = endAddress;
    }

    public DirectionsLatLng getEndLocation() {
        return endLocation;
    }

    public void setEndLocation(DirectionsLatLng endLocation) {
        this.endLocation = endLocation;
    }

    public String getStartAddress() {
        return startAddress;
    }

    public void setStartAddress(String startAddress) {
        this.startAddress = startAddress;
    }

    public DirectionsLatLng getStartLocation() {
        return startLocation;
    }

    public void setStartLocation(DirectionsLatLng startLocation) {
        this.startLocation = startLocation;
    }

    public DirectionsRouteSteps getRouteSteps() {
        return routeSteps;
    }

    public void setRouteSteps(DirectionsRouteSteps routeSteps) {
        this.routeSteps = routeSteps;
    }
}