package com.nathanielmotus.theplaceiwas.model;

import android.content.Context;
import android.location.Location;

import com.nathanielmotus.theplaceiwas.controller.IOUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class Place {

    private String mName;
    private ArrayList<CustomDate> mHistory;
    private Location mLocation;
    private float mAccuracy;
    private boolean mInCalendar;
    private int mDayCount;
    private static ArrayList<Place> sPlaces=new ArrayList<>();

    //**********************************************************************************************
    //Constructors
    //**********************************************************************************************

    public Place(String name, ArrayList<CustomDate> history, Location location, float accuracy, boolean inCalendar) {
        mName = name;
        mHistory = history;
        mLocation = location;
        mAccuracy = accuracy;
        mInCalendar = inCalendar;
        mDayCount=0;
        sPlaces.add(this);
    }

    //**********************************************************************************************
    //Getters and setters
    //**********************************************************************************************

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public ArrayList<CustomDate> getHistory() {
        return mHistory;
    }

    public void setHistory(ArrayList<CustomDate> history) {
        mHistory = history;
    }

    public Location getLocation() {
        return mLocation;
    }

    public void setLocation(Location location) {
        mLocation = location;
    }

    public float getAccuracy() {
        return mAccuracy;
    }

    public void setAccuracy(float accuracy) {
        mAccuracy = accuracy;
    }

    public boolean isInCalendar() {
        return mInCalendar;
    }

    public void setInCalendar(boolean inCalendar) {
        mInCalendar = inCalendar;
    }

    public static ArrayList<Place> getPlaces() {
        return sPlaces;
    }

    public static void setPlaces(ArrayList<Place> places) {
        Place.sPlaces = places;
    }

    public int getDayCount() {
        return mDayCount;
    }

    public void setDayCount(int dayCount) {
        mDayCount = dayCount;
    }

    //**********************************************************************************************
    //Modifiers
    //**********************************************************************************************
    public static void clearPlaces() {
        sPlaces.clear();
    }

    public static void removePlace(Place place) {
        sPlaces.remove(place);
    }

    public static void sortPlaces() {
        Collections.sort(sPlaces,new PlaceComparator());
    }

    public static void addInFirstPositionToPlaces(Place place) {
        sPlaces.add(0,place);
    }

    public void addDateToHistory(CustomDate customDate) {
        this.mHistory.add(customDate);
    }

    public void removeDateFromHistory(CustomDate customDate) {
        for (CustomDate d:mHistory)
            if (d.compareTo(customDate)==0)
                mHistory.remove(d);
    }

    //**********************************************************************************************
    //Test
    //**********************************************************************************************
    public boolean isInVicinityOf(Place refPlace) {
        //check whether distance(this,refPlace) is lower than refPlace Accuracy

        return this.mLocation.distanceTo(refPlace.mLocation)<=refPlace.mAccuracy;
    }

    public boolean hasRecordForToday() {
        //check whether this already has a record its history for today

        for (CustomDate d:mHistory)
            if (d.compareTo(CustomDate.todayToCustomDate())==0)
                return true;
            return false;
    }

    //**********************************************************************************************
    //Calculation
    //**********************************************************************************************
    public int countDaysAt(CustomDate startDate, CustomDate endDate) {
        int count=0;
        for (CustomDate d : mHistory) {
            if (d.compareTo(startDate)>=0 && d.compareTo(endDate)<=0)
                count++;
        }
        return count;
    }

    //**********************************************************************************************
    //JSON methods
    //**********************************************************************************************
    public static final String JSON_NAME="name";
    public static final String JSON_HISTORY="history";
    public static final String JSON_LATITUDE="latitude";
    public static final String JSON_LONGITUDE="longitude";
    public static final String JSON_ACCURACY="accuracy";
    public static final String JSON_IN_CALENDAR="inCalendar";

    public JSONObject toJSONObject() {
        JSONObject jsonObject=new JSONObject();
        try {
            jsonObject.put(JSON_NAME, mName);
            jsonObject.put(JSON_HISTORY,getHistoryToJSONArray());
            jsonObject.put(JSON_LATITUDE,mLocation.getLatitude());
            jsonObject.put(JSON_LONGITUDE,mLocation.getLongitude());
            jsonObject.put(JSON_ACCURACY,mAccuracy);
            jsonObject.put(JSON_IN_CALENDAR,mInCalendar);
            return jsonObject;
        } catch (JSONException jsonException) {
            return null;
        }
    }

    public static Place fromJSONObject(JSONObject jsonObject) {
        try {
            Location location = new Location("");
            location.setLatitude(jsonObject.getDouble(JSON_LATITUDE));
            location.setLongitude(jsonObject.getDouble(JSON_LONGITUDE));
            return new Place(jsonObject.getString(JSON_NAME),
                    getHistoryFromJSONArray(jsonObject.getJSONArray(JSON_HISTORY)),
                    location,
                    (float) jsonObject.getDouble(JSON_ACCURACY),
                    jsonObject.getBoolean(JSON_IN_CALENDAR));
        } catch (JSONException jsonException) {
            return null;
        }
    }

    private JSONArray getHistoryToJSONArray() {
        JSONArray historyJSONArray=new JSONArray();
        for (CustomDate d:mHistory)
            historyJSONArray.put(d.toJSONObject());
        return historyJSONArray;
    }

    private static ArrayList<CustomDate> getHistoryFromJSONArray(JSONArray jsonArray) {
        ArrayList<CustomDate> history=new ArrayList<>();
        try {
            for (int i = 0; i < jsonArray.length(); i++)
                history.add(CustomDate.fromJSONObject((JSONObject) jsonArray.get(i)));
            return history;
        } catch (JSONException jsonException) {
            return null;
        }
    }

    public static JSONArray placesToJSONArray() {
        JSONArray jsonArray=new JSONArray();
            for (Place p : sPlaces)
                jsonArray.put(p.toJSONObject());
            return jsonArray;
    }

    public static void createPlacesFromJSONArray(JSONArray jsonArray,CustomDate startDate,CustomDate endDate) {
        Place place;
        try {
            for (int i = 0; i < jsonArray.length(); i++) {
                place = fromJSONObject((JSONObject) jsonArray.get(i));
                place.setDayCount(place.countDaysAt(startDate,endDate));
            }
        } catch (JSONException jsonException) {
            jsonException.printStackTrace();
        }
    }
}
