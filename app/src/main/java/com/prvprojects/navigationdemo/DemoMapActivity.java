package com.prvprojects.navigationdemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Created by Pankaj on 31/08/17.
 */

public class DemoMapActivity extends AppCompatActivity
        implements OnMapReadyCallback {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Retrieve the content view that renders the map.
        setContentView(R.layout.activity_demomap);
        // Get the SupportMapFragment and request notification
        // when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        // Add a marker in Bangalore, India
        // and move the map's camera to the same location.
        LatLng bangalore = new LatLng(12.9758, 77.5657);
        googleMap.addMarker(new MarkerOptions().position(bangalore)
                .title("Marker in Bagalore"));
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(bangalore));

    }
}
