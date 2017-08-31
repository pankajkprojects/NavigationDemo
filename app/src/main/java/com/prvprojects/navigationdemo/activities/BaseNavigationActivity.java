package com.prvprojects.navigationdemo.activities;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.maps.android.PolyUtil;
import com.google.maps.android.SphericalUtil;
import com.google.maps.android.ui.IconGenerator;
import com.prvprojects.navigationdemo.R;
import com.prvprojects.navigationdemo.datatypes.NavigationData;
import com.prvprojects.navigationdemo.datatypes.directionsapi.DirectionsApiResponse;
import com.prvprojects.navigationdemo.datatypes.directionsapi.DirectionsBounds;
import com.prvprojects.navigationdemo.datatypes.directionsapi.DirectionsLatLng;
import com.prvprojects.navigationdemo.datatypes.directionsapi.DirectionsPolyline;
import com.prvprojects.navigationdemo.datatypes.directionsapi.DirectionsRoute;
import com.prvprojects.navigationdemo.datatypes.directionsapi.DirectionsRouteLeg;
import com.prvprojects.navigationdemo.datatypes.directionsapi.DirectionsRouteSteps;
import com.prvprojects.navigationdemo.retrofit.ApiClient;
import com.prvprojects.navigationdemo.retrofit.ApiInterface;
import com.prvprojects.navigationdemo.utils.NavigationMapUtils;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.graphics.drawable.GradientDrawable.LINE;

/**
 * Base class used to segregate some basic code used in Navigation Activity
 * Created by Pankaj on 31/08/17.
 */

public abstract class BaseNavigationActivity extends AppCompatActivity {

    // Represents the Tag used in Log statements
    private static String TAG = null;

    // Represents the current Google Map object loaded on screen
    private GoogleMap googleMap = null;

    // Represents the current navigation related data in activity
    private NavigationData mNavigationData;

    // Represents currently displayed directions api response.
    private DirectionsApiResponse mDirectionsApiResponse;

    // Used to keep track of map bounds we have to show to user after a Directions API Query
    private LatLngBounds mapBounds;

    ApiInterface apiService;

    // Request codes used by this base class so that child class can be relieved of keeping a track
    // of activity result handling
    private static final int REQUEST_CODE_PLACES_API_FOR_SOURCE = 800;
    private static final int REQUEST_CODE_PLACES_API_FOR_DESTINATION = 820;
    private static final int REQUEST_CODE_REQUEST_PERMISSION_FINE_LOCATION = 830;

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
        setupActionbar(getString(R.string.app_name), false);
        initializeNavigationData();
        loadGoogleMapFragment();


//        // Code to print routes and set up UI using string data without making Google API Calls
//        // To reduce API quota usage during development
//        Handler handler = new Handler();
//        handler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                mDirectionsApiResponse = getDirectionsAPIResponse();
//                handleDirectionsApiResponse(mDirectionsApiResponse);
//
//            }
//        }, 3000);

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

    protected GoogleMap getGoogleMap(){return this.googleMap;}

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

    private void initializeNavigationData(){
        mNavigationData = new NavigationData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // To check play services are available in device or not
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getApplicationContext());

