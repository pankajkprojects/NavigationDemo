package com.prvprojects.navigationdemo.datatypes.directionsapi;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Pankaj on 31/08/17.
 */

public class DirectionsRoute {

    @SerializedName("bounds")
    DirectionsBounds bounds;

    @SerializedName("copyrights")
    String copyrights;

    @SerializedName("legs")
    DirectionsRouteLeg routeLegs[];

    @SerializedName("overview_polyline")
    DirectionsPolyline overviewPolyline;

    @SerializedName("summary")
    String summary;

    @SerializedName("warnings")
    DirectionsWarnings warnings[];

    @SerializedName("waypoint_order")
    DirectionsWayPointOrder wayPointOrder[];

    public DirectionsBounds getBounds() {
        return bounds;
    }

    public void setBounds(DirectionsBounds bounds) {
        this.bounds = bounds;
    }

    public String getCopyrights() {
        return copyrights;
    }

    public void setCopyrights(String copyrights) {
        this.copyrights = copyrights;
    }

    public DirectionsRouteLeg[] getRouteLegs() {
        return routeLegs;
    }

    public void setRouteLegs(DirectionsRouteLeg[] routeLegs) {
        this.routeLegs = routeLegs;
    }

    public DirectionsPolyline getOverviewPolyline() {
        return overviewPolyline;
    }

    public void setOverviewPolyline(DirectionsPolyline overviewPolyline) {
        this.overviewPolyline = overviewPolyline;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public DirectionsWarnings[] getWarnings() {
        return warnings;
    }

    public void setWarnings(DirectionsWarnings[] warnings) {
        this.warnings = warnings;
    }

    public DirectionsWayPointOrder[] getWayPointOrder() {
        return wayPointOrder;
    }

    public void setWayPointOrder(DirectionsWayPointOrder[] wayPointOrder) {
        this.wayPointOrder = wayPointOrder;
    }
}
