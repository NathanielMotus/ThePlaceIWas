package com.nathanielmotus.theplaceiwas.controller;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

import com.nathanielmotus.theplaceiwas.model.Place;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;

public class DataIOController {
    private Context mContext;
    private Place mNowhereKnownPlace;
    private Calendar mStartDate;
    private Calendar mEndDate;

    public static final String DATA_FILENAME = "tpiwdata";
    public static final String JSON_APP_VERSION_CODE = "appVersionCode";
    public static final String JSON_PLACES = "places";
    public static final String JSON_NOWHERE_KNOWN="nowhereKnown";

    public DataIOController(Context context) {
        mContext = context;
        mStartDate = Calendar.getInstance();
        mEndDate = Calendar.getInstance();
    }

    //**********************************************************************************************
    //setters
    //**********************************************************************************************


    public void setStartDate(Calendar startDate) {
        mStartDate = startDate;
    }

    public void setEndDate(Calendar endDate) {
        mEndDate = endDate;
    }

    //**********************************************************************************************
    //IO
    //**********************************************************************************************

    public void saveData() {
        if (mNowhereKnownPlace==null) {
            mNowhereKnownPlace = Place.getPlaces().get(0);
        }
        IOUtils.saveFileToInternalStorage(getPlacesToJSONObject().toString(), new File(mContext.getFilesDir(), DATA_FILENAME));
        Log.i("TEST","Data saved : "+Place.getPlaces().size());
    }

    public void loadData() {
        String jsonString=IOUtils.getFileFromInternalStorage(new File(mContext.getFilesDir(),DATA_FILENAME));
        JSONObject jsonObject=new JSONObject();
        Place.clearPlaces();
        try {
            jsonObject=new JSONObject(jsonString);
        } catch (JSONException jsonException) {
            Log.i("TEST","Exception : jsonString pas chargé");
            jsonException.printStackTrace();
        }
        loadPlacesFromJSONObject(jsonObject);
    }

    private JSONObject getPlacesToJSONObject() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(JSON_APP_VERSION_CODE, IOUtils.getAppVersionCode(mContext));
            jsonObject.put(JSON_NOWHERE_KNOWN,mNowhereKnownPlace.toJSONObject());
            Place.removePlace(mNowhereKnownPlace);
            jsonObject.put(JSON_PLACES, Place.placesToJSONArray());
            Place.addInFirstPositionToPlaces(mNowhereKnownPlace);
        } catch (JSONException jsonException) {
            jsonException.printStackTrace();
        }
        return jsonObject;
    }

    private void loadPlacesFromJSONObject(JSONObject jsonObject) {
        JSONArray jsonArray;
        JSONObject nowhereKnownJSONObject;
        try {
            nowhereKnownJSONObject=jsonObject.getJSONObject(JSON_NOWHERE_KNOWN);
            jsonArray = jsonObject.getJSONArray(JSON_PLACES);
        } catch (JSONException jsonException) {
            Log.i("TEST","Exception : pas de jsonArray ou pas de nowherKnownJSONObject");
            nowhereKnownJSONObject=null;
            jsonArray=null;
        }
        if (jsonArray!=null){
            Place.createPlacesFromJSONArray(jsonArray,mStartDate,mEndDate);
            mNowhereKnownPlace = Place.fromJSONObject(nowhereKnownJSONObject);
            mNowhereKnownPlace.setDayCount(mNowhereKnownPlace.countDaysAt(mStartDate,mEndDate));
        }
        else
            mNowhereKnownPlace=new Place("Nowhere known",new ArrayList<>(),new Location(""),500,true);
        Place.removePlace(mNowhereKnownPlace);
        Place.addInFirstPositionToPlaces(mNowhereKnownPlace);
    }
}
