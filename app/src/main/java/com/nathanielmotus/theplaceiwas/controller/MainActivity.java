package com.nathanielmotus.theplaceiwas.controller;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;

import com.google.android.material.appbar.AppBarLayout;
import com.nathanielmotus.theplaceiwas.R;
import com.google.android.material.tabs.TabLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements DataProviderActivity {

    private Calendar mStartDate;
    private Calendar mEndDate;
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private Place mNowhereKnownPlace;
    private DataIOController mDataIOController;

    public static final int REQUEST_PERMISSION = 1000;
    private boolean sAccessCoarseLocation = true;
    private boolean sAccessFineLocation = true;
    private boolean sWriteExternalStorage=true;

    private Toolbar mToolbar;

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
        configureToolbar();
        loadPreferences();
        configureCheckLocationWorker();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu,menu);
        return super.onCreateOptionsMenu(menu);
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

    private void configureToolbar() {
        mToolbar=findViewById(R.id.main_activity_toolbar);
        setSupportActionBar(mToolbar);
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
        showDatePickerDialogForBoundaries(mStartDate);
    }

    @Override
    public void onEndDateTextClicked() {
        showDatePickerDialogForBoundaries(mEndDate);
    }

    //**********************************************************************************************
    //Menu events
    //**********************************************************************************************

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId=item.getItemId();
        switch (itemId) {
            case R.id.toolbar_menu_export:
                Log.i("TEST","Permission : "+sWriteExternalStorage);
                if (sWriteExternalStorage)
                    menuItemExport();
                break;
            case R.id.toolbar_menu_import:
                if (sWriteExternalStorage)
                    menuItemImport();
                break;
            case R.id.toolbar_menu_clear_data:
                menuItemClearData();
                break;
        }
        return true;
    }

    private void menuItemExport() {
        Intent intent=new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.setType("text/*");
        startActivityForResult(intent,CREATE_DOCUMENT);
    }

    private void menuItemImport() {
        Intent intent=new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("*/*");
        startActivityForResult(intent,OPEN_DOCUMENT);
    }

    private void menuItemClearData() {
        showDatePickerDialogForClearingData(IOUtils.today());
    }

    //**********************************************************************************************
    //Menu methods
    //**********************************************************************************************
    public static final int CREATE_DOCUMENT=1000;
    public static final int OPEN_DOCUMENT=2000;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == CREATE_DOCUMENT && resultCode == RESULT_OK) {
            Uri filenameUri=data.getData();
            mDataIOController.exportData(filenameUri);
        }
        if (requestCode == OPEN_DOCUMENT && resultCode == RESULT_OK) {
            Uri filenameUri=data.getData();
            mDataIOController.importData(filenameUri);
            mNowhereKnownPlace=Place.getPlaces().get(0);
            mSectionsPagerAdapter.getSummaryFragment().updateViews();
            mSectionsPagerAdapter.getCalendarFragment().updateViews();
            mDataIOController.saveData();
        }

        super.onActivityResult(requestCode, resultCode, data);
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
            title = getString(R.string.dialog_edit_place);
        }
        else {
            accuracy = 500;
            name = "";
            title = getString(R.string.dialog_new_place);
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
        builder.setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        if (place != null)
            builder.setNeutralButton(R.string.dialog_delete, new DialogInterface.OnClickListener() {
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
        builder.setTitle(R.string.dialog_no_provider)
                .setMessage(R.string.dialog_enable_service)
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
        builder.setTitle(R.string.dialog_no_location_found)
                .setMessage(R.string.dialog_unable_to_provide_location)
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
        builder.setTitle(R.string.dialog_delete_place)
                .setMessage(getString(R.string.dialog_delete_alert)+place.getName()+" ?")
                .setNegativeButton(R.string.dialog_no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setPositiveButton(R.string.dialog_yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Place.removePlace(place);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mSectionsPagerAdapter.getSummaryFragment().getSummaryAdapter().notifyDataSetChanged();
                            }
                        });
                        mSectionsPagerAdapter.getCalendarFragment().updateViews();
                    }
                })
                .create()
                .show();
    }

    private void showDatePickerDialogForBoundaries(Calendar calendar) {
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

    private void showDatePickerDialogForClearingData(Calendar calendar) {
        DatePickerDialog datePickerDialog=new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                calendar.set(view.getYear(),view.getMonth(),view.getDayOfMonth());
                Place.clearUntil(calendar);
                mSectionsPagerAdapter.getSummaryFragment().updateViews();
                mSectionsPagerAdapter.getCalendarFragment().updateViews();
            }
        },calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH),calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.setTitle(getString(R.string.dialog_clear_data));
        datePickerDialog.setMessage(getString(R.string.dialog_enter_date));
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
    public static final int WRITE_EXTERNAL_STORAGE_INDEX=2;


    private void checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String[] permissionString = {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.WRITE_EXTERNAL_STORAGE};

            if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    || checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    || checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)!=PackageManager.PERMISSION_GRANTED)
                requestPermissions(permissionString, REQUEST_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        sAccessCoarseLocation = (requestCode == REQUEST_PERMISSION && grantResults[ACCESS_COARSE_LOCATION_INDEX] == PackageManager.PERMISSION_GRANTED);
        sAccessFineLocation = (requestCode == REQUEST_PERMISSION && grantResults[ACCESS_FINE_LOCATION_INDEX] == PackageManager.PERMISSION_GRANTED);
        sWriteExternalStorage=(requestCode==REQUEST_PERMISSION && grantResults[WRITE_EXTERNAL_STORAGE_INDEX]==PackageManager.PERMISSION_GRANTED);
    }

}