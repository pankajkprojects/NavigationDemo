package com.prvprojects.navigationdemo.activities;

import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

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
}
