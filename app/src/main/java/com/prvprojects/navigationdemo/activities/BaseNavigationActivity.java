package com.prvprojects.navigationdemo.activities;

import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.prvprojects.navigationdemo.R;

/**
 * Base class used to segregate some basic code used in Navigation Activity
 * Created by Pankaj on 31/08/17.
 */

public abstract class BaseNavigationActivity extends AppCompatActivity {

    private static String TAG = null;
    private GoogleMap googleMap = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TAG = getLogTag();

        if(TAG==null) {
            Log.e(TAG, getString(R.string.error_log_logtag_not_set));
            throw new RuntimeException(getString(R.string.error_log_logtag_not_set));
        }

        Log.d(TAG, getString(R.string.debug_log_logtag_set, TAG));

        loadActivityLayout();
        loadGoogleMapFragment();
    }

    /**
     * Method to check if the essential objects have been initialized or not
     * Just a check.
     */
    private void checkEssentials(){
        if(this.googleMap==null) {
            Log.e(TAG, getString(R.string.error_log_googlemap_null));
            throw new RuntimeException(getString(R.string.error_log_googlemap_null));
        }
        Log.e(TAG, getString(R.string.debug_log_googlemap_initialized));
    }

    abstract void loadActivityLayout();

    abstract void loadGoogleMapFragment();

    protected void setGoogleMap(GoogleMap googleMap) {
        this.googleMap = googleMap;
    }

    /**
    * Ths activity extenting this activity shall implement this method
    * @return Log tag to be used by the base activity
    */
    abstract String getLogTag();

}
