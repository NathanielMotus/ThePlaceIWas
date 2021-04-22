package com.nathanielmotus.theplaceiwas.model;

public interface DataProviderActivity {

    CustomDate getStartDate();
    CustomDate getEndDate();
    void onAddPlaceButtonClicked();
    void onPlaceClicked(int position);
    void onPlaceCheckboxClicked(int position,boolean isChecked);
}
