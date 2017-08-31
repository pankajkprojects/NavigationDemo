package com.prvprojects.navigationdemo.retrofit;

import com.prvprojects.navigationdemo.datatypes.directionsapi.DirectionsApiResponse;
import com.prvprojects.navigationdemo.datatypes.directionsapi.DirectionsLatLng;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by Pankaj on 31/08/17.
 */

public class ApiInterface {


    // https://maps.googleapis.com/maps/api/directions/json?origin=1,2&destination=4,5&key=KEY_HERE

    @GET("directions/json")
    public Call<DirectionsApiResponse> getDirections(
            @Query("origin") DirectionsLatLng source,
            @Query("destination") DirectionsLatLng destination,
            @Query("key") String apiKey
            ) {
        return null;
    }


}