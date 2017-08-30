package com.prvprojects.navigationdemo.datatypes;

import com.google.android.gms.location.places.Place;
import com.google.android.gms.maps.model.LatLng;

import java.util.Calendar;
import java.util.Date;

/**
 * A POJO class o represent Navigation data at any point.
 * We may save this data in sqlite db and store a history of routes
 * or recommend source/destinations.
 * Created by Pankaj on 31/08/17.
 */

public final class NavigationData {

    Place sourcePlace;
    Place destinationPlace;
    Date lastUpdated;

    public NavigationData(){
        sourcePlace = null;
        destinationPlace = null;
        lastUpdated = Calendar.getInstance().getTime();
    }

    public Place getSourcePlace() {
        return sourcePlace;
    }

    public String getSourcePlaceAsString(){
        return getPlaceAsString(sourcePlace);
    }

    public void setSourcePlace(Place sourcePlace) {
        this.sourcePlace = sourcePlace;
    }

    public Place getDestinationPlace() {
        return destinationPlace;
    }

    public String getDestinationPlaceAsString(){
        return getPlaceAsString(destinationPlace);
    }

    public void setDestinationPlace(Place destinationPlace) {
        this.destinationPlace = destinationPlace;
    }

    public Date getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    /**
     * Converts given Place object as String
     * @param place
     * @return
     */
    private String getPlaceAsString(Place place){

        if(place==null)
            return null;

        StringBuilder stringBuilder = new StringBuilder();
        if(place.getName()!=null)
            stringBuilder.append(place.getName());
        if(place.getAddress()!=null) {
            if(stringBuilder.length()>0)
                stringBuilder.append(", ");
            stringBuilder.append(place.getAddress());
        }

        return stringBuilder.toString();

    }

    /**
     * Returns source place as LatLng object
     * @return
     */
    public LatLng getSourcePlaceAsLatLng(){
        if(sourcePlace==null)
            return null;
        return sourcePlace.getLatLng();
    }

    /**
     * Returns destination place as LatLng object
     * @return
     */
    public LatLng getDestinationPlaceAsLatLng(){
        if(destinationPlace==null)
            return null;
        return destinationPlace.getLatLng();
    }

    /**
     *
     * @return true is user has selected a source position
     */
    public boolean hasUserSelected_Source(){
        return sourcePlace!=null && sourcePlace.getLatLng()!=null;
    }

    /**
     *
     * @return true is user has selected a destination position
     */
    public boolean hasUserSelected_Destination(){
        return destinationPlace!=null && destinationPlace.getLatLng()!=null;
    }

    /**
     *
     * @return true if bot source and destination places have been selected
     */
    public boolean isReadyForNavigation(){
        return sourcePlace!=null && sourcePlace.getLatLng()!=null
                    && destinationPlace!=null && destinationPlace.getLatLng()!=null;
    }

}
