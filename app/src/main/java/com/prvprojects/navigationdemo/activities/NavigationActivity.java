package com.prvprojects.navigationdemo.activities;

import android.annotation.SuppressLint;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.prvprojects.navigationdemo.R;
import com.prvprojects.navigationdemo.datatypes.NavigationData;

/**
 * This is the core UI screen providing user with Navigation handling
 * Created by Pankaj on 31/08/17.
 */

public class NavigationActivity extends BaseNavigationActivity {

    private static final String TAG = "NavigationActivity";

    private TextView tvSelectedSource;
    private TextView tvSelectedDestnation;
    private TextView tvBottomSheetContent;

    private Button btnSelectedSource;
    private Button btnSelectedDestination;

    private ImageView ivResetMapZoomLevel;

    private View bottomSheetRootView;
    private BottomSheetBehavior bottomSheetBehavior;

    private FusedLocationProviderClient mFusedLocationClient;



    @Override
    void loadActivityLayout() {
        setContentView(R.layout.activity_navigation);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
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

                if(isLocationPermissionGranted()) {
                    setGoogleMap(googleMap);
                    onLocationPermissionGranted();
                }
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
            currNavData.setSourcePlace(sourceSelected);

        setupUiViews();

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

        setupUiViews();

        if(currNavData.isReadyForNavigation())
            fetchRoutesFromGoogle(currNavData.getSourcePlaceAsLatLng(),
                    currNavData.getDestinationPlaceAsLatLng());

    }

    @Override
    void setupUiViews() {

        NavigationData currNavData = getmNavigationData();

        tvSelectedSource = findViewById(R.id.navigation_activity_tv_selectedSource);
        tvSelectedDestnation = findViewById(R.id.navigation_activity_tv_selectedDestination);
        tvBottomSheetContent = findViewById(R.id.navigation_activity_tv_bottomSheetContent);

        btnSelectedSource = findViewById(R.id.navigation_activity_btn_selectSource);
        btnSelectedDestination = findViewById(R.id.navigation_activity_btn_selectDestination);

        ivResetMapZoomLevel = findViewById(R.id.navigation_activity_iv_resetCamera);

        bottomSheetRootView = findViewById(R.id.navigation_activity_ll_bottomsheet_rootView);
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetRootView);

        ivResetMapZoomLevel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                NavigationData navData = getmNavigationData();

                if(navData!=null && getGoogleMap()!=null) {

                    resetMapCamera();

                }

            }
        });

        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_DRAGGING) {

                    NavigationData navData = getmNavigationData();

                    if(navData==null
                            || !navData.hasUserSelected_Source()
                            || !navData.hasUserSelected_Destination())
                        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });


        if(currNavData!=null) {

            Log.d(TAG, "Nav object is not null");
            Log.d(TAG, "currNavData.hasUserSelected_Source(): "+currNavData.hasUserSelected_Source());

            if(currNavData.hasUserSelected_Source()) {
                tvSelectedSource.setText(currNavData.getSourcePlaceAsString());
                Log.d(TAG, "tvSelectedSource.setText(currNavData.getSourcePlaceAsString()): "+currNavData.getSourcePlaceAsString());
            }
            else
                tvSelectedSource.setText(R.string.select_source);

            if(currNavData.hasUserSelected_Destination())
                tvSelectedDestnation.setText(currNavData.getDestinationPlaceAsString());
            else
                tvSelectedDestnation.setText(R.string.select_destination);


            if(currNavData.hasUserSelected_Source()
                    && currNavData.hasUserSelected_Destination()) {
                btnSelectedSource.setEnabled(false);
                btnSelectedDestination.setEnabled(false);
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            } else {
                btnSelectedSource.setEnabled(true);
                btnSelectedDestination.setEnabled(true);
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }

        } else {

            Log.d(TAG, "Nav object is null");
            btnSelectedSource.setEnabled(true);
            btnSelectedDestination.setEnabled(true);
            tvSelectedSource.setText(R.string.select_source);
            tvSelectedDestnation.setText(R.string.select_destination);
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

         }

        btnSelectedSource.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestLocationUserInput_Source();
            }
        });

        btnSelectedSource.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                getmNavigationData().setSourcePlace(null);
                setupUiViews();
                return true;
            }
        });

        btnSelectedDestination.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestLocationUserInput_Destination();
            }
        });

        btnSelectedDestination.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                getmNavigationData().setDestinationPlace(null);
                setupUiViews();
                return true;
            }
        });

    }

    @Override
    protected void displayRouteDirections(String htmlEncodedDirections) {

        tvBottomSheetContent.setText(Html.fromHtml(htmlEncodedDirections));

    }

    /**
     * This method is called after checking permission grant only
     */
    @SuppressLint("MissingPermission")
    @Override
    protected void onLocationPermissionGranted() {
        if(getGoogleMap()!=null) {
            getGoogleMap().setMyLocationEnabled(true);
            getGoogleMap().getUiSettings().setZoomControlsEnabled(true);
            getGoogleMap().getUiSettings().setCompassEnabled(true);
            getGoogleMap().getUiSettings().setMyLocationButtonEnabled(true);

            mFusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // Handle last known location
                            if (location != null) {
                                CameraUpdate cu = CameraUpdateFactory.newLatLng(
                                        new LatLng(location.getLatitude(),
                                        location.getLongitude())
                                );
                                getGoogleMap().animateCamera(cu);
                            }
                        }
                    });
        }
    }
}
