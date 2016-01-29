package com.fergus.esa.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.fergus.esa.fragments.ImageFragment;
import com.fergus.esa.fragments.LinksFragment;
import com.fergus.esa.fragments.SummaryFragment;
import com.fergus.esa.fragments.TweetFragment;

public class PageAdapter extends FragmentStatePagerAdapter {
    int tabCount;


    public PageAdapter(FragmentManager fm, int tabCount) {
        super(fm);
        this.tabCount = tabCount;
    }


    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                SummaryFragment tab0 = new SummaryFragment();
                return tab0;
			case 1:
				ImageFragment tab1 = new ImageFragment();
				return tab1;
            case 2:
                TweetFragment tab2 = new TweetFragment();
                return tab2;
            case 3:
                LinksFragment tab3 = new LinksFragment();
                return tab3;
            default:
                return null;
        }
    }


    @Override
    public int getCount() {
        return tabCount;
    }
}