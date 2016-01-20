package com.fergus.esa.listeners;

import android.widget.AbsListView;

import java.util.ArrayList;

/**
 * Author: Raymond Dubovik (https://github.com/RaymondDubovik)
 * Date: 20.01.2016
 * @link // http://stackoverflow.com/questions/5465204/how-can-i-set-up-multiple-listeners-for-one-event
 */
public class CompositeScrollListener implements AbsListView.OnScrollListener {
    private ArrayList<AbsListView.OnScrollListener> listeners;


    public CompositeScrollListener() {
        this(new ArrayList<AbsListView.OnScrollListener>());
    }


    public CompositeScrollListener(ArrayList<AbsListView.OnScrollListener> listeners) {
        this.listeners = listeners;
    }


    public void addOnScrollListener(AbsListView.OnScrollListener listener){
        listeners.add(listener);
    }


    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        for (AbsListView.OnScrollListener listener : listeners) {
            listener.onScrollStateChanged(view, scrollState);
        }
    }


    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        for (AbsListView.OnScrollListener listener : listeners) {
            listener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
        }
    }
}
