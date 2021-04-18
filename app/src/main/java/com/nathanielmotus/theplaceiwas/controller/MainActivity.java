package com.nathanielmotus.theplaceiwas.controller;

import android.Manifest;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.EditText;

import com.nathanielmotus.theplaceiwas.R;
import com.google.android.material.tabs.TabLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import com.nathanielmotus.theplaceiwas.model.CustomDate;
import com.nathanielmotus.theplaceiwas.model.DataProviderActivity;
import com.nathanielmotus.theplaceiwas.model.Place;
import com.nathanielmotus.theplaceiwas.view.SectionsPagerAdapter;
import com.nathanielmotus.theplaceiwas.view.SummaryFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements DataProviderActivity {

    private CustomDate mStartDate;
    private CustomDate mEndDate;
    private SectionsPagerAdapter mSectionsPagerAdapter;

    public static final int REQUEST_PERMISSION = 1000;
    private static boolean sAccessCoarseLocation = false;
    private static boolean sAccessFineLocation = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager(), FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT, this);
        ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(mSectionsPagerAdapter);
        TabLayout tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);
        loadPreferences();
        loadData();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        saveData();
        savePreferences();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            checkPermissions();
    }

    //**********************************************************************************************
    //DataProviderActivity
    //**********************************************************************************************

    @Override
    public CustomDate getStartDate() {
        return mStartDate;
    }

    @Override
    public CustomDate getEndDate() {
        return mEndDate;
    }

    @Override
    public void onAddPlaceButtonClicked(){
        CurrentLocation.LocationResult locationResult=new CurrentLocation.LocationResult() {
            @Override
            public void gotLocation(Location location) {
                if (location==null)
                    showNoLocationFoundAlert();
                else
                    showCreateOrEditPlaceDialog(null,location);
                Log.i("TEST",location.toString());
            }
        };

        CurrentLocation currentLocation=new CurrentLocation();
        if(!currentLocation.getLocation(this,locationResult))
            showNoProviderEnabledAlert();
    }

    @Override
    public void onPlaceClicked(int position) {
        showCreateOrEditPlaceDialog(Place.getPlaces().get(position),null);
    }

    //**********************************************************************************************
    //Dialogs
    //**********************************************************************************************

    private void showCreateOrEditPlaceDialog(Place place,Location location) {
        float accuracy;
        String name;
        String title;

        if (place != null) {
            accuracy = place.getAccuracy();
            name = place.getName();
            title = "Edit place";
        }
        else {
            accuracy = 500;
            name = "";
            title = "New place";
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        View customView = getLayoutInflater().inflate(R.layout.add_place_dialog_layout, null);
        builder.setView(customView);
        EditText nameEdit = customView.findViewById(R.id.add_place_dialog_name_edit);
        EditText accuracyEdit = customView.findViewById(R.id.add_place_dialog_accuracy_edit);
        nameEdit.setText(name);
        accuracyEdit.setText(Integer.toString((int) accuracy));
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (place == null) {
                    Place newPlace = new Place(nameEdit.getText().toString(),
                            new ArrayList<>(),
                            location,
                            Float.parseFloat(accuracyEdit.getText().toString()),
                            false);
                } else {
                    place.setName(nameEdit.getText().toString());
                    place.setAccuracy(Float.parseFloat(accuracyEdit.getText().toString()));
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mSectionsPagerAdapter.getSummaryFragment().getSummaryAdapter().notifyDataSetChanged();
                    }
                });
            }
        });
        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        if (place != null)
            builder.setNeutralButton("DELETE", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    showDeletePlaceAlert(place);
                }
            });
        builder.create()
                .show();
    }

    private void showNoProviderEnabledAlert() {
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setTitle("No location provider enabled")
                .setMessage("You must enable location service to create here as a new place")
                .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .create()
                .show();
    }

    private void showNoLocationFoundAlert() {
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setTitle("No location found")
                .setMessage("Location provider was unable to provide a location. Please try again !")
                .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .create()
                .show();
    }

    private void showDeletePlaceAlert(Place place) {
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setTitle("Delete place")
                .setMessage("Are you sure you want to delete "+place.getName()+" ?")
                .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Place.removePlace(place);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mSectionsPagerAdapter.getSummaryFragment().getSummaryAdapter().notifyDataSetChanged();
                            }
                        });
                    }
                })
                .create()
                .show();
    }

    //**********************************************************************************************
    //Save and load data
    //**********************************************************************************************
    public static final String DATA_FILENAME = "tpiwdata";
    public static final String JSON_APP_VERSION_CODE = "appVersionCode";
    public static final String JSON_PLACES = "places";

    private void saveData() {
        IOUtils.saveFileToInternalStorage(getPlacesToJSONObject().toString(), new File(this.getFilesDir(), DATA_FILENAME));
    }

    private void loadData() {
        String jsonString=IOUtils.getFileFromInternalStorage(new File(this.getFilesDir(),DATA_FILENAME));
        JSONObject jsonObject=new JSONObject();
        Place.clearPlaces();
        try {
            jsonObject=new JSONObject(jsonString);
        } catch (JSONException jsonException) {
            jsonException.printStackTrace();
        }
        loadPlacesFromJSONObject(jsonObject);
    }

    private JSONObject getPlacesToJSONObject() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(JSON_APP_VERSION_CODE, IOUtils.getAppVersionCode(this));
            jsonObject.put(JSON_PLACES, Place.placesToJSONArray());
        } catch (JSONException jsonException) {
            jsonException.printStackTrace();
        }
        return jsonObject;
    }

    private void loadPlacesFromJSONObject(JSONObject jsonObject) {
        JSONArray jsonArray=new JSONArray();
        try {
            jsonArray = jsonObject.getJSONArray(JSON_PLACES);
        } catch (JSONException jsonException) {
            jsonArray=null;
        }
        Place.createPlacesFromJSONArray(jsonArray);
    }

    //**********************************************************************************************
    //Save and load preferences
    //**********************************************************************************************
    public static final String START_DATE_DAY_OF_WEEK = "startDateDayOfWeek";
    public static final String START_DATE_DAY_OF_MONTH = "startDateDayOfMonth";
    public static final String START_DATE_MONTH = "startDateMonth";
    public static final String START_DATE_YEAR = "startDateYear";
    public static final String END_DATE_DAY_OF_WEEK = "endDateDayOfWeek";
    public static final String END_DATE_DAY_OF_MONTH = "endDateDayOfMonth";
    public static final String END_DATE_MONTH = "endDateMonth";
    public static final String END_DATE_YEAR = "endDateYear";


    private void savePreferences() {
        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(START_DATE_DAY_OF_WEEK, mStartDate.getDayOfWeek());
        editor.putInt(START_DATE_DAY_OF_MONTH, mStartDate.getDayOfMonth());
        editor.putInt(START_DATE_MONTH, mStartDate.getMonth());
        editor.putInt(START_DATE_YEAR, mStartDate.getYear());
        editor.putInt(END_DATE_DAY_OF_WEEK, mEndDate.getDayOfWeek());
        editor.putInt(END_DATE_DAY_OF_MONTH, mEndDate.getDayOfMonth());
        editor.putInt(END_DATE_MONTH, mEndDate.getMonth());
        editor.putInt(END_DATE_YEAR, mEndDate.getYear());
        editor.apply();
    }

    private void loadPreferences() {
        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        mStartDate = new CustomDate(preferences.getInt(START_DATE_DAY_OF_WEEK, 1),
                preferences.getInt(START_DATE_DAY_OF_MONTH, 1),
                preferences.getInt(START_DATE_MONTH, 1),
                preferences.getInt(START_DATE_YEAR, 2000));
        mEndDate = new CustomDate(preferences.getInt(END_DATE_DAY_OF_WEEK, 7),
                preferences.getInt(END_DATE_DAY_OF_MONTH, 31),
                preferences.getInt(END_DATE_MONTH, 12),
                preferences.getInt(END_DATE_YEAR, 2025));
    }

    //**********************************************************************************************
    //Check permissions
    //**********************************************************************************************
    public static final int ACCESS_COARSE_LOCATION_INDEX = 0;
    public static final int ACCESS_FINE_LOCATION_INDEX = 1;


    private void checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String[] permissionString = {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};

            if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    || checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                requestPermissions(permissionString, REQUEST_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        sAccessCoarseLocation = (requestCode == REQUEST_PERMISSION && grantResults[ACCESS_COARSE_LOCATION_INDEX] == PackageManager.PERMISSION_GRANTED);
        sAccessFineLocation = (requestCode == REQUEST_PERMISSION && grantResults[ACCESS_FINE_LOCATION_INDEX] == PackageManager.PERMISSION_GRANTED);
    }

    //**********************************************************************************************
    //GPS
    //**********************************************************************************************

    private Location getMyLocation() {
        LocationManager locationManager = (LocationManager) getSystemService(this.LOCATION_SERVICE);
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                checkPermissions();
            }
            return locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private boolean checkGPSIsActivated() {
        LocationManager locationManager=(LocationManager)getSystemService(this.LOCATION_SERVICE);
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
            return true;
        else {
            AlertDialog.Builder builder=new AlertDialog.Builder(this);
                builder.setTitle("GPS is off !")
                .setMessage("GPS has to be activated to create here as a new place")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .create()
                .show();
        }
        return false;
    }
}