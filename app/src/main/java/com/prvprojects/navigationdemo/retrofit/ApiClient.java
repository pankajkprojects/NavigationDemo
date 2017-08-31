package com.prvprojects.navigationdemo.retrofit;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Pankaj on 31/08/17.
 */

public class ApiClient {

    public static final String BASE_URL = "https://maps.googleapis.com/maps/api/directions/json";
    private static Retrofit retrofitClient = null;


    public static Retrofit getClient() {
        if (retrofitClient==null) {
            retrofitClient = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofitClient;
    }

}
