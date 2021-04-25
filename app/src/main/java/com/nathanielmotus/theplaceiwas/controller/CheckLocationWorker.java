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
    String mPackageName;

    public CheckLocationWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        mContext=context;
        mPackageName=context.getPackageName();
    }

    @SuppressLint("MissingPermission")
    @NonNull
    @Override
    public Result doWork() {

        DataIOController dataIOController=new DataIOController(mContext);
        if (!AppStateChecker.isAppInForeground(mContext,mPackageName)) {
            dataIOController.loadData();
            Log.i("TEST","Data loaded : "+Place.getPlaces().size());
        }

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
                    if (!p.hasRecordForToday()) {
                        p.addDateToHistory(IOUtils.today());
                    }
                }
            }
            if (!located && !Place.getPlaces().get(0).hasRecordForToday())
                Place.getPlaces().get(0).addDateToHistory(IOUtils.today());
            if(located && Place.getPlaces().get(0).hasRecordForToday())
                Place.getPlaces().get(0).removeDateFromHistory(IOUtils.today());
        }

        dataIOController.saveData();

        return Result.success();
    }
}
