package com.nathanielmotus.theplaceiwas.controller;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import androidx.annotation.NonNull;

import java.util.Timer;
import java.util.TimerTask;

public class CurrentLocation {
    private Timer mTimer;
    private LocationManager mLocationManager;
    private LocationResult mLocationResult;
    private boolean gpsEnabled=false;
    private boolean networkEnabled=false;

    LocationListener gpsLocationListener=new LocationListener() {
        @Override
        public void onLocationChanged(@NonNull Location location) {
            mTimer.cancel();
            mLocationResult.gotLocation(location);
            mLocationManager.removeUpdates(this);
            mLocationManager.removeUpdates(networkLocationListener);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(@NonNull String provider) {

        }

        @Override
        public void onProviderDisabled(@NonNull String provider) {

        }
    };

    LocationListener networkLocationListener=new LocationListener() {
        @Override
        public void onLocationChanged(@NonNull Location location) {
            mTimer.cancel();
            mLocationResult.gotLocation(location);
            mLocationManager.removeUpdates(this);
            mLocationManager.removeUpdates(gpsLocationListener);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(@NonNull String provider) {

        }

        @Override
        public void onProviderDisabled(@NonNull String provider) {

        }
    };

    class GetLastLocation extends TimerTask {

        @SuppressLint("MissingPermission")
        @Override
        public void run() {
            mLocationManager.removeUpdates(gpsLocationListener);
            mLocationManager.removeUpdates(networkLocationListener);

            Location networkLocation=null, gpsLocation=null;
            if (gpsEnabled)
                gpsLocation=mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (networkEnabled)
                networkLocation=mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

            if (gpsLocation != null && networkLocation != null) {
                if (gpsLocation.getTime()>=networkLocation.getTime())
                    mLocationResult.gotLocation(gpsLocation);
                else
                    mLocationResult.gotLocation(networkLocation);
                return;
            }

            if (gpsLocation!=null) {
                mLocationResult.gotLocation(gpsLocation);
                return;
            }

            if (networkLocation != null) {
                mLocationResult.gotLocation(networkLocation);
                return;
            }
            mLocationResult.gotLocation(null);
        }
    }

    @SuppressLint("MissingPermission")
    public boolean getLocation(Context context, LocationResult result) {
        mLocationResult=result;
        if (mLocationManager==null)
            mLocationManager=(LocationManager)context.getSystemService(Context.LOCATION_SERVICE);

        try {
            gpsEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception e) {

        }

        try {
            networkEnabled = mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception e) {

        }

        if (!gpsEnabled && !networkEnabled)
            return false;

        if (gpsEnabled)
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,gpsLocationListener);
        if (networkEnabled)
            mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,0,0,networkLocationListener);

        mTimer=new Timer();
        mTimer.schedule(new GetLastLocation(),20000);
        return true;
    }

    public static abstract class LocationResult{
        public abstract void gotLocation(Location location);
    }
}
