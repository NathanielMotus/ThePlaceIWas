package com.nathanielmotus.theplaceiwas.controller;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.nathanielmotus.theplaceiwas.model.Place;

import java.util.Calendar;

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

        DataIOController dataIOController=new DataIOController(mContext);
        if (Place.getPlaces().size()==0) {
            dataIOController.loadData();
            Log.i("TEST","Load effectué");
        }

        Log.i("TEST","Avant check");
        for (Place p:Place.getPlaces())
            Log.i("TEST", Place.getPlaces().indexOf(p)+" : "+p.getName());

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
            Calendar today=Calendar.getInstance();
            today.clear(Calendar.HOUR);
            today.clear(Calendar.MINUTE);
            today.clear(Calendar.SECOND);
            today.clear(Calendar.MILLISECOND);
            boolean located=false;
            for (Place p : Place.getPlaces()) {
                if (Place.getPlaces().indexOf(p)!=0 && location.distanceTo(p.getLocation())<=p.getAccuracy()) {
                    located=true;
                    if (!p.hasRecordForToday()) {
                        p.addDateToHistory(today);
                    }
                }
            }
            if (!located && !Place.getPlaces().get(0).hasRecordForToday())
                Place.getPlaces().get(0).addDateToHistory(today);
            if(located && Place.getPlaces().get(0).hasRecordForToday())
                Place.getPlaces().get(0).removeDateFromHistory(today);
        }

        Log.i("TEST","Après check");
        for (Place p:Place.getPlaces())
            Log.i("TEST", Place.getPlaces().indexOf(p)+" : "+p.getName());

        dataIOController.saveData();

        return Result.success();
    }
}
