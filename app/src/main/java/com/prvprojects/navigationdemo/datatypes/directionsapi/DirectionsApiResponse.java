package com.prvprojects.navigationdemo.datatypes.directionsapi;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Pankaj on 31/08/17.
 */

public class DirectionsApiResponse {

    @SerializedName("geocoded_waypoints")
    DirectionsWayPoint wayPoints[];

    @SerializedName("routes")
    DirectionsRoute routes[];

    @SerializedName("status")
    String status;

    public DirectionsApiResponse(){}

    public DirectionsWayPoint[] getWayPoints() {
        return wayPoints;
    }

    public void setWayPoints(DirectionsWayPoint[] wayPoints) {
        this.wayPoints = wayPoints;
    }

    public DirectionsRoute[] getRoutes() {
        return routes;
    }

    public void setRoutes(DirectionsRoute[] routes) {
        this.routes = routes;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
