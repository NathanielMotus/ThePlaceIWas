package com.nathanielmotus.theplaceiwas.controller;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.nathanielmotus.theplaceiwas.model.CustomDate;
import com.nathanielmotus.theplaceiwas.model.Place;

public class CheckLocationWorker extends Worker {

    Context mContext;

    public CheckLocationWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        mContext=context;
    }

    @SuppressLint("MissingPermission")
    @NonNull
    @Override
    public Result doWork() {

        Location location,gpsLocation,networkLocation;
        LocationManager locationManager;
        locationManager=(LocationManager)mContext.getSystemService(Context.LOCATION_SERVICE);

        location=null;
        gpsLocation=locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        networkLocation=locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        if (gpsLocation != null && networkLocation != null) {
            if (gpsLocation.getTime()>=networkLocation.getTime())
                location=gpsLocation;
            else
                location=networkLocation;
        } else if (gpsLocation!=null)
            location=gpsLocation;
        else if (networkLocation!=null)
            location=networkLocation;

        if (location != null) {
            boolean located=false;
            for (Place p : Place.getPlaces()) {
                if (Place.getPlaces().indexOf(p)!=0 && location.distanceTo(p.getLocation())<=p.getAccuracy()) {
                    located=true;
                    if (!p.hasRecordForToday())
                        p.addDateToHistory(CustomDate.todayToCustomDate());
                }
            }
            if (!located && !Place.getPlaces().get(0).hasRecordForToday())
                Place.getPlaces().get(0).addDateToHistory(CustomDate.todayToCustomDate());
            if(located && Place.getPlaces().get(0).hasRecordForToday())
                Place.getPlaces().get(0).removeDateFromHistory(CustomDate.todayToCustomDate());
        }

        return Result.success();
    }
}
