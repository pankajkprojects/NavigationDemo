package com.prvprojects.navigationdemo.activities;

import android.os.Bundle;
import android.support.v7.widget.SearchView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.prvprojects.navigationdemo.R;

/**
 * This is the core UI screen providing user with Navigation handling
 * Created by Pankaj on 31/08/17.
 */

public class NavigationActivity extends BaseNavigationActivity {

    private static final String TAG = "NavigationActivity";

    @Override
    void loadActivityLayout() {
        setContentView(R.layout.activity_demomap);
    }

    @Override
    void loadGoogleMapFragment() {
        // Get the SupportMapFragment and request notification
        // when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
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

}
