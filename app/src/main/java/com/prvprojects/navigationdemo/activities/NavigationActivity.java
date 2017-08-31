package com.prvprojects.navigationdemo.activities;

import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.location.places.Place;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.prvprojects.navigationdemo.R;
import com.prvprojects.navigationdemo.datatypes.NavigationData;

/**
 * This is the core UI screen providing user with Navigation handling
 * Created by Pankaj on 31/08/17.
 */

public class NavigationActivity extends BaseNavigationActivity {

    private static final String TAG = "NavigationActivity";
    private EditText etSource;
    private EditText etDestination;

    @Override
    void loadActivityLayout() {
        setContentView(R.layout.activity_navigation);
    }

    @Override
    void loadGoogleMapFragment() {
        // Get the SupportMapFragment and request notification
        // when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.navigation_actvity_fragment_map);
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                setGoogleMap(googleMap);
            }
        });
    }

    @Override
    String getLogTag() {
        return TAG;
    }

    @Override
    Toolbar getActivityToolBar() {
        return (Toolbar) findViewById(R.id.navigation_activity_toolbar);
    }

    @Override
    TextView getActivityToolbarTitle() {
        return (TextView) findViewById(R.id.navigation_activity_toolbar_tv_title);
    }

    @Override
    void handleLocationSelectedByUser_Source(Place sourceSelected) {

        if(sourceSelected!=null)
            Log.d(TAG, "Source selected: "+sourceSelected.getName());
        else
            Log.d(TAG, "Source selected is null.");

        NavigationData currNavData = getmNavigationData();
        if(currNavData!=null)
            currNavData.setDestinationPlace(sourceSelected);

        if(currNavData.isReadyForNavigation())
            fetchRoutesFromGoogle(currNavData.getSourcePlaceAsLatLng(),
                    currNavData.getDestinationPlaceAsLatLng());

    }


    @Override
    void handleLocationSelectedByUser_Destination(Place destinationSelected) {

        if(destinationSelected!=null)
            Log.d(TAG, "Destination selected: "+destinationSelected.getName());
        else
            Log.d(TAG, "Destination selected is null.");

        NavigationData currNavData = getmNavigationData();
        if(currNavData!=null)
            currNavData.setDestinationPlace(destinationSelected);

        if(currNavData.isReadyForNavigation())
            fetchRoutesFromGoogle(currNavData.getSourcePlaceAsLatLng(),
                    currNavData.getDestinationPlaceAsLatLng());

    }

    @Override
    void setupUiViews() {

        NavigationData currNavData = getmNavigationData();

        etSource = (EditText) findViewById(R.id.navigation_activity_et_userinput_src);
        etDestination = (EditText) findViewById(R.id.navigation_activity_et_userinput_dest);

        if(currNavData!=null) {

            if(currNavData.hasUserSelected_Source())
                etSource.setText(currNavData.getSourcePlaceAsString());
            else
                etDestination.setText(currNavData.getDestinationPlaceAsString());

        }

        etSource.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {

                if(hasFocus)
                    requestLocationUserInput_Source();

            }
        });
        etSource.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {

                // Clear the current location, if the navigation is not in start mode
                return false;
            }
        });

        etDestination.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {

                if(hasFocus)
                    requestLocationUserInput_Destination();

            }
        });
        etDestination.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {

                // Clear the current location, if the navigation is not in start mode
                return false;
            }
        });

    }

}
