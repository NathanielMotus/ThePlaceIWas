package com.nathanielmotus.theplaceiwas.view;

import android.location.Location;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.nathanielmotus.theplaceiwas.R;
import com.nathanielmotus.theplaceiwas.model.CustomDate;
import com.nathanielmotus.theplaceiwas.model.DataProviderActivity;
import com.nathanielmotus.theplaceiwas.model.Place;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SummaryFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SummaryFragment extends Fragment {

    private ListView mSummaryListView;
    private Button mSummaryAddPlaceButton;
    private TextView mStartDateTextView;
    private TextView mEndDateTextView;
    private DataProviderActivity mDataProviderActivity;
    private PlaceListViewAdapter mPlaceListViewAdapter;

    public SummaryFragment() {
        // Required empty public constructor
    }

    public static SummaryFragment newInstance() {
        SummaryFragment fragment = new SummaryFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        createCallbackToParentActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_summary, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        configureViews();
        populateSummary();
    }

    //**********************************************************************************************
    //Configuration
    //**********************************************************************************************

    public void configureViews() {
        mSummaryListView =getActivity().findViewById(R.id.summary_listview);

        mStartDateTextView=getActivity().findViewById(R.id.summary_start_date_text);
        mStartDateTextView.setText(mDataProviderActivity.getStartDate().toString());

        mEndDateTextView=getActivity().findViewById(R.id.summary_end_date_text);
        mEndDateTextView.setText(mDataProviderActivity.getEndDate().toString());

        mSummaryAddPlaceButton=getActivity().findViewById(R.id.summary_add_button);
        mSummaryAddPlaceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDataProviderActivity.onAddPlaceButtonClicked();
            }
        });
    }

    private void createCallbackToParentActivity() {
        mDataProviderActivity=(DataProviderActivity)getActivity();
    }

    private void populateSummary() {
        mPlaceListViewAdapter=new PlaceListViewAdapter(Place.getPlaces(),this.getContext());
        mSummaryListView.setAdapter(mPlaceListViewAdapter);
    }

    //**********************************************************************************************
    //Getters and setters
    //**********************************************************************************************

    public PlaceListViewAdapter getSummaryAdapter() {
        return mPlaceListViewAdapter;
    }

}