package com.nathanielmotus.theplaceiwas.view;

import android.content.Context;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.nathanielmotus.theplaceiwas.R;
import com.nathanielmotus.theplaceiwas.model.CustomDate;
import com.nathanielmotus.theplaceiwas.model.DataProviderActivity;

/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class SectionsPagerAdapter extends FragmentPagerAdapter {

    private SummaryFragment mSummaryFragment;
    private CalendarFragment mCalendarFragment;

    @StringRes
    private static final int[] TAB_TITLES = new int[]{R.string.tab_text_1, R.string.tab_text_2};
    private final Context mContext;

    public SectionsPagerAdapter(@NonNull FragmentManager fm, int behavior, Context context) {
        super(fm, behavior);
        mContext = context;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                return SummaryFragment.newInstance();
            case 1:
                return CalendarFragment.newInstance("1","2");
            default:return null;
        }
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        Fragment createdFragment=(Fragment) super.instantiateItem(container, position);
        switch (position) {
            case 0:
                mSummaryFragment=(SummaryFragment) createdFragment;
                break;
            case 1:
                mCalendarFragment=(CalendarFragment)createdFragment;
                break;
        }
        return createdFragment;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return mContext.getResources().getString(TAB_TITLES[position]);
    }

    @Override
    public int getCount() {
//         Show 2 total pages.
        return 2;
    }

    public SummaryFragment getSummaryFragment() {
        return mSummaryFragment;
    }

    public CalendarFragment getCalendarFragment() {
        return mCalendarFragment;
    }

}