        if (resultCode == ConnectionResult.SUCCESS){
            ;// Nothing required
        }else{
            Toast.makeText(BaseNavigationActivity.this, "Play Services are not available, app may crash.", Toast.LENGTH_SHORT).show();
        }
        setupUiViews();
    }

    protected NavigationData getmNavigationData(){
        return mNavigationData;
    }

    protected abstract void displayRouteDirections(String htmlEncodedDirections);

    /**
     * Makes an HTTP request to fetch routes from Google API using
     * @param source
     * @param destination
     */
    protected void fetchRoutesFromGoogle(LatLng source, LatLng destination){

        Log.d(TAG, "User has selected Source and Destination Locations. " +
                "Please fetch routes from Google API");

        apiService = ApiClient.getClient().create(ApiInterface.class);

        Log.d(TAG, "API Making request at URL: "+apiService.getDirections(new DirectionsLatLng(source), new DirectionsLatLng(destination), getString(R.string.key_google_maps)).request().url().toString());

        Call<DirectionsApiResponse> call = apiService.getDirections(new DirectionsLatLng(source), new DirectionsLatLng(destination), getString(R.string.key_google_maps));

        call.enqueue(new Callback<DirectionsApiResponse>() {


            @Override
            public void onResponse(Call<DirectionsApiResponse> call, Response<DirectionsApiResponse> response) {
                String status = response.body().getStatus();
                handleDirectionsApiResponse(response.body());
                Log.d(TAG, "Status Received: "+status);
                Log.w("JSON Response => ",new GsonBuilder().setPrettyPrinting().create().toJson(response));
            }

            @Override
            public void onFailure(Call<DirectionsApiResponse> call, Throwable t) {
                Log.d(TAG, "Error received on Directions API response.");
                t.printStackTrace();
            }


        });

    }

    /**
     * Dummy method to parse API Response from a string
     * @return
     */
    protected DirectionsApiResponse getDirectionsAPIResponse(){

        //<editor-fold desc="Api Response">
        String response = "{\n" +
                "   \"geocoded_waypoints\" : [\n" +
                "      {\n" +
                "         \"geocoder_status\" : \"OK\",\n" +
                "         \"place_id\" : \"ChIJG4Lch82ZyzsRInxVkHp6LHU\",\n" +
                "         \"types\" : [ \"premise\" ]\n" +
                "      },\n" +
                "      {\n" +
                "         \"geocoder_status\" : \"OK\",\n" +
                "         \"place_id\" : \"ChIJ9-CdPncWrjsRqTc5lcq2_Pw\",\n" +
                "         \"types\" : [ \"premise\" ]\n" +
                "      }\n" +
                "   ],\n" +
                "   \"routes\" : [\n" +
                "      {\n" +
                "         \"bounds\" : {\n" +
                "            \"northeast\" : {\n" +
                "               \"lat\" : 17.4042258,\n" +
                "               \"lng\" : 78.48665539999999\n" +
                "            },\n" +
                "            \"southwest\" : {\n" +
                "               \"lat\" : 12.9716014,\n" +
                "               \"lng\" : 77.5811703\n" +
                "            }\n" +
                "         },\n" +
                "         \"copyrights\" : \"Map data ©2017 Google\",\n" +
                "         \"legs\" : [\n" +
                "            {\n" +
                "               \"distance\" : {\n" +
                "                  \"text\" : \"576 km\",\n" +
                "                  \"value\" : 575578\n" +
                "               },\n" +
                "               \"duration\" : {\n" +
                "                  \"text\" : \"8 hours 33 mins\",\n" +
                "                  \"value\" : 30781\n" +
                "               },\n" +
                "               \"end_address\" : \"ADA Building, Vittal Mallya Rd, KG Halli, D' Souza Layout, Ashok Nagar, Bengaluru, Karnataka 560001, India\",\n" +
                "               \"end_location\" : {\n" +
                "                  \"lat\" : 12.9716014,\n" +
                "                  \"lng\" : 77.5945493\n" +
                "               },\n" +
                "               \"start_address\" : \"Omnitech InfoSolutions, 1st Floor, Bulding No 17, Main Rd, Goutam Nagar, Badi Chowdi, Kachiguda, Hyderabad, Telangana 501301, India\",\n" +
                "               \"start_location\" : {\n" +
                "                  \"lat\" : 17.3849048,\n" +
                "                  \"lng\" : 78.48665539999999\n" +
                "               },\n" +
                "               \"steps\" : [\n" +
                "                  {\n" +
                "                     \"distance\" : {\n" +
                "                        \"text\" : \"34 m\",\n" +
                "                        \"value\" : 34\n" +
                "                     },\n" +
                "                     \"duration\" : {\n" +
                "                        \"text\" : \"1 min\",\n" +
                "                        \"value\" : 9\n" +
                "                     },\n" +
                "                     \"end_location\" : {\n" +
                "                        \"lat\" : 17.3848728,\n" +
                "                        \"lng\" : 78.4869698\n" +
                "                     },\n" +
                "                     \"html_instructions\" : \"Head \\u003cb\\u003eeast\\u003c/b\\u003e on \\u003cb\\u003eHyderabad - Janagam Hwy\\u003c/b\\u003e/\\u003cb\\u003eKoti Women's College Rd\\u003c/b\\u003e/\\u003cb\\u003eMain Rd\\u003c/b\\u003e\",\n" +
                "                     \"polyline\" : {\n" +
                "                        \"points\" : \"snbiBsl`~MD}@\"\n" +
                "                     },\n" +
                "                     \"start_location\" : {\n" +
                "                        \"lat\" : 17.3849048,\n" +
                "                        \"lng\" : 78.48665539999999\n" +
                "                     },\n" +
                "                     \"travel_mode\" : \"DRIVING\"\n" +
                "                  },\n" +
                "                  {\n" +
                "                     \"distance\" : {\n" +
                "                        \"text\" : \"0.3 km\",\n" +
                "                        \"value\" : 286\n" +
                "                     },\n" +
                "                     \"duration\" : {\n" +
                "                        \"text\" : \"1 min\",\n" +
                "                        \"value\" : 80\n" +
                "                     },\n" +
                "                     \"end_location\" : {\n" +
                "                        \"lat\" : 17.3870369,\n" +
                "                        \"lng\" : 78.48624579999999\n" +
                "                     },\n" +
                "                     \"html_instructions\" : \"Turn \\u003cb\\u003eleft\\u003c/b\\u003e toward \\u003cb\\u003eSultan Bazar Juction Rd\\u003c/b\\u003e/\\u003cb\\u003eSultan Bazar-Ramkote Main Rd\\u003c/b\\u003e\",\n" +
                "                     \"maneuver\" : \"turn-left\",\n" +
                "                     \"polyline\" : {\n" +
                "                        \"points\" : \"mnbiBqn`~MI]IMACGCICKAO@{@l@YT[Ra@Na@Ji@PkA`@i@RWJ\"\n" +
                "                     },\n" +
                "                     \"start_location\" : {\n" +
                "                        \"lat\" : 17.3848728,\n" +
                "                        \"lng\" : 78.4869698\n" +
                "                     },\n" +
                "                     \"travel_mode\" : \"DRIVING\"\n" +
                "                  },\n" +
                "                  {\n" +
                "                     \"distance\" : {\n" +
                "                        \"text\" : \"0.4 km\",\n" +
                "                        \"value\" : 371\n" +
                "                     },\n" +
                "                     \"duration\" : {\n" +
                "                        \"text\" : \"1 min\",\n" +
                "                        \"value\" : 84\n" +
                "                     },\n" +
                "                     \"end_location\" : {\n" +
                "                        \"lat\" : 17.3902835,\n" +
                "                        \"lng\" : 78.48551999999999\n" +
                "                     },\n" +
                "                     \"html_instructions\" : \"At \\u003cb\\u003eSultan Bazar Chowrasta\\u003c/b\\u003e, continue onto \\u003cb\\u003eSultan Bazar Juction Rd\\u003c/b\\u003e/\\u003cb\\u003eSultan Bazar-Ramkote Main Rd\\u003c/b\\u003e\",\n" +
                "                     \"polyline\" : {\n" +
                "                        \"points\" : \"_|biBaj`~M_@Jw@L}@Pi@JiAJ_ABQBc@F{@JMBA@_@FKBe@LE@_@DWFk@@\"\n" +
                "                     },\n" +
                "                     \"start_location\" : {\n" +
                "                        \"lat\" : 17.3870369,\n" +
                "                        \"lng\" : 78.48624579999999\n" +
                "                     },\n" +
                "                     \"travel_mode\" : \"DRIVING\"\n" +
                "                  },\n" +
                "                  {\n" +
                "                     \"distance\" : {\n" +
                "                        \"text\" : \"0.5 km\",\n" +
                "                        \"value\" : 496\n" +
                "                     },\n" +
                "                     \"duration\" : {\n" +
                "                        \"text\" : \"2 mins\",\n" +
                "                        \"value\" : 102\n" +
                "                     },\n" +
                "                     \"end_location\" : {\n" +
                "                        \"lat\" : 17.3944132,\n" +
                "                        \"lng\" : 78.486572\n" +
                "                     },\n" +
                "                     \"html_instructions\" : \"Continue straight through \\u003cb\\u003eRamkoti Chowrasta\\u003c/b\\u003e\\u003cdiv style=\\\"font-size:0.9em\\\"\\u003ePass by Harrow School (on the left in 400&nbsp;m)\\u003c/div\\u003e\",\n" +
                "                     \"maneuver\" : \"straight\",\n" +
                "                     \"polyline\" : {\n" +
                "                        \"points\" : \"gpciBoe`~M_@Gc@G]IUEUGUGQGUEsBcAkAi@OCsAc@e@SOMYKYG[CU?a@BSFo@RgAZ\"\n" +
                "                     },\n" +
                "                     \"start_location\" : {\n" +
                "                        \"lat\" : 17.3902835,\n" +
                "                        \"lng\" : 78.48551999999999\n" +
                "                     },\n" +
                "                     \"travel_mode\" : \"DRIVING\"\n" +
                "                  },\n" +
                "                  {\n" +
                "                     \"distance\" : {\n" +
                "                        \"text\" : \"0.1 km\",\n" +
                "                        \"value\" : 97\n" +
                "                     },\n" +
                "                     \"duration\" : {\n" +
                "                        \"text\" : \"1 min\",\n" +
                "                        \"value\" : 19\n" +
                "                     },\n" +
                "                     \"end_location\" : {\n" +
                "                        \"lat\" : 17.3947174,\n" +
                "                        \"lng\" : 78.4857432\n" +
                "                     },\n" +
                "                     \"html_instructions\" : \"Continue onto \\u003cb\\u003eKing Koti Rd\\u003c/b\\u003e\\u003cdiv style=\\\"font-size:0.9em\\\"\\u003ePass by Shalimar Function Hall (on the left)\\u003c/div\\u003e\",\n" +
                "                     \"polyline\" : {\n" +
                "                        \"points\" : \"ajdiBal`~MQfBEJYb@KL\"\n" +
                "                     },\n" +
                "                     \"start_location\" : {\n" +
                "                        \"lat\" : 17.3944132,\n" +
                "                        \"lng\" : 78.486572\n" +
                "                     },\n" +
                "                     \"travel_mode\" : \"DRIVING\"\n" +
                "                  },\n" +
                "                  {\n" +
                "                     \"distance\" : {\n" +
                "                        \"text\" : \"0.4 km\",\n" +
                "                        \"value\" : 386\n" +
                "                     },\n" +
                "                     \"duration\" : {\n" +
                "                        \"text\" : \"1 min\",\n" +
                "                        \"value\" : 66\n" +
                "                     },\n" +
                "                     \"end_location\" : {\n" +
                "                        \"lat\" : 17.3973731,\n" +
                "                        \"lng\" : 78.4835581\n" +
                "                     },\n" +
                "                     \"html_instructions\" : \"Continue straight onto \\u003cb\\u003eOld MLA Quarters Rd\\u003c/b\\u003e\\u003cdiv style=\\\"font-size:0.9em\\\"\\u003ePass by Bhavans New Science College (on the right)\\u003c/div\\u003e\",\n" +
                "                     \"maneuver\" : \"straight\",\n" +
                "                     \"polyline\" : {\n" +
                "                        \"points\" : \"_ldiB{f`~M@HOPyBbAyAt@cCnASLOHOJKHIJYb@aAdBEH\"\n" +
                "                     },\n" +
                "                     \"start_location\" : {\n" +
                "                        \"lat\" : 17.3947174,\n" +
                "                        \"lng\" : 78.4857432\n" +
                "                     },\n" +
                "                     \"travel_mode\" : \"DRIVING\"\n" +
                "                  },\n" +
                "                  {\n" +
                "                     \"distance\" : {\n" +
                "                        \"text\" : \"0.9 km\",\n" +
                "                        \"value\" : 881\n" +
                "                     },\n" +
                "                     \"duration\" : {\n" +
                "                        \"text\" : \"3 mins\",\n" +
                "                        \"value\" : 175\n" +
                "                     },\n" +
                "                     \"end_location\" : {\n" +
                "                        \"lat\" : 17.4008648,\n" +
                "                        \"lng\" : 78.47617780000002\n" +
                "                     },\n" +
                "                     \"html_instructions\" : \"Continue straight onto \\u003cb\\u003eHyderguda-Basheerbagh Rd\\u003c/b\\u003e\\u003cdiv style=\\\"font-size:0.9em\\\"\\u003ePass by MLA Quarters (on the left)\\u003c/div\\u003e\",\n" +
                "                     \"maneuver\" : \"straight\",\n" +
                "                     \"polyline\" : {\n" +
                "                        \"points\" : \"q|diBgy_~MKPcAfBEDe@|@_@p@U^MVMVQ`@K\\\\Uh@GNEJs@jBM^e@zAGRKVSp@kAhDKRc@`ASf@ELg@xAUr@Sn@e@`BOh@AJAH?H@HDL\"\n" +
                "                     },\n" +
                "                     \"start_location\" : {\n" +
                "                        \"lat\" : 17.3973731,\n" +
                "                        \"lng\" : 78.4835581\n" +
                "                     },\n" +
                "                     \"travel_mode\" : \"DRIVING\"\n" +
                "                  },\n" +
                "                  {\n" +
                "                     \"distance\" : {\n" +
                "                        \"text\" : \"62 m\",\n" +
                "                        \"value\" : 62\n" +
                "                     },\n" +
                "                     \"duration\" : {\n" +
                "                        \"text\" : \"1 min\",\n" +
                "                        \"value\" : 14\n" +
                "                     },\n" +
                "                     \"end_location\" : {\n" +
                "                        \"lat\" : 17.4010014,\n" +
                "                        \"lng\" : 78.4756587\n" +
                "                     },\n" +
                "                     \"html_instructions\" : \"\\u003cb\\u003eHyderguda-Basheerbagh Rd\\u003c/b\\u003e turns slightly \\u003cb\\u003eleft\\u003c/b\\u003e and becomes \\u003cb\\u003eBade Ustad Gulam Ali Khan Marg\\u003c/b\\u003e/\\u003cb\\u003eLB Stadium Rd\\u003c/b\\u003e\",\n" +
                "                     \"polyline\" : {\n" +
                "                        \"points\" : \"kreiBck~}M?@@??@@??@@??@?@@@?@?@@B?@?BCDA@A@A@[`A\"\n" +
                "                     },\n" +
                "                     \"start_location\" : {\n" +
                "                        \"lat\" : 17.4008648,\n" +
                "                        \"lng\" : 78.47617780000002\n" +
                "                     },\n" +
                "                     \"travel_mode\" : \"DRIVING\"\n" +
                "                  },\n" +
                "                  {\n" +
                "                     \"distance\" : {\n" +
                "                        \"text\" : \"0.6 km\",\n" +
                "                        \"value\" : 556\n" +
                "                     },\n" +
                "                     \"duration\" : {\n" +
                "                        \"text\" : \"1 min\",\n" +
                "                        \"value\" : 82\n" +
                "                     },\n" +
                "                     \"end_location\" : {\n" +
                "                        \"lat\" : 17.400666,\n" +
                "                        \"lng\" : 78.47048719999999\n" +
                "                     },\n" +
                "                     \"html_instructions\" : \"Continue straight onto \\u003cb\\u003eLB Stadium Rd\\u003c/b\\u003e\\u003cdiv style=\\\"font-size:0.9em\\\"\\u003ePass by Aayakar Bhavan (on the right)\\u003c/div\\u003e\",\n" +
                "                     \"maneuver\" : \"straight\",\n" +
                "                     \"polyline\" : {\n" +
                "                        \"points\" : \"gseiB{g~}MGZADGr@GtCEnAAn@?XBb@@VDd@Fz@Db@?@F~@@LHpAV`EJt@Pf@\"\n" +
                "                     },\n" +
                "                     \"start_location\" : {\n" +
                "                        \"lat\" : 17.4010014,\n" +
                "                        \"lng\" : 78.4756587\n" +
                "                     },\n" +
                "                     \"travel_mode\" : \"DRIVING\"\n" +
                "                  },\n" +
                "                  {\n" +
                "                     \"distance\" : {\n" +
                "                        \"text\" : \"0.4 km\",\n" +
                "                        \"value\" : 387\n" +
                "                     },\n" +
                "                     \"duration\" : {\n" +
                "                        \"text\" : \"1 min\",\n" +
                "                        \"value\" : 67\n" +
                "                     },\n" +
                "                     \"end_location\" : {\n" +
                "                        \"lat\" : 17.4025627,\n" +
                "                        \"lng\" : 78.4679292\n" +
                "                     },\n" +
                "                     \"html_instructions\" : \"At \\u003cb\\u003ePublic Garden X Rd\\u003c/b\\u003e, take the \\u003cb\\u003e2nd\\u003c/b\\u003e exit onto \\u003cb\\u003eNH 44\\u003c/b\\u003e/\\u003cb\\u003eNH65\\u003c/b\\u003e\",\n" +
                "                     \"maneuver\" : \"roundabout-left\",\n" +
                "                     \"polyline\" : {\n" +
                "                        \"points\" : \"eqeiBqg}}MB@@?B@@?@@B@@@@@HFBD@DBD@F@D?F?DADAFA@ABABABABC@C@ABK\\\\K\\\\ABSVUTk@^k@^{@f@[T}A~A}@hA\"\n" +
                "                     },\n" +
                "                     \"start_location\" : {\n" +
                "                        \"lat\" : 17.400666,\n" +
                "                        \"lng\" : 78.47048719999999\n" +
                "                     },\n" +
                "                     \"travel_mode\" : \"DRIVING\"\n" +
                "                  },\n" +
                "                  {\n" +
                "                     \"distance\" : {\n" +
                "                        \"text\" : \"0.4 km\",\n" +
                "                        \"value\" : 422\n" +
                "                     },\n" +
                "                     \"duration\" : {\n" +
                "                        \"text\" : \"1 min\",\n" +
                "                        \"value\" : 71\n" +
                "                     },\n" +
                "                     \"end_location\" : {\n" +
                "                        \"lat\" : 17.4040695,\n" +
                "                        \"lng\" : 78.46446589999999\n" +
                "                     },\n" +
                "                     \"html_instructions\" : \"Turn \\u003cb\\u003eleft\\u003c/b\\u003e at \\u003cb\\u003eRavindra Bharti X Rd\\u003c/b\\u003e onto \\u003cb\\u003eNH65\\u003c/b\\u003e\",\n" +
                "                     \"maneuver\" : \"turn-left\",\n" +
                "                     \"polyline\" : {\n" +
                "                        \"points\" : \"_}eiBqw|}MEr@Ct@GxAYfCCXCJGNWb@a@r@QTe@f@Y^IJiAbAGLGLAN\"\n" +
                "                     },\n" +
                "                     \"start_location\" : {\n" +
                "                        \"lat\" : 17.4025627,\n" +
                "                        \"lng\" : 78.4679292\n" +
                "                     },\n" +
                "                     \"travel_mode\" : \"DRIVING\"\n" +
                "                  },\n" +
                "                  {\n" +
                "                     \"distance\" : {\n" +
                "                        \"text\" : \"0.9 km\",\n" +
                "                        \"value\" : 926\n" +
                "                     },\n" +
                "                     \"duration\" : {\n" +
                "                        \"text\" : \"3 mins\",\n" +
                "                        \"value\" : 163\n" +
                "                     },\n" +
                "                     \"end_location\" : {\n" +
                "                        \"lat\" : 17.4034047,\n" +
                "                        \"lng\" : 78.4563964\n" +
                "                     },\n" +
                "                     \"html_instructions\" : \"Turn \\u003cb\\u003eleft\\u003c/b\\u003e onto \\u003cb\\u003eAC Guards Rd\\u003c/b\\u003e/\\u003cb\\u003eLakdikapul Bridge\\u003c/b\\u003e\\u003cdiv style=\\\"font-size:0.9em\\\"\\u003eContinue to follow AC Guards Rd\\u003c/div\\u003e\\u003cdiv style=\\\"font-size:0.9em\\\"\\u003eParts of this road may be closed at certain times or days\\u003c/div\\u003e\\u003cdiv style=\\\"font-size:0.9em\\\"\\u003ePass by Garden Bakery (on the left)\\u003c/div\\u003e\",\n" +
                "                     \"maneuver\" : \"turn-left\",\n" +
                "                     \"polyline\" : {\n" +
                "                        \"points\" : \"mffiB}a|}Mx@nAx@jAFHFHPTr@nAFPHNDHDLHPLf@Lr@Hr@Fl@?T?JAFCPg@nCIj@C`@AJ?D?TB|@?HDn@F`@Fn@@T?VLlC@TARARE\\\\Uh@IX]~@_AdC\"\n" +
                "                     },\n" +
                "                     \"start_location\" : {\n" +
                "                        \"lat\" : 17.4040695,\n" +
                "                        \"lng\" : 78.46446589999999\n" +
                "                     },\n" +
                "                     \"travel_mode\" : \"DRIVING\"\n" +
                "                  },\n" +
                "                  {\n" +
                "                     \"distance\" : {\n" +
                "                        \"text\" : \"0.9 km\",\n" +
                "                        \"value\" : 938\n" +
                "                     },\n" +
                "                     \"duration\" : {\n" +
                "                        \"text\" : \"2 mins\",\n" +
                "                        \"value\" : 112\n" +
                "                     },\n" +
                "                     \"end_location\" : {\n" +
                "                        \"lat\" : 17.3993726,\n" +
                "                        \"lng\" : 78.45044639999999\n" +
                "                     },\n" +
                "                     \"html_instructions\" : \"Keep \\u003cb\\u003eright\\u003c/b\\u003e to continue on \\u003cb\\u003eMasab Tank Flyover\\u003c/b\\u003e\\u003cdiv style=\\\"font-size:0.9em\\\"\\u003eMay be closed at certain times or days\\u003c/div\\u003e\",\n" +
                "                     \"maneuver\" : \"keep-right\",\n" +
                "                     \"polyline\" : {\n" +
                "                        \"points\" : \"gbfiBooz}Mg@`Aw@zA]p@CFQ`@IXCJCLCRAV?@?TB\\\\BNBP@@LX^n@Xd@DFBDDDRZh@v@TZTZz@`AFFJHNJFBLHZNvCtAfAh@`Bv@`Bv@`Ad@bAd@XNj@V\"\n" +
                "                     },\n" +
                "                     \"start_location\" : {\n" +
                "                        \"lat\" : 17.4034047,\n" +
                "                        \"lng\" : 78.4563964\n" +
                "                     },\n" +
                "                     \"travel_mode\" : \"DRIVING\"\n" +
                "                  },\n" +
                "                  {\n" +
                "                     \"distance\" : {\n" +
                "                        \"text\" : \"0.7 km\",\n" +
                "                        \"value\" : 725\n" +
                "                     },\n" +
                "                     \"duration\" : {\n" +
                "                        \"text\" : \"2 mins\",\n" +
                "                        \"value\" : 116\n" +
                "                     },\n" +
                "                     \"end_location\" : {\n" +
                "                        \"lat\" : 17.3984547,\n" +
                "                        \"lng\" : 78.44403799999999\n" +
                "                     },\n" +
                "                     \"html_instructions\" : \"Merge onto \\u003cb\\u003eInner Ring Rd\\u003c/b\\u003e/\\u003cb\\u003eMasab Tank Road\\u003c/b\\u003e/\\u003cb\\u003eMehdipatnam - Banjara Hills Rd\\u003c/b\\u003e/\\u003cb\\u003eUtkoor - Mogdumpur Rd\\u003c/b\\u003e\\u003cdiv style=\\\"font-size:0.9em\\\"\\u003eMay be closed at certain times or days\\u003c/div\\u003e\\u003cdiv style=\\\"font-size:0.9em\\\"\\u003ePass by Universal Bakes &amp; Cakes (on the left)\\u003c/div\\u003e\",\n" +
                "                     \"maneuver\" : \"merge\",\n" +
                "                     \"polyline\" : {\n" +
                "                        \"points\" : \"aieiBijy}MRJB?x@^FDFDBDBBBHBJBR@D?D?D?F?HALItAAPI~AStCIvA?f@@LBPTdAVfAH`@?DHj@Bj@Br@@fBDX\\\\`D\"\n" +
                "                     },\n" +
                "                     \"start_location\" : {\n" +
                "                        \"lat\" : 17.3993726,\n" +
                "                        \"lng\" : 78.45044639999999\n" +
                "                     },\n" +
                "                     \"travel_mode\" : \"DRIVING\"\n" +
                "                  },\n" +
                "                  {\n" +
                "                     \"distance\" : {\n" +
                "                        \"text\" : \"11.6 km\",\n" +
                "                        \"value\" : 11586\n" +
                "                     },\n" +
                "                     \"duration\" : {\n" +
                "                        \"text\" : \"13 mins\",\n" +
                "                        \"value\" : 778\n" +
                "                     },\n" +
                "                     \"end_location\" : {\n" +
                "                        \"lat\" : 17.319518,\n" +
                "                        \"lng\" : 78.4288797\n" +
                "                     },\n" +
                "                     \"html_instructions\" : \"Continue straight onto \\u003cb\\u003ePV Narasimha Rao Expy\\u003c/b\\u003e\\u003cdiv style=\\\"font-size:0.9em\\\"\\u003eParts of this road may be closed at certain times or days\\u003c/div\\u003e\",\n" +
                "                     \"maneuver\" : \"straight\",\n" +
                "                     \"polyline\" : {\n" +
                "                        \"points\" : \"iceiBgbx}MFZN`@JRPVXVNLTNPJ`@LdATr@NJB^HLD@?H@D@VHZLPJB@XV?@PNBDNRHNJNz@`CN\\\\z@|BRh@DNNb@Ph@DVFd@BT?R@LAx@S|EQzDAP?P{@jSCh@G`@O`@k@nACDA@CHGJADEJEJCJCNAN?N?N?JDZBJ@DBF@DHRDHNTFFFFHDPHNFLBTFd@@p@?f@?lB?bB?J?B?R?r@?n@BZ@ZBR@h@Fh@F\\\\F\\\\Hr@LnB^zE~@|Dx@TFF@bBb@~@PVDRBxAHlEDzBBL?N?@?N@v@?t@?lB@lLFR?~JFL?L?~KBhC@j@?P?B?R@F?vCBhA@xA@B?H?H?^@v@B@@h@DL@P@H@F@RBR@T@n@@vADzACl@CP?NA`@EF?t@Ex@EX?dAAz@@L?N?Z?tADz@FL?J@F@pAFvAN~AR~@JdBVnANTBL@LBt@HdBPnALz@H|BVfBPP@B@L@bEb@b@BpBThD^^HZHRHJF\\\\PLHHHLHLNLNz@hAdAxAp@|@RTb@f@fCnCvHfIbEjEPPNRtCjDfDhEhG|HVXh@j@|@x@p@`@d@TRJd@Np@Tp@PLBtFlARFn@Pn@LrEfAd@Lt@PrAXnAZl@LVD\\\\BN@^@d@AVCVEZIBAZKXMLITOrAcA\\\\Ut@i@r@i@f@e@VYLULUFOJWb@_BT_AFYHc@Fe@h@kEpAkKJaADWJk@DSDKPg@Vg@\\\\e@NMRSVQNKd@UTMjBeA|@c@NI~K_Fv@c@ZOpAk@r@W`@Kv@Qz@I`CGlOY@?T?Z@RBN@|@N|@PjEx@tHvAn@LD@nAJzD\\\\p@Dh@BR?L?HALCLCNGNGNMTS^e@x@kADERYPYJMJObCyDrAqB~BsDx@oAjAgBp@aAr@aATYX[FEFEHEBCFA^MPCXCL?N?J@RDXFRFJFPJNNPRR\\\\j@jAVj@Zl@RXNVV^n@z@PRJHJH^T\"\n" +
                "                     },\n" +
                "                     \"start_location\" : {\n" +
                "                        \"lat\" : 17.3984547,\n" +
                "                        \"lng\" : 78.44403799999999\n" +
                "                     },\n" +
                "                     \"travel_mode\" : \"DRIVING\"\n" +
                "                  },\n" +
                "                  {\n" +
                "                     \"distance\" : {\n" +
                "                        \"text\" : \"70.5 km\",\n" +
                "                        \"value\" : 70534\n" +
                "                     },\n" +
                "                     \"duration\" : {\n" +
                "                        \"text\" : \"1 hour 7 mins\",\n" +
                "                        \"value\" : 4008\n" +
                "                     },\n" +
                "                     \"end_location\" : {\n" +
                "                        \"lat\" : 16.7808664,\n" +
                "                        \"lng\" : 78.13760320000002\n" +
                "                     },\n" +
                "                     \"html_instructions\" : \"Merge onto \\u003cb\\u003eNH 44\\u003c/b\\u003e\\u003cdiv style=\\\"font-size:0.9em\\\"\\u003ePartial toll road\\u003c/div\\u003e\\u003cdiv style=\\\"font-size:0.9em\\\"\\u003ePass by the lake (on the left in 400&nbsp;m)\\u003c/div\\u003e\",\n" +
                "                     \"maneuver\" : \"merge\",\n" +
                "                     \"polyline\" : {\n" +
                "                        \"points\" : \"_vuhBocu}MVZXXVT|AnAPN^ZvMzK^XTRTLl@\\\\ZNVLn@TnC|@NF`PjF~CdA`Bh@pGtBjBj@jEpAn@PfBb@jCp@rCn@xFpArAX~Cr@^FTFtAZ~Dx@hCl@jB`@vAXJBtFjAJBp@Pl@PPBfAVzGxAnCj@lBVx@Ff@FrEVvCRlALf@HZFZHbAX^LtAh@lAh@DDhB~@hB|@tAp@|@b@x@f@x@h@n@h@~@v@rBtBpApAp@n@@@fAx@hAn@tAx@jCvA|ElC`B~@BBfFpCpAv@t@l@~@|@f@h@p@x@b@l@Xb@Xf@P\\\\P`@Vj@vBxGfA|CVp@Zt@jAlCfAtBl@|@rBnCzFvHJNjBfCt@`Aj@v@~@pAx@hAtBvCv@`Av@z@b@\\\\`Aj@@@fHhDdD|A~@f@n@\\\\^Vt@j@x@p@XT`@^XZFFFFLNJPLXV`@nA|ANPRR\\\\^Z^~@pA|@rArB|C`ArAtBzCPTdBdCHLf@p@dArATZ\\\\\\\\bBzAXR`@ZXR`@`@d@l@f@x@LXRf@Xx@h@xADNPd@`@hANf@Vn@JVNX\\\\j@^h@Z\\\\XX`@Zb@Z\\\\RxCxA`@Rr@^|Av@l@ZxEzCb@X@?JF@@^Vd@\\\\^Tn@d@`@Vf@Zf@Zv@j@B@fAz@dBvA|@t@f@b@r@p@|@~@`AdAj@p@`@d@p@|@tBtCrAhBlBjClA~AVXb@f@V^r@hAFHHLHFJFFDZPLFVLb@Tp@b@d@ZjAz@RN`@X^\\\\ZXVXPRT^r@dALTJRrBpDjAlB`DjF`@p@fAhBXb@R\\\\l@`AT^Rb@xAbD~ApDv@jBP^NXR\\\\T\\\\NTNPPPPTRR\\\\^ZXRPXR^RdAl@lAn@p@\\\\x@`@nCxA`@Tt@`@b@V^RXRZTTTX\\\\VVRTT\\\\TZRZTb@^x@r@|AdDlHJRRb@PZvApCZh@\\\\h@\\\\j@TZLRPRt@bA`@d@V\\\\TVTV|@`A|ErEt@t@VX\\\\^LPFHJNX`@T^Xh@v@xAn@jAJTNTNVV^LRX`@LNNN`@d@LLXZfBhBbAbAf@j@@?pClCv@|@vExE~@~@|@x@d@`@n@h@~CfCj@b@f@^`@VVNr@b@v@b@hCvAVNzAv@vDpBpAp@x@d@pC|Az@d@zBnAtAt@JD\\\\Ph@T`@PbBr@xB|@bDvAZNRJJFJF^VZVLJFDNNFHTVJLFJJNNXR\\\\h@jAtBjE^p@PXNTNTLPRTJLLLZXj@f@ZT`@Z\\\\TdIxFdBtAzArAhMhLpChCpFzEt@p@`@^Z\\\\JLLNPVJNHN`@t@PZf@z@`@p@`@r@Xf@R\\\\Vb@`@n@n@`A^l@j@z@x@jA`BdC`AtAPVLR`@l@V^NRn@x@^b@VXh@j@XXZX^\\\\~BzBt@p@NNRP`@\\\\d@\\\\v@h@r@b@`@XVRRNNNJLFDFHLRFHDJ^r@FLLVLVLXJZz@bCz@`C^~@Pd@FNHPLVP\\\\HLJPJPJNZb@jCpDZ`@h@p@dAvAZd@f@n@`BxB|ArBfAxAn@z@|@nAvBvClEbGt@bALLRZr@`A|BbDt@bA`@f@Z^XVRPNLTTTPVPRJTN^R\\\\Pv@`@|@b@ZN\\\\P\\\\NZL^Ld@NTH\\\\H\\\\JXFr@N`B\\\\b@Jp@PNDLDPHd@PPH^Nn@Zr@\\\\~@d@RJPHVLNFh@T`@Pt@V~@\\\\zBx@pBr@d@PdC`AdDlAbA^`FfBh@Pn@Tb@LbCv@`HvB|Ad@|@X|C~@lA^~@Zh@Nf@LpAXd@JXHXDTDb@HbBVrCb@v@NXFVFh@LpAZn@PlA^dAZlA^?@`Cv@zFlBpE|AtAd@fA^xDrAdC~@nAd@f@Rt@ZlDzAdEdBjCjA~B`An@XrElBpAj@tAj@`A`@`Ab@|BbA`A\\\\dAXjARH?nABpAB`BQdASLE~By@xCgA`Bm@|Ai@tA]`AQp@Ij@A`BG|@Dl@FRBNDf@JD@x@Zn@Vl@XdAr@r@n@pBtBtAtAfCfCfBfB|B|BfBfB@?vArAxAnARPZRx@l@xFfEn@f@n@f@`E`DzAfAtAbAlB|ArAbA~@p@TR`D~BrCpBjA|@r@h@fBrAzB`BhBrAtAdAj@b@pBzApAdAf@d@z@t@fHjGdGlFpCdCfHhGvBjBvHvGzIxHfC|BHHtCjC`FnFTVVXTVhCpChCpCLNhCrC|BjChCrCxBdCxA`BtA|AvA~AhDvDxBdCxDhEpFfGpFbG`I|I|BjCxBbC|BjClCxCvEhFbFvFZ^bGzGjHbIfI`JbLfM~J|K~LdNnBrBpEpEdHpGfIvGvJpHhJdHtB~Ar@l@pAdArHjGLJBBZT|DhDjGfFbE|DhDfDj@f@vAxAfBfBbCxBlBvApA~@nAz@bAn@h@\\\\`DlBdLhHn@d@l@XPJ@@VL`GnDtD|BtBrAfHjErBjAtBjAl@b@bAf@|A|@nD`BdDlAdEpAlAXbCh@fLxBbF`AbDn@jKpBzEz@j@NvFbApC\\\\pCXbCNfFNlBBlA@|A@d@?f@?l@?`JBnA?jBFp@Hv@L`@Hd@Jx@\\\\f@Tt@b@p@f@f@b@d@h@j@v@Vd@Tb@Zv@^pARbA`@lBNv@Nv@l@xDv@rE^hBPr@L`@^bAj@bA`@f@LNh@h@v@p@~@j@fAf@r@RZJlB\\\\tHz@zAND?tBTpFl@~`@dE`BPhAJtCZbTzBvHx@bLlAtGt@rBVjBRzFj@zBR`Fd@fHf@bE\\\\pAL|@J|@JhC\\\\lCT|BZ|B\\\\v@JpBV`D`@~Er@fCZ`CVf@DjBRB?`ALvD\\\\`Gp@`D`@h@FfFl@|CZd@Fx@HbFf@j@Fd_@vDNB|@JxAP|F`AbMfBx@LdALxIv@N@tE^fGh@jANfDVR@xDT\\\\BrG^H?xBPvEb@nAJjBPbE^fE\\\\hE`@`LbAzDZvBLfBLdBLxANrARxB`@jDp@vCl@dC\\\\`F\\\\rHZ|FV\\\\BtAJhCTB@pAN~APfALvB^`BVxARhAP`AL`DZrJ|@~@FfCTzD\\\\L@|C\\\\jBRtJlAhD^`BP~ATHB~B^`En@|Dh@rBXfKpAhAL@?~NdAnBLjL`AhDTjBLj@BfFZdF^~BJdBLlBRdAH|M|AzC\\\\|B\\\\jARfB\\\\bEv@dC^t@DdADlDAdGAhC@|@Dv@Dj@FbR|B~BZ|Df@~Dn@vAZnDp@pEbAzAZL@lB`@XDf@LdA\\\\fJzD@@lD|AxCnA|BfAbCpAp@\\\\|BdAtCpAlDzAv@Zd@Pj@PrEdAPD~LpCnH|Af@LvMpCdCf@`Ev@hBXxDn@n@L`ARpH|ApIjBxR|D`P|CdEx@lGnAhFfAzB`@zAVxARbALd@D|Jt@dDVvFd@nCZbBRvC\\\\rCZhALrG~@rKrA`Ed@zDb@jALvAHpDJfBAdBCtDKnBGdHM~CCxCC~@Dz@FbAPx@R@?v@Xj@V^Rt@f@lAbAlBjBbA`AdAbAd@b@d@\\\\dAr@n@\\\\`@RhBf@~A^tB^`Dn@xBb@bAVdKhC^JnH`Bh@NhGxALBpHjBtG~AdDt@pDz@|EhA~@TpBh@jBh@hCx@dDbA~DnAlNpE|EvArEvAbHxBnA`@ZJdElAdEhAfLvCvFvA|FvAvHbBxHfBZH~TbGhAZvFrA~Bp@lBf@tElAbEbAfAXfB`@pE`AnGvA~EfApTnEdEx@xDt@dDp@~Bb@dEt@b@JxBb@j@L`ALn@HzAN|AJzAFt@Dr@DbJd@p@Dt@DdALl@L\"\n" +
                "                     },\n" +
                "                     \"start_location\" : {\n" +
                "                        \"lat\" : 17.319518,\n" +
                "                        \"lng\" : 78.4288797\n" +
                "                     },\n" +
                "                     \"travel_mode\" : \"DRIVING\"\n" +
                "                  },\n" +
                "                  {\n" +
                "                     \"distance\" : {\n" +
                "                        \"text\" : \"459 km\",\n" +
                "                        \"value\" : 459457\n" +
                "                     },\n" +
                "                     \"duration\" : {\n" +
                "                        \"text\" : \"6 hours 15 mins\",\n" +
                "                        \"value\" : 22487\n" +
                "                     },\n" +
                "                     \"end_location\" : {\n" +
                "                        \"lat\" : 13.1915882,\n" +
                "                        \"lng\" : 77.6471086\n" +
                "                     },\n" +
                "                     \"html_instructions\" : \"Keep \\u003cb\\u003eright\\u003c/b\\u003e to stay on \\u003cb\\u003eNH 44\\u003c/b\\u003e\\u003cdiv style=\\\"font-size:0.9em\\\"\\u003ePartial toll road\\u003c/div\\u003e\",\n" +
                "                     \"maneuver\" : \"keep-right\",\n" +
                "                     \"polyline\" : {\n" +
                "                        \"points\" : \"moleB_g|{MF@v@Tl@XpAr@r@j@|AvAjG~F~ErEbB`Bz@t@x@r@|@z@z@x@x@x@f@h@FDdAbAHHnCfCxCrC`C|Bh@b@j@`@~JxJbA~@jBfBnQbP~BpBjGxFtFdFnLtKvG|FzK|Jv@r@d@^`B~An@j@r@p@vArA|C~CzBhCjBbCrElGdCnC`A`ArF~EbB~AnAnA~CfDfAjA|LjOzCvDtB~BfCdCpC`CrCbC`@ZtBfBxAlAfB~Ab@f@d@j@RZv@|APd@\\\\fAVtAp@pE^bCl@fD\\\\zA\\\\pAhAtD|@zCTp@jAxCRr@Rp@|AdFfDfJbAnCvAvDn@pA`@p@f@r@t@bAv@t@BDhFjF|CdDxAnAzCrCbCzBlIbIpGlGfCbC`B~AxCvClCtChCtCdCbCrApAb@b@fBhBjBpBtDzDdEhEpIxIvCxCfAhA~AzA|AtApAhAfD|CtBhB~BrBn@r@`B~AxArAtAhAdItHxEvEpEnE~E|Ex@t@lArA|DrDv@t@d@`@TVb@d@tApAhDxCp@l@zGtG`A`AbAdAXVf@f@FF@@\\\\^\\\\XNLrCvCbAbAnJxJpCvCn@n@tBdBVPpBbBrBhBh@h@jBjBpErEtCtCnClCxAzAZ\\\\`AdA|A|AbC|BfF|ErAnAJNrBfB|CvCbCfChBfBrKlKpBlBxJrJfCbCnHpHDBlEjElB`BhGlF|BpB`A~@zAtArApAzDxD`E|DbDpC`A|@pEtD~AhAbDpBbEnB~DrArExAdFhBnAn@t@b@vAbAlBzAB@jBpAdBvAj@j@NNfAjAbB|Bp@lAZp@rBdF~A`EZr@Vr@|@lBv@nAf@l@f@f@p@d@d@\\\\`Ar@hRhMdJhGRNtA|@NHlG~DrHvExDhClDbClCtBj@`@`CxBlClBtCvBfCvBzBtBxFjFdDzCfPnNzHzGZVhCvB\\\\XPL`IvFZTtCdBtBjAjCnA~B~@~Bz@~CdAfA^pCdAh@VTLZRl@`@LLPPTVZ\\\\RXVb@RXz@nAj@p@n@h@XT^T~BpAjFpChE~BZNbAh@lAf@vL`FPFlHnCzFjBbCx@pQpG`Cx@nC|@tCz@pMvDzF|AtExAdIzBzExA|JlChEpAh@LlDbAzBn@rEpAtQnFxExAjCv@|YvIhIvB|ObEdX~GtGbBfHfBlDz@|@XpAd@p@Z|@b@vHrDh@Vb@TNHf@VdBz@nE`B~BdAVNdAh@^Tp@\\\\d@ZtDxBjF~CpObJtBhA|DjBdCbA`AXt@L\\\\DD?^D\\\\@n@@nA@PAbAGxFUhBIx@ArA@bALd@FhAPz@Tb@Pj@Vv@d@xCtB`BpAr@h@~@l@|@b@`@P`@Nr@RzA\\\\`@DtEr@nATjAVfDbA|Bt@|DvA~Bz@xFpB|IfD~D`Br@T`P`IdHlD\\\\Nv@`@|BdAfCfAn@V`Bx@rCxA~BjA|BlAt@\\\\TL`@T`@Rb@TXNz@\\\\|An@tFtBfA`@nHdC`JvCbJ`DhNdFnI`DfGdCrEfBdDrA`DvAfBt@bIdDtC~@f@NPDz@RvAVnBTvCRlDDnBCvEMpFMf@AhLe@b]eA`K[|HGtKGzCEP?R?xAE`@?JAv[y@JAvMs@`I[vNm@`DMhEQlBC|B?ba@l@rIBbE]xEi@xAMtAK|D]tFW|JYbNSlAAhZ_@dXO`MGhDE~Fg@rC[pIm@tFa@dBKfIw@tGa@jGU`@?TAzWeAxJKnGQ|IU`La@z@ClL]pMi@hKY`FOdIIfPUfMO|CA|FIlHWlACnOg@jCInACbH_@`RExYSbOU`KM~NIrAJvAPhBd@bBp@bAl@|@j@pBnBJJ`@h@`@h@`C~CfIzKf@t@~CtErDlFlBpCf@l@t@z@`A~@dBxAPJBBTLbCnAxBz@bBh@|Bd@jC`@lCP~CDbDIfD]|Ck@vBm@|EsBXSXSbBiArAkAhCiCvC}CbDeD~FcGvAuAbLeLfPkPBCj@k@j@i@dC_CbDcDhBiBJM\\\\[Z]\\\\]`@c@h@g@j@e@z@o@h@]v@c@\\\\OVMtAe@xBi@HCh@KpAMfG]|Ec@rSuB~BYr@MpCW~De@dFw@rWcCz@KvAMxBQbCA`AE~CG|HMhCI`FIvFQzAE|DSzBKlH_@bCIbBGdCOrBMdDO`ESnXkA`AEzBMfCIlEYzEa@|@IlPeBz@IzLo@rPcAvKi@~DS|AKhCSzKcAvAOlNsAbUyBnQwAjBOdLMnAEvD[~BOr@EfCOrAM`AGx@CrAAnE?rA@xA?zCAn@?nCCv@A|AK`D[x@EdCK^?dC?jA@`A?|C?lB?dA?vFFrA@`IFfDBdC@`@?t@Aj@A|@GdI_ApAQrFMbDIdCAlA@xB?nAAjHAxACt@CzBQf@CvBWhAObBYbGmAdCk@LCpCg@JAjH}@\\\\GD?VEfBWRExNsBbFw@pSyB~Gw@lDUxHYpFIbZm@`DO|BU|BUfAQ`JaBpD{@fBa@fAYZKHA`@Sv@e@x@i@dBoAdEgCrDkB`E}BzBgA`Ai@HEvA_AfFoDhHwEvAy@~DiC`DiBpBiArA}@hAaAx@aAzAaCjBoCvFoIvDuFjAeBzCwEh@y@dAeBzIoMjI}LnBuCbAyAvBkDhBaBjBmANIHC`Aa@d@Qj@Qh@OlAWXENCv@Gn@C|@C|@?lDPNBNBvA\\\\hCp@lH`BhBTzFx@\\\\FZBzE^nGVbGDtEG`BC`ACr@EzBMbBM`BO`@EdAKpBWj@I~AYl@M|A[hBc@bAWdBa@dAWhA[xAe@l@Qx@Un@IhASl@G`AE|@En@Al@@rAHrALr@N@?lAVfAXhAb@xAr@jAx@fA`A^d@hApAlOrPdBnBfL`MvBdCpFzFtEbF|ChD~XvZhD~DrC|C`@h@t@z@b@v@j@lA\\\\dAP`Al@zFZtDXzCPbBNjBXlDHl@TfC^vDZlD\\\\lDZnDZlDVzBx@rIx@pIz@pIx@rIP~B@NPvAJxAXbCPdCTfCTfCNhAJhAPjAVhAVz@Z|@bA|BnAjCrApCbVvg@z@jB~@lBh@rAzBvEtAdDTf@~@pBTf@pA|CpAvClA|ClA|CxAvDpAhD|AzDbDhIdDhIfDfItCjHtChHpCjHvCjHjAvCjAtCf@dAj@jA`AlBR\\\\FLjAnBJRrAnBlAlB~AxBbBxBr@z@dAlAxDlDxAxArNhMZZdA~@nE|Dd@`@\\\\\\\\jFtEPNxEdEtFzEdA~@p@j@~BrBp@l@d@b@zAhArDbDzCpCjB~ApAbAd@^|FtDB@v@`@p@XnB|@tGvCfEhBhEbBrDpAnDbAfB^pATzBXp@JN@fBRlBVfDb@jH|@rAN`BLP@t@Bx@DdCJpCD|CAdD@j@AVAl@AfCIhEWrDa@hAMl@C`CShCU`CUxDg@tAS@?pAWlCm@bDaA|@[zGkCfDwAxKsEbH_Df@SHEt[_NhBw@fAe@fJ}DdViKxIuDpLcFvI}D|CwAzBeAx@_@j@Ul@[n@UnQsHrMuFhFwBhMkF~PoHBAdCkA|Au@fDcBlCkA~FeCpF}BbBs@hJsDzDaBtIgDbDoArNeG|Ao@`EeBn@YjRcI~KsE~@a@n@YdFwBj@WrZiMjJ}D|GuC~K{EfCgAbCiA|Aq@xEqBdf@wSrTuJzAs@|M{F~BaAjCiA`EgBjB}@h@[FEfDwBr@i@rAcATQjDgDzA_B|@eAfAuAhFsH`DqErDeFtDiFpEiG|@oA|Zyb@l@}@vEsGxE{Gv@gAjJmMr@aApAgBnAaB~BuC`@k@bCcD`CmD`FoHh@}@lCcEzLqRvGuKfLiRb@u@d@y@bAyAnAmBHKn@aARWbAeA|@_AnEiF~VyY`G_HdYi\\\\tBaC|EyFZ_@rUaYlOiRlBqB~HuJ~FmHhTsWlCeDhCyCpDkExNiQn@s@@AJOdKeMp@w@`GqHd@m@~CwDlDkErOeQv@aAdAkArIeJx@u@r@i@d@]dAe@r@Uz@U`BU|@Gl@AX?d@@~AFfEJrI^rERdJ`@rAFpW~@jJVbADh@Bh@@fGPlMd@xDJdGNpBDzKZlS`@bBFj@@F?`@@pTh@zDJR?dIVhDHlDFpGV`ABpMd@nRt@bEL\\\\@`a@tArBDpM`@nEJlFLtNRp@B|GRN@zADpBJbIRpCLnJ\\\\pC?r@BrCLvBNhAJv@Lj@FdC`@hB\\\\ZHTDpD`AbIfCrIvClC`A^LpIpCJDz@V~JbDvHhC|DlAvFpBdDjAtBp@`MfE|Bt@tBn@rBr@v@VfA\\\\bDdAj@TNFdA`@`FfB~Bz@VJhCbAtAd@~@Z`ATbCVvBJxBC|ACr@I?Av@IlB]hBm@nCmAv@c@XWlA}@xGsGt@u@dDcDrDwDh@g@dDiD~@}@`AeAbCcCRUd@i@|ByBBCr@w@XYd@a@JKt@k@PMd@Y\\\\Q`Ac@bAa@bAa@xAk@vAm@`Bq@`Bm@dCaArBu@~F{B~FyBNGfBq@jCeAhCcAtBw@f@SdDsAjAg@vEkBbBo@nCkArAc@ZMXMnAg@nAe@xEoBjEmBfBu@p@YtCoA~DiBjB}@dCoAdBo@hA[HCxAe@TIrBu@lAk@tAs@h@a@hC{BzG}Gj@k@tCmCrAsAdAeAhAuAtAwAxC_DdDeD^]^_@h@i@v@q@r@g@z@i@v@]l@Wb@QfBc@JAXENCRCd@ELA\\\\A`AEVAhA?vABP?B?hA?rA@~A@jAAB?x@CPCNDLBXA~@@jBBv@?jB@L?~@@pC?tCBxHBtABp@@~@AjNAzFCrBAL@vDBvDDvADZBj@F|@LjBd@|Bn@xDjApA`@fBh@fBl@hBl@jBn@jBn@fBt@vBt@B@nBz@x@\\\\r@b@x@j@p@l@v@n@\\\\Xt@n@`@^`Ar@TNB@t@h@D@LFJFx@Vj@PHBz@Nz@LB@hBRjBXdD\\\\dDb@pATpA\\\\pAb@rHpCzCjAr@VfBj@`@Nv@VnAd@lAl@jAp@lAr@hAx@`CnBhCpB|@~@lAfAjAdAbAx@z@l@XT`@VhBt@bA`@bBr@jDzAtClAtCpA`CbA|Ap@`CbAnDrAhExAjBp@nBx@hEdC|BtAjClAfAb@vDxAxDvApDhA~DpAv@XhBn@|CfAdEvAdExAjE|ArCbArAh@r@Vz@^zHbDbFxBrBv@rBx@~B`A`OjGdEfBdH~C~CtA~An@|ItDhAd@d@RpAf@z@Z`DjAxCdArCfArDlAjCbAt@XvAd@hA\\\\~@PF@nCf@fEv@`GfAdHlAfHnAdBXZF|AZd@Pz@b@XR^ZbAlANPxBfCjAzAJL`Ar@lAp@\\\\Lx@X~Al@~B~@VJVJXL|DdBtCpAtCnAdFxBFBvExBx@ZrGnClEhBjEhBpB|@nB~@pF|BxAx@rBzAfAdAX^RVX`@^j@^n@Zn@zCxHh@tAbBlEfAnCv@`BdAtBt@rAzCbFzFpHxBtClCbEvAlCdAtBjFrL~@tB|@|Al@z@x@bApBhBpA|@t@`@h@XvAn@f@LzAb@~HnBlAXrLxCdD|@hBj@tFnBnK|DbGrBrOhFbEtA|@ZtHhCPDFBv@TlE~A~[|KlGtBtDpA`]jLxN~EXJnBf@nBb@dBZdBTfEZjH^vDXx@JjC^rDr@pXfGxEbAPD@?JB^FdIfBfGxAhCh@`C`@fQ`CvJpAl@JlNjBrC^|Db@xADtA?jACxAKpBYtBg@bNeElOyE`JqClBm@l@MbAQvBULAbAEfAEpBD`ALfBXz@Tj@PrKlDz@VjXlHv@TtI~BpVxGrFzAvL`D`Dz@|A`@lAXhB^pHzA|Cj@vHvAxE~@dBb@x@VfAb@hBt@nCvAtIhEnRlJtDfBrL`G~CzA~BhApAn@bD~A|]hQvKpFxGdDv@^tFnC|Az@tCdB`EbC~ExCvDxBlAr@FB~AbAv@`@t@\\\\hA^fB^|AR|@H~Db@`ANTBr@PTDJDNHhBr@~An@r@X`JrD`B`@b@Hh@J~@JfBJdCLdV~@hCFxK^~FRdEJ`GHpMNtFJbABF?fAFfAFfBR`BTb@H~A\\\\rK~BpVvEzUdE`F`Aj@JtCl@zCh@bAPnDh@@@xBNdBNlBRxANbCX`APd@JJB`@NXJbAj@lBtAtDfCrA|@dDjBjA|@rClBfEpChChBrAhAvDjDr@n@zAhAtGdD`IzDlAf@zAn@|NvFhGnCrGzCfBv@hBp@fBr@fC~@tAf@`E~A|Al@jAn@xAx@zA~@x@f@b@Td@P`@Rb@N`AZnAZnEvA~EnBnD|A~GrC~B~@lCdApKnDpD`AhDl@fPfCjMfCxAX@?rAV`BRf@JbBPv@FhIz@fAR|AZvD`AvEhA~Bj@xEjA\\\\Hb@J|J`CvF|AzJzC`LlDhHdClA^vAd@pAh@bAj@nBlA`GnDFDtCdBn@b@xCbBbCbApBt@tEpArGhAnBVdJjAtPjBt_@`EzDVzFPtLLzAFpAL`ARhBd@nBf@lCp@~JjCxAThBJX@|AJ`DPdBPpC`@hALfAPnAXF@lA^D@|Aj@fAh@rAv@fBhAv@j@l@`@h@Zh@Xf@Rb@PhATLDdANN@xHr@vGv@zALjE`@hGj@bDVfDb@jBNnE^bE`@nAHr@Hz@Hx@Np@Rl@RdAf@d@XfA~@|@fA~@bBxCpF`AlBx@|AZp@~KtTr@tAnDxHvA|C`@v@vHlPh@fAf@dAl@nA~T~f@rArCxGjOt@fBt@dB|AvD|BjFLXjBbE^r@f@v@V\\\\\\\\^n@r@tAlAlBxAxCtBdI|FbNxJpMrJPNx@j@jBpAzAhAVNbErCfAp@xAt@bAh@pDpAdCp@lKtBvAf@VJh@Vn@Zp@^bAr@n@d@@@h@f@bAfAzA~A`AdAt@p@n@j@hAz@tAhA`HjFtBtATLNHNFxBz@d@P`@Lt@NhEr@fAV`APzCd@`AX`AVXF`@L^Pd@NzCbBvFzDzF|DnDfC`DxB`ErBfEfB`FpB~CpAx@d@bBlAlBdBtH~Hd@b@jCvBzDdD`@^zAxA|ArA`CtBxAxAlJpJtA`Al@b@p@^|Az@dJ|EzAv@pAj@pBx@fAb@v@`@n@^j@\\\\p@f@r@b@x@j@z@h@lAp@zAt@fAf@lAf@p@Tb@LdAVnFlAtBr@v@`@bAb@n@ZvCxAvAv@`@Xr@d@zB|AnDjCj@\\\\p@\\\\xAn@bA^|Ad@rC~@`AT~@TbBVtARrBTnBTt@LjGx@fBZjGzAVHhA\\\\vAl@|E~BvC|AxDhBtFvCt@^~@`@bC|@nJ`D`FdBtDtA`Cl@`D`AfNpEbFfBtAd@nC~@|Bn@pFvAfE|@nAV`GzAbBj@nCdAv@b@xBrAxAdAdJvHl@f@dBtAvCjCnBhBvEhFx@bAjHjKnAdB|AhBjAbApA~@vAt@~Ar@|MrFr@ZRHhAf@n@`@j@^ZTXX~@z@tBhClDzDtMnOtCbDzBjCbBhBv@l@rC|AvI|ChJfDf@PzEbBlIvC|B|@bAd@d@XbBfApI~FfItFbDxBxAjAjAbAfA`AtArAbBfBnBjBp@d@b@Rf@Vn@Tj@PpATx@FvAL~Jd@hABt@BdBHjBJpAPzAZnA`@z@^~@h@f@\\\\f@^d@`@`AbAJR|@vArArBpEdHp@`Av@fArCvDjBfCx@hAh@|@b@r@Xh@p@xA|F`MBBl@lAFLl@xAl@rA`BdDzErK`@jABHn@pCJ^`@hD@J?BLhADd@\\\\|Cf@bE\\\\dD@BTfBVtAf@lBp@rBbAjDt@|BvGbTZbA`@fATj@`@t@p@pAfBzB`@^x@x@n@f@fAp@tSbLtBbA\\\\RXNXPn@\\\\zJ`FlDdBnLzFfFjCPHPJrDjB|BlAdA^r@XTJfCz@rD`A|Bb@bHbAzB\\\\~XxEbKfBlAPz@TjAZbA^nAl@`Ap@ZRb@b@`@`@TXzCvDpA~A\\\\Xn@j@l@X~BdAdC^^D|@@fAAhAIbBQ~BYpAMzBOpCB`D\\\\jEd@nBXtA\\\\p@TfAf@p@f@bAx@j@p@l@z@hAfBbAzAv@`An@r@pBlBjBdBtB~BtAvAfAbArAlAjBtAhN`K?@z@n@jBhApBbAxAz@l@`@^\\\\v@x@r@~@x@|Af@~Al@bCh@rBbA~DnDvNtAjFt@hC\\\\t@d@z@Zf@b@n@XZf@d@`@\\\\XNpBfAxBjAbAp@x@p@b@`@h@p@V\\\\JP`@r@r@zA~@rBf@dAjDxH~@pBjBfEp@tAn@bATZd@p@bAjA~@bAl@t@X^t@jA^z@z@rBz@dCdBnEn@nAv@dAr@r@x@j@z@j@tAj@l@Pv@Nv@JlBHxGBnABv@DbAJz@L`AVxAh@~BbAjNpGdUdKhChAzCrAvAp@nAp@vA~@~AxAbEdDhAhAjBhBf@d@|@|@jAxAdAnAlBjCpDhFr@bAl@z@`@d@^f@pAnA|BjBlAx@lAt@LJhAh@b@NbAf@vDxAvBfAhAv@LJLJ`@Z~@|@vAfBfAvAj@dAfBrCh@n@rA|AjJfHxB~Aj@\\\\hBfAJFJHlBj@d@Pp@PVDp@LbBXlEr@nKdB~Cn@zJtBnDr@rKxBbNrChF~@tLhBlJxA~Dn@hAPhAPtGjAfEj@nCXl^z@``@z@fFJVCfFInGMnLSfEEbDG`DM|FMj@AvACvAE`So@fGGpDInJSzFDhFVbCPx@HfDb@xBZnCZrH`AbHz@vAPpAR|@Pd@L~@Z`A^l@XdBfA\\\\X\\\\XNN`@^p@t@|@pAXd@\\\\r@Xl@Tn@Ph@L\\\\p@bCf@`BpAhEFPpApE|B|HjBnG|F|RfC|IRp@Rn@bCvIvBfHnAfEj@hBl@bBd@nAdAzB~@bBNXfAhBPZ`B|BhBrBv@x@fAfAdA|@z@p@fBnAxA~@jAp@xAv@|@`@hAd@lBv@n@TdBf@|Bf@LDdAR|@N|B\\\\`BPjBNx@D|DJrKRzJl@rKlAz]hEnR|Bp^|EdEr@r@NvO~CrI`BfBZfBZzPtCx@LdAJlGr@`LbAtALhUvBvAPnCh@lKlCxh@`MfH|A`BVxG~@rK~A|Er@r@Jr@JzItAVD`Gz@`TdDLBv@Jp]hFvKhBtFx@vATj@Hd@DdAF~@BfB?|BClPYxBCpAHV@ZFh@JfAXb@N`@Nh@Xn@`@bC~ArB|A~DbD`BpAnShOf@\\\\xAdARLVNPHRJZLj@V^Ld@Ph@N~@VfBf@jErAxH~BnHbCzBt@~\\\\hK~DlAzErAdHjBfEjAjA^vFrBvAl@~DhBbFpBlBt@fBl@nBl@`B^|@RrD~@z@Th@Rj@RrAf@~DhBxD`Bv@XhBp@r@Rh@NdAV~BZn@Dt@Fn@BlBB`FKRAvCEzAEf@AzACjIQz@?hB?B?v@?xD@|@?dAA~BEvV]jCEnEIrL_@lWe@rGEjIQl@?hDGrIQb@Af@?VAV@\\\\@h@BT@\\\\Bd@Dh@DNBPBp@LZF`@Jv@TrAf@b@Rh@X^Rb@Zf@`@`@^nCfC~DtDRR`XdWTV`FzE~IxIhOrN|BxBXV^VfAp@TLdAd@l@Tb@Nt@Rr@Nz@Nh@F`AHjDTbADx@?nAC|@CfHo@vAMbBOfCW~BUbCW~BWvEc@fGm@zE_@vGW|I[|BGhHa@rAMxGs@fEc@bBKvH[hDKxEQlAIp@Gt@K`FqAdJmCnH{BdEmAv@Wx@OtFgAnGgArCc@bAQrF}@t@Mx@GN?nACl@?vABhH\\\\`CHn@DrGZjADjAAvAG|@IlAQbJwAdHeA`Di@vCi@z@OxHiB`SuE~G{A|K_CbASz@Ob@E~@Kd@CzCOpHYfFQlAGfBMj@Gl@Kv@Mr@Ot@Q|@WfMuDfFwAhD{@pA]jCk@fCg@zA[nAS|AWlC]rDc@bBSxGy@bCYv@K~@Ql@Ml@OlFiBRIrHcDlFoBfBi@fDo@r@Kn@ItBO^C^AdCEZ?X?jAB`DRnPrAdKz@TBT@xPvAl[hCvWzBvDZtCVvF^nAJj@Df@@jCHtC@nVHhC?|MD~@?hOBzZLfD?|CCnDGlCG|AAnCD~BLvHj@pG`@fBDnA@fJD`G@tFB`CAdBApDMdDWlBS~ASnFaAxBi@xGeBfCo@`@If@Iv@MhBIn@ArBBz@BnDN^@d@@p@A|AEf@At@I~@MlBW`Ca@rAY~GeBhD}@^Id@I`@GhAKh@Ch@Av@AfJCpBAxCCvA?fAB|BJt@JF@vCb@hDl@xBZ|ALfABtDDhE@bB?z@An@GTCTCb@Gd@Kb@Kl@Op@Yb@Qh@[f@[x@q@`@e@r@{@rBgCXWp@o@|@s@ZSl@_@`Bm@`@OnAWt@Ox@Kp@Cx@C`BGtCGhFO`GK^ApBI\\\\CnDMjCOrCW~B[bCc@B?dCk@xCw@hDeAdASt@KrAIjAAjABhAFtB\\\\|DhAfGlBbATbAR|Ep@nCVrAL|@HvAL`@D@?@?B?\\\\D~@JrBNpBLrBFtBJzBN`BJn@FrDVVBxCRp@F`DVhL~@dCNd@Br@F~Fb@x@HTDx@Jp@NfCr@j@R~Ax@j@\\\\p@d@^ZVVn@r@j@p@dC~CvBtCxBvCv@dAn@z@HHJPpAbBLN?@TZ~@jADFbArAX`@d@l@^f@h@p@rDbFnDvENRHJX`@DDhB`CxAjB|@rAr@bALTr@jAbBpCjArB^h@bApAPRfApAz@z@zAhAt@h@t@d@l@\\\\z@`@JDj@Vf@P`@NvEhAl@Jl@JhBTnDb@bAL|@LxIlApANjC\\\\bALjBZnCb@r@HtDj@l@HhBXhBNb@DB?@@^BdDTxBN~AJbCP^B@?f@B|AN`DV`CPr@FhAJnCPhCR`D\\\\bCVD?XDzUbCp@FxB\\\\|AVr@JTDfANjEp@j@D?@TB^BbA@vA?b@A`@?z@AhCApAAfAAp@?NAdDAbCAvEEjB?hD?|DChEEfCC|A?t@AfFApGMjGQzDOhDEpDMxAGrDQpDShCEh@AhGShBE~CIbKWf@CvBK|@Gr@If@Gd@If@Il@Kh@K`@Mn@O~@Wb@OlAc@fAe@vAs@hAu@tBsAVStA_A|AeAhDaCjEyC~CwBTOrAaAfBuAnAcAvDyCtFiEjB}ArGcFhCuBlCuBbDiCxC_CfDmCxAgA`JiHbJgH`DiCfDiCtH{FjA{@zAiApEkDrEiDf@a@dAy@~CyBx@i@dAm@zAs@bBk@XIREr@Q@?HCb@KzAYzAS`BKd@CbEArEFhHDtGBfAElAIbCYfYaFvCg@bAOD?`Cg@`B]xDmAnDqAtIeD|Aq@jAs@\\\\S|@s@t@w@n@q@jAmAjEqEhAaA~BaBFCp@a@bAe@dCgA`FaBzAi@rAi@jB}@tC_BlCgBlD}Br@k@t@o@hAqAlBuBdBkBpAeAnCmBzBgAfDiA~A[xAOpBEj@?vAAtADtBL`BNtHXzECpJk@~UkBrAOzLy@jSkBtFs@xGmA`FwAnHkC~C_AjCk@dAOhBYxDo@tDm@`Eo@fAQrBYrAUvB]~D_A~E_BdDs@`Cc@bI{@lDa@xAWdLgBfFk@zDYpC_@bBSdHaBfBg@rKaDLChBWfEc@`Fo@jDi@hKwAjFoAlFoBdIkE~As@lAi@p@YZQ|Ag@dAWlAK~@CjAEbB@J@zBTbBPbAJbCTrJ|@lGj@pEd@xBRnBLx@Bt@AdACxAC|FObH[jAGfBA^Br@BlALz@PfB`@fFvAlBb@lARnC`@dG`AvCThI\\\\fADxEJ|CFxBAdDOnESjFe@jGg@`AGjBWbAEjDIdBNhBTnHfA~@PfJxAnE`@rYhDpi@zFrE`@zF\\\\pNj@tETbFTp@BbEPfGXdFRhDJfA@dB@fBCtGY|Gk@~Fe@nE_@|Ec@~AMvAGvBAdDDxNXtGNzCLtMbAlAJvBNtF^~E^bQnAjGd@pBHZ?XAnACfDc@xCs@hDmAjCgAbDuAx@Yf@St@[JCl@SrA]lAUdB]zAGnDK|CBtGDtGIhFIfDOfDWtCc@jAYz@YnBu@RIfCeA`M}DnBo@dASp@KnCU|BWdOkB`AUbHkBfHyBtFkBnJqCnA]pIyBxCw@zCgAlAYvAY~FwA~ImArMkAtCg@pE}@bAOBAfEu@fEm@tAUbDk@fG{@vHgA~Ba@vA_@pAe@rBmAlBqAnDwBbAc@bA[rAW`BQvDYjCGbA?`AD~CX|F\\\\t@JnAPzDbAfDx@nDl@zDb@fDZxHj@rADjAFtBA|BClFBvBE~AE@?nB?xA@jOBtIr@hb@dEbOzBhZzCn@@x@?pTb@tR\\\\jHHhCBfCHjOvAbIz@lBL`DDrCBz@C`ACjEI`@?`EDhMr@\\\\BfPt@nIj@xJl@xDPtCEbCM|Ca@jDe@xAQn@Il@IZEXEd@GhAIlE]`AAjADr@F`ALfAVn@Rp@Rx@`@pDnBvGnDhIjEpEbCzHdEdDhBdHxDfFnCzEfCpH~DtCfAnBh@xB^~AP`Hb@tDVnFb@~DR`@@l@A|@Cn@En@IdAUx@QdBi@fDeAt@O`BQ|@Ir@?jA?rEL~FR`W|@tGRlCEdDs@xMwDdOmErJ{CjEaAtAK\\\\@pACzAJtBT|AZfCbAdIrEvJnFzAx@rGdExFfDr@^xCfAxTtC|ANrHX|DJtB?|CGrD_@|IiAlF]nNaB`O}AbD]nAMLAjIaAbC]`B]bCm@zA_@pAUvAG|@E`BG`EIzEMjCKzCKjAGp@GjCo@hCw@lAg@|CiAdG_CrAe@jAWnB_@FA~@Mz@MlDQnKa@bBGd@AdBStBc@DAjFuAPCbFoA~Dy@|AQbJi@pJQxCAfIUz`@gCrDOdGCfIf@tHl@vGp@VDr@LnFlANFbA\\\\THt@`@fJnDzB|@hA^t@Tt@VvCdArBXrCLj@?d@?dCS|D]f@I~ASjAQzCa@RArDe@LAhD]x@KnAKbAEbAEhAFF@rBRfC^z@PnFpAfEbAv@Pp@Jt@FbAF|@H~BZz@Rx@Lh@Pp@Z~@l@|@v@bCnCpCxCnAtAzAfAzAdA|EzD\\\\ZrF~EdFxE`EtDvEnEbAfA`AhAzAbBnElErEtEnE~ExDpE~@jA`AnA`AxAhA|AlAtBlA`BlDfDfDzB~HlDvGxBrBf@jFrAFBh@Nn@Nd@JfF~@@?|A`@tAb@jCx@jG`B|Cv@nFdAjBf@`FpBj@Xr@Vz@Tt@N|Cd@lKxAvFt@|@JlCPvEIdHqA~EsAvBi@rGaBxMmDtBe@rRyEn]}IlFuAvf@aMz@UhAYhAYvGcBnG_Bx]}IfXaHd]kIlKsC~JqBzAY~@EjCLjDb@vBXjDf@xBd@xBv@jHnDbB|@XNXNbG`DnJbFzGjDnDxBbC~BhObSvDzEdBnA|BbA|GpCvJfDfBn@rBr@tBd@RDp@DZBF?p@Dr@D|@?~ACp@EtAMpAWlA[~Ai@hAi@bAm@n@c@bAy@|AaAl@[`Ac@rBs@tEuAlEoArA[jB[`De@~Dm@TCdCa@vEs@hCa@fAYpAe@r@_@j@[l@_@x@m@p@g@DCd@[ZUh@]z@c@`A_@ZMh@OfAUhC]nB[`AS|A[l@QtBq@~CeAz@UjAWdBQ`AGxA?rAHj@F`@F~@P`FtAhD`ApCv@fAXhARfAJjBJzBLhBRVFn@LpBf@bDz@nGfBnGdBrHrBbHlBhG`BjD`AxAZr@LjAJfAFX?P@XAhAAz@Cd@EfE_@vEa@tEa@`AIvBMh@Ax@?z@BD@bF^`F`@~DZzBRjBNjABdA?lBMd@EPAj@K\\\\Iv@WlAe@dAg@`DkBd@[bF_DfHiE|DeCjBiAdEkCpB{AbA_A|@cA|@oA|@aBj@uA`@gAT{@XoAXgBPoBPiB?IZsDt@wHDa@NoBP{BN_BP}AP_ATcATw@Zy@^y@t@kAd@m@j@m@rAmAfAeAhAgAV[Xc@t@cArCkEpAwBhBsC`@u@\\\\y@^gA@CViATgATcBLcC@SJuBHyAHyBJiBDeAB}@?aA?mA@oB?kB?aADeABe@@CFk@Hi@Lk@ZqAXmAZwATqAb@qCT_BPw@Li@Ru@f@aBj@_B^oA\\\\}@\\\\_AxAmDh@wAx@yBPi@Pq@Nq@Lm@Ji@Hi@D_@Ho@PmAP_AR_ATq@h@sAn@iAHOFGHMZa@`@c@hAkAbCgCbCcC`EkEbCiCd@g@zB}Bb@c@hAiAhAmA`BkBlDmE~@iAp@{@z@gAl@cAf@aAh@mAXy@`@cA^y@j@eAr@gAt@eAp@w@p@q@j@g@x@q@hAw@nA}@dA{@^a@f@m@p@aAl@oAh@uALc@To@Pc@\\\\{@^q@Vg@rAoBv@iAdCaDrDqE|AmBlBgCfAkAfAaAjA}@pAw@jAq@XMRIr@[jBs@xDyAnAg@dAe@r@_@p@_@p@c@v@m@dA}@j@k@xB_CzCcDvBcCzAgBdAeAr@o@t@i@n@a@|@a@`A_@n@Qj@Mh@Kz@Kn@EzBInDI`FMhDG~BEdACL?t@Bd@@vALvAVhBj@pE`B`FfBfExAr@V~Al@`AVp@Nn@Lf@Fh@Dt@Bv@Bz@Ar@E`@Ez@Kp@Mr@Qn@Uj@Sv@]f@[t@e@ZUXUVWVYXYn@y@t@}@bAoA~@cA^_@l@k@`@]h@e@`As@|AcAhCcB|ByAf@]j@c@z@s@b@a@^c@b@k@f@o@`@i@TYTWVYRURSnAiA|BsB|A{ArA{A^c@jBuB|@_Al@k@l@e@f@[r@a@t@]r@Wf@QhAWTEt@K~@Ix@Cj@?bABj@BhAJ`E`@fCXhD^zBRx@DfAB`ACv@EfAM|@On@Qd@MfA_@hDwAj@WdBo@`C_AtAk@z@[f@QpDgAdD{@bDy@pD_ATIjD{@pEiAzBa@~ASrBQvAEzBCzADnAFd@B~@HhBNn@F|@D^@|@?j@?~@Ix@MfASp@QXIVIVKNGVKl@[ZSZSjBcBd@g@t@_Aj@o@dAiAd@_@VSl@_@v@g@VMx@e@d@QvC_AfCaArAg@rAc@xBi@xA[lB[x@Ml@IzAOpAIdBIn@CvAIhBGjAI`AIzCUr@GnBKfBQjB]n@St@UjBu@vCqApF_CfBe@jC[hBId@@P?pE@H?tGBtCAz@Cz@G|@KvAWf@Q|@W~@a@l@a@n@e@hA_AFI`B_CzAmCp@sAlH{NpHoMnNoUvB_DpFaJfAuAzAoAfDgCvGuF@C|RkO~AqAp@e@`BaAdBu@VIpCcAzAc@|C_@|DUbBC|B@hDVF?z@FbADvBPhBNvLhA`Fh@hF\\\\bBD~B@hC@nD?hA?pBCz@G`De@tX{EjGmAnAW^Kx@Yr@]v@e@~@m@lH_Gx@o@pAgAf@e@d@k@~@iAz@iAtBkChCeD|AyBjK}MDEbH_JtCeDnFcGbHoHzEsFdCuChG}GfAeAn@o@b@_@xAy@zA}@bEkB~FkClBy@xCgAjKoC`@OVIx@]~Ay@x@g@rHwF`@]\\\\SZUl@[fB}@hA_@p@Wd@Qn@UbEgBtBoA`CkA|@Yj@OfAUhAWd@IlGaAhF_AvB_@zBc@|Aa@~Ak@jBw@fBu@bBi@pAY~Cq@pFiAvFcAvKyBxIsBjOkGpV{KnFaCzB_ApAo@lAw@t@q@v@w@~EiG|OqS~BuCxL_Pv@cA|LyOjA}AX[pAaBx@eAT]`@i@n@}@jA}AxBuCRWZ]JINQTS\\\\YVQNKNKRMXOv@e@l@Yl@Yj@]b@S^Sp@]t@a@NIdAi@z@e@x@c@tAu@\\\\Sx@a@z@e@n@]p@_@r@a@`By@hBaAxAw@hHsDfG_Dt@_@~BmAlCwA~CcBpDmBdDgBfBcAxCaBrB_AnFwCzAw@tAi@x@Wr@OpEu@pD_@jEi@`H}@nBU`YqDfe@aGdD]vC]dBO|@?zBEjJStDMdBUn@It@OxBe@tBa@hE_AfAQ|AMt@El@Eb@@~@Dh@DhBXnFx@dKjBlZlFbC`@hFt@lCRlAHv\\\\n@`PTvZj@jXf@xLNfD?`C?hA@je@Tfk@ZB?tNHdFB`KD|HBbLHl[HzNFrPJ~LFx@?x@@r@@r@@lEDD?`KDp_@NhCIx\\\\kAlIU~AI|AIrBMvAK`BMz@GlAGxBGzCIxMe@nBGhVcAlMm@zKWvNg@vEY|AUdC}@v@Oz@c@fAo@x@i@t@q@|E{D`EaDRQv@i@LI|@g@NKv@_@fAc@fBk@lBe@hB[jBWxAKjCGlCAp@D|Ox@rMt@vFb@vJx@r^pDlAPjD^hCXnAL~APfBPlEf@rC^zCd@nFt@bBV`i@lHxk@zHtPfC`SrChAPdD^hb@pEhEf@`DZlPlB`AJ~N~ApXpCdCXvS|BfIz@|Ed@dAJ~Iv@pMnAtD^zGb@tBTlBRtC^dC`@nA\\\\LDrA`@vErBbF~B`DnAhCfAjIzDbM~F~H~DjC`AdBt@fNdGvSbJxD`BnBv@t@^hFzBzCrAl@V^N`@L`@NdAVnATd@Fj@Ff@Bl@Dj@B`A@bA@dABn@@|@Bj@BjBDdCD|@B^@n@BnABbABl@BJ?P@n@@bA@lAB`AB`AB|@B`@@rBDpBF`CF`ABx@BlABbB@vADxA?dABr@Fx@FjAPlAVf@LjAb@ZN`@P~@d@dAr@n@b@rA|@dBfAbBbA~BnArAr@lAj@xC`A`@HvAd@t@TrA^THl@Pz@Xz@Vv@X~@VjA\\\\rAd@fA\\\\t@ThA\\\\jA\\\\lA^vBt@p@Pv[pJTFzAd@tA^zAd@fBf@|Bp@jCx@jBl@\\\\HJBlDhAxBp@vBr@jBj@`A\\\\r@ZtBp@z@XrAb@pA`@zAd@|Ad@z@THBfBj@LDbCt@|Br@lDdA`Bj@hEjAbBf@xBp@tA`@lA`@|@VjA^lA^H@xAf@xAb@hA^HBpA`@hBj@nBn@fBd@|@XfCt@H@lCx@|Ab@nCv@bBd@pA\\\\l@Px@TxAb@hBf@rBj@`AXvBn@|Bn@xAb@lA\\\\rA^zAb@~@VhA\\\\jA\\\\fBf@~Ab@jA\\\\lBf@rBj@hA\\\\rCx@lA\\\\bBf@f@N~Bl@jCt@zCz@lCv@`D~@rA\\\\bAZdBd@jD`AjAXhBf@^JfAZ~Bl@hCr@nBh@|@V~Bp@hCr@pA`@^JjA\\\\pBj@vA^|Bp@p@PrElAlAZdBf@|Ab@|Ab@fAZrA\\\\jAPx@N`ADn@Bx@@zACj@E`@C`@GrAQ~@Sl@QhA[bCw@jDcAjDaAjBg@j@Q@?l@QvGoBfFwA|DiApGoB~C}@zAc@rBk@~Ac@PGn@Sv@WvAc@`DeAfD}@dCq@zBo@zEwA~E{A|EaBpEuAtBm@nBa@lB]rBQvBKrBAlB@`DPfCN|@DlERdFZT@dENV@\\\\@B?XBtAFnEVnETtETzEX^@b@BH@V@nE^dFTT@hIb@vCLxBJlCLtCLH@~@Dn@DrAL`BNxAJdBFlBFtBB~AFxDPtCNfCJpBJdCP|B^xBd@xBn@lBv@bAh@|A~@RL~@p@p@j@v@r@TTRTbAfAjA|A`BrC|AhCbCxDb@p@\\\\j@p@jAnB|Cb@l@fRpZzEzHlA|A`B`B|AbAb@PTN`Bn@jCh@VLbBPlAFfABbE@p@@|EH`ED|DFlEBrEFjA@dB@rCDfGBjB@hB?dB?bBB~@?\\\\@\\\\?|q@b@rKC`QEfB@pBAfA?hA@hBAtFJlNTrPBdA?nKA|FBtE?`C?dF?|F?xG@f@?|E@x@?dD?fGAbDAtE?jD?vCA~C@~CAX?`CAB?hC?nG?pG@bG@|F@bH?nA@lE?`G@pG@nH?vF?jC@xA?fF@zC@`A?bE@V?jFAfFAdEAx@?pGCtGAnFAbBAdDArC?jA?xA@zA@`EBZ@nADnADD?V@b@FL@`BV`F`ALBpEp@fDb@p@HbKdArN~AdCXnEf@pN|Al@FzBVfCZbCX|BZvC^dD\\\\pCXRBzC^zATfCZ~BV`CXzBTxBVnD^ZFxBXbCX`CZhCZtBV|BZbC\\\\j@Hj@J|Bh@ZJ|@VjA`@RHhBv@zAz@|ChBpC~A~CnBtCbBdCzAjBfAdBbAnAz@tA`ApAlAx@r@|@bAl@t@rApB@?xAjCJRj@nAfAxBn@pAZn@j@~@p@~@h@p@x@|@~@z@z@r@bAn@l@\\\\PJpAl@hAd@zAd@z@Rz@PlC`@pBXtBZfBV|B^jB\\\\tB`@pATNBF@xB\\\\xBZ|AVd@HjCp@hBp@xAr@tAx@~@j@r@d@\\\\ZRP^b@bApAz@jAzBzCv@dAfDvE~DtFdBbCx@dAHJd@h@bAdA~BzBrCpCfBbBpAlAlAjAbB`BzAxA`A|@vAtAvArAhBfBjAjAfBbBv@r@lBlBJJjDdDfCbCpAnAp@n@bA`AxGrGjEfEnFbFnOtNf@b@v@x@lEdEt@r@fE|DbEzD|CzCdB`B?@lBlBt@p@r@p@vAvAhAjAfGnGjAjAnBlB~@z@lGdGfJzIjBfBtChClCdCdCzBtAnAZXlFzEf@f@TRd@f@`@d@^f@X^\\\\d@^n@V`@Xf@z@~Ah@`ArCfF~AtCd@x@~ArCfCzEtJ|PtCdFtArCR`@x@bB~FxJnAvBRd@z@dCx@vA^j@b@h@RN|A~@pA|Bp@hA~@|ADFZf@z@nA\"\n" +
                "                     },\n" +
                "                     \"start_location\" : {\n" +
                "                        \"lat\" : 16.7808664,\n" +
                "                        \"lng\" : 78.13760320000002\n" +
                "                     },\n" +
                "                     \"travel_mode\" : \"DRIVING\"\n" +
                "                  },\n" +
                "                  {\n" +
                "                     \"distance\" : {\n" +
                "                        \"text\" : \"4.7 km\",\n" +
                "                        \"value\" : 4661\n" +
                "                     },\n" +
                "                     \"duration\" : {\n" +
                "                        \"text\" : \"5 mins\",\n" +
                "                        \"value\" : 272\n" +
                "                     },\n" +
                "                     \"end_location\" : {\n" +
                "                        \"lat\" : 13.156781,\n" +
                "                        \"lng\" : 77.6234879\n" +
                "                     },\n" +
                "                     \"html_instructions\" : \"Continue straight to stay on \\u003cb\\u003eNH 44\\u003c/b\\u003e\",\n" +
                "                     \"maneuver\" : \"straight\",\n" +
                "                     \"polyline\" : {\n" +
                "                        \"points\" : \"mnooAmm|xMXb@t@|@h@h@t@r@\\\\Xb@Zv@h@z@j@hBhApD|BzGrEhBjAlBnAvE|CdAp@TPLH\\\\R~@l@FBfAt@tD|BZRh@^bAp@zCpBxA`Aj@^JFvA~@vA~@bAn@n@b@n@`@~AdAtA|@hCbBFDbAp@fEvCxCvBVPv@d@j@Zf@Tp@V|Br@fA\\\\bBf@d@Pte@bQ`Bn@l@ZxA`AjE|CbD|Bz@p@|ErDlAx@lAz@`CzAn@b@`@XxBxAfEvCjA|@lD~B\"\n" +
                "                     },\n" +
                "                     \"start_location\" : {\n" +
                "                        \"lat\" : 13.1915882,\n" +
                "                        \"lng\" : 77.6471086\n" +
                "                     },\n" +
                "                     \"travel_mode\" : \"DRIVING\"\n" +
                "                  },\n" +
                "                  {\n" +
                "                     \"distance\" : {\n" +
                "                        \"text\" : \"0.7 km\",\n" +
                "                        \"value\" : 687\n" +
                "                     },\n" +
                "                     \"duration\" : {\n" +
                "                        \"text\" : \"1 min\",\n" +
                "                        \"value\" : 66\n" +
                "                     },\n" +
                "                     \"end_location\" : {\n" +
                "                        \"lat\" : 13.1514591,\n" +
                "                        \"lng\" : 77.62033459999999\n" +
                "                     },\n" +
                "                     \"html_instructions\" : \"Slight \\u003cb\\u003eleft\\u003c/b\\u003e\",\n" +
                "                     \"maneuver\" : \"turn-slight-left\",\n" +
                "                     \"polyline\" : {\n" +
                "                        \"points\" : \"{thoAyywxMdAHpEvCb@VzCbBv@f@^Th@Xp@\\\\r@Zj@XrBz@nB~@\\\\LJFl@Z\"\n" +
                "                     },\n" +
                "                     \"start_location\" : {\n" +
                "                        \"lat\" : 13.156781,\n" +
                "                        \"lng\" : 77.6234879\n" +
                "                     },\n" +
                "                     \"travel_mode\" : \"DRIVING\"\n" +
                "                  },\n" +
                "                  {\n" +
                "                     \"distance\" : {\n" +
                "                        \"text\" : \"0.1 km\",\n" +
                "                        \"value\" : 146\n" +
                "                     },\n" +
                "                     \"duration\" : {\n" +
                "                        \"text\" : \"1 min\",\n" +
                "                        \"value\" : 18\n" +
                "                     },\n" +
                "                     \"end_location\" : {\n" +
                "                        \"lat\" : 13.1502867,\n" +
                "                        \"lng\" : 77.6197191\n" +
                "                     },\n" +
                "                     \"html_instructions\" : \"Keep \\u003cb\\u003eleft\\u003c/b\\u003e to continue toward \\u003cb\\u003eNH 44\\u003c/b\\u003e\",\n" +
                "                     \"maneuver\" : \"keep-left\",\n" +
                "                     \"polyline\" : {\n" +
                "                        \"points\" : \"ssgoAafwxMlClAzAj@\"\n" +
                "                     },\n" +
                "                     \"start_location\" : {\n" +
                "                        \"lat\" : 13.1514591,\n" +
                "                        \"lng\" : 77.62033459999999\n" +
                "                     },\n" +
                "                     \"travel_mode\" : \"DRIVING\"\n" +
                "                  },\n" +
                "                  {\n" +
                "                     \"distance\" : {\n" +
                "                        \"text\" : \"12.2 km\",\n" +
                "                        \"value\" : 12245\n" +
                "                     },\n" +
                "                     \"duration\" : {\n" +
                "                        \"text\" : \"11 mins\",\n" +
                "                        \"value\" : 674\n" +
                "                     },\n" +
                "                     \"end_location\" : {\n" +
                "                        \"lat\" : 13.0462212,\n" +
                "                        \"lng\" : 77.5914762\n" +
                "                     },\n" +
                "                     \"html_instructions\" : \"Take the ramp on the \\u003cb\\u003eright\\u003c/b\\u003e onto \\u003cb\\u003eNH 44\\u003c/b\\u003e\\u003cdiv style=\\\"font-size:0.9em\\\"\\u003ePass by The Parsi Tower Of Silence (on the left in 11.4&nbsp;km)\\u003c/div\\u003e\",\n" +
                "                     \"polyline\" : {\n" +
                "                        \"points\" : \"ilgoAgbwxMhB|AzB`A`CdAtBz@f@Rb@L^L^Hf@Jb@F`@Dv@FbBJpDRfCNn@Dx@B~ADtADt@Bj@?F?rDCdGG^?vFIfFAhCA^@d@Dt@Jh@Jl@NrBl@vAb@xEbBjFlBt@VjBr@LD@@\\\\LfA\\\\z@\\\\zEhBdDnAtYtKvAf@pFpB~D|AZJZLnAd@pEbBhDvAhAd@nCjA`CbA`ChAdIdEvAt@jFpCpJbFb@T`@TpBdAlEzB`Bz@pCtAvFtCdBz@x@`@|Ax@fAh@nAl@`@Rf@R|@^ZJbA\\\\fA\\\\|@VVHXFdARv@Nx@N`C`@vJbBdC`@xCl@pAZn@NxA`@F@lErAfGfBn@NZJrAXhARnDh@hC^zEp@p@JRBt@Ft@Lx@RpA\\\\pA\\\\jBj@j@RfJ`DlA`@HBr@TbBf@FBpHvB`Bb@pA\\\\dARhANr@HVBb@DpCL|CJhSx@~CNnCJpBDdA@dA?rBAP?PAzBGpSk@tIYz@E^C^AfBKdCI`AAhDCrAAhDEfIQjEI`CCrB?vGBrB?`A@X?fC@lC@fCCxBIv@At@EjAAd@A`@@b@?ZBP?^BXBd@F|Cd@fAPhBXzCf@|@N^Hz@PZFl@Px@ZjAd@`Cz@dBl@pB^NB\"\n" +
                "                     },\n" +
                "                     \"start_location\" : {\n" +
                "                        \"lat\" : 13.1502867,\n" +
                "                        \"lng\" : 77.6197191\n" +
                "                     },\n" +
                "                     \"travel_mode\" : \"DRIVING\"\n" +
                "                  },\n" +
                "                  {\n" +
                "                     \"distance\" : {\n" +
                "                        \"text\" : \"3.4 km\",\n" +
                "                        \"value\" : 3437\n" +
                "                     },\n" +
                "                     \"duration\" : {\n" +
                "                        \"text\" : \"6 mins\",\n" +
                "                        \"value\" : 366\n" +
                "                     },\n" +
                "                     \"end_location\" : {\n" +
                "                        \"lat\" : 13.0164909,\n" +
                "                        \"lng\" : 77.5840403\n" +
                "                     },\n" +
                "                     \"html_instructions\" : \"Continue straight onto \\u003cb\\u003eBellary Rd\\u003c/b\\u003e/\\u003cb\\u003eHebbal Flyover\\u003c/b\\u003e\\u003cdiv style=\\\"font-size:0.9em\\\"\\u003eContinue to follow Bellary Rd\\u003c/div\\u003e\\u003cdiv style=\\\"font-size:0.9em\\\"\\u003ePass by Saaga (on the left in 1.1&nbsp;km)\\u003c/div\\u003e\",\n" +
                "                     \"maneuver\" : \"straight\",\n" +
                "                     \"polyline\" : {\n" +
                "                        \"points\" : \"{asnAwqqxM~@ZZBrBLF@L@L@H@H@LBJ@J@HBJ@JBF@HBPBJBLDJBH@LDNDJ@NB`@N~@b@LDz@\\\\f@Pr@P|@P~@TJ@NDVDfANzG|@`BPx@FfAHnAHjCJrA@fA@fABVBZDvAb@b@N`Bj@x@RjA\\\\b@Hh@FN@z@DN@l@Jp@LZLB?JDtAj@xBx@xAf@nC~@XJjEpALDnDlAhCx@bB`@~AXnCj@vBZRBH@H@@?pHjA`ANbALZDP@P@\\\\@J@tAV^HdBTj@NbEl@pBFjBB~DCpEEtACx@?\"\n" +
                "                     },\n" +
                "                     \"start_location\" : {\n" +
                "                        \"lat\" : 13.0462212,\n" +
                "                        \"lng\" : 77.5914762\n" +
                "                     },\n" +
                "                     \"travel_mode\" : \"DRIVING\"\n" +
                "                  },\n" +
                "                  {\n" +
                "                     \"distance\" : {\n" +
                "                        \"text\" : \"1.5 km\",\n" +
                "                        \"value\" : 1543\n" +
                "                     },\n" +
                "                     \"duration\" : {\n" +
                "                        \"text\" : \"2 mins\",\n" +
                "                        \"value\" : 146\n" +
                "                     },\n" +
                "                     \"end_location\" : {\n" +
                "                        \"lat\" : 13.0026247,\n" +
                "                        \"lng\" : 77.58417\n" +
                "                     },\n" +
                "                     \"html_instructions\" : \"Continue straight to stay on \\u003cb\\u003eBellary Rd\\u003c/b\\u003e\\u003cdiv style=\\\"font-size:0.9em\\\"\\u003ePass by Kalyani Bajaj Showroom (on the right in 350&nbsp;m)\\u003c/div\\u003e\",\n" +
                "                     \"maneuver\" : \"straight\",\n" +
                "                     \"polyline\" : {\n" +
                "                        \"points\" : \"ahmnAgcpxMhHEl@@v@@v@@`AB~FFtBDb@?pBFr@EnA?jAAr@AzD@hEKnFMn@@nIWdAAl@?pID\"\n" +
                "                     },\n" +
                "                     \"start_location\" : {\n" +
                "                        \"lat\" : 13.0164909,\n" +
                "                        \"lng\" : 77.5840403\n" +
                "                     },\n" +
                "                     \"travel_mode\" : \"DRIVING\"\n" +
                "                  },\n" +
                "                  {\n" +
                "                     \"distance\" : {\n" +
                "                        \"text\" : \"0.4 km\",\n" +
                "                        \"value\" : 429\n" +
                "                     },\n" +
                "                     \"duration\" : {\n" +
                "                        \"text\" : \"1 min\",\n" +
                "                        \"value\" : 60\n" +
                "                     },\n" +
                "                     \"end_location\" : {\n" +
                "                        \"lat\" : 12.998768,\n" +
                "                        \"lng\" : 77.58427209999999\n" +
                "                     },\n" +
                "                     \"html_instructions\" : \"Merge onto \\u003cb\\u003eGuttahalli Main Rd\\u003c/b\\u003e/\\u003cb\\u003eRamana Maharshi Rd\\u003c/b\\u003e/\\u003cb\\u003eSankey Rd\\u003c/b\\u003e\",\n" +
                "                     \"maneuver\" : \"merge\",\n" +
                "                     \"polyline\" : {\n" +
                "                        \"points\" : \"kqjnAadpxM|IAj@?rBEbGK\"\n" +
                "                     },\n" +
                "                     \"start_location\" : {\n" +
                "                        \"lat\" : 13.0026247,\n" +
                "                        \"lng\" : 77.58417\n" +
                "                     },\n" +
                "                     \"travel_mode\" : \"DRIVING\"\n" +
                "                  },\n" +
                "                  {\n" +
                "                     \"distance\" : {\n" +
                "                        \"text\" : \"1.5 km\",\n" +
                "                        \"value\" : 1485\n" +
                "                     },\n" +
                "                     \"duration\" : {\n" +
                "                        \"text\" : \"3 mins\",\n" +
                "                        \"value\" : 172\n" +
                "                     },\n" +
                "                     \"end_location\" : {\n" +
                "                        \"lat\" : 12.9868286,\n" +
                "                        \"lng\" : 77.5887577\n" +
                "                     },\n" +
                "                     \"html_instructions\" : \"Keep \\u003cb\\u003eright\\u003c/b\\u003e to stay on \\u003cb\\u003eGuttahalli Main Rd\\u003c/b\\u003e/\\u003cb\\u003eRamana Maharshi Rd\\u003c/b\\u003e/\\u003cb\\u003eSankey Rd\\u003c/b\\u003e\\u003cdiv style=\\\"font-size:0.9em\\\"\\u003eContinue to follow Sankey Rd\\u003c/div\\u003e\\u003cdiv style=\\\"font-size:0.9em\\\"\\u003ePass by Le Méridien Bangalore (on the left in 900&nbsp;m)\\u003c/div\\u003e\",\n" +
                "                     \"maneuver\" : \"keep-right\",\n" +
                "                     \"polyline\" : {\n" +
                "                        \"points\" : \"iyinAudpxMfD[d@Gd@GtASNC`@I@?~@SXEb@GxH_At@ExBOrAEb@I^Eb@EjCUnEUnCUdAGhAMb@ETETGNGNIPKFGjCcDdCaDtBgCPYHQFS\"\n" +
                "                     },\n" +
                "                     \"start_location\" : {\n" +
                "                        \"lat\" : 12.998768,\n" +
                "                        \"lng\" : 77.58427209999999\n" +
                "                     },\n" +
                "                     \"travel_mode\" : \"DRIVING\"\n" +
                "                  },\n" +
                "                  {\n" +
                "                     \"distance\" : {\n" +
                "                        \"text\" : \"0.7 km\",\n" +
                "                        \"value\" : 695\n" +
                "                     },\n" +
                "                     \"duration\" : {\n" +
                "                        \"text\" : \"2 mins\",\n" +
                "                        \"value\" : 116\n" +
                "                     },\n" +
                "                     \"end_location\" : {\n" +
                "                        \"lat\" : 12.9842192,\n" +
                "                        \"lng\" : 77.5926534\n" +
                "                     },\n" +
                "                     \"html_instructions\" : \"\\u003cb\\u003eSankey Rd\\u003c/b\\u003e turns slightly \\u003cb\\u003eleft\\u003c/b\\u003e and becomes \\u003cb\\u003eRaj Bhavan Rd\\u003c/b\\u003e\\u003cdiv style=\\\"font-size:0.9em\\\"\\u003ePass by the park (on the right)\\u003c/div\\u003e\",\n" +
                "                     \"polyline\" : {\n" +
                "                        \"points\" : \"ungnAw`qxM?EDIJKHEJCPCLKPOhAaBpHuFzDmDPSt@iABUQY}AWcBW\"\n" +
                "                     },\n" +
                "                     \"start_location\" : {\n" +
                "                        \"lat\" : 12.9868286,\n" +
                "                        \"lng\" : 77.5887577\n" +
                "                     },\n" +
                "                     \"travel_mode\" : \"DRIVING\"\n" +
                "                  },\n" +
                "                  {\n" +
                "                     \"distance\" : {\n" +
                "                        \"text\" : \"0.5 km\",\n" +
                "                        \"value\" : 518\n" +
                "                     },\n" +
                "                     \"duration\" : {\n" +
                "                        \"text\" : \"2 mins\",\n" +
                "                        \"value\" : 107\n" +
                "                     },\n" +
                "                     \"end_location\" : {\n" +
                "                        \"lat\" : 12.9826812,\n" +
                "                        \"lng\" : 77.59715299999999\n" +
                "                     },\n" +
                "                     \"html_instructions\" : \"Turn \\u003cb\\u003eright\\u003c/b\\u003e onto \\u003cb\\u003eInfantry Rd\\u003c/b\\u003e\\u003cdiv style=\\\"font-size:0.9em\\\"\\u003ePass by The Indian Express (on the left in 400&nbsp;m)\\u003c/div\\u003e\",\n" +
                "                     \"maneuver\" : \"turn-right\",\n" +
                "                     \"polyline\" : {\n" +
                "                        \"points\" : \"k~fnAayqxM|DsRH]Rs@d@sAp@iB\"\n" +
                "                     },\n" +
                "                     \"start_location\" : {\n" +
                "                        \"lat\" : 12.9842192,\n" +
                "                        \"lng\" : 77.5926534\n" +
                "                     },\n" +
                "                     \"travel_mode\" : \"DRIVING\"\n" +
                "                  },\n" +
                "                  {\n" +
                "                     \"distance\" : {\n" +
                "                        \"text\" : \"0.7 km\",\n" +
                "                        \"value\" : 724\n" +
                "                     },\n" +
                "                     \"duration\" : {\n" +
                "                        \"text\" : \"3 mins\",\n" +
                "                        \"value\" : 177\n" +
                "                     },\n" +
                "                     \"end_location\" : {\n" +
                "                        \"lat\" : 12.9766844,\n" +
                "                        \"lng\" : 77.5993434\n" +
                "                     },\n" +
                "                     \"html_instructions\" : \"Turn \\u003cb\\u003eright\\u003c/b\\u003e after Prestige Nebula (on the right)\",\n" +
                "                     \"maneuver\" : \"turn-right\",\n" +
                "                     \"polyline\" : {\n" +
                "                        \"points\" : \"wtfnAeurxMnCXH@RC\\\\GXCb@E^IRGLGf@e@|@]dIsCnFkBv@YlAe@z@[DA\"\n" +
                "                     },\n" +
                "                     \"start_location\" : {\n" +
                "                        \"lat\" : 12.9826812,\n" +
                "                        \"lng\" : 77.59715299999999\n" +
                "                     },\n" +
                "                     \"travel_mode\" : \"DRIVING\"\n" +
                "                  },\n" +
                "                  {\n" +
                "                     \"distance\" : {\n" +
                "                        \"text\" : \"0.8 km\",\n" +
                "                        \"value\" : 751\n" +
                "                     },\n" +
                "                     \"duration\" : {\n" +
                "                        \"text\" : \"2 mins\",\n" +
                "                        \"value\" : 116\n" +
                "                     },\n" +
                "                     \"end_location\" : {\n" +
                "                        \"lat\" : 12.9723585,\n" +
                "                        \"lng\" : 77.59425299999999\n" +
                "                     },\n" +
                "                     \"html_instructions\" : \"Turn \\u003cb\\u003eright\\u003c/b\\u003e onto \\u003cb\\u003eKasturba Rd\\u003c/b\\u003e\",\n" +
                "                     \"maneuver\" : \"turn-right\",\n" +
                "                     \"polyline\" : {\n" +
                "                        \"points\" : \"goenA{bsxMPODd@~@xAn@|@pB~ChBvCDFFJDD^d@zBnCNTX^z@hAFFDFDDFFJHFFJHPJvC|B\"\n" +
                "                     },\n" +
                "                     \"start_location\" : {\n" +
                "                        \"lat\" : 12.9766844,\n" +
                "                        \"lng\" : 77.5993434\n" +
                "                     },\n" +
                "                     \"travel_mode\" : \"DRIVING\"\n" +
                "                  },\n" +
                "                  {\n" +
                "                     \"distance\" : {\n" +
                "                        \"text\" : \"81 m\",\n" +
                "                        \"value\" : 81\n" +
                "                     },\n" +
                "                     \"duration\" : {\n" +
                "                        \"text\" : \"1 min\",\n" +
                "                        \"value\" : 44\n" +
                "                     },\n" +
                "                     \"end_location\" : {\n" +
                "                        \"lat\" : 12.9718847,\n" +
                "                        \"lng\" : 77.59460829999999\n" +
                "                     },\n" +
                "                     \"html_instructions\" : \"Turn \\u003cb\\u003eleft\\u003c/b\\u003e onto \\u003cb\\u003eGrant Rd\\u003c/b\\u003e/\\u003cb\\u003eVittal Mallya Rd\\u003c/b\\u003e\",\n" +
                "                     \"maneuver\" : \"turn-left\",\n" +
                "                     \"polyline\" : {\n" +
                "                        \"points\" : \"gtdnAacrxMXBD?B?F?D?D?DAFAXgA\"\n" +
                "                     },\n" +
                "                     \"start_location\" : {\n" +
                "                        \"lat\" : 12.9723585,\n" +
                "                        \"lng\" : 77.59425299999999\n" +
                "                     },\n" +
                "                     \"travel_mode\" : \"DRIVING\"\n" +
                "                  },\n" +
                "                  {\n" +
                "                     \"distance\" : {\n" +
                "                        \"text\" : \"32 m\",\n" +
                "                        \"value\" : 32\n" +
                "                     },\n" +
                "                     \"duration\" : {\n" +
                "                        \"text\" : \"1 min\",\n" +
                "                        \"value\" : 14\n" +
                "                     },\n" +
                "                     \"end_location\" : {\n" +
                "                        \"lat\" : 12.9716014,\n" +
                "                        \"lng\" : 77.5945493\n" +
                "                     },\n" +
                "                     \"html_instructions\" : \"Turn \\u003cb\\u003eright\\u003c/b\\u003e\",\n" +
                "                     \"maneuver\" : \"turn-right\",\n" +
                "                     \"polyline\" : {\n" +
                "                        \"points\" : \"gqdnAierxMv@J\"\n" +
                "                     },\n" +
                "                     \"start_location\" : {\n" +
                "                        \"lat\" : 12.9718847,\n" +
                "                        \"lng\" : 77.59460829999999\n" +
                "                     },\n" +
                "                     \"travel_mode\" : \"DRIVING\"\n" +
                "                  }\n" +
                "               ],\n" +
                "               \"traffic_speed_entry\" : [],\n" +
                "               \"via_waypoint\" : []\n" +
                "            }\n" +
                "         ],\n" +
                "         \"overview_polyline\" : {\n" +
                "            \"points\" : \"snbiBsl`~Mi|@`Ece@f{@kTfcC|a@ty@pRfX{@by@jtB~OdxAvG`eAp_A`n@zCfe@mv@jfATp_@}`@n|@bn@dbEluAnfEp~E`cD`zDvvBhdBn}@hpAh{AtkA|tD`iA|^}Frc@|_@haClnBdeI~_I|tCxx@bb@fG|Mbg@bvDlf@n{Ch^t~Gvq@pmD`[dkCru@zkH`nAjsIrqBntCnkCdiB`{BlcBvgCx{BzxBb|FrcFnkBrcBbvB|{ApcKrtDhiEbxAriBhq@tvBwB|oHyQnoIyRpr@fr@nj@lPzmBu}AfmDmXvpJsd@~`DeXrnA_Tf_Aen@v`A{sAh]]~jB{D~aCnwBlY~tCjkBflExiBtgBtpBnb@`nEc{Af~FadC`iCyhAdsAccBbwDwdFtaE{sExqLve@lmCp{@rkAuy@fhEyzB~z@IjgB`Zn}D|bBd~Fj{Bxw@za@js@`tAlyE`hBfsDvg@~hAuR`mCxq@d|DjlB~oCdVrzCl_ApmC`kA~xDjbAfzDxl@lbBjZtlBrvDlcFdsClhB`rAdaBxh@`fDpwApqCtrB|bBrt@`{@pdBfXxdA|kAdq@dtBlr@vp@jEfp@nn@jx@pqAr\\\\~m@rf@|YluB`vAbiB|{@~hDlZ|tB}Bbt@zNlThk@te@rxA~h@b]paIfiAxpDbb@dbDjtAfgBpXllCuApqBtvAttI_bAn|Bo_@d`AsHvoC|O|fEgGv|AwFjbAsR~tBeAb{AvMxu@l~@vlAdn@fjBzQbjBt@teBmS|xCc|BvaC}`@ndB__AhaFym@xxCwb@xtCnItrFrSb`DqDrfEsv@thAaXz~@~CniGtYn|AvAdx@tTt|Axe@ljAcBh|@iTdf@bQb{@nUbeAcIb`AuIz|@aPttBaPbdA|T~|AdCp~BpwBn`AlVf~AkAhmForAvoAdg@zcArm@nm@gSjqAo]`yB|`@t_BwLbi@cp@|Kyk@v[wmA`xA_sCn|@eaAnsAeh@xaAlEbx@yu@faDy_@j~Cwu@t~AosBpq@_Gf`B}Fby@_}@h{Bu{Azv@iTbaC_lA`dAwiAvhBu|@dbCuVlwA~IdxLnGnzBsIfj@eXhvBvF`nEpk@p_Dv[laB`p@lxAr\\\\loDv|@dpDlfAneBdf@z~A_G|cCk^xoB|Jlh@tQbx@~jAhzD|EleJdAdxC`^|wAnv@dfA|c@~sAlrAzbCf_CtbB`_CrrDrbCt}At_AryAlNzqFltBbaBr`@p|Ca@b{AnTx|Cld@tyB{Bp~@uYhUml@vb@oPf`@x\\\\~A_A\"\n" +
                "         },\n" +
                "         \"summary\" : \"NH 44\",\n" +
                "         \"warnings\" : [],\n" +
                "         \"waypoint_order\" : []\n" +
                "      }\n" +
                "   ],\n" +
                "   \"status\" : \"OK\"\n" +
                "}";
        //</editor-fold>

        //<editor-fold desc="Canada route">
        response = "{\n" +
                "   \"geocoded_waypoints\" : [\n" +
                "      {\n" +
                "         \"geocoder_status\" : \"OK\",\n" +
                "         \"place_id\" : \"ChIJpTvG15DL1IkRd8S0KlBVNTI\",\n" +
                "         \"types\" : [ \"locality\", \"political\" ]\n" +
                "      },\n" +
                "      {\n" +
                "         \"geocoder_status\" : \"OK\",\n" +
                "         \"place_id\" : \"ChIJDbdkHFQayUwR7-8fITgxTmU\",\n" +
                "         \"types\" : [ \"locality\", \"political\" ]\n" +
                "      }\n" +
                "   ],\n" +
                "   \"routes\" : [\n" +
                "      {\n" +
                "         \"bounds\" : {\n" +
                "            \"northeast\" : {\n" +
                "               \"lat\" : 45.5018118,\n" +
                "               \"lng\" : -73.5663868\n" +
                "            },\n" +
                "            \"southwest\" : {\n" +
                "               \"lat\" : 43.6533096,\n" +
                "               \"lng\" : -79.3834186\n" +
                "            }\n" +
                "         },\n" +
                "         \"copyrights\" : \"Map data ©2017 Google\",\n" +
                "         \"legs\" : [\n" +
                "            {\n" +
                "               \"distance\" : {\n" +
                "                  \"text\" : \"541 km\",\n" +
                "                  \"value\" : 540990\n" +
                "               },\n" +
                "               \"duration\" : {\n" +
                "                  \"text\" : \"5 hours 19 mins\",\n" +
                "                  \"value\" : 19111\n" +
                "               },\n" +
                "               \"end_address\" : \"Montreal, QC, Canada\",\n" +
                "               \"end_location\" : {\n" +
                "                  \"lat\" : 45.5017123,\n" +
                "                  \"lng\" : -73.5672184\n" +
                "               },\n" +
                "               \"start_address\" : \"Toronto, ON, Canada\",\n" +
                "               \"start_location\" : {\n" +
                "                  \"lat\" : 43.6533096,\n" +
                "                  \"lng\" : -79.3827656\n" +
                "               },\n" +
                "               \"steps\" : [\n" +
                "                  {\n" +
                "                     \"distance\" : {\n" +
                "                        \"text\" : \"0.3 km\",\n" +
                "                        \"value\" : 280\n" +
                "                     },\n" +
                "                     \"duration\" : {\n" +
                "                        \"text\" : \"1 min\",\n" +
                "                        \"value\" : 72\n" +
                "                     },\n" +
                "                     \"end_location\" : {\n" +
                "                        \"lat\" : 43.6557259,\n" +
                "                        \"lng\" : -79.38373369999999\n" +
                "                     },\n" +
                "                     \"html_instructions\" : \"Head \\u003cb\\u003enorth\\u003c/b\\u003e on \\u003cb\\u003eBay St\\u003c/b\\u003e toward \\u003cb\\u003eHagerman St\\u003c/b\\u003e\",\n" +
                "                     \"polyline\" : {\n" +
                "                        \"points\" : \"e`miGhmocNs@Rm@N]JmA^KBcAZSFWHqA`@a@Le@L\"\n" +
                "                     },\n" +
                "                     \"start_location\" : {\n" +
                "                        \"lat\" : 43.6533096,\n" +
                "                        \"lng\" : -79.3827656\n" +
                "                     },\n" +
                "                     \"travel_mode\" : \"DRIVING\"\n" +
                "                  },\n" +
                "                  {\n" +
                "                     \"distance\" : {\n" +
                "                        \"text\" : \"2.5 km\",\n" +
                "                        \"value\" : 2492\n" +
                "                     },\n" +
                "                     \"duration\" : {\n" +
                "                        \"text\" : \"9 mins\",\n" +
                "                        \"value\" : 549\n" +
                "                     },\n" +
                "                     \"end_location\" : {\n" +
                "                        \"lat\" : 43.6618361,\n" +
                "                        \"lng\" : -79.35452389999999\n" +
                "                     },\n" +
                "                     \"html_instructions\" : \"Turn \\u003cb\\u003eright\\u003c/b\\u003e onto \\u003cb\\u003eDundas St W\\u003c/b\\u003e\",\n" +
                "                     \"maneuver\" : \"turn-right\",\n" +
                "                     \"polyline\" : {\n" +
                "                        \"points\" : \"iomiGhsocNEe@g@yDGg@AASaB[_CGi@E_@EWAGKo@?EAE?EAE?C?C?C?C?G?C@G?GBKNeAFc@PkADU@I@E@I?C?C?C?A?C?E?EACAIE]Ky@q@gFWwBg@qDm@{EGa@Gc@M_AKw@CO?EAC?ACIAECGCCCECCIEGEKGMGCCECGEEEIIEECCMSKOIQIOISGSEQCMCGAIAGAEAICS?IAI?KAO?K?Q@G?G?I@Q@G?G?E?GAE?EAEAIa@{Ca@oCc@eDEW[wBYwB]eCCSYsBQsAMeA_@qCa@wCa@qC[_CAIWgBe@gDU_BMcAOiAEWg@kD]aCOeAg@kDUuACQ?ICUUkDUmD\"\n" +
                "                     },\n" +
                "                     \"start_location\" : {\n" +
                "                        \"lat\" : 43.6557259,\n" +
                "                        \"lng\" : -79.38373369999999\n" +
                "                     },\n" +
                "                     \"travel_mode\" : \"DRIVING\"\n" +
                "                  },\n" +
                "                  {\n" +
                "                     \"distance\" : {\n" +
                "                        \"text\" : \"0.2 km\",\n" +
                "                        \"value\" : 220\n" +
                "                     },\n" +
                "                     \"duration\" : {\n" +
                "                        \"text\" : \"1 min\",\n" +
                "                        \"value\" : 22\n" +
                "                     },\n" +
                "                     \"end_location\" : {\n" +
                "                        \"lat\" : 43.66366379999999,\n" +
                "                        \"lng\" : -79.3555052\n" +
                "                     },\n" +
                "                     \"html_instructions\" : \"Turn \\u003cb\\u003eleft\\u003c/b\\u003e onto the \\u003cb\\u003eDon Valley Parkway\\u003c/b\\u003e ramp\",\n" +
                "                     \"polyline\" : {\n" +
                "                        \"points\" : \"ouniGv|icNa@NOH[L}CvAKDOFOFODSD]JE@C@A@A@A@?BAH\"\n" +
                "                     },\n" +
                "                     \"start_location\" : {\n" +
                "                        \"lat\" : 43.6618361,\n" +
                "                        \"lng\" : -79.35452389999999\n" +
                "                     },\n" +
                "                     \"travel_mode\" : \"DRIVING\"\n" +
                "                  },\n" +
                "                  {\n" +
                "                     \"distance\" : {\n" +
                "                        \"text\" : \"12.9 km\",\n" +
                "                        \"value\" : 12897\n" +
                "                     },\n" +
                "                     \"duration\" : {\n" +
                "                        \"text\" : \"9 mins\",\n" +
                "                        \"value\" : 536\n" +
                "                     },\n" +
                "                     \"end_location\" : {\n" +
                "                        \"lat\" : 43.7628257,\n" +
                "                        \"lng\" : -79.33669689999999\n" +
                "                     },\n" +
                "                     \"html_instructions\" : \"Merge onto \\u003cb\\u003eDon Valley Pkwy N\\u003c/b\\u003e\",\n" +
                "                     \"maneuver\" : \"merge\",\n" +
                "                     \"polyline\" : {\n" +
                "                        \"points\" : \"{`oiG|bjcNkI~AsGnA_F`AqCh@cAR{@Ro@PSHa@Nc@P]P[P_@T[RMHUPg@`@gA~@iD|C}BtB{@r@q@b@y@h@A@MF_@RCBq@\\\\GBSJUHe@Ry@Xg@PgAXc@Js@NoAVeBZ{@Pa@Fc@FiBVcAHcAHy@Fo@BeAFeCLgAFcAD{@B[@c@?]?YCSAYEIAQAm@MQESGc@OcA][M[I}@]q@Yq@Wy@[s@U_AYc@KA?o@Me@I[E[COA]C_@Aa@AW?[?s@As@Aq@Ak@Em@E[EUCWCSEWEIAe@ISEYIQEw@Se@O_@M]Ow@[WMKE[OSK[SUMSMc@YQKQOu@k@g@a@_@[[[WWUUc@g@[_@QSCAsE{FoBcCm@s@wAgBy@cAyAgB}AoB]c@W]Y_@MQm@aA[m@Q_@Se@Sm@Sm@Ok@Mo@Ki@G[Kq@Gm@Gq@Ek@Cm@A_@Aq@?q@@{@DcAHaBFkAJuAJ{AP_CNiCHwAD_ADeA@yA?m@Ac@Am@C_@C[Em@Eg@Gc@Ik@Ms@Kc@I[Om@I[O_@Qi@Sc@Ym@MWS][g@a@m@i@s@[e@c@o@m@y@]i@[e@Yc@Q]EIQ[Yo@Sc@KWIQEMa@oAY_A[kAW_AQq@Og@Ka@a@sASi@Wo@Wm@_@u@MQMUW_@SUw@_AOO[WSOWQWOUOOI_@OQIm@Sa@K[Ik@Kg@IuDe@w@Is@K}@M}@Ke@IOCMAWG]Iq@Qs@Yg@UWOa@Yc@]]Y]_@]_@Y_@]c@q@aA_AyAgBmCg@u@yBeDk@{@KMOSQWW]UYKMa@c@[WWUWSUOOIIGOIIEEC]MYMUIMCEA]K[G[GUCGAWCOAC?[A_@?U@C?Q@]@SBK@SBUDOBMDWFODIBSHWJIBGB[POHSJSNMH_@XYTUTKJUReAdAGDeBdBIHa@`@e@`@OL]Z_Ar@KFEBOLs@d@YPOJOFKFKHo@Zw@^?@sB`Ay@Z_@LWJKBc@NWFMD[Hi@No@NmAT]DUBSBoALkAH_AFuAHE?c@@Y@M@a@?E@sA@u@AuCDiBBcABwABa@@gA@e@@s@BQ@Q@S@Q@_@B]Ba@BcCRcCTgEf@aALWDE?K?mBToC`@a@Fc@FoAPuDf@o@HSBqBVmAL}@LkAF{BFgA@U?QCkGC_AA_A?i@?a@B[?_@@S?]?]@{DPeAJg@F}Cb@m@H_@FoAPw@PSB{@N{@LmAPwAPmAN{@FmAHeADqAFiAD[@yAD{AFe@Bk@@qAFQ@[BaAFaCRE?SBC?q@HoC`@mAPq@Lm@Je@JyAZ_ARe@LcAVUF]Ja@JeD`AaAT_@J_B\\\\WDK@M?]Fo@Jk@Jw@JaIpAiDf@uBZq@JYDWDeANi@H\"\n" +
                "                     },\n" +
                "                     \"start_location\" : {\n" +
                "                        \"lat\" : 43.66366379999999,\n" +
                "                        \"lng\" : -79.3555052\n" +
                "                     },\n" +
                "                     \"travel_mode\" : \"DRIVING\"\n" +
                "                  },\n" +
                "                  {\n" +
                "                     \"distance\" : {\n" +
                "                        \"text\" : \"1.1 km\",\n" +
                "                        \"value\" : 1070\n" +
                "                     },\n" +
                "                     \"duration\" : {\n" +
                "                        \"text\" : \"1 min\",\n" +
                "                        \"value\" : 48\n" +
                "                     },\n" +
                "                     \"end_location\" : {\n" +
                "                        \"lat\" : 43.7680179,\n" +
                "                        \"lng\" : -79.3292728\n" +
                "                     },\n" +
                "                     \"html_instructions\" : \"Take the \\u003cb\\u003eON-401 E\\u003c/b\\u003e exit\",\n" +
                "                     \"maneuver\" : \"ramp-right\",\n" +
                "                     \"polyline\" : {\n" +
                "                        \"points\" : \"ulbjGjmfcNQCU?i@@gA@qA@iA@u@?OAE?OAM?aBE}ACeBE_@AQAMAOCQGMGIECAEEEEGEEEKOEGEGMWGQEOAICIAICKAIAK?IAKAW?]CuDA]A}BESAEAIAo@OwCCu@AOImBIsBKiCGmBAOAGAEOa@\"\n" +
                "                     },\n" +
                "                     \"start_location\" : {\n" +
                "                        \"lat\" : 43.7628257,\n" +
                "                        \"lng\" : -79.33669689999999\n" +
                "                     },\n" +
                "                     \"travel_mode\" : \"DRIVING\"\n" +
                "                  },\n" +
                "                  {\n" +
                "                     \"distance\" : {\n" +
                "                        \"text\" : \"23.0 km\",\n" +
                "                        \"value\" : 22987\n" +
                "                     },\n" +
                "                     \"duration\" : {\n" +
                "                        \"text\" : \"12 mins\",\n" +
                "                        \"value\" : 732\n" +
                "                     },\n" +
                "                     \"end_location\" : {\n" +
                "                        \"lat\" : 43.83811679999999,\n" +
                "                        \"lng\" : -79.07197540000001\n" +
                "                     },\n" +
                "                     \"html_instructions\" : \"Merge onto \\u003cb\\u003eOntario 401 Express\\u003c/b\\u003e\",\n" +
                "                     \"maneuver\" : \"merge\",\n" +
                "                     \"polyline\" : {\n" +
                "                        \"points\" : \"cmcjG|~dcNCkBE{BCwBAwBA}@AwBAuBAsEAwCAwCAyB?yBAeHAwC?aBAwC?wBAyA?oCA}A?{@C_BA}@Cy@C{@E{@Cy@I{ACa@Iq@ImAIiAKkAKkAMkAUmBc@aDm@aEw@qFy@sFSsAk@}Ds@aFCS}@iGKo@Im@WcB_@kCgEuYe@cDCQa@sCc@sCo@qE_@_CIo@Ii@Gg@oEwZw@sFi@oDSsASuAeAmHSuA]aCiA{HM{@M{@k@yDYsBc@wCe@cDYsBS}AG_@M_AAAOsAWkBQkAUkBg@uDQuA[gCOqA]qCQyA[iCc@{D}@{HYiC]aDqB}QmFqe@[mCsAyLe@cEI]AOOgA_AqIu@{GWwBm@}Ec@oDCUQoAeAwHq@uE{@wFiBcL_AkFy@oEk@uC[aB[_B}@oEWiAe@}Ba@gBa@kB_@cBi@_Ca@_BKe@[oAMe@a@yAW}@Qo@a@oAk@cBa@gAo@eB}@yBq@cBYm@[q@[o@i@kAmAcCqB}D?Aw@{A_@y@_@{@c@eA{@yBeA{Cu@cCw@qCU_As@{CYyAScAIg@Mo@a@kCWqBM{@I{@OsAI_AEk@KoAOkBUqCQ{BUsCSoCU{CUoCUuCUwCSmC_@wEOwBc@kFYyDm@eIoC{]gAwNEu@yAiRiA}N}IsjAEUI_AKqAGy@IiAIoAC_@Eq@A[A_@Cw@CoAAiAA_@?q@@oA?{@@i@@s@BcADgADy@B_@Dw@Dg@Fq@H{@JgAFg@Hq@Hk@Lw@Hg@Jm@Nu@F_@Pu@^cB\\\\wAxBwIt@}CXiAl@}Bt@{CRy@d@kBXiA\\\\sAT}@`@cBZsA^_BTgAPcARmAPoALqAJiAHiAJoCB}@BcB?sBC_A?i@KuCOwBSmBMmAWaBKg@Mq@Ow@WgAEMMe@i@iBIY[{@e@mAk@qA[m@c@{@gAsBoAyBaBoCq@eAkDyFiAiBgAgByA_C_BkCsAwBmAsBiAoBaAeBc@w@Ua@uAgCYg@Wg@y@{AaAmBs@wAa@{@e@_AQ]c@_AK_@uHiPuB}DI_@{DiHoCaFgC{EqK}S{BaEoE_IcMqT_BsCOWgHgMyBeD}@wAeDmFkB{Ca@m@uAyBoAqBaAcBw@uA]q@iA}B{@mBk@yAu@qBoCqHkDqJgB_F]_A[_AoAqDkDuJ}HiTqEgMkDuJSk@IUWo@{HoTeSuj@yCoIaCyGgA{CwAuDUk@_@cA_@eAAC?AAA?C@A?G\"\n" +
                "                     },\n" +
                "                     \"start_location\" : {\n" +
                "                        \"lat\" : 43.7680179,\n" +
                "                        \"lng\" : -79.3292728\n" +
                "                     },\n" +
                "                     \"travel_mode\" : \"DRIVING\"\n" +
                "                  },\n" +
                "                  {\n" +
                "                     \"distance\" : {\n" +
                "                        \"text\" : \"319 km\",\n" +
                "                        \"value\" : 318835\n" +
                "                     },\n" +
                "                     \"duration\" : {\n" +
                "                        \"text\" : \"2 hours 51 mins\",\n" +
                "                        \"value\" : 10283\n" +
                "                     },\n" +
                "                     \"end_location\" : {\n" +
                "                        \"lat\" : 44.7393229,\n" +
                "                        \"lng\" : -75.50066799999999\n" +
                "                     },\n" +
                "                     \"html_instructions\" : \"Merge onto \\u003cb\\u003eON-401 E\\u003c/b\\u003e\",\n" +
                "                     \"maneuver\" : \"merge\",\n" +
                "                     \"polyline\" : {\n" +
                "                        \"points\" : \"gcqjGzvraNmAgDm@}AY{@M]k@}Aa@iAe@uAc@mAm@gBe@qAe@qAe@sAc@kAWu@Wu@i@wAAEe@qAc@oAqGmQ_Osa@a@kAQk@Wy@U}@Sq@Oq@UoAMk@Mu@WqBa@qDMeAq@{F{Ds\\\\G_@qCsUi@aEUsBWsBU_Cm@oFq@wF[uCWwBIq@[kCy@_H}@gHOkAm@mEyAoKUaBi@yCwBeN_@cCs@kEg@{CQkAkBgLyAiJw@_FcA{GuBkOEWCYESCSCSCS_@uC_@uC}AoLAQiAeKe@sDwAkKuB{NuAcK}AwJsE{YeBuKaBsKe@cDY{AY_B}@wEqDyQeCsMmCgPeBcNqAeLkBwS_BuSqAsOe@wEoAaS[_FcA}KCwDBuELsFrAw^BoAHeDFkDJqFNoHFwCBiBF_CHqDBcAL}ENuHFeDJaD@o@@oADwAD}ADmAFwB@y@LcFVwKFsD@WJqFP}IDyB@wA?iA?}AA_BA}AEcBEuAGoBKsBKwAK}AM}AQaBUwBWoB[}B]aCWkB[sBQgAGe@]aC_@iC]eCgAwH{Eu\\\\c@}C}B_P}Lk{@_BcLeCeQmByMqBoMCQe@mCy@sEUkAs@sDcA}EuEwTUgAo@aDEUwA}GgBiIi@qCWgAS_AUuAQcA[}BIc@M_AuDwWc@}CAEIm@aAsG}B_PWqBCMMw@OgAgA_IKm@qAgJEYyBcPsDcWc@gDIk@AGMu@kAmIwGce@g@kDYsBKs@OiAYqBMaACMMgAMo@U}Ba@sE[oEOyCUuFMmHCyC?{BFwI?GB_CDoBHyBHmBJcBJaBHiAL_BNiBRwBVqBR_BVmBJs@L}@f@}Cl@eDd@yBJi@Ja@Z{Ah@wBr@qCnBsG`AoC\\\\cAZy@\\\\{@Xq@l@{ATg@z@mBv@}ALY`@u@p@qApA{BjAmB`BmCbAcBfCcEbBsCh@_Av@sAvAyC`B_E`@kAVs@\\\\iAh@iB~@uDf@}BVyAb@cC`@sCJ_A^yDT_DPwDB}@D{ABwA@qA?{@?iAA_AA}@CwACmAE}@Cm@GuAIkAMuBiLegBw@qLwAuTYiEW{D_BwVK_Bk@{Iy@_Mu@oLwAqTc@oGO{BgE}o@wDel@w@{Lq@qKYeEUaDUoDQkCWyDa@eG[eF[yEO_CSuDSaDAGGmAqCyb@IkAI_AS{BMeAMmAIm@_@gCSsA[kB[{Aa@qBaA}D]oAe@aBUs@a@qAe@sAu@qBaAcCeCoGoKwXwCsHc@kAeBsEi@qAkB{EeAsCcByEk@gBm@mBQm@{AeF_AcD_B}FYiAoAiFa@cBg@yBQy@EQ{@kE[cBQ}@i@_Ds@gEo@sDQcAc@yCOiAk@sEa@kDIq@Gs@Ec@a@{DO{AQ{B_@mEUoCQ{BMkBKgBSqDOeDMeD{@uYc@gNi@mQAq@}Ami@u@qUe@sPoAab@CwBKqDEqBEwBIeDKo@AEIuBMsEOcFSsGe@cO]yHE}@E{@]eGa@kGWuD{@uJYoCWcCe@eEYaCkBiOqAiKmCqTg@}DIq@mBiOi@cEYcCwAyLiBcOkCaTeBuNkBkOy@qGKy@yAgLYaCQqAaBeNiB{NKy@kB{O_CqRiC{SqCeUQyAs@}FqAoKcAiIwAoL{@_HcBgNo@iFqAeKmAcKaAyHwBgQ_A}HcBgNq@qFcAgIi@oEs@sFcAkIsBwPeAsIaB{M_Ee\\\\w@sGy@uGY_Cg@mDq@cFmAwIc@eDoEc\\\\mBiNmA}IiAoIgA_Iu@oFm@qEwByOsAsJS}AwAmKsF}`@E[E[mEw[iD{VaFq^cByL_@kCgAoIQmAsDiXoAeJgE_[oA_JeAuHaAkHsAuJ}@yG{@_Gw@iGeFa`@oBmNgCeR}AgLyLa}@SyAkCuRKq@cF{^eA_IqAkJcDkVeBeMoCaSuKcx@_CkQkAiHIg@WqBi@uD_AeHi@{DkB_NgCgRqDgXo@kEc@gD]gCi@{D_A{GqBaO}Ly}@_@iCyG{f@kAwIgQaqA_@wCe@mDa@_DM{@MkAYmCSuBU{BQ{BQyBIsAIeACc@?ICYM}BSqEYwG}Aq]_A}S_@yHWkFQwDCk@Ag@KqCScHO}FKsEEwAGyAK}CU_Fa@gJOkDWaGW_FUcEQkCQmCk@gIG{@Ew@C]G{AIiBWmFCm@AKGgAQgEE}@E}@SeF[iHc@kKi@cMc@qK_@{IO}CK{BIwAGeAMyAM}AIo@Gu@a@eDWgB]uB_A{FaAeGkAiHaBoK[yB]yB]qBiBcL}L_v@uEwY}@uFcDkSiIch@eAaHiAiHQqASqAcBoKuAeJgBeLyAsJ_@gCKy@K}@Iu@KcAEm@GaAEs@CeAE_B?g@?m@?{@@}@@{@@]@]Bu@FeAFy@HcAH}@Jy@L{@T{AFYJi@ZsAb@yBhAcFdAsEjBmIr@uCbBuHfA}E`@qB\\\\kBVyAZwBR_BNqAJaAP_BDo@Da@LaBHuADy@Ds@D{@DiADeABaAB}A@uA@gB?cA?mACsBC}ACcACmACk@MsCGmAKyAMyASiBgEsd@uB}TQmBOcBUiC[wC]}CU{Aa@sCSwAMq@_@yBo@wD_@qBSaAkB_Js@wCo@uCg@uBKc@Kc@kAaFSaAk@_C_AaEI_@[mAi@eC]sAOw@Qw@y@sD{BsJuAiFcAeDaA{CeBcFo@eBs@}Au@qAc@s@IM}AsCwAgC}AqCy@}A_@s@OYc@}@]{@k@wAi@}Ae@eBYiAUeAMs@QaAOiAOgAKeAIeAG_AEw@Ci@CaAC{@CyA?aCEkMGaP?yBCcF?eAC{AGgCGyAQ}BKaAMcAOmAg@gDYmBk@gDOo@Ke@c@cB[eA_@eAQk@o@}AkBgEgAcCsA_Dw@cBw@iBe@eAeAcC{AeDe@gAcA{Bq@{As@aBcAeC}@kCs@{By@qCq@mCe@qBaAqEc@oB_AgEm@sCcAqEq@aDoAuFS}@u@yD_B_I_AeFUsAk@iDKs@M{@U_BQwAYcCOuAOaBOoBQ_CQwCMwCKsBIqDCoAAg@A{BA{D?cABeDDuCH_DP}FJyCf@uNPcFLkDj@eOf@{Lh@oMTiFb@_LLcDVaGT}FH{AHoBJcBNuBNyANsAXyB\\\\sBXwA^aBTaATw@f@eBt@wBh@wA\\\\aAhEqLJYb@kA`@kAj@_Bf@qAVs@Rq@`@oARu@\\\\qANo@XsAPy@N{@Ly@ZqBPyAH{@JiAJqAJoADs@DaADuAB{@?a@BuA?}@?y@A{AAy@C_AEqAE_AIwAG{@KsAMwAG{@I{@Gy@MwAI{@KuAMwAWoDMyAMwAMwAOsB[qDsCw]o@aIcA}Lc@eFa@kF_AoLu@cJo@_IcA{LWaDWuC_@oDUcBy@yGaCgReAqIc@gD}@cHsAyK}@cHeBoNG_@WqBQuAo@eFaB}MIo@e@qDiAiJ[kCeAgIk@uE_@uCc@kDSaB[cCc@oDg@yDo@_Fw@uGs@wFq@mF_@{Co@}Eu@mGm@sEGg@g@}D}@gHg@aEc@_EQqBYuDO_CYuEGcAUuDM_COyBE}@KqAKkBOoCAQQkCg@yIm@wJGgAASs@{KUgEaAgPWeEYeFGaAc@eHK_BAOEg@c@oHg@eIk@oJaAsPu@aMcAwPGiAk@qJI}Ao@aKE{@_@{GWgEIqAGqAKcB]yFE_AYoEc@qHYyEa@uGk@kJc@cHYaFGy@s@_MIeA_@{EKoAs@kGOqAUwBQ{AOsAe@cEe@uE]aDg@mESmBSeBYkCSkBUkB]cD_@iDu@aHq@iGa@qDa@yD[sCYcCSmBW}BUuBg@oEc@gE]eDQ{Au@wG[{C]uCo@uFi@{EkEs`@sL{fAY}BWuB[oBm@}C{@aEEOa@_BOq@yBoJw@_DkB{G}@yCcAgDo@yBs@_Co@uBs@{B}@yCgAoDwEwOmHiVw@mCs@{BcAgDMc@w@iCq@yBk@kB]iAOk@k@iB_@mAc@wAg@eB_@mAwAwEiAwD}@yCs@}BUy@w@iC_A}CaA_Dw@iCe@_Bi@eBe@}A[eA]gAa@sA_@sA[cAg@qBQs@k@eC_@cBWyAYaBO_A]uBQgAWwAa@gCc@mC[gBy@gFy@cFs@gE{@iFk@kD}@}Ew@eD_@qAgAgDe@gAgA_Cq@oAu@oAIMU]iBaCaAeAs@u@y@y@c@c@{D{DuIwIuC}CsAkB{CiFcAmCwAsEg@_CeAqFoKik@oGa]mEaVmC{No@_EYaCg@eFe@wFIiAEmAIcDUyRMuIGqEEaDOmEGiAOeBYkCc@qCc@yBa@_Bc@}Ag@aBo@{A]{@g@aAy@}AiAiBkA_BoCmDwCuDuE}FwCuD_EeFq@{@eDeEaDcEc@m@u@_AeC_Da@g@cBwBsCmDwAgBqCkDkAaB_@m@q@qAmA}C]iAk@uBSu@SgAe@uCWmBO}AOyBQoDM}DWoGMaDk@cOUoFSoFM{C[{I_@oHSsCYiDs@aGqA}IM_AaAmGOaAqCiRsAkJuJeo@iB}K{@_Fw@{DkA}EaEuNw@qCmCgJeRcp@sE}OyA_Fa@wAe@aB]iAsAsEWy@q@iBi@yA}@uBqAqCqAcCoAuBaFaIeHcLeEaH_DcFkB}CaA{A_CcEwAgCoBcEgAqCq@iBi@aBwAsEuBuIg@wCo@{D_AuGkBsMmBwM}BwN_BsIqEyTcJ{c@YyAyDaRmAgG]yAc@cBe@cBc@yAa@qA[_AsByFcCqGg@_BkF_OkCyHoUgq@qBgG{@eDw@mEIa@Gg@Kq@E_@Gs@Iy@IkAE_AEs@I{CEcFAy@SqVEmEOoV?e@?m@?EAg@SgZq@}}@MeDIcBOqBOyAIk@o@iDq@oDg@cBkA_DsLsWsCsGsFwLWg@EI[o@uE_KmB}Do@cBkEeMqAkDAEQg@?AyAgEaDiJ{AoEcDqJc@mA}@aCm@aB]aAe@yAmGyQiA{CaCaHo@iBu@{BmDcK_C{GSm@uBgGyBqGqAqDmAcD_AcCmAcD{A{DuAmDoCgHmBeFiB{E_BiEqCmH{FgOqCiHm@}AeCyGyB{F{CeI{FgO_BcEuAqD}Ly[eIcTsDqJsAqDeFwMwByF_BgEk@{AiCyGqFsNy@yB{@_Cg@uAYaAg@aBU{@g@iBm@eCS_Ao@}CUoA]uBe@mCc@kC{@_Fs@gE_AyFwAuI]qBk@iDk@gDQeAw@sEWuA]sBg@wCg@_Da@aC[kBWaBe@mCm@oDc@aCc@kCYcBg@sCUyA]sB[mBUsA]sBe@iCu@qEc@mCy@yEk@gDk@kDk@eDe@mCuBgMc@iCe@qCq@cE}@gFeAqGs@eEk@eD{B}M{@iFgAoGm@qDoD_Tc@oCyAqIs@gEa@kC{@mFi@iDIk@EYmAmIyA}JoCcReDaUkA_I}AwKqA{IkA}HaBaLu@gF{@aGeAcH{CgSw@eFc@yC}AcKi@oDwAgJw@kFc@uCwAoJu@aF[mBuAmI}AmJgAyGeBeKy@eFgCwOWuA[oBc@mC]qB_A{FG]My@aA}F]sB_A}FUuAk@iDq@gEKg@QeAWsAMu@Ow@Oy@Mu@WuAe@kC]qBOw@Ow@]qB}@aF}CoQc@eCoAqHoAwHi@eDE]Kk@mCkPoAqHiAaHeCiOoCoPmAoHi@{C]sBa@iBEWg@{BaCcK_AsDi@oBg@_BuAoE_BcF]gAa@sAe@{AOg@}A{EeCeIUw@Yy@eF_PyD}LmCsIiBaGUu@W{@o@aCc@aBi@yBU_A_AgEYyAc@_Ca@wBy@uEYcBa@qCk@{D{@yF]}BeAyGoBsMcCkPyAuJq@yEmCsQaBqKqAwIc@wCkAwHgBsL{@yFiDmUcCmP_CsOaAuGqAyImBoM_BsKcA}G]wBq@{EiBuLaBwK{G}c@}AoKEQy@eGu@aFaAsGaA{G_AqGU_BMkAIq@MyAKiAM_BOcCGsAImBEwAAkAMaF?eGBcBF_CH}B@i@Bk@ZgFToCp@{Fb@_Dn@uDX{Av@sDrBgJxBgKhDyOZ{An@yDh@wD^aDP}AXaDRaDRaEHcEBeC@cCCaDAcAA_AEaBMgDK{BIgB]kEqBuXwAwRkAgPq@eJ_@cF]sE}Bk[IaA{K}{Ag@yHWeDWeD{AeSaCc\\\\k@aIk@cI_BgTkBcWuCia@q@gJ}@kL{H{fAGy@K_BM}AO{BcQacCqA}PCWkDgh@{AcSyLudBuFmv@aAeMyAcROkCyAqTsHkfA_MgfBqGmz@qAoQiAmPcBmUEg@cBwUoCk_@uCob@gEen@cPa{BmF{s@iAsNw@aKS{CYsDSqCWkDM_BQsCYmDYwDWiDk@kIYsD[kEO{A]qCIw@My@My@SsA]mBQ{@YuAa@kB[qAo@eCaA{De@mBGU{@gDe@kBo@eCg@mBe@kBe@kB[qASw@e@kBoCuKo@cC[qAUw@c@iBcA_Eo@eC[qA_BmGaC}IwAoF{@aDeA}Dq@cC[qAi@mBy@_DQu@]qAuAqFeA_Ey@aD_@{AqDoNI_@EOy@eDmAaF]uAQu@WqA_@qBSuAYsBOwAIy@G{@Gy@Eo@G_AGuAEgACyACyA?{A@yA@yAD{AFwADy@F}@PwBJwAH{@LyAHw@H_AFy@LwAHy@F{@NwAF{@LwAF{@B_@Fw@H{AFy@FyAD{@@_@Bw@FuDDiEA[AeEEsBE{AIcCMgCQuCOmBO{AQ_BOwAS}AW_B[qBq@mDiAsFe@{BgAoFoBoJ_AuEeA{E{DiRuHg_@Y{ASeAO}@YoB]eCQaBMkA]uDOuBO_CIoBI{BGwDCkCEmFWi[OeOKaOGgGK_NAgBE{DEwDEcCCsAG}BEwAGcAMiCQgDQgDOsCk@kKi@wK]uGcAwRk@wKMyBMkCc@gI]aH]qG]iHg@uJCe@Cc@OuCQ_EQcEMsEMoFMiHKsHQwJOiKSqLOiLMeJOsKIeHM_KOuKI}GQqKK{IMuIOqJIuEQ{KKgIIwFSeOKcIIeFI{FIgFIkFK_FMkHSmNYySOiK_@{WQcMKcGEmDMmIMmHYkOOaIOmICqBGsD?KEsC?KE{DIeFU{NIoGQuL?KGuEGqDGuFQqUImJEwEEsEImGEyBm@}b@?EGiDKoHEuCIsEWcMIoE[eNImEKuEOmHEsCGuBQoI]}Qa@sQSoJOqFYeLMgGE_B[}NWcM_@ySm@{ZKuFEeBCcB_@kR?YWkMCiAm@u^E{BKqCKuC[gHYmH_@cJ[kHe@aLWmGSoEQoE]iIQqEYoHMoDCwA?}@EoEAsEAsCCuEAwBA{A?wBAuC?uDAoE?oGAuFAkGCsECqFCoGAsDCqFCmHEkHEiICoHEeK?WCoHCyEC{CI_DMkEQkFUcGOaFQuE[gJUeGOoEIiCm@gQm@oQsAy_@{@cWM_HGgLG{U?}SCoc@Qoz@A}FEqOAwGC}GAcFAcFAaFAmFA}F?sAAkAGqBIsBQuBKkAMkAYoBIe@SgA}@qDWcAy@gCw@cCe@iBYiA_@mBYqBOkAW{CMsBGsBCkAAoA@mA@kA@{@Dw@JqBDg@NiBFi@^uCZaBVqA^{A|@_DRi@Ne@N_@p@{AlIiQrE}J^u@Ta@pBmEP_@jAcDZcARq@lAqFnAoFtBcJLg@bE{Qp@sCZuAf@uB~Iga@T_Ah@eCt@yCf@yBd@wBR_ALy@XgB`@qCR}AT{BPuBV_DJeBDc@HmA?GNqBRwDVmELwBH{AHuA^kH`@iHXuFPqCPyBXqC\\\\uC|@cGpA{HtC{OjC}NDWzAuIjBkKv@eEz@uE^qB\\\\sB~@mFd@oCTqARiAh@wC^yBRyALw@D]D_@F_@Hw@H{@J{@Hy@F_AH}@F}@HwADy@D}@D{@B_AB}@D}@b@oN\\\\gMZqK^mLBgABgAXoIJmCBuBAuBEgBGoBOwBEe@KeAUgBOeAIe@SgAYqA_@}AOk@So@]gAi@{Aa@eAo@yAw@mBsCwGuF}Mw@gB}@yBkAsC}AsDEMsByEeD{HgBeEM[Um@Wo@Us@Us@Oe@g@qBIa@Ou@Ie@_@_CmCuQiAwHGc@_@_CCQ]aC_@kCYqBYmBo@kEi@mD[sBg@kDo@gEg@kD{@iF_@kC_@}Bw@uFUwAUuAe@cDs@{EQkAkA_Ii@qDc@wCe@}C_@gCg@kDQmAc@sCa@qCIe@Im@U{ASqAQiAKy@a@kC]{BmAqI[qB{@uFy@{FoAmISuAUyAMy@My@Ow@Ow@Ou@Ou@Os@Qu@Qs@Qq@Su@Ss@Sq@Uu@Wu@_@mAeA{C}@aCYu@c@mAe@oAWs@Ys@Ws@i@wA]_Ae@kAWs@Wq@Wq@Wq@Wq@c@kA_@aA]}@c@kAc@iAYs@uAqDo@cBo@aB}@_Co@cBEKi@wAy@}B}@}Bc@iAWo@Yo@Yo@[m@[k@]k@]g@o@_A_@e@_@e@a@c@a@c@c@a@c@a@c@]e@_@e@]c@YmAs@mAu@oBgAaAm@mAu@y@g@{@g@y@m@w@m@a@_@e@c@]]UW[]W[]e@a@g@_@k@k@}@m@cAi@gAYk@[s@O_@Sg@Oa@So@w@qBc@kAe@oAsAsD{BkGiB_FgEiL{AsEeAsCIWM_@{CmI}@cC_CkGqAsDyFwOEMgIwTaBuEcCuGmA}CeAqCeAsC_@iAa@mA]kA]oA[oAc@iBc@qBe@cCi@gDa@sCUiBQ_BQiBo@sG{Fwm@eA}KuAsOcBoQiBiRaCyVwAsNc@gEc@sEYeDQsBMqBOwBMsBKwBMqCs@oQ}@uUM}HImF_@wIUaHM_DOsDIgBk@{Me@sJg@gNs@gT[wI]gJg@qLMuCGgB[kG[wHk@{NkAqZk@mNIuBEwBKoEQqMGqDGsDGkEGmFEuDGoCEwBEwAG{AEuAGyAWkFk@wLUkEi@{JQoDg@{Ki@wKa@cIYiFQoDUiEYiFMqCMuBOqDIuBUmFSkEOqDYiGYiGWkFSqDa@aIg@_J_@eHc@_J_AkRk@yKa@eIOoDMqD[aKOkFIuDi@mSk@{SCs@Cs@SeHOoEEwAE{@IuASsCMwAIw@SiBQ}ASsAa@oCc@iCG[_@mBk@eCYqAm@gCm@eCiAwEcBeHw@aDiAuEeBgHiCsKwB{IeBgHeBgHk@gCWuAk@oE_@kCQsAOwA]kDUoCk@aHwAgQmBuUgBwTkAsNg@gGo@eIw@{J_@oE]iEQsBYoDUsCUqCa@gFu@}IiAkNEo@SaCQwBKwAkBeUM}Ae@wFUwC[oDYmDOsBGs@IeAKuAI_AEy@IuAE_AGsAC_ACu@Ac@CsAAe@AwA?y@?{@?{@?mABgB@c@@u@B}@DqAF}ABy@D{@F}@Bg@@OF{@J{APuBPeBHy@Hm@BSL}@F]LaAHe@DWJe@n@gD\\\\mBToALw@L{@Ly@PyAPqBB_@BYDy@HwB@w@BaA@wB@{A@sCHuODkHDaJFaLDcE?oD?_A?}@CyAAy@C{@Cy@Ey@E_AKuAIcAEe@KeAGc@Io@Ec@QkAUsAUuA_@qBaAgFkJgh@eN_v@eBmJu@aEg@kC_@iBg@kCa@mBs@cDYqAi@gCgA{Eu@_D]sAkAuESs@e@iBq@cCe@cBk@oBs@eC}AgF}AgFUu@sB{GuAqEsAqE}AeFqAqE_BmFeGgSiBcGg@aBy@kC{BwHiBeG}AgFsB}G_CyHs@_Ca@uAsHuV[gAgD}K}BwH_A{Cq@{BiAwDiBaGuAsE}AgFgAuDu@aCgImXeDaLiTat@qAqEg@iBi@qBi@_Cc@mBa@kBYsAYqAWsAg@kC]mBO}@i@gDYsBQqAYsB[mCi@_FU{B[kDc@}FMeCOwCKcBIiBKeDG_CEwAGsDCwBCsC?wC?oD@wBBsD@y@DyBFqCFsDTeKr@w]T}Jj@eYDwBDuB?wA?}@?iAAiBAyACsAK{CGwAKwBQeCSeCSwBAKSeBYwBi@{D_@qCg@oD_AyGg@mDs@_Fe@mDy@}Fy@wF]aCe@cDgF{^E]EUE]CUKs@y@cGq@{Eg@mD}@qGsB_OcAeH?Gm@qDg@cDs@gF_@kCc@qC_@oBa@mB_@sAe@cBa@sA_@gA[y@Yw@Yo@Yo@m@mAe@}@iEqH{BuDcCcEcCuEgAiBiAkBiAkBwAaCyAcC{@uAgAkByAeCwAaCuAaCiB{Cy@uAgAiBy@wA{@wAy@wAwAeC[m@e@cAc@{@_@{@c@cAc@iAc@iAa@iA_@kAa@oASs@]mAUy@YmA[qAa@oBi@kC[kBg@kCm@eDKm@Km@Mo@WwA]kB]kBWsAWuAa@iBUaA[gAMc@Og@Us@a@iAq@}Ae@eA[q@g@aA]k@_@m@wBiD{A_C{@oAm@_Am@}@}@uA{@qA{@sA{A_CkBsCiBqC}@sAi@aA{A}BiAgB}AaCyA{Bm@_A}@sAkAiB}@uA{@sAm@_A{@qA}@uAm@}@y@oAq@eA_AoAq@{@s@{@eAgAq@q@u@q@y@o@u@k@q@k@w@i@g@_@w@k@w@o@u@m@c@a@s@o@u@u@u@w@q@s@_@e@cAmAo@y@o@}@m@}@m@}@[i@QW[i@i@aA]o@[m@[m@Yo@Yk@Yo@q@}Aq@eBo@_B_A}Bq@aBgAqCc@iAcBeE_BaEiAuCkAsCUm@o@{AcAcC_AuBw@}AMU[m@i@aAMUMU]i@]k@kAeBw@eAyBqCiDyDwCgDeCsCqAyAuA_BsA}A_AgAc@e@u@u@a@a@QOw@o@w@m@iAs@{@c@UMc@SsAg@k@SQG}@UgB_@}D}@yHgBcH_BOEkDw@eFkAyFqA{D_A{D}@mA[uEgAuLwCsA[y@Si@Mk@O}@WSESGe@Oi@SgBq@e@S}@a@_Ae@e@Uq@_@o@a@[QgAu@eAw@}AkA_EyCmG}EgDgCmCqBoCuBmDmCqCsBgDgC}EqDwCyB{AkAyAiAkA{@{AiAkA}@YUkCoBoFcEoB{AwAgAWS{AkAKI_Au@kCqBaAu@cCmBeCkB_DcCyI{GaAs@_BmAiBuAi@a@aCiBmByAsAeAo@i@MKmBcBsCeCqCeCQQu@o@kBcBiBaBaDuCsCgC{BqByAqAu@q@iAcAa@_@w@q@yAsAkAiAaA_AkCoCuA{AgCsCuBcCaGwGoGiHqGiHkCyCq@u@uA{AeBqBaAgAeAkAiAsAo@q@_AeAwRsTcDyD]]eAkAqAyA{AcBiF_GSUkFeGgCoCkFgFoCsCw@w@wAuAyBkBuAuAwCyCoEsEy@y@gBiBs@u@mAoA_A}@u@w@a@c@kAmAqBqBwAwAo@q@UWOOgAgAyB{BsBuBW[YYUYQUW[MSIM]i@_@q@Yk@Yo@a@gAWu@Su@IYSw@G]GWG[OaAa@cD]sC_@qCo@eFiA}IaA}H]qCYuBqDyYs@sFi@kEUgBKs@uAeLWsBEa@I{@IeAEk@Ew@KgCEiACsCAyA@uBB{ADuANqCNwBPuBZoDp@eHrAsN~@}JHcAF{@F{@FcAFeBBcB?eA?q@CsACaAI{AIgAG_AKaASwAO_AMu@YuAQu@]oAe@{AsAaEqJmYwLk^{@eCs@oBaAiCg@mAs@_ByA_Dy@}A{@wAy@sA]i@q@aAo@}@o@{@q@s@c@c@c@a@y@o@y@i@w@a@]O[Kk@Qe@MaAQaAKy@Em@Ay@@q@BuCHaELcELqUx@sCJqIVsELmBBsA?yACaAE{@GuAKuAQmAQgASsAYiBa@sAa@qAa@uAi@mAg@{@_@iCsAmAs@s@c@k@a@aAq@m@e@y@q@s@m@u@s@iAeAcBkBeAmAoAaB_AuAe@u@e@u@sBqDqC_Fy@{A_D{FmHwM}N{W}K_SuF}JyDaHcB}CyAeC{@uAm@{@_AsAq@{@a@g@qA{AwA{AeAeAoBeBw@o@wAgA}@k@mBiAyBmAg@UoAk@{@_@iLkEyIgDiNkFyBy@s@Yq@YyB}@k@Ya@SmAs@YSYQ}@q@iA}@u@q@gAgAuBcCq@y@oEyFs@aAo@}@k@_Am@gAu@yAaAuBe@gAg@sA}AcEy@}BgAwCcGaPiBaFaAeCcJwVsD{JuC}H}@cCgB{E{@sBoAmCsAiCiAqBq@gAoAmBEEAAQSa@c@e@k@oAsBcBgCkAiBk@w@[c@kKePIMe@q@S[e@u@Wc@{BmDaDqEQYQWkAeBeA_B{@qAQYq@gAcD_F_B_CyBaDe@o@_AsAoAmBWa@Yc@u@kAuB_DoAqB_@k@a@i@oBaDiDqFuB_DOQaBaC_G}Iu@oAwHiLw@gAEGi@s@mBwCcA}AoAuBgBiCaBeCsAmBs@iAyAcC}BeEgAuBgAuBuAmCsFuKoAaCmBwDeIyOsEaJqIkPsCuF_@u@}CaGcDmGyAwCs@sAgAqBuAoCmBsDaAiBsAgCuAkCuAoCwAoCsAkCgB_DWa@q@gAyAuB_@g@}@iAeAqAi@m@i@i@s@u@wAsAkB_B}AsAiAaAmBaBwDcDkBaBs@o@{A{AsAyAcC{C}A{BqAqBgBqCm@cAwA{BgCcEaA{AqBcDoAoBwAaC{AaCkAiB{AcCq@eA[i@MS{BoD{EuHMSm@aAQU]k@{@sAm@aAk@}@mAmBu@mAq@eAeIsMoD}EcN{PwGkIsAcBmDmEmA{As@}@mA}AcAoA_AmAq@}@Y]mBeCoAaBu@aAw@eAiAuAqAcB}@gAo@w@kMcPiCaD_c@si@kL}NmBaCqCkDuCqDsAaBeEkFkEqFaBsBy@eAy_@af@iCaDsCqDgHaJaFiGeAsA_LqN{CyDcBwBcBwB_AkAgFsG_AkAgLyNgEmFeJgLuAgBoA}AoA}AaAoAaAkAsDwEgJmLq@{@oJuLgCcDgAuAqA}Aa@k@o@w@q@y@q@{@m@w@sAcBq@{@q@y@_@g@q@y@o@{@s@}@[c@SW[c@OU_@i@[g@i@}@Q[k@aA[m@i@cA]q@Yk@Ym@g@kAWo@e@mAWq@Um@Yw@Us@Sm@Ww@]mAUw@I]YgA]wAYmAWqAYyA]kBa@mC]uBYoB[sBc@oC[mBSwAMu@U{AYkBUyA[kBU{AKu@UyASqAUyA[mBSuA[qBc@oC[sBa@mC[oB[sB[sBWsAUoAQy@YqA[uASw@YkAi@iB_@oAQg@Mc@c@oAc@kA[y@Yu@Se@Yo@c@eA[o@]q@[m@[m@[m@i@_Ak@aAaBoCgKaQo@eAoEuHaJiOiGiKgB{CmDaGwBoDcCeEyAeCwBoDk@cAk@aAgByCsBgDqBgDoEqHU_@EGYg@{AeCgKoQqDgG\"\n" +
                "                     },\n" +
                "                     \"start_location\" : {\n" +
                "                        \"lat\" : 43.83811679999999,\n" +
                "                        \"lng\" : -79.07197540000001\n" +
                "                     },\n" +
                "                     \"travel_mode\" : \"DRIVING\"\n" +
                "                  },\n" +
                "                  {\n" +
                "                     \"distance\" : {\n" +
                "                        \"text\" : \"109 km\",\n" +
                "                        \"value\" : 108807\n" +
                "                     },\n" +
                "                     \"duration\" : {\n" +
                "                        \"text\" : \"1 hour 0 mins\",\n" +
                "                        \"value\" : 3580\n" +
                "                     },\n" +
                "                     \"end_location\" : {\n" +
                "                        \"lat\" : 45.2083667,\n" +
                "                        \"lng\" : -74.3482841\n" +
                "                     },\n" +
                "                     \"html_instructions\" : \"Keep \\u003cb\\u003eleft\\u003c/b\\u003e at the fork to stay on \\u003cb\\u003eON-401 E\\u003c/b\\u003e\",\n" +
                "                     \"maneuver\" : \"fork-left\",\n" +
                "                     \"polyline\" : {\n" +
                "                        \"points\" : \"wcapGdfykMeFsIsZch@sMuTuBoD}CkFqJaP]o@gTm^QYQYm@eA[q@cBwCiD}FEGmCqEiAiBuA}Bs@oAwDoGeCgEiDuFgAaBmBoCmB{BwA}AiBiBqC_Cu@m@s@g@_Ao@}@k@aAi@y@c@[OkEwBaCkAkCoAs@[aBy@wBeA{E_CiCoAuBeAoAm@aCkAsCsAkB{@_Ae@sAq@}@e@_Ag@}@i@{@m@cBmAoAcAe@a@g@c@{@w@y@u@g@g@w@{@w@}@mA{Aw@eAo@}@g@y@k@_AeA}AaAqBkAuBw@yAaByCwAkCeCuEsByDiBgDqBsDqAaC{AqCiC}EoBqDaIyNcKkR{Pq[sOoYwMoVkDsGuEuI{Yyi@kDqGaG{KoCcF{GgM}LeUm@kAIOk@cAaB_DiAsBcAmB_@m@gBmDsD_HoBqDuByDaAiBm@gAqAcCm@iAc@y@iAyBgBeDmCcFcB}CsAeCq@oAq@qAmAyBg@_AsAaCcCsEwDeHq@oAq@mAo@oAqBuDsBwDqA_CoA_C_DaGqBsDaB}CU_@kAaCsB}DqAcCqBuDqCkFwFeKsFeK_DaGmCcF{EcJ_CmEaD_GaDaGaJwPcCuEgCwEiDoGQYgCyEcAmBs@sAoVqd@uB}Dq@qAq@oAq@qAgCwEuAgCs@qAcIgO}BiE{FmK_@q@_@q@_@s@s@sA_@s@i@cACEeE{HaFiJSc@oF_KgAwBw@_Bu@}AM[mBmE]{@o@wAy@sBq@_B}@wBQa@m@wA}@wBo@}Ac@gAw@mB_@{@gAkCaAaCaA_CeBgEiAqCmAuCwh@ipAc@gA?Aw@kBcSif@_`A}}BgDeIaC_G_CyFeBeEwAoDoA}DiA}DcZajAUy@oQwq@mKea@gRcm@aOyc@uFkQsJcZcG{RgHuTySgq@qHgVsJm[oG_Ts@}BqGkSsR_n@oNqd@gEkN{Ku\\\\qBgGwG_S_EsLy@gC}AqEwCwIsA_EsAaEwCwIeFkOoF}OgEkMwBgHqGgTkKc^qGuTgA{DoFoQ_A}C}F_SqByGeCoIcCmIcH{UkI_YmDwLuHsVoLga@mEuOwAcFsBcHuDeMaCiI_Ke]eB{Fu@qCcAaDoH}Vc@sAo@eBYq@Wg@]q@_@q@]q@w@oASYY_@q@{@k@q@y@y@eAaASQMK[UWQc@[YQ]Qc@Wa@QaAc@_@O]MsA][I}A[}Ew@}`@iGaAQc@Ia@Ia@IYGiA]e@O[MqAk@{@c@c@Y{@k@_@WWS_A{@c@c@]]c@e@[e@u@aAi@w@Ye@S]_@q@i@kAc@aAYu@[_AWy@Y{@sAmEmBmGuMyc@eCkImPuj@IWa^ulAsDcM_DmKuBcHoAcEsDeMs@eCy@gCu@iCm@qBs@cCi@iBi@gBy@sCk@iBs@aCyA_Fe@_Bu@iCoFuQmBsGoBwGqAmEaCcI_C_IuA{EaA_DuBiHeAiDuAwEeBuFmA}DcAeDqAiE_C{HcAoDs@gCaBuF}@}CgA{Dy@{C}AoFq@}BK_@Ss@{CcK_BqFM_@]qAgAoDm@wBcAgDoAeEe@_Bo@sBmAgEwAaFy@uCmAiEiAuDgAuDiAaEqAoEcAiDkBqGkAyD{@yCgAqDiAyD]iA]iAu@eC{ByHiHiViBiGuA}EqByG_BmFgBcGkBkGeFeQiC{I{BuH{CcKiCuI{AcF{AaFgB{Fq@}BcByFsBwGmBwGoDwLaGcSeFaQyAaFeH_V_EeNs@cC{GgUgAqDcCkIyAaFqCoJiBqGaBoGqGuWwA}F{@aDwEiRU{@Mk@mCuK{DuOiCeK{AeGoBkIkLod@mDgOyEmRyFsToKsb@?AaC{Jw@_Dq@kCmCsK_DiM{FcVm@oCMs@Mo@WwAw@aFa@sCg@oD}AwKgFs^qBgNm@gEy@eGaAgHoAeJ{AmKgAuHOqAS_BOaBOqAQmBMkBKcBIoBGsBEqB?mA@oA@uBDgCFwBFaBBe@HoAFcAHcALqAP{ANqA^sCRqANy@Hg@VmA^aBVeAj@qBz@wCnIqYzEaPpJc\\\\|B{H^sAbEcN|A{FBEFYrG_UzCiL|@}Ct@oC`AaEt@_Dr@iDp@gDn@gDj@iDv@gFl@iEh@mE^oDXsCZqD\\\\kERwCLqBFqAHwANaEJuCJsENoGRiJ^mOPcINyFN}GTuJZ}MVuKb@sQPiIHsDBcB@aA?mAAwCCwBOkJMgKG_DAsACuAKeGOmJMmHKmGMuHKiGAqAKyFKcGEcCKoHMkIKqHG{CCoAImFEaBGyAU_FAUG}@g@kFa@sDWqBKo@SsAOy@WuAOw@WsAe@qBm@eCYeAw@kCeAkDu@gC_BgFyAaFyA{E{BqHqAiEmAaEsAmEeB{FoAgEs@cCmFgQ}BuHiAyDi@oB[qAa@oBWuAMy@Kq@U}AOwAOyAKwAG{@G}@GyAE{ACwACaB?EGmFEiEYk\\\\U}SAw@EgFM_NCcEEyEUsRg@gh@C_DQwNOgNCeBGqBI{AMiBWwC]eD_AeJkAsL_AcJq@qGImAMmAu@yHmAiL?AgAaLo@wG_@yD]{COiAOcAk@wCi@{Bs@kCk@gB}AgFuFuQkGwSsCkJoMqb@s@}BoCiJkBwFqLm`@qAeEeQal@_HeUcFwPq@yB[cAiB}FcAgD_@sAgAoDq@yBc@{AQg@qGiTmAeE{@sCo@wB[}@oAcEm@mBs@sBYq@c@kAk@uAeAuBu@gB_F{LkAwCqA}CyAuDoJ{UkCqG_C_GoB_FqHaR_C{FsCgHuCiH}B{F}ByFoB}E_C}FoB_FwAoDiAsC}BwFa@gAw@wB}@{B{BwFkAsCiAsCwAmD}@{BuAmD}CcIsAoDy@_CyJiXyJkXKYqAmD{AgEuB}F_BmE{AiE_BkEeAuCeAuCo@eBcAuC}@cCa@iAy@}BgAyCeAuC{@_Cy@{By@_C{@_CeKeYWs@eB_FiBcFqD}J_CuGiBaFaCwGgAwCoAmDiBeFwIgV_BqEaC{GqAqDcFgNIW_AiCISoCqHiGyPgHwRm@aBo@eBo@cBc@kAe@gAYq@g@gAYm@{@cBcAiB}@wAk@aA]g@_@i@[e@a@i@q@}@_@c@q@{@s@{@eD{DsLoNcBoBaAkAaAkAaAmAs@{@o@{@o@{@o@_Ag@w@c@u@e@u@w@wAaAkBu@}Aw@kBcAkCYw@s@qBc@wAu@cCIUsQsl@iAsDq@}Bc@sAs@cCW{@}@{Cq@_C{@uCmBsG_BsF_@qAw@mC_CgIgDiL_A_DsAwEiAwD_BwFo@wBiAyDu@cCs@eCk@mBs@cCe@}Ac@yAq@aCk@mBu@eC]oAm@wBm@qB_@kAW_A}@}C_@qAk@iB]oA{@uC_AcDs@cCgBgGsCwJqAoE}BaIsAuEqAoEkLw`@wAaFsBeHqAsE}AoFsAsE{AoFiBiGqAuEsAuEgBiGwEiPgC}IoDeMgBiGs@eC_BcG}@gDcBoGaG{TwBeIuBaImFgS_DmLwBeIq@eCg@iBWw@Uo@Qc@Sc@Ym@Sa@Uc@Wa@U]U[Y_@]c@c@e@e@c@a@]USYQ[Ue@Y{Ay@gB_A}C_BkCuAqAq@KESMeB}@aB}@oAq@e@[e@]c@]e@a@q@w@QUQU]e@MSIKa@u@]o@Q_@Ug@Qa@Si@gAsC}@_Cy@wBoFqNkCaH}DiKcCsGaBiEsAmDM[cCqGy@wBeBoEkBaF_BeEsCmH_CgGqBmFeCsG_BcEACiAwCc@kAWq@eCsGqCkHcCsGiAsCiCoGWk@[q@e@cA\"\n" +
                "                     },\n" +
                "                     \"start_location\" : {\n" +
                "                        \"lat\" : 44.7393229,\n" +
                "                        \"lng\" : -75.50066799999999\n" +
                "                     },\n" +
                "                     \"travel_mode\" : \"DRIVING\"\n" +
                "                  },\n" +
                "                  {\n" +
                "                     \"distance\" : {\n" +
                "                        \"text\" : \"67.3 km\",\n" +
                "                        \"value\" : 67288\n" +
                "                     },\n" +
                "                     \"duration\" : {\n" +
                "                        \"text\" : \"45 mins\",\n" +
                "                        \"value\" : 2702\n" +
                "                     },\n" +
                "                     \"end_location\" : {\n" +
                "                        \"lat\" : 45.4623177,\n" +
                "                        \"lng\" : -73.6095157\n" +
                "                     },\n" +
                "                     \"html_instructions\" : \"Continue onto \\u003cb\\u003eAutoroute du Souvenir\\u003c/b\\u003e/\\u003cb\\u003eAutoroute 20\\u003c/b\\u003e\\u003cdiv style=\\\"font-size:0.9em\\\"\\u003eEntering Quebec\\u003c/div\\u003e\",\n" +
                "                     \"polyline\" : {\n" +
                "                        \"points\" : \"iw|rGvcxdMqC_GqAoCe@aA{AeDACsAqCmDoHwGmNcHcOcEsIu@aB_AqBaBkDwBsEsBkEUe@iB{DoAgCk@mAs@sAw@_BqAkC_AgB{@kBO_@O_@Wo@a@oAM_@I[IWK_@EQU{@WoAWwAa@{BUsA_@}BcGu^k@gDyAoIaGg]aBoJY}Ak@yCa@sB_@cBc@gB_@sA_@qAg@eBk@cBOg@Sg@e@mAy@qBo@}Aq@_Be@kA{@qB]y@mAwCg@kAmAyCaP_`@}@sBs@eBo@}Aq@_B{@uByAmDyDeJ]}@aBgEyCaIaFsMkCeHkCcHiDcJw@qBkCcHwB{FoCcHeBwEuCyHcCoGaCqG}AaEe@mAiCcHmCiHeDoIcA_CmAsCeK}SaC}E}B{EqCuFwBoEKWaAwBUe@uAaDcAgC}@aCk@}AeA}Cu@{Bs@}BYw@sN{c@c^yhAc@sA}@qCqA_EcAcDyB}Gu@_Ci@aBc@kAw@oBSe@Uk@y@gBcBeDi@aAs@mA_@o@e@w@e@s@a@k@]e@a@i@aAmAa@i@cC_De@m@MOiIwJ_d@sj@kAwA_O_RQUsNiQ[a@}BsCup@uy@sDqEuC}DY[W]sCmDkBcCc@m@kBkCiC{Dk@{@uAuBcA}AoEaHyBeDiBuCs@eAw@mAgBqCiDoFy@oAu@qAk@cAk@cA}@iB{@kBcAeCg@qAsEwMgJqXuIeW_B{E}AuEqGiRoDsKw@kC]eAm@oBqAeEgB{FiAsDcL{^{Sqq@}E_PyLi`@Mc@{A}EuMwb@iAmDm@sBaAyCiB_GiGiS}DiMaDiKgB}FwBcHeBwFyBeHMa@a@oA}Qam@iEgNu@eCuAqEoDiL_BkFUs@GQoQ{k@_AwCoBoGqPwi@sG}S}C}Ja@iA_BaFmA{DaAsCKYsDqLuAsEkDcLoEyN_BgFy@iCs@mBu@mB_AaCcA_CqAoC{AwCu@wAi@aAaA_BgDiFgBmCw@iAsB{CwH}KaDwE}BiDqAoBOSoAkBmAmB[e@k@}@oAsBS]gAgBiCgEmAsB{AeCkDyFuBoDqBeDWc@EEMUc@s@w@qAaG{JmEkHaBmCsAuBs@cAa@m@{@aAMMg@g@q@o@aAy@_DgC}AoAkCwB][IIYYuAuA}BgDu@oAcAsB_A{Bm@_BcBmEmFyNm@{A{B{F_@aA_A{BeDqIu@qBkAyCa@iAcAmCk@}A}AwDgAeCWo@a@}@i@mAi@kAWm@e@gA[y@o@yAeAiCcAiCgAqCa@}AQ}@QoAG}@G}AEmC[kSGgEMiHE_BCaDa@iUAm@Am@EgDIkEQuEIgBIqAMeBQkCc@_Fi@yFmAoMUqBKy@OyAIi@Ku@SuAGWWgB]qBg@mCc@sBYkAo@qCwAsF{BcJyHwZgAoEi@uBi@sBG[uAsF[mAW{@W_AWq@IUq@}Ag@gAk@eAMWw@qAEGg@u@Yc@[_@g@k@]e@}AsAu@o@}E_E}AoAgA{@y@m@eAy@_BmAeBwAqAiAwBqB}AsAkAaAIKi@c@ECQOgLoJGECCMK[YCAKKe@]g@e@w@u@[_@c@g@QWQWq@cAk@eAg@eAQa@g@kAa@gA[}@W}@Og@U_AS{@QaAUsASqA_AiHa@sCQiACSIk@OaAQsASsAUuAQaASaAS{@cAsDc@oA_@kAa@kAa@oA_@mA_@qAe@eBw@yCiAuEuAsFiAmEq@oCq@kCk@{Bi@yBa@eBU{@yAmGw@aDq@mCaA{DuDyNm@{BwAsFiAiEYaACKYgAcAiDiAiEwAqFiC_KYeAYeAkF{S{@oDmA_Fo@iCoBsH}AgGuAmF{@gDgAkEi@{Bs@gD_@iBe@}Ba@aCg@sC[mBm@aEWoB[cCUkB]yC[}CWyCSoBUeCSsBEg@O{AGg@[yC]sD_@kEg@yFmAiNYeD_AyKYaDc@}ESiC]mE]wE]cE[qDaAiLs@iIo@cHeA}L_AsKa@iEi@kGc@eFMuAg@wFEg@IwAMuA_@cDq@sFy@cGu@oEaAkF_AmF}AuIo@iDsEqWG_@Ie@ACIa@kAwGoDqSiBcKOy@uEyVqA_HiAoGa@{Bi@wCiCoNm@}DmAeI_CyOaAcHm@_FYaCgAaKsC}Va@gD_AgIaAuG_@sBKm@g@aCq@wCWcAe@cBq@}B_A{CcBsFwAqE_DaKiAoDs@_CYiAgAeEEO[}Am@{C[mBYgBg@kD_@}CU}B_@aFWcE[yFKaCGsAOgEEoAMqFCoAAaBAgDAmA@uGHaN?]DgF@uA?OF}IL}SRoa@h@sbAFqNBsB@iD?uBDiDH{CJ{CRyCDo@BWDg@t@wHd@_EdA}Jf@{EN}APwAFw@HaABs@Bq@By@@_@@{@?s@Am@A{@Am@GeAEs@Gk@AI?EGc@Im@Mq@g@uCQeAi@wCa@aCMq@WyAMw@QsAKcAOcBEs@Es@Ae@A]?U?s@@mA@qC@wCBkDFgE@iH@_BD{FDiG@uC@{@@yAHcQ@}@?C@_ADeLDqI@kFB_CBaFHyODaJTe]H{NDyFBgHBoEBkE?kAF}KBkD?Q@Y?A?i@?C@a@@kB?}CHsEJoET_JFmBFaDFgCDeCHcLDiCDsFBmEDyHFoKF_I@mAB_F@gC@gADy@DgAHgALuAHs@Ly@Jy@TqAH_@VkARq@V}@z@sCd@{AbBoFj@gBH]l@qBb@cB\\\\{AlAyFvEcU\\\\iBHg@TyANeAXsBRgBHeAHoAFiC@q@h@qQBoA?g@?W?kAAy@Cs@Cm@Ca@AIUsBO_AOy@[uASs@Oc@EMIQOc@CGGOCESg@aAcCkCsGc@gAw@uBuAiDaA}BqAyCeBuD}DwIyAcD{@gBc@aAgAcCs@_B}AyDi@wAg@wAi@yAm@cB_@uAc@mAcC{G_BsEsAyDuB_GyAiEo@cBmD_KmAkDqAmDu@oBQ]Q]gAiCuAuCaAsBiB_EyByEyB{EaFwKaE{IuBuEaHuN\"\n" +
                "                     },\n" +
                "                     \"start_location\" : {\n" +
                "                        \"lat\" : 45.2083667,\n" +
                "                        \"lng\" : -74.3482841\n" +
                "                     },\n" +
                "                     \"travel_mode\" : \"DRIVING\"\n" +
                "                  },\n" +
                "                  {\n" +
                "                     \"distance\" : {\n" +
                "                        \"text\" : \"0.2 km\",\n" +
                "                        \"value\" : 226\n" +
                "                     },\n" +
                "                     \"duration\" : {\n" +
                "                        \"text\" : \"1 min\",\n" +
                "                        \"value\" : 11\n" +
                "                     },\n" +
                "                     \"end_location\" : {\n" +
                "                        \"lat\" : 45.4635675,\n" +
                "                        \"lng\" : -73.60723279999999\n" +
                "                     },\n" +
                "                     \"html_instructions\" : \"Keep \\u003cb\\u003eleft\\u003c/b\\u003e to continue on \\u003cb\\u003eAutoroute 720 E\\u003c/b\\u003e\",\n" +
                "                     \"maneuver\" : \"keep-left\",\n" +
                "                     \"polyline\" : {\n" +
                "                        \"points\" : \"ojntGnzg`MaAsByBeF}@oB\"\n" +
                "                     },\n" +
                "                     \"start_location\" : {\n" +
                "                        \"lat\" : 45.4623177,\n" +
                "                        \"lng\" : -73.6095157\n" +
                "                     },\n" +
                "                     \"travel_mode\" : \"DRIVING\"\n" +
                "                  },\n" +
                "                  {\n" +
                "                     \"distance\" : {\n" +
                "                        \"text\" : \"0.8 km\",\n" +
                "                        \"value\" : 837\n" +
                "                     },\n" +
                "                     \"duration\" : {\n" +
                "                        \"text\" : \"1 min\",\n" +
                "                        \"value\" : 79\n" +
                "                     },\n" +
                "                     \"end_location\" : {\n" +
                "                        \"lat\" : 45.4685691,\n" +
                "                        \"lng\" : -73.59926759999999\n" +
                "                     },\n" +
                "                     \"html_instructions\" : \"Keep \\u003cb\\u003eright\\u003c/b\\u003e to stay on \\u003cb\\u003eAutoroute 720 E\\u003c/b\\u003e\\u003cdiv style=\\\"font-size:0.9em\\\"\\u003eMay be closed at certain times or days\\u003c/div\\u003e\",\n" +
                "                     \"maneuver\" : \"keep-right\",\n" +
                "                     \"polyline\" : {\n" +
                "                        \"points\" : \"irntGdlg`MsD_ImBcEgA}BKSsAuCeCwFYi@]o@cAcBs@gAOUcAsAo@w@m@u@i@i@KK\"\n" +
                "                     },\n" +
                "                     \"start_location\" : {\n" +
                "                        \"lat\" : 45.4635675,\n" +
                "                        \"lng\" : -73.60723279999999\n" +
                "                     },\n" +
                "                     \"travel_mode\" : \"DRIVING\"\n" +
                "                  },\n" +
                "                  {\n" +
                "                     \"distance\" : {\n" +
                "                        \"text\" : \"3.2 km\",\n" +
                "                        \"value\" : 3241\n" +
                "                     },\n" +
                "                     \"duration\" : {\n" +
                "                        \"text\" : \"3 mins\",\n" +
                "                        \"value\" : 181\n" +
                "                     },\n" +
                "                     \"end_location\" : {\n" +
                "                        \"lat\" : 45.4910209,\n" +
                "                        \"lng\" : -73.5747258\n" +
                "                     },\n" +
                "                     \"html_instructions\" : \"Keep \\u003cb\\u003eright\\u003c/b\\u003e at the fork to stay on \\u003cb\\u003eAutoroute 720 E\\u003c/b\\u003e, follow signs for \\u003cb\\u003eMontreal\\u003c/b\\u003e\\u003cdiv style=\\\"font-size:0.9em\\\"\\u003eParts of this road may be closed at certain times or days\\u003c/div\\u003e\",\n" +
                "                     \"maneuver\" : \"fork-right\",\n" +
                "                     \"polyline\" : {\n" +
                "                        \"points\" : \"qqotGlze`MCOCE?AOMMSQUUe@a@_Aw@aB]e@WU_@Oe@Oo@SuAc@oC_AgBm@_@KIGa@Kc@Ga@Ge@GYESG_@Kg@QiDiAoDgAaASaD}@kBw@_B_AkA_A{@w@y@{@w@cA{@qAyAqC_@q@_@s@gDoGgHiM{FyKwB_EyAoCIMGGMCa@Ea@y@o@oAy@_B_BqCmA}BOYIOGKS]aBwCq@mAq@iAaA_Bm@aAoAkBmBuC_@g@]e@sAiBiGqI\"\n" +
                "                     },\n" +
                "                     \"start_location\" : {\n" +
                "                        \"lat\" : 45.4685691,\n" +
                "                        \"lng\" : -73.59926759999999\n" +
                "                     },\n" +
                "                     \"travel_mode\" : \"DRIVING\"\n" +
                "                  },\n" +
                "                  {\n" +
                "                     \"distance\" : {\n" +
                "                        \"text\" : \"0.8 km\",\n" +
                "                        \"value\" : 844\n" +
                "                     },\n" +
                "                     \"duration\" : {\n" +
                "                        \"text\" : \"1 min\",\n" +
                "                        \"value\" : 74\n" +
                "                     },\n" +
                "                     \"end_location\" : {\n" +
                "                        \"lat\" : 45.4958323,\n" +
                "                        \"lng\" : -73.56690469999999\n" +
                "                     },\n" +
                "                     \"html_instructions\" : \"Take exit \\u003cb\\u003e4\\u003c/b\\u003e toward \\u003cb\\u003eRue de la Montagne N\\u003c/b\\u003e/\\u003cb\\u003eRue Saint-Jacques\\u003c/b\\u003e\",\n" +
                "                     \"maneuver\" : \"ramp-right\",\n" +
                "                     \"polyline\" : {\n" +
                "                        \"points\" : \"{}stG`aa`MA[A??AMY_AwAiEgGa@s@}@aBs@yAWo@_@sA[qAGUs@sBUu@YeAcBcHK]IMUUSOIEMIUE[@QAO?KCGACAECGEIEEEKO\"\n" +
                "                     },\n" +
                "                     \"start_location\" : {\n" +
                "                        \"lat\" : 45.4910209,\n" +
                "                        \"lng\" : -73.5747258\n" +
                "                     },\n" +
                "                     \"travel_mode\" : \"DRIVING\"\n" +
                "                  },\n" +
                "                  {\n" +
                "                     \"distance\" : {\n" +
                "                        \"text\" : \"66 m\",\n" +
                "                        \"value\" : 66\n" +
                "                     },\n" +
                "                     \"duration\" : {\n" +
                "                        \"text\" : \"1 min\",\n" +
                "                        \"value\" : 12\n" +
                "                     },\n" +
                "                     \"end_location\" : {\n" +
                "                        \"lat\" : 45.4961802,\n" +
                "                        \"lng\" : -73.5675859\n" +
                "                     },\n" +
                "                     \"html_instructions\" : \"Turn \\u003cb\\u003eleft\\u003c/b\\u003e onto \\u003cb\\u003eRue Jean d'Estrées\\u003c/b\\u003e\",\n" +
                "                     \"maneuver\" : \"turn-left\",\n" +
                "                     \"polyline\" : {\n" +
                "                        \"points\" : \"}{ttGbp_`Ms@`BQf@\"\n" +
                "                     },\n" +
                "                     \"start_location\" : {\n" +
                "                        \"lat\" : 45.4958323,\n" +
                "                        \"lng\" : -73.56690469999999\n" +
                "                     },\n" +
                "                     \"travel_mode\" : \"DRIVING\"\n" +
                "                  },\n" +
                "                  {\n" +
                "                     \"distance\" : {\n" +
                "                        \"text\" : \"0.3 km\",\n" +
                "                        \"value\" : 295\n" +
                "                     },\n" +
                "                     \"duration\" : {\n" +
                "                        \"text\" : \"1 min\",\n" +
                "                        \"value\" : 64\n" +
                "                     },\n" +
                "                     \"end_location\" : {\n" +
                "                        \"lat\" : 45.49822349999999,\n" +
                "                        \"lng\" : -73.56518009999999\n" +
                "                     },\n" +
                "                     \"html_instructions\" : \"Turn \\u003cb\\u003eright\\u003c/b\\u003e onto \\u003cb\\u003eRue Saint-Antoine O\\u003c/b\\u003e\",\n" +
                "                     \"maneuver\" : \"turn-right\",\n" +
                "                     \"polyline\" : {\n" +
                "                        \"points\" : \"c~ttGlt_`M_AoAaAoAKKYQgB_CgAwA_AkA\"\n" +
                "                     },\n" +
                "                     \"start_location\" : {\n" +
                "                        \"lat\" : 45.4961802,\n" +
                "                        \"lng\" : -73.5675859\n" +
                "                     },\n" +
                "                     \"travel_mode\" : \"DRIVING\"\n" +
                "                  },\n" +
                "                  {\n" +
                "                     \"distance\" : {\n" +
                "                        \"text\" : \"0.4 km\",\n" +
                "                        \"value\" : 359\n" +
                "                     },\n" +
                "                     \"duration\" : {\n" +
                "                        \"text\" : \"2 mins\",\n" +
                "                        \"value\" : 97\n" +
                "                     },\n" +
                "                     \"end_location\" : {\n" +
                "                        \"lat\" : 45.500069,\n" +
                "                        \"lng\" : -73.56896190000001\n" +
                "                     },\n" +
                "                     \"html_instructions\" : \"Turn \\u003cb\\u003eleft\\u003c/b\\u003e onto \\u003cb\\u003eRue Mansfield\\u003c/b\\u003e\",\n" +
                "                     \"maneuver\" : \"turn-left\",\n" +
                "                     \"polyline\" : {\n" +
                "                        \"points\" : \"{jutGje_`MIPM^_B~DUj@GRmB~E}A`EO^\"\n" +
                "                     },\n" +
                "                     \"start_location\" : {\n" +
                "                        \"lat\" : 45.49822349999999,\n" +
                "                        \"lng\" : -73.56518009999999\n" +
                "                     },\n" +
                "                     \"travel_mode\" : \"DRIVING\"\n" +
                "                  },\n" +
                "                  {\n" +
                "                     \"distance\" : {\n" +
                "                        \"text\" : \"0.2 km\",\n" +
                "                        \"value\" : 231\n" +
                "                     },\n" +
                "                     \"duration\" : {\n" +
                "                        \"text\" : \"1 min\",\n" +
                "                        \"value\" : 55\n" +
                "                     },\n" +
                "                     \"end_location\" : {\n" +
                "                        \"lat\" : 45.5018118,\n" +
                "                        \"lng\" : -73.56734449999999\n" +
                "                     },\n" +
                "                     \"html_instructions\" : \"Turn \\u003cb\\u003eright\\u003c/b\\u003e onto \\u003cb\\u003eBoulevard René-Lévesque O\\u003c/b\\u003e\",\n" +
                "                     \"maneuver\" : \"turn-right\",\n" +
                "                     \"polyline\" : {\n" +
                "                        \"points\" : \"mvutG~|_`MuBoBiAaA{CqC\"\n" +
                "                     },\n" +
                "                     \"start_location\" : {\n" +
                "                        \"lat\" : 45.500069,\n" +
                "                        \"lng\" : -73.56896190000001\n" +
                "                     },\n" +
                "                     \"travel_mode\" : \"DRIVING\"\n" +
                "                  },\n" +
                "                  {\n" +
                "                     \"distance\" : {\n" +
                "                        \"text\" : \"15 m\",\n" +
                "                        \"value\" : 15\n" +
                "                     },\n" +
                "                     \"duration\" : {\n" +
                "                        \"text\" : \"1 min\",\n" +
                "                        \"value\" : 14\n" +
                "                     },\n" +
                "                     \"end_location\" : {\n" +
                "                        \"lat\" : 45.5017123,\n" +
                "                        \"lng\" : -73.5672184\n" +
                "                     },\n" +
                "                     \"html_instructions\" : \"Turn \\u003cb\\u003eright\\u003c/b\\u003e onto \\u003cb\\u003eBoulevard Robert-Bourassa\\u003c/b\\u003e\",\n" +
                "                     \"maneuver\" : \"turn-right\",\n" +
                "                     \"polyline\" : {\n" +
                "                        \"points\" : \"iavtGzr_`MRW\"\n" +
                "                     },\n" +
                "                     \"start_location\" : {\n" +
                "                        \"lat\" : 45.5018118,\n" +
                "                        \"lng\" : -73.56734449999999\n" +
                "                     },\n" +
                "                     \"travel_mode\" : \"DRIVING\"\n" +
                "                  }\n" +
                "               ],\n" +
                "               \"traffic_speed_entry\" : [],\n" +
                "               \"via_waypoint\" : []\n" +
                "            }\n" +
                "         ],\n" +
                "         \"overview_polyline\" : {\n" +
                "            \"points\" : \"e`miGhmocNiI`CeFmHaAmIiDki@oPi`AqHgi@sHkE_Bf@yh@jMmVjRir@dHkd@gIoZsP}[oc@}AaXgF{t@iVkh@ue@yO}\\\\i^{p@v[_yBdNoaCtZq_@{@}Ckj@kBakBag@imDglA_uIcf@ovAie@ccGlB{s@bRev@pA{k@yq@ovA_qBgsDsxC{rIozBsmPfCosEu@{{@uc@__Dmw@yxEke@icEvXwkBv`@wt@hL}w@cr@__L{KoaBi_@kmAsp@qjCiWmgHggCehTqmEq}[as@qpF{ImqB}PwzDwkA}~HbImpA`Me_Bof@ujDwl@ahBmAqgA{Okp@ys@sjChEcfEn^k{AeBwjA_tAomM}t@wvKux@chGm~@s{C{e@mxByn@q{@gi@ycEcQyu@s|@shAqo@omFiy@e|Cyx@myA}t@azDkw@gbCuBcqAiCojBi`@ebAeh@auAcm@}dBudCstGee@olCeaC{iOsc@sjCmjAo{EkhA_iHaa@goCg@uz@zYmgBsJk|BmpBqtXydBs~Uam@kcCka@erB|FamAgj@{tD}P_dGsZycRs^wdQ{Ky|GgCahG_Kgp@gAgk@~`@ceAfm@mfDvg@{qD|C_bC}w@meCia@koC{[odByb@kiAoj@gg@ilAi_Dcq@}}F}X}mIiUc~EsWmjEs`@ojBcWq_DgOyaCrJe`AsA}uBspAm{FyuBahHuNkcCvBgxBoPypAoW_jBiUgu@y_AwfBga@csAk_AatAoj@ov@c`Ak~AsnBai@_mBktAgcBkuAmcCgmC}bAaeAm^ilCrD_sBip@kfB}VqMyqAjB{d@oR}rBy~CycA{b@s^se@qxAm`DcbEwaHoxD}jFa}HyyJwZo}@kXi`B}~F{`Ku`B_cAgfCakEojGyjL{lB{pD_[mu@mhCckGsm@gsBgvDe`Mc`By`FifD__L{PcPy|@{PuS{Rw|AufFgsA_qEibAqiDycCekIcfCk|Jga@{`EfoAq}EnQqlF_LsxDqy@s}CoQwzGob@azBeyAy|EsjBg_FkrEe`Mi_AosAinAobEe_DsrKco@ii@y`BofE_jAudCuv@ciDslFajNqxDo}Esl@chA}kBw`Ge_DgcKmk@}aBwo@uaAmoAkfBs}@s~BoQ_hDq^gvAu[i]at@yu@{_@eeB_dAi`E{]oeDeq@ghF_eA}yFoJghAhBqhEpHk_BeF{wBhDuvE|Ao}AbNgj@jOcqAeDkWu_AaaC_|AefDmk@{ZiTaMy_@}r@_Tc^gp@anA}IiAmKoAiF`NkJcHRW\"\n" +
                "         },\n" +
                "         \"summary\" : \"ON-401 E\",\n" +
                "         \"warnings\" : [],\n" +
                "         \"waypoint_order\" : []\n" +
                "      }\n" +
                "   ],\n" +
                "   \"status\" : \"OK\"\n" +
                "}";
        //</editor-fold>

        Gson gson = new GsonBuilder().create();
        Type listType = new TypeToken<DirectionsApiResponse>(){}.getType();

        DirectionsApiResponse directionsApiResponse = gson.fromJson(response, DirectionsApiResponse.class);

        Log.d(TAG, "Response:"+response);

        if(directionsApiResponse==null)
            Log.d(TAG, "Object read is null");
        else
            Log.d(TAG, "Object is read");

        return directionsApiResponse;

    }

    /**
     * Handle the Directions API Response and print markets, routes, etc on Map
     * @param directionsApiResponse
     */
    protected void handleDirectionsApiResponse(DirectionsApiResponse directionsApiResponse){

        if(directionsApiResponse != null)
            mDirectionsApiResponse = directionsApiResponse;

        GoogleMap currMap = getGoogleMap();

        if(currMap==null) {
            Log.d(TAG, "Google Map is null. Cannot handle Directions API Response");
            return;
        }

        // We will do the following steps for each route Google Provides
        // 1. Print route overlay polyline received
        // 2. Print Legs of route
        // 3. Add source and destination markers.

        DirectionsRoute routes[] = mDirectionsApiResponse.getRoutes();

        if(routes==null || routes.length<1) {
            String logMessage = "No routes exist between "
                    +mNavigationData.getSourcePlaceAsString()
                    +" and "
                    +mNavigationData.getDestinationPlaceAsString();
            Log.d(TAG, logMessage);
            Toast.makeText(BaseNavigationActivity.this, logMessage, Toast.LENGTH_SHORT).show();
            return;
        }

        //<editor-fold desc="Adding the source and destination markers to map as chosen by user">
        LatLng startLocationChosen = new LatLng(mNavigationData.getSourcePlaceAsLatLng().latitude, mNavigationData.getSourcePlaceAsLatLng().longitude);
        String startAddressChosen = mNavigationData.getSourcePlaceAsString();
        if(startAddressChosen==null || startAddressChosen.length()<0)
            startAddressChosen = getString(R.string.label_src);

        LatLng endLocationChosen = new LatLng(mNavigationData.getDestinationPlaceAsLatLng().latitude, mNavigationData.getDestinationPlaceAsLatLng().longitude);
        String endAddressChosen = mNavigationData.getDestinationPlaceAsString();
        if(endAddressChosen==null || endAddressChosen.length()<0)
            endAddressChosen = getString(R.string.label_dest);

        IconGenerator iconFactory = new IconGenerator(this);

        addTextMarker(iconFactory, startAddressChosen, startLocationChosen,
                NavigationMapUtils.getUniqueBackgroundColorForSourceMarker(BaseNavigationActivity.this),
                NavigationMapUtils.getUniqueTextStyleForSourceMarker());

        addTextMarker(iconFactory, endAddressChosen, endLocationChosen,
                NavigationMapUtils.getUniqueBackgroundColorForDestinationMarker(BaseNavigationActivity.this),
                NavigationMapUtils.getUniqueTextStyleForDestinationMarker());
        //</editor-fold>


        int numRoutes = routes.length;
        Toast.makeText(BaseNavigationActivity.this, "Number of routes found: " + numRoutes, Toast.LENGTH_SHORT).show();

        int routeNumber = 1;

        // We will load all the directions for each route and legs
        StringBuilder routesDirections = new StringBuilder();

        for(DirectionsRoute route : routes) {

            Log.d(TAG, "Started printing route "+routeNumber+" on map.");

            routesDirections.append("Route: "+routeNumber+" - "+route.getSummary());
            routesDirections.append("<br/>Legal info: "+route.getCopyrights());

            DirectionsPolyline routeOverViewPolyLine = route.getOverviewPolyline();
            if(routeOverViewPolyLine!=null && routeOverViewPolyLine.getPoints()!=null) {

                List<LatLng> decodedPath = PolyUtil.decode(routeOverViewPolyLine.getPoints());
                currMap.addPolyline(
                        new PolylineOptions()
                                .addAll(decodedPath)
                                .color(NavigationMapUtils.getUniqueColor(this, routeNumber))
                );

            }

            DirectionsRouteLeg[] routeLegs = route.getRouteLegs();

            routesDirections.append("<br/>");
            // We will add one leg only

            if(routeLegs!=null && routeLegs.length>0) {

                Log.d(TAG, "Printing legs for current route.");

                boolean oneLegAdded = false;
                for(DirectionsRouteLeg currLeg : routeLegs) {

                    int legDistance = currLeg.getLegDistance().getValue();

                    LatLng startLocation = new LatLng(currLeg.getStartLocation().getLat(),
                            currLeg.getStartLocation().getLng());
                    String startAddress = currLeg.getStartAddress();
                    if(startAddress==null || startAddress.length()<0)
                        startAddress = getString(R.string.label_src);

                    LatLng endLocation = new LatLng(currLeg.getEndLocation().getLat(),
                            currLeg.getEndLocation().getLng());
                    String endAddress = currLeg.getEndAddress();
                    if(endAddress==null || endAddress.length()<0)
                        endAddress = getString(R.string.label_dest);

                    if(startLocation==null || endLocation==null) {
                        Log.d(TAG, "Invalid start location and end location in current leg.");
                        continue;
                    }

                    addTextMarker(iconFactory, startAddress, startLocation,
                            NavigationMapUtils.getUniqueBackgroundColorForSourceMarker(BaseNavigationActivity.this),
                            NavigationMapUtils.getUniqueTextStyleForSourceMarker());

                    addTextMarker(iconFactory, endAddress, endLocation,
                            NavigationMapUtils.getUniqueBackgroundColorForDestinationMarker(BaseNavigationActivity.this),
                            NavigationMapUtils.getUniqueTextStyleForDestinationMarker());


                    // We will draw arrows on each step start location
                    DirectionsRouteSteps[] steps = currLeg.getRouteSteps();

                    if(steps!=null && steps.length>0) {

                        int stepNumber = 1;
                        for(DirectionsRouteSteps currStep : steps) {

                            int numSteps = steps.length;
                            LatLng startLocationStep = new LatLng(currStep.getStart_location().getLat(),
                                    currStep.getStart_location().getLng());

                            LatLng endLocationStep = new LatLng(currStep.getEnd_location().getLat(),
                                    currStep.getEnd_location().getLng());

                            if(startLocation==null || endLocation==null) {
                                Log.d(TAG, "Invalid start location and end location in current step.");
                                continue;
                            }

                            // Find the direction of route from source to destination
                            float distance = (float) SphericalUtil.computeDistanceBetween(startLocationStep,
                                    endLocationStep);

                            // Draw arrown only if distance between start and end step locations
                            // is more than equated distance of steps to avoida congestion of markers
                            if(distance>=legDistance/numSteps) {
                                float rotation = (float) SphericalUtil.computeHeading(startLocationStep,
                                        endLocationStep);
                                LatLng arrowzMarkerPosition = SphericalUtil.computeOffset(startLocationStep,
                                        50, rotation - 90);


                                Log.d(TAG, "Rotation: "+rotation+" between "+startLocationStep
                                        +" and "+endLocationStep);

                                Marker arrowMarker = getGoogleMap().addMarker(
                                        new MarkerOptions()
                                                .position(arrowzMarkerPosition)
                                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_arrow_large))
                                                .anchor(0.5f,0.5f)
                                                .flat(true)
                                );

                                arrowMarker.setRotation(rotation-90);

                            } else {

                                Log.d(TAG, "Navigation arrow not displayed to reduce congestion of markers.");

                            }

                            if(!oneLegAdded) {

                                String maneuver = "";
                                if(stepNumber==1)
                                    maneuver = "Lets Start";
                                else if(stepNumber==numSteps)
                                    maneuver = "Reached!!!";
                                else
                                    maneuver = currStep.getManeuver();

                                routesDirections.append("<br/><br/><b>" + stepNumber + "--> " + maneuver);
                                routesDirections.append("<br/>"+currStep.getHtmlInstructions());
                            }

                            stepNumber++;

                        }

                    }

                    oneLegAdded = true;

                }

            }

            DirectionsBounds bounds = route.getBounds();

            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            if(bounds.getNorthEast()!=null)
                builder.include(bounds.getNorthEastLatLng());
            if(bounds.getSouthWest()!=null)
                builder.include(bounds.getSouthWestLatLng());

            mapBounds = builder.build();

            resetMapCamera();

            Log.d(TAG, "Repositioned Google Map to show routes as given by bounds in Directions API response");

            Log.d(TAG, "Completed printing route "+routeNumber+" on map.");

            routesDirections.append("<br/><br/>****** Route "+routeNumber+" completed *******");

            routeNumber++;


        }

        displayRouteDirections(routesDirections.toString());

    }

    /**
     * Adjust zoom level to show recent Directions API query received latitude longitude bounds area on Map
     */
    protected void resetMapCamera() {
        if(mapBounds!=null) {
            int padding = 60; // offset from edges of the map in pixels
            CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(mapBounds, padding);
            getGoogleMap().animateCamera(cu);
        }
    }

    /**
     * Adds a marker, does not keep any object reference in activity.
     * @param iconFactory
     * @param text
     * @param position
     */
    private void addTextMarker(IconGenerator iconFactory, CharSequence text, LatLng position,
                               int backgroundColor, int textAppearance) {

        Log.d(TAG, "Adding marker for "+text+" at "+position.latitude+", "+position.longitude);


        String markerLabel = String.valueOf(text.charAt(0));
        if(text.length()>=2)
            markerLabel += String.valueOf(text.charAt(1));

        iconFactory.setColor(backgroundColor);
        iconFactory.setTextAppearance(textAppearance);
        MarkerOptions markerOptions = new MarkerOptions()
                .icon(BitmapDescriptorFactory.fromBitmap(iconFactory.makeIcon(markerLabel)))
                .position(position)
                .anchor(iconFactory.getAnchorU(), iconFactory.getAnchorV())
                .snippet(text.toString());


        Marker temp = getGoogleMap().addMarker(markerOptions);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_navigation_demo_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_NewDirections:
                loadNewDirections();
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    /**
     * Clears the map and resets UI so user can create new directions
     */
    protected void loadNewDirections(){

        initializeNavigationData();
        getGoogleMap().clear();
        setupUiViews();

    }
    protected abstract void onLocationPermissionGranted();

    /**
     * Makes a request for permission grant if permission has not been granted earlier.
     * @return true if location permission is granted - fine location
     * false if not granted
     */
    protected boolean isLocationPermissionGranted() {

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission. ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission. ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(this)
                        .setTitle("Permission for location")
                        .setMessage("To display your current location along with directions API, please allow location permission.")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ActivityCompat.requestPermissions(BaseNavigationActivity.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        REQUEST_CODE_REQUEST_PERMISSION_FINE_LOCATION);
                            }
                        })
                        .create()
                        .show();


            } else {
                // First time, no explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission. ACCESS_FINE_LOCATION},
                        REQUEST_CODE_REQUEST_PERMISSION_FINE_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_REQUEST_PERMISSION_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission. ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        onLocationPermissionGranted();


                    }

                } else {

                    Log.d(TAG, "Permission denied for location.");

                }
                return;
            }

        }
    }

}
