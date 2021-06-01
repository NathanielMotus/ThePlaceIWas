package com.nathanielmotus.theplaceiwas.controller;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.nathanielmotus.theplaceiwas.model.Place;

public class CheckLocationWorker extends Worker {

    Context mContext;
    String mPackageName;
    boolean mIsLoadOK;

    public CheckLocationWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        mContext=context;
        mPackageName=context.getPackageName();
    }

    @SuppressLint("MissingPermission")
    @NonNull
    @Override
    public Result doWork() {

        mIsLoadOK=true;

        DataIOController dataIOController=new DataIOController(mContext);
        if (!AppStateChecker.isAppInForeground(mContext,mPackageName)) {
            mIsLoadOK=dataIOController.loadData();
            Log.i("TEST","Data loaded : "+Place.getPlaces().size());
            Log.i("TEST","Data loaded OK : "+mIsLoadOK);
        }

        CurrentLocation currentLocation=new CurrentLocation();
        Runnable runnable=new Runnable() {
            @Override
            public void run() {
                currentLocation.getLocation(mContext, new CurrentLocation.LocationResult() {
                    @Override
                    public void gotLocation(Location location) {

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
                            if (!located && !Place.aPlaceHasRecordForToday())
                                Place.getPlaces().get(0).addDateToHistory(IOUtils.today());
                            if(located && Place.getPlaces().get(0).hasRecordForToday())
                                Place.getPlaces().get(0).removeDateFromHistory(IOUtils.today());
                        }

                        if(mIsLoadOK)
                            dataIOController.saveData();
                    }
                });
            }
        };
        android.os.Handler handler=new Handler(Looper.getMainLooper());
        handler.post(runnable);

        return Result.success();
    }
}
