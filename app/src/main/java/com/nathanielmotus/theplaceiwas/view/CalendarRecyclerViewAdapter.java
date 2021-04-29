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
    private CalendarFragment.CalendarFilter mCalendarFilter;

    public CalendarRecyclerViewAdapter(ArrayList<Place> places, ArrayList<Integer> CheckedPlacesIndexes, Calendar startDate, Calendar endDate, Context context, CalendarFragment.CalendarFilter calendarFilter) {
        mPlaces = places;
        mCheckedPlacesIndexes = CheckedPlacesIndexes;
        mStartDate = startDate;
        mEndDate = endDate;
        mContext = context;
        mCalendarFilter=calendarFilter;
        mDateRecords=new ArrayList<>();
        updateDateRecords();
    }

    @Override
    public void onBindViewHolder(@NonNull CalendarRecyclerViewAdapter.ViewHolder holder, int position) {
        DateFormat df=DateFormat.getDateInstance(DateFormat.FULL);
        holder.dateText.setText(df.format(mDateRecords.get(position).calendar.getTime()));
        for (int i = 0; i < 5; i++) {
            if (mDateRecords.get(position).arePlacesVisitedOnThatDay.size() > i && mDateRecords.get(position).arePlacesVisitedOnThatDay.get(i))
                holder.mTextViews[i].setText("1");
            else
                holder.mTextViews[i].setText("");
        }
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

        if (!(endDate.compareTo(startDate)==0 && isNoneOfThemFilterOK(endDate))) {

            boolean filter;
            while (startDate.compareTo(endDate) <= 0) {
                ArrayList<Boolean> indexes = new ArrayList<>();
                switch (mCalendarFilter) {
                    case NONE_OF_THEM:
                        filter = isNoneOfThemFilterOK(startDate);
                        break;
                    case ONE_OF_THEM:
                        filter = isOneOfThemFilterOK(startDate);
                        break;
                    case ALL_OF_THEM:
                        filter = isAllOfThemFilterOK(startDate);
                        break;
                    default:
                        filter = true;
                }
                if (filter) {
                    for (int i = 0; i < mCheckedPlacesIndexes.size(); i++) {
                        indexes.add(i, mPlaces.get(mCheckedPlacesIndexes.get(i)).hasRecordFor(startDate));
                    }
                    mDateRecords.add(new DateRecord(startDate, indexes));
                }
                startDate.set(Calendar.DAY_OF_MONTH, startDate.get(Calendar.DAY_OF_MONTH) + 1);
            }
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
    //Tests
    //**********************************************************************************************
    private boolean isNoneOfThemFilterOK(Calendar calendar) {
        for (int i:mCheckedPlacesIndexes)
            if (mPlaces.get(i).hasRecordFor(calendar))
                return false;
        return true;
    }

    private boolean isOneOfThemFilterOK(Calendar calendar) {
        for (int i:mCheckedPlacesIndexes)
            if (mPlaces.get(i).hasRecordFor(calendar))
                return  true;
        return false;
    }

    private boolean isAllOfThemFilterOK(Calendar calendar) {
        for (int i:mCheckedPlacesIndexes)
            if (!mPlaces.get(i).hasRecordFor(calendar))
                return false;
        return true;
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
        public static final String ROOT_TAG="calendar_recycler_view_text_";
        public TextView dateText;
        public TextView[] mTextViews=new TextView[5];

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.dateText=itemView.findViewById(R.id.calendar_recyclerview_date_text);
            for (int i = 0; i < 5; i++) {
                this.mTextViews[i]=itemView.findViewWithTag(ROOT_TAG+i);
            }

        }
    }

}
