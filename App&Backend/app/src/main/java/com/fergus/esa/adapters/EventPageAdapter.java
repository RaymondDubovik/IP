package com.fergus.esa.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.SparseArray;
import android.view.ViewGroup;

import com.fergus.esa.fragments.EventsFragment;

public class EventPageAdapter extends FragmentStatePagerAdapter {
	private SparseArray<Fragment> registeredFragments = new SparseArray<>();
	private int tabCount;


    public EventPageAdapter(FragmentManager fm, int tabCount) {
        super(fm);
        this.tabCount = tabCount;
    }


    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                EventsFragment tab0 = new EventsFragment().setType(EventsFragment.TYPE_EVENTS_NEW);
                return tab0;
			case 1:
				EventsFragment tab1 = new EventsFragment().setType(EventsFragment.TYPE_EVENTS_RECOMMENDED);
				return tab1;
            default:
                return null;
        }
    }


    @Override
    public int getCount() {
        return tabCount;
    }


	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		Fragment fragment = (Fragment) super.instantiateItem(container, position);
		registeredFragments.put(position, fragment);
		return fragment;
	}


	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		registeredFragments.remove(position);
		super.destroyItem(container, position, object);
	}


	public Fragment getRegisteredFragment(int position) {
		return registeredFragments.get(position);
	}
}