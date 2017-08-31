package com.prvprojects.navigationdemo.datatypes.directionsapi;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Pankaj on 31/08/17.
 */

public class DirectionsBounds {

    @SerializedName("northeast")
    DirectionsLatLng northEast;

    @SerializedName("southwest")
    DirectionsLatLng southWest;

}
