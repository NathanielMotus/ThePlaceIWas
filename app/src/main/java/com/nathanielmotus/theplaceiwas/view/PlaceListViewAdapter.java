package com.nathanielmotus.theplaceiwas.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.nathanielmotus.theplaceiwas.R;
import com.nathanielmotus.theplaceiwas.model.DataProviderActivity;
import com.nathanielmotus.theplaceiwas.model.Place;

import java.util.ArrayList;

public class PlaceListViewAdapter extends BaseAdapter {

    private ArrayList<Place> mPlaces;
    private Context mContext;

    public PlaceListViewAdapter(ArrayList<Place> places, Context context) {
        mPlaces = places;
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
        daysAtView.setText(Integer.toString(mPlaces.get(position).getDayCount()));
        checkBox.setChecked(mPlaces.get(position).isInCalendar());
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                ((DataProviderActivity)mContext).onPlaceCheckboxClicked(position,isChecked);
            }
        });
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((DataProviderActivity)mContext).onPlaceClicked(position);
            }
        });

        return convertView;
    }
}
