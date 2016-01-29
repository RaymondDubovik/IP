package com.fergus.esa.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.fergus.esa.fragments.NewEventsFragment;
import com.fergus.esa.fragments.RecommendedEventsFragment;

public class EventPageAdapter extends FragmentStatePagerAdapter {
    int tabCount;


    public EventPageAdapter(FragmentManager fm, int tabCount) {
        super(fm);
        this.tabCount = tabCount;
    }


    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                NewEventsFragment tab0 = new NewEventsFragment();
                return tab0;
			case 1:
				RecommendedEventsFragment tab1 = new RecommendedEventsFragment();
				return tab1;
            default:
                return null;
        }
    }


    @Override
    public int getCount() {
        return tabCount;
    }
}