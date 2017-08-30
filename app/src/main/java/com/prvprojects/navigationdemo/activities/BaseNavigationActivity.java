package com.prvprojects.navigationdemo.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
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

    // Request codes used by this base class so that child class can be relieved of keeping a track
    // of activity result handling
    private static final int REQUEST_CODE_PLACES_API_FOR_SOURCE = 800;
    private static final int REQUEST_CODE_PLACES_API_FOR_DESTINATION = 820;

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

    /**
     * Requests Places Auto Complete Chooser for given request code
     * @param requestCode
     */
    private void requestPlacesApiChooser(int requestCode) {
        try {
            // The autocomplete activity requires Google Play Services to be available. The intent
            // builder checks this and throws an exception if it is not the case.
            Intent intent = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN)
                    .build(this);
            startActivityForResult(intent, requestCode);
        } catch (GooglePlayServicesRepairableException e) {
            // Indicates that Google Play Services is either not installed or not up to date. Prompt
            // the user to correct the issue.
            GoogleApiAvailability.getInstance().getErrorDialog(this, e.getConnectionStatusCode(),
                    0 /* requestCode */).show();
        } catch (GooglePlayServicesNotAvailableException e) {
            // Indicates that Google Play Services is not available and the problem is not easily
            // resolvable.
            String message = "Google Play Services is not available: " +
                    GoogleApiAvailability.getInstance().getErrorString(e.errorCode);

            Log.e(TAG, message);
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Requests the Places Autocomplete form to fill source location
     */
    protected void requestLocationUserInput_Source() {

        requestPlacesApiChooser(REQUEST_CODE_PLACES_API_FOR_SOURCE);

    }

    /**
     * Requests the Places Autocomplete form to fill destination location
     */
    protected void requestLocationUserInput_Destination() {

        requestPlacesApiChooser(REQUEST_CODE_PLACES_API_FOR_DESTINATION);

    }

    /**
     * A centralized method to abstract away internal code of handling activity results
     * from Navigation Activity
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            // Get the user's selected place from the Intent.
            Place placeSelected = PlaceAutocomplete.getPlace(this, data);
            Log.i(TAG, "Place Selected: " + placeSelected.getName());

            switch (requestCode) {

                case REQUEST_CODE_PLACES_API_FOR_SOURCE:
                    handleLocationSelectedByUser_Source(placeSelected);
                    break;

                case REQUEST_CODE_PLACES_API_FOR_DESTINATION:
                    handleLocationSelectedByUser_Destination(placeSelected);
                    break;
                default:
                    Log.d(TAG, "Invalid request code:"+requestCode);
                    break;

            }

        } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
            Status status = PlaceAutocomplete.getStatus(this, data);
            Log.e(TAG, "Error: Status = " + status.toString());
        } else if (resultCode == RESULT_CANCELED) {
            Log.d(TAG, "User closed activity before making a selection.");
        }

    }

    /**
     * Handle activity result for place selected as source by user
     * @param sourceSelected
     */
    abstract void handleLocationSelectedByUser_Source(Place sourceSelected);

    /**
     * Handle activity result for place selected as destination by user
     * @param destinationSelected
     */
    abstract void handleLocationSelectedByUser_Destination(Place destinationSelected);

    abstract void setupUiViews();

    @Override
    protected void onResume() {
        super.onResume();
        setupUiViews();
    }

}
