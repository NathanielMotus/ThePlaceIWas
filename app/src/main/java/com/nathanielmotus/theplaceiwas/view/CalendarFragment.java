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
import android.widget.RadioGroup;
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
    private TextView[] mHeaderTextViews=new TextView[5];
    public static final String HEADER_ROOT_TAG="calendar_header_text";
    private RecyclerView mCalendarRecyclerView;
    private ArrayList<Integer> mCheckedPlacesIndexes=new ArrayList<>();
    private CalendarRecyclerViewAdapter mRecyclerViewAdapter;
    private RadioGroup mFilterRadioGroup;
    private TextView mFilterCountText;
    private CalendarFilter mCalendarFilter=CalendarFilter.NO_FILTER;

    enum CalendarFilter {
        NO_FILTER,
        NONE_OF_THEM,
        ONE_OF_THEM,
        ALL_OF_THEM
    }

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

        for (int i=0;i<5;i++)
            mHeaderTextViews[i]=getActivity().findViewById(R.id.calendar_header_linear).findViewWithTag(HEADER_ROOT_TAG+i);

        mFilterRadioGroup=getActivity().findViewById(R.id.calendar_radio_group);
        mFilterRadioGroup.check(R.id.calendar_radio_no_filter);
        mFilterRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.calendar_radio_no_filter:
                        mCalendarFilter=CalendarFilter.NO_FILTER;
                        break;
                    case R.id.calendar_radio_none_of_them:
                        mCalendarFilter=CalendarFilter.NONE_OF_THEM;
                        break;
                    case R.id.calendar_radio_one_of_them:
                        mCalendarFilter=CalendarFilter.ONE_OF_THEM;
                        break;
                    case R.id.calendar_radio_all_of_them:
                        mCalendarFilter=CalendarFilter.ALL_OF_THEM;
                        break;
                }
                updateViews();
            }
        });

        mFilterCountText=getActivity().findViewById(R.id.calendar_count_text);
    }

    public void updateViews() {
        setupCheckedPlacesIndexes();

        for (int i=0;i<5;i++)
            if (mCheckedPlacesIndexes.size()>i)
                mHeaderTextViews[i].setText(Integer.toString(mCheckedPlacesIndexes.get(i)));
            else
                mHeaderTextViews[i].setText("");

        mRecyclerViewAdapter=new CalendarRecyclerViewAdapter(Place.getPlaces(),
                mCheckedPlacesIndexes,
                mDataProviderActivity.getStartDate(),
                mDataProviderActivity.getEndDate(),
                getContext(),
                mCalendarFilter);

        mCalendarRecyclerView.setAdapter(mRecyclerViewAdapter);

        mFilterCountText.setText(Integer.toString(mRecyclerViewAdapter.getItemCount()));
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