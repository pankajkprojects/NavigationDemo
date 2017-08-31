package com.prvprojects.navigationdemo.utils;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;

import com.google.maps.android.ui.IconGenerator;
import com.prvprojects.navigationdemo.R;

/**
 * Created by Pankaj on 31/08/17.
 */

public class NavigationMapUtils {

    /**
     * Returns a unique color from color palette based on seed value
     * We need this to get color for printing routes on Google Map
     * 100 colors seem to be sufficient
     * @return
     */
    public static int getUniqueColor(Context context, int seed){

        int[] colorPalette = new int[] {
                ContextCompat.getColor(context, R.color.colorBlue500),
                ContextCompat.getColor(context, R.color.colorTeal500),
                ContextCompat.getColor(context, R.color.colorPurple500),
                ContextCompat.getColor(context, R.color.colorDeepPurple),
                ContextCompat.getColor(context, R.color.colorAmber500),
                ContextCompat.getColor(context, R.color.colorIndigo500),
                ContextCompat.getColor(context, R.color.colorLightBlue500),
                ContextCompat.getColor(context, R.color.colorCyan500),
                ContextCompat.getColor(context, R.color.colorGreen500),
                ContextCompat.getColor(context, R.color.colorLightGreen500),
                ContextCompat.getColor(context, R.color.colorLime500),
                ContextCompat.getColor(context, R.color.colorDeepOrange500),
                ContextCompat.getColor(context, R.color.colorBrown500)
        };

        return colorPalette[seed%colorPalette.length];

    }

    /**
     * Returns color for Source Marker
     * @param context
     * @return
     */
    public static int getUniqueBackgroundColorForSourceMarker(Context context){

        return ContextCompat.getColor(context, R.color.colorBlue800);

    }

    /**
     * Returns text appearance style for Source Marker
     * @return
     */
    public static int getUniqueTextStyleForSourceMarker(){

        //return IconGenerator.STYLE_WHITE;
        return R.style.Marker_Source;

    }

    /**
     * Returns color for Destination Marker
     * @param context
     * @return
     */
    public static int getUniqueBackgroundColorForDestinationMarker(Context context){

        return ContextCompat.getColor(context, R.color.colorCyan800);

    }



    /**
     * Returns text appearance style for Destination Marker
     * @return
     */
    public static int getUniqueTextStyleForDestinationMarker(){

        return R.style.Marker_Destination;

    }

}
