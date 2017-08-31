package com.prvprojects.navigationdemo.datatypes.directionsapi;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Pankaj on 31/08/17.
 */

public class DirectionsRouteSteps {

    @SerializedName("distance")
    DirectionsLegDistance stepDistance;

    @SerializedName("duration")
    DirectionsLegDistance stepDuration;

    @SerializedName("end_location")
    DirectionsLatLng end_location;

    @SerializedName("html_instructions")
    String htmlInstructions;

    @SerializedName("maneuver")
    String maneuver;

    @SerializedName("polyline")
    DirectionsPolyline polyline;

    @SerializedName("start_location")
    DirectionsLatLng start_location;

    @SerializedName("travel_mode")
    String travelMode;

    public DirectionsRouteSteps(){}

    public DirectionsLegDistance getStepDistance() {
        return stepDistance;
    }

    public void setStepDistance(DirectionsLegDistance stepDistance) {
        this.stepDistance = stepDistance;
    }

    public DirectionsLegDistance getStepDuration() {
        return stepDuration;
    }

    public void setStepDuration(DirectionsLegDistance stepDuration) {
        this.stepDuration = stepDuration;
    }

    public DirectionsLatLng getEnd_location() {
        return end_location;
    }

    public void setEnd_location(DirectionsLatLng end_location) {
        this.end_location = end_location;
    }

    public String getHtmlInstructions() {
        return htmlInstructions;
    }

    public void setHtmlInstructions(String htmlInstructions) {
        this.htmlInstructions = htmlInstructions;
    }

    public String getManeuver() {
        return maneuver;
    }

    public void setManeuver(String maneuver) {
        this.maneuver = maneuver;
    }

    public DirectionsPolyline getPolyline() {
        return polyline;
    }

    public void setPolyline(DirectionsPolyline polyline) {
        this.polyline = polyline;
    }

    public DirectionsLatLng getStart_location() {
        return start_location;
    }

    public void setStart_location(DirectionsLatLng start_location) {
        this.start_location = start_location;
    }

    public String getTravelMode() {
        return travelMode;
    }

    public void setTravelMode(String travelMode) {
        this.travelMode = travelMode;
    }
}
