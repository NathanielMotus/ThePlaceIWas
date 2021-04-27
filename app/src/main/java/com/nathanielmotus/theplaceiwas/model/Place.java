package com.nathanielmotus.theplaceiwas.model;

import android.content.Context;
import android.location.Location;
import android.util.Log;

import com.nathanielmotus.theplaceiwas.controller.IOUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;

public class Place {

    private String mName;
    private ArrayList<Calendar> mHistory;
    private Location mLocation;
    private float mAccuracy;
    private boolean mInCalendar;
    private int mDayCount;
    private static ArrayList<Place> sPlaces=new ArrayList<>();

    //**********************************************************************************************
    //Constructors
    //**********************************************************************************************

    public Place(String name, ArrayList<Calendar> history, Location location, float accuracy, boolean inCalendar) {
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

    public ArrayList<Calendar> getHistory() {
        return mHistory;
    }

    public void setHistory(ArrayList<Calendar> history) {
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

    public Calendar getFirstDate() {
        Calendar firstDate=IOUtils.today();
        for (Calendar c : mHistory) {
            if (firstDate.compareTo(c) > 0) {
                firstDate.set(c.get(Calendar.YEAR),c.get(Calendar.MONTH),c.get(Calendar.DAY_OF_MONTH));
            }
        }
        return firstDate;
    }

    public static Calendar getAbsoluteFirstDate() {
        //return date of first record
        Calendar firstDate=IOUtils.today();
        for (Place p : sPlaces) {
            if (firstDate.compareTo(p.getFirstDate()) > 0) {
                firstDate.set(p.getFirstDate().get(Calendar.YEAR),p.getFirstDate().get(Calendar.MONTH),p.getFirstDate().get(Calendar.DAY_OF_MONTH));
            }
        }
        return firstDate;
    }

    public static Calendar getRelativeFirstDate(ArrayList<Place> places) {
        //return date of first record for places
        Calendar firstDate=IOUtils.today();
        for (Place p : places) {
            if (firstDate.compareTo(p.getFirstDate()) > 0) {
                firstDate.set(p.getFirstDate().get(Calendar.YEAR),p.getFirstDate().get(Calendar.MONTH),p.getFirstDate().get(Calendar.DAY_OF_MONTH));
            }
        }
        return firstDate;
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

    public void addDateToHistory(Calendar calendar) {
        this.mHistory.add(calendar);
    }

    public void removeDateFromHistory(Calendar calendar) {
        if (mHistory.size()>0) {
            for (int i=mHistory.size()-1;i>=0;i--)
                if (mHistory.get(i).compareTo(calendar) == 0)
                    mHistory.remove(mHistory.get(i) );
        }
    }

    //**********************************************************************************************
    //Test
    //**********************************************************************************************
    public boolean isInVicinityOf(Place refPlace) {
        //check whether distance(this,refPlace) is lower than refPlace Accuracy

        return this.mLocation.distanceTo(refPlace.mLocation)<=refPlace.mAccuracy;
    }

    public boolean hasRecordFor(Calendar calendar) {
        for (Calendar c : mHistory) {
            if (c.compareTo(calendar)==0)
                return true;
        }
        return false;
    }

    public boolean hasRecordForToday() {
        //check whether this already has a record in its history for today

        for (Calendar d:mHistory){
            if (d.compareTo(IOUtils.today())==0) {
                return true;
            }
        }
        return false;
    }

    public static boolean aPlaceHasRecordForToday() {
        for (Place p : sPlaces) {
            if (p.hasRecordForToday()) {
                return true;
            }
        }
        return false;
    }
    //**********************************************************************************************
    //Calculation
    //**********************************************************************************************
    public int countDaysAt(Calendar startDate, Calendar endDate) {
        int count=0;
        for (Calendar d : mHistory) {
            if (d.compareTo(startDate)>=0 && d.compareTo(endDate)<=0)
                count++;
        }
        return count;
    }

    public static void updateDayCounts(Calendar startDate,Calendar endDate) {
        for (Place p : sPlaces) {
            p.setDayCount(p.countDaysAt(startDate,endDate));
        }
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
    public static final String JSON_YEAR="year";
    public static final String JSON_MONTH="month";
    public static final String JSON_DAY_OF_MONTH="dayOfMonth";

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
        for (Calendar d:mHistory)
            historyJSONArray.put(calendarToJSONObject(d));
        return historyJSONArray;
    }

    private static ArrayList<Calendar> getHistoryFromJSONArray(JSONArray jsonArray) {
        ArrayList<Calendar> history=new ArrayList<>();
        try {
            for (int i = 0; i < jsonArray.length(); i++)
                history.add(calendarFromJSONObject((JSONObject) jsonArray.get(i)));
            return history;
        } catch (JSONException jsonException) {
            return null;
        }
    }

    private static JSONObject calendarToJSONObject(Calendar calendar) {
        JSONObject jsonObject=new JSONObject();
        try {
            jsonObject.put(JSON_YEAR, calendar.get(Calendar.YEAR));
            jsonObject.put(JSON_MONTH, calendar.get(Calendar.MONTH));
            jsonObject.put(JSON_DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH));
        } catch (JSONException jsonException) {
            jsonException.printStackTrace();
        }
        return jsonObject;
    }

    private static Calendar calendarFromJSONObject(JSONObject jsonObject) {
        Calendar calendar=Calendar.getInstance();
        calendar.clear();
        try {
            calendar.set(jsonObject.getInt(JSON_YEAR),
                    jsonObject.getInt(JSON_MONTH),
                    jsonObject.getInt(JSON_DAY_OF_MONTH));
        } catch (JSONException jsonException) {
            jsonException.printStackTrace();
        }
        return calendar;
    }

    public static JSONArray placesToJSONArray() {
        JSONArray jsonArray=new JSONArray();
            for (Place p : sPlaces)
                jsonArray.put(p.toJSONObject());
            return jsonArray;
    }

    public static void createPlacesFromJSONArray(JSONArray jsonArray,Calendar startDate,Calendar endDate) {
        Place place;
        try {
            for (int i = 0; i < jsonArray.length(); i++) {
                place = fromJSONObject((JSONObject) jsonArray.get(i));
            }
        } catch (JSONException jsonException) {
            jsonException.printStackTrace();
        }
    }
}
