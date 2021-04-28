package com.nathanielmotus.theplaceiwas.view;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.nathanielmotus.theplaceiwas.R;
import com.nathanielmotus.theplaceiwas.model.DataProviderActivity;
import com.nathanielmotus.theplaceiwas.model.Place;

import java.text.DateFormat;

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
    }

    @Override
    public void onResume() {
        super.onResume();
        updateViews();
    }

    //**********************************************************************************************
    //Configuration
    //**********************************************************************************************

    public void configureViews() {
        mSummaryListView =getActivity().findViewById(R.id.summary_listview);

        mStartDateTextView=getActivity().findViewById(R.id.summary_start_date_text);
        mStartDateTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDataProviderActivity.onStartDateTextClicked();
            }
        });

        mEndDateTextView=getActivity().findViewById(R.id.summary_end_date_text);
        mEndDateTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDataProviderActivity.onEndDateTextClicked();
            }
        });

        mSummaryAddPlaceButton=getActivity().findViewById(R.id.summary_add_button);
        mSummaryAddPlaceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDataProviderActivity.onAddPlaceButtonClicked();
            }
        });

        updateViews();
    }

    private void createCallbackToParentActivity() {
        mDataProviderActivity=(DataProviderActivity)getActivity();
    }

    private void populateSummary() {
        mPlaceListViewAdapter=new PlaceListViewAdapter(Place.getPlaces(),this.getContext());
        mSummaryListView.setAdapter(mPlaceListViewAdapter);
    }

    public void updateViews() {
        DateFormat df=DateFormat.getDateInstance();
        mStartDateTextView.setText(df.format(mDataProviderActivity.getStartDate().getTime()));
        mEndDateTextView.setText(df.format(mDataProviderActivity.getEndDate().getTime()));
        Place.updateDayCounts(mDataProviderActivity.getStartDate(),mDataProviderActivity.getEndDate());
        populateSummary();
    }

    //**********************************************************************************************
    //Getters and setters
    //**********************************************************************************************

    public PlaceListViewAdapter getSummaryAdapter() {
        return mPlaceListViewAdapter;
    }

}