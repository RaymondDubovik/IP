package com.fergus.esa.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.fergus.esa.fragments.LinksFragment;
import com.fergus.esa.fragments.SummaryFragment;
import com.fergus.esa.fragments.TweetFragment;

public class PageAdapter extends FragmentStatePagerAdapter {
    int mNumOfTabs;


    public PageAdapter(FragmentManager fm, int NumOfTabs) {
        super(fm);
        this.mNumOfTabs = NumOfTabs;
    }


    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                SummaryFragment tab1 = new SummaryFragment();
                return tab1;
            case 1:
                TweetFragment tab2 = new TweetFragment();
                return tab2;
            case 2:
                LinksFragment tab3 = new LinksFragment();
                return tab3;
            default:
                return null;
        }
    }


    @Override
    public int getCount() {
        return mNumOfTabs;
    }
}