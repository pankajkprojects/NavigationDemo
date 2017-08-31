package com.prvprojects.navigationdemo.datatypes.directionsapi;

import android.app.ActivityManager;
import android.content.Context;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.prvprojects.navigationdemo.R;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Pankaj on 31/08/17.
 */

public class DirectionsApiRequest {

    private static final String TAG = "DirectionsAPIReq";

    /**
     * returns api request url for given lat long
     * @param source
     * @param destination
     * @return
     */
    public static final URL getDirectionsApiRequestURL(Context context, LatLng source, LatLng destination) {

        if(context==null || source==null || destination==null) {
            Log.e(TAG, "Please provide non null parameters to generate Directions API URL");
            throw new RuntimeException("Please provide non null parameters to generate Directions API URL");
        }
        StringBuilder stringBuilder = new StringBuilder("https://maps.googleapis.com/maps/api/directions/json?");
        stringBuilder.append("origin=");
        stringBuilder.append(source.latitude);
        stringBuilder.append(',');
        stringBuilder.append(source.longitude);
        stringBuilder.append("&");
        stringBuilder.append("destination=");
        stringBuilder.append(destination.latitude);
        stringBuilder.append(',');
        stringBuilder.append(destination.longitude);
        stringBuilder.append("&");
        stringBuilder.append("key=");
        stringBuilder.append(context.getString(R.string.key_google_maps));

        Log.d(TAG, "URL for Directions API request:"+stringBuilder.toString());
        try {
            return new URL(stringBuilder.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
            Log.e(TAG, "URL for Directions API request:null");
            return null;
        }

    }

}
