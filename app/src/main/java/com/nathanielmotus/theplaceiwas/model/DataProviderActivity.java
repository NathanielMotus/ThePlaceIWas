package com.nathanielmotus.theplaceiwas.model;

import com.nathanielmotus.theplaceiwas.view.SummaryFragment;

import java.util.Calendar;

public interface DataProviderActivity {

    Calendar getStartDate();
    Calendar getEndDate();
    void onAddPlaceButtonClicked();
    void onPlaceClicked(int position);
    void onPlaceCheckboxClicked(int position,boolean isChecked);
    void onStartDateTextClicked();
    void onEndDateTextClicked();
}
