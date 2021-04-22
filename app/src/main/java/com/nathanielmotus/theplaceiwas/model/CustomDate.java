package com.nathanielmotus.theplaceiwas.model;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;

public class CustomDate {

    private int mDayOfWeek;
    private int mDayOfMonth;
    private int mMonth;
    private int mYear;

    //**********************************************************************************************
    // Constructor
    //**********************************************************************************************

    public CustomDate(int dayOfWeek, int dayOfMonth, int month, int year) {
        mDayOfWeek = dayOfWeek;
        mDayOfMonth = dayOfMonth;
        mMonth = month;
        mYear = year;
    }

    //**********************************************************************************************
    //Getters and setters
    //**********************************************************************************************

    public int getDayOfWeek() {
        return mDayOfWeek;
    }

    public void setDayOfWeek(int dayOfWeek) {
        mDayOfWeek = dayOfWeek;
    }

    public int getDayOfMonth() {
        return mDayOfMonth;
    }

    public void setDayOfMonth(int dayOfMonth) {
        mDayOfMonth = dayOfMonth;
    }

    public int getMonth() {
        return mMonth;
    }

    public void setMonth(int month) {
        mMonth = month;
    }

    public int getYear() {
        return mYear;
    }

    public void setYear(int year) {
        mYear = year;
    }

    //**********************************************************************************************
    //Modifiers
    //**********************************************************************************************
    public static CustomDate todayToCustomDate() {
        return new CustomDate(Calendar.getInstance().get(Calendar.DAY_OF_WEEK),
                Calendar.getInstance().get(Calendar.DAY_OF_MONTH),
                Calendar.getInstance().get(Calendar.MONTH),
                Calendar.getInstance().get(Calendar.YEAR));
    }

    //**********************************************************************************************
    //JSON methods
    //**********************************************************************************************
    public static final String JSON_DAY_OF_WEEK="dayOfWeek";
    public static final String JSON_DAY_OF_MONTH="dayOfMonth";
    public static final String JSON_MONTH="month";
    public static final String JSON_YEAR="year";

    public JSONObject toJSONObject() {
        JSONObject jsonObject=new JSONObject();
        try {
            jsonObject.put(JSON_DAY_OF_WEEK,mDayOfWeek);
            jsonObject.put(JSON_DAY_OF_MONTH,mDayOfMonth);
            jsonObject.put(JSON_MONTH,mMonth);
            jsonObject.put(JSON_YEAR,mYear);
        } catch (JSONException jsonException) {
            jsonException.printStackTrace();
            return null;
        }
        return jsonObject;
    }

    public static CustomDate fromJSONObject(JSONObject jsonObject) {
        try {
            return new CustomDate(jsonObject.getInt(JSON_DAY_OF_WEEK),
                    jsonObject.getInt(JSON_DAY_OF_MONTH),
                    jsonObject.getInt(JSON_MONTH),
                    jsonObject.getInt(JSON_YEAR));
        } catch (JSONException jsonException) {
            jsonException.printStackTrace();
            return null;
        }
    }

    //**********************************************************************************************
    //Tests
    //**********************************************************************************************

    public int compareTo(CustomDate d1) {
        if (this.mYear!=d1.mYear)
            return Integer.compare(this.mYear,d1.mYear);
        else if (this.mMonth!=d1.mMonth)
            return Integer.compare(this.mMonth,d1.mMonth);
        else return Integer.compare(this.mDayOfMonth,d1.mDayOfMonth);
    }

    //**********************************************************************************************
    //Converters
    //**********************************************************************************************

    public String toString() {
        return mDayOfMonth + "/" + mMonth + "/" + mYear;
    }

}
