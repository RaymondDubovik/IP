package com.fergus.esa.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.fergus.esa.fragments.NewEventsFragment;

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
                NewEventsFragment tab0 = new NewEventsFragment().setType(NewEventsFragment.TYPE_EVENTS_NEW);
                return tab0;
			case 1:
				NewEventsFragment tab1 = new NewEventsFragment().setType(NewEventsFragment.TYPE_EVENTS_RECOMMENDED);
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