package com.nathanielmotus.theplaceiwas.view;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nathanielmotus.theplaceiwas.R;
import com.nathanielmotus.theplaceiwas.controller.IOUtils;
import com.nathanielmotus.theplaceiwas.model.Place;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class CalendarRecyclerViewAdapter extends RecyclerView.Adapter<CalendarRecyclerViewAdapter.ViewHolder> {
    private ArrayList<Place> mPlaces;
    private ArrayList<Integer> mCheckedPlacesIndexes;
    private ArrayList<DateRecord> mDateRecords;
    private Calendar mStartDate,mEndDate;
    private Context mContext;

    public CalendarRecyclerViewAdapter(ArrayList<Place> places, ArrayList<Integer> CheckedPlacesIndexes, Calendar startDate, Calendar endDate, Context context) {
        mPlaces = places;
        mCheckedPlacesIndexes = CheckedPlacesIndexes;
        mStartDate = startDate;
        mEndDate = endDate;
        mContext = context;
        mDateRecords=new ArrayList<>();
        updateDateRecords();
    }

    @Override
    public void onBindViewHolder(@NonNull CalendarRecyclerViewAdapter.ViewHolder holder, int position) {
        DateFormat df=DateFormat.getDateInstance();
        holder.dateText.setText(df.format(mDateRecords.get(position).calendar.getTime()));
        if (mDateRecords.get(position).arePlacesVisitedOnThatDay.size()>0 && mDateRecords.get(position).arePlacesVisitedOnThatDay.get(0))
            holder.place0.setText("1");
        else holder.place0.setText("");
        if (mDateRecords.get(position).arePlacesVisitedOnThatDay.size()>1 && mDateRecords.get(position).arePlacesVisitedOnThatDay.get(1))
            holder.place1.setText("1");
        else holder.place1.setText("");
        if (mDateRecords.get(position).arePlacesVisitedOnThatDay.size()>2 && mDateRecords.get(position).arePlacesVisitedOnThatDay.get(2))
            holder.place2.setText("1");
        else holder.place2.setText("");
        if (mDateRecords.get(position).arePlacesVisitedOnThatDay.size()>3 && mDateRecords.get(position).arePlacesVisitedOnThatDay.get(3))
            holder.place3.setText("1");
        else holder.place3.setText("");
        if (mDateRecords.get(position).arePlacesVisitedOnThatDay.size()>4 && mDateRecords.get(position).arePlacesVisitedOnThatDay.get(4))
            holder.place4.setText("1");
        else holder.place4.setText("");
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater=(LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View listItem=layoutInflater.inflate(R.layout.calendar_recyclerview_layout,parent,false);
        ViewHolder viewHolder=new ViewHolder(listItem);

        return viewHolder;
    }


    @Override
    public int getItemCount() {
        return mDateRecords.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    //**********************************************************************************************
    //Modifiers
    //**********************************************************************************************
    public void updateDateRecords() {
        Calendar startDate= IOUtils.today();
        Calendar endDate=IOUtils.today();

        if (getCheckedPlacesFirstDate().compareTo(mStartDate) > 0) {
            startDate.set(getCheckedPlacesFirstDate().get(Calendar.YEAR),
                    getCheckedPlacesFirstDate().get(Calendar.MONTH),
                    getCheckedPlacesFirstDate().get(Calendar.DAY_OF_MONTH));
        } else
            startDate.set(mStartDate.get(Calendar.YEAR),
                    mStartDate.get(Calendar.MONTH),
                    mStartDate.get(Calendar.DAY_OF_MONTH));

        if (endDate.compareTo(mEndDate) > 0) {
            endDate.set(mEndDate.get(Calendar.YEAR),
                    mEndDate.get(Calendar.MONTH),
                    mEndDate.get(Calendar.DAY_OF_MONTH));
        }

        while (startDate.compareTo(endDate) <= 0) {
            ArrayList<Boolean> indexes=new ArrayList<>();
            for (int i=0;i<mCheckedPlacesIndexes.size();i++) {
                indexes.add(i,mPlaces.get(mCheckedPlacesIndexes.get(i)).hasRecordFor(startDate));
            }
            mDateRecords.add(new DateRecord(startDate,indexes));
            startDate.set(Calendar.DAY_OF_MONTH,startDate.get(Calendar.DAY_OF_MONTH)+1);
        }

    }

    //**********************************************************************************************
    //Getters
    //**********************************************************************************************
    private Calendar getCheckedPlacesFirstDate() {
        ArrayList<Place> places=new ArrayList<>();
        for (int index: mCheckedPlacesIndexes)
            places.add(mPlaces.get(index));
        return Place.getRelativeFirstDate(places);
    }

    //**********************************************************************************************
    //Private inner class
    //**********************************************************************************************

    private class DateRecord {
        //change data from (Place, Dates at) to (Date, visited Places indexes)
        Calendar calendar;
        ArrayList<Boolean> arePlacesVisitedOnThatDay;

        public DateRecord(Calendar calendar, ArrayList<Boolean> arePlacesVisitedOnThatDay) {
            this.calendar = IOUtils.today();
            this.calendar.set(calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH),calendar.get(Calendar.DAY_OF_MONTH));
            this.arePlacesVisitedOnThatDay = arePlacesVisitedOnThatDay;
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView dateText,place0,place1,place2,place3,place4;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.dateText=itemView.findViewById(R.id.calendar_recyclerview_date_text);
            this.place0=itemView.findViewById(R.id.calendar_recyclerview_group0_text);
            this.place1=itemView.findViewById(R.id.calendar_recyclerview_group1_text);
            this.place2=itemView.findViewById(R.id.calendar_recyclerview_group2_text);
            this.place3=itemView.findViewById(R.id.calendar_recyclerview_group3_text);
            this.place4=itemView.findViewById(R.id.calendar_recyclerview_group4_text);

        }
    }

}
