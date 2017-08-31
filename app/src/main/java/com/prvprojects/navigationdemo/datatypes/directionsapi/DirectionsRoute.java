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

}
