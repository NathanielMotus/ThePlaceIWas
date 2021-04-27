package com.nathanielmotus.theplaceiwas.controller;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;

import com.nathanielmotus.theplaceiwas.R;
import com.google.android.material.tabs.TabLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import com.nathanielmotus.theplaceiwas.model.DataProviderActivity;
import com.nathanielmotus.theplaceiwas.model.Place;
import com.nathanielmotus.theplaceiwas.view.SectionsPagerAdapter;
import com.nathanielmotus.theplaceiwas.view.SummaryFragment;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements DataProviderActivity {
    //todo : recyclerview adapter
    
    private Calendar mStartDate;
    private Calendar mEndDate;
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private Place mNowhereKnownPlace;
    private DataIOController mDataIOController;

    public static final int REQUEST_PERMISSION = 1000;
    private boolean sAccessCoarseLocation = false;
    private boolean sAccessFineLocation = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager(), FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT, this);
        ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(mSectionsPagerAdapter);
        TabLayout tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);
        mDataIOController=new DataIOController(this);
        loadPreferences();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mDataIOController.saveData();
        savePreferences();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mDataIOController.setStartDate(mStartDate);
        mDataIOController.setEndDate(mEndDate);
        mDataIOController.loadData();
        mNowhereKnownPlace=Place.getPlaces().get(0);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            checkPermissions();
        configureCheckLocationWorker();
    }

    //**********************************************************************************************
    //Configuration
    //**********************************************************************************************
    private void configureCheckLocationWorker() {
        WorkRequest CheckLocationRequest=new PeriodicWorkRequest.Builder(CheckLocationWorker.class,15, TimeUnit.MINUTES)
                .build();
//        WorkRequest CheckLocationRequest=new OneTimeWorkRequest.Builder(CheckLocationWorker.class)
//                .setInitialDelay(10,TimeUnit.SECONDS)
//                .build();
        WorkManager.getInstance(this)
                .enqueueUniquePeriodicWork("checkLocationRequest", ExistingPeriodicWorkPolicy.REPLACE,(PeriodicWorkRequest) CheckLocationRequest);
    }

    //**********************************************************************************************
    //DataProviderActivity
    //**********************************************************************************************

    @Override
    public Calendar getStartDate() {
        return mStartDate;
    }

    @Override
    public Calendar getEndDate() {
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
            }
        };

        CurrentLocation currentLocation=new CurrentLocation();
        if(!currentLocation.getLocation(this,locationResult))
            showNoProviderEnabledAlert();
    }

    @Override
    public void onPlaceClicked(int position) {
        if (position!=0)
            showCreateOrEditPlaceDialog(Place.getPlaces().get(position),null);
    }

    @Override
    public void onPlaceCheckboxClicked(int position,boolean isChecked) {
        Place.getPlaces().get(position).setInCalendar(isChecked);
        mSectionsPagerAdapter.getCalendarFragment().updateViews();
    }

    @Override
    public void onStartDateTextClicked() {
        showDatePickerDialog(mStartDate);
    }

    @Override
    public void onEndDateTextClicked() {
        showDatePickerDialog(mEndDate);
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
                Place.removePlace(mNowhereKnownPlace);
                Place.sortPlaces();
                Place.addInFirstPositionToPlaces(mNowhereKnownPlace);
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

    private void showDatePickerDialog(Calendar calendar) {
        DatePickerDialog datePickerDialog=new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                calendar.set(view.getYear(),view.getMonth(),view.getDayOfMonth());
                mSectionsPagerAdapter.getSummaryFragment().updateViews();
                mSectionsPagerAdapter.getCalendarFragment().updateViews();
            }
        },calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH),calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    //**********************************************************************************************
    //Save and load preferences
    //**********************************************************************************************
    public static final String START_DATE_DAY_OF_MONTH = "startDateDayOfMonth";
    public static final String START_DATE_MONTH = "startDateMonth";
    public static final String START_DATE_YEAR = "startDateYear";
    public static final String END_DATE_DAY_OF_MONTH = "endDateDayOfMonth";
    public static final String END_DATE_MONTH = "endDateMonth";
    public static final String END_DATE_YEAR = "endDateYear";


    private void savePreferences() {
        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(START_DATE_DAY_OF_MONTH, mStartDate.get(Calendar.DAY_OF_MONTH));
        editor.putInt(START_DATE_MONTH, mStartDate.get(Calendar.MONTH));
        editor.putInt(START_DATE_YEAR, mStartDate.get(Calendar.YEAR));
        editor.putInt(END_DATE_DAY_OF_MONTH, mEndDate.get(Calendar.DAY_OF_MONTH));
        editor.putInt(END_DATE_MONTH, mEndDate.get(Calendar.MONTH));
        editor.putInt(END_DATE_YEAR, mEndDate.get(Calendar.YEAR));
        editor.apply();
    }

    private void loadPreferences() {
        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        mStartDate=Calendar.getInstance();
        mStartDate.clear();
        mStartDate.set(preferences.getInt(START_DATE_YEAR,2000),preferences.getInt(START_DATE_MONTH,0),preferences.getInt(START_DATE_DAY_OF_MONTH,1));
        mEndDate=Calendar.getInstance();
        mEndDate.clear();
        mEndDate.set(preferences.getInt(END_DATE_YEAR,2025),preferences.getInt(END_DATE_MONTH,11),preferences.getInt(END_DATE_DAY_OF_MONTH,31));
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

}