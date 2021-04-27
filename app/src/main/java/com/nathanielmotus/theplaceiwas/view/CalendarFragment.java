package com.nathanielmotus.theplaceiwas.view;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.nathanielmotus.theplaceiwas.R;
import com.nathanielmotus.theplaceiwas.model.DataProviderActivity;
import com.nathanielmotus.theplaceiwas.model.Place;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CalendarFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CalendarFragment extends Fragment {

    private DataProviderActivity mDataProviderActivity;
    private TextView mHeaderPlace0TextView,mHeaderPlace1TextView,mHeaderPlace2TextView;
    private TextView mHeaderPlace3TextView,mHeaderPlace4TextView;
    private RecyclerView mCalendarRecyclerView;
    private ArrayList<Integer> mCheckedPlacesIndexes=new ArrayList<>();
    private CalendarRecyclerViewAdapter mRecyclerViewAdapter;

    public CalendarFragment() {
        // Required empty public constructor
    }

    public static CalendarFragment newInstance() {
        CalendarFragment fragment = new CalendarFragment();
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
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_calendar, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        configureViews();
        updateViews();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    //**********************************************************************************************
    //Configuration
    //**********************************************************************************************
    private void createCallbackToParentActivity() {
        mDataProviderActivity=(DataProviderActivity)getActivity();
    }

    private void configureViews() {
        mCalendarRecyclerView=getActivity().findViewById(R.id.calendar_detail_recyclerview);
        mCalendarRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        mHeaderPlace0TextView=getActivity().findViewById(R.id.calendar_header_place0_text);
        mHeaderPlace1TextView=getActivity().findViewById(R.id.calendar_header_place1_text);
        mHeaderPlace2TextView=getActivity().findViewById(R.id.calendar_header_place2_text);
        mHeaderPlace3TextView=getActivity().findViewById(R.id.calendar_header_place3_text);
        mHeaderPlace4TextView=getActivity().findViewById(R.id.calendar_header_place4_text);
    }

    public void updateViews() {
        setupCheckedPlacesIndexes();

        if (mCheckedPlacesIndexes.size()>0)
            mHeaderPlace0TextView.setText(Integer.toString(mCheckedPlacesIndexes.get(0)));
        else
            mHeaderPlace0TextView.setText("");

        if (mCheckedPlacesIndexes.size()>1)
            mHeaderPlace1TextView.setText(Integer.toString(mCheckedPlacesIndexes.get(1)));
        else
            mHeaderPlace1TextView.setText("");

        if (mCheckedPlacesIndexes.size()>2)
            mHeaderPlace2TextView.setText(Integer.toString(mCheckedPlacesIndexes.get(2)));
        else
            mHeaderPlace2TextView.setText("");

        if (mCheckedPlacesIndexes.size()>3)
            mHeaderPlace3TextView.setText(Integer.toString(mCheckedPlacesIndexes.get(3)));
        else
            mHeaderPlace3TextView.setText("");

        if (mCheckedPlacesIndexes.size()>4)
            mHeaderPlace4TextView.setText(Integer.toString(mCheckedPlacesIndexes.get(4)));
        else
            mHeaderPlace4TextView.setText("");

        mRecyclerViewAdapter=new CalendarRecyclerViewAdapter(Place.getPlaces(),
                mCheckedPlacesIndexes,
                mDataProviderActivity.getStartDate(),
                mDataProviderActivity.getEndDate(),
                getContext());

        mCalendarRecyclerView.setAdapter(mRecyclerViewAdapter);


    }

    //**********************************************************************************************
    //Calculation
    //**********************************************************************************************
    private void setupCheckedPlacesIndexes() {
        //put in mPlaceCheckedIndexes the indexes of the first 5 checked for calendar places

        mCheckedPlacesIndexes.clear();
        for (int i=0;i<Place.getPlaces().size();i++) {
            if (mCheckedPlacesIndexes.size()<5 && Place.getPlaces().get(i).isInCalendar())
                mCheckedPlacesIndexes.add(i);
        }
    }
}