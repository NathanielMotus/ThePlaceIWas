package com.nathanielmotus.theplaceiwas.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.nathanielmotus.theplaceiwas.R;
import com.nathanielmotus.theplaceiwas.model.CustomDate;
import com.nathanielmotus.theplaceiwas.model.DataProviderActivity;
import com.nathanielmotus.theplaceiwas.model.Place;

import java.util.ArrayList;

public class PlaceListViewAdapter extends BaseAdapter {

    private ArrayList<Place> mPlaces;
    private CustomDate mStartDate;
    private CustomDate mEndDate;
    private Context mContext;

    public PlaceListViewAdapter(ArrayList<Place> places, Context context) {
        mPlaces = places;
        mContext = context;
        mStartDate=new CustomDate(1,1,1,2000);
        mEndDate=new CustomDate(7,31,12,2100);
    }

    public PlaceListViewAdapter(ArrayList<Place> places, CustomDate startDate, CustomDate endDate, Context context) {
        mPlaces = places;
        mStartDate = startDate;
        mEndDate = endDate;
        mContext = context;
    }

    @Override
    public int getCount() {
        return mPlaces.size();
    }

    @Override
    public Object getItem(int position) {
        return mPlaces.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position ;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater layoutInflater= (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView=layoutInflater.inflate(R.layout.summary_layout,null);
        }
        TextView numberView=convertView.findViewById(R.id.summary_listview_layout_number_text);
        TextView placeView=convertView.findViewById(R.id.summary_listview_layout_place_text);
        TextView daysAtView=convertView.findViewById(R.id.summary_listview_layout_days_at_text);
        CheckBox checkBox=convertView.findViewById(R.id.summary_listview_layout_checkbox);

        numberView.setText(Integer.toString(position));
        placeView.setText(mPlaces.get(position).getName());
        daysAtView.setText(Integer.toString(mPlaces.get(position).countDaysAt(mStartDate,mEndDate)));
        checkBox.setChecked(mPlaces.get(position).isInCalendar());
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((DataProviderActivity)mContext).onPlaceClicked(position);
            }
        });

        return convertView;
    }
}
