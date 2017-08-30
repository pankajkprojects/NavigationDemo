package com.prvprojects.navigationdemo.activities;

import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.TextView;

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
        setupActionbar(getString(R.string.app_name), true);
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

    abstract Toolbar getActivityToolBar();

    abstract TextView getActivityToolbarTitle();

    /**
     * Sets up action bar for current activity
     * @param activityTitle
     * @param showIcon
     */
    protected void setupActionbar(@NonNull String activityTitle, boolean showIcon){

        try {
            Toolbar toolbar = getActivityToolBar();
            if(toolbar==null) {
                String message = "Please set the id of Toolbar.";
                Log.e(TAG, message);
                throw new IllegalStateException(message);
            }

            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            toolbar.setContentInsetsAbsolute(0,0);
            if(showIcon) {
                getSupportActionBar().setIcon(R.mipmap.ic_launcher);
                Log.d(TAG, "launcher icon set in toolbar");
            }
            else {
                //activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                //activity.getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close);
            }
            TextView toolbar_title = getActivityToolbarTitle();

            if(activityTitle!=null && activityTitle.length()>0 && toolbar_title!=null) {
                toolbar_title.setText(activityTitle);
                Log.d(TAG, "app title set in toolbar");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.w(TAG, "Error in setting up the action bar");
        }

    }

}
