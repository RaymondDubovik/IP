package com.fergus.esa.listeners;

import android.view.View;
import android.widget.AbsListView;

/**
 * Created by budius on 16.05.14.
 * This improves on Zsolt Safrany answer on stack-overflow (see link)
 * by making it a detector that can be attached to any AbsListView.
 * http://stackoverflow.com/questions/8471075/android-listview-find-the-amount-of-pixels-scrolled
 */
public class PixelScrollDetector implements AbsListView.OnScrollListener {
    private final PixelScrollListener listener;
    private TrackElement[] trackElements = {
            new TrackElement(0), // top view, bottom Y
            new TrackElement(1), // mid view, bottom Y
            new TrackElement(2), // mid view, top Y
            new TrackElement(3)};// bottom view, top Y


    public PixelScrollDetector(PixelScrollListener listener) {
        this.listener = listener;
    }


    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        // init the values every time the list is moving
        if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL || scrollState == AbsListView.OnScrollListener.SCROLL_STATE_FLING) {
            for (TrackElement t : trackElements) {
                t.syncState(view);
            }
        }
    }


    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        boolean wasTracked = false;
        for (TrackElement t : trackElements) {
            if (!wasTracked) {
                if (t.isSafeToTrack(view)) {
                    wasTracked = true;
                    if (listener != null)
                        listener.onScroll(view, t.getDeltaY());
                    t.syncState(view);
                } else {
                    t.reset();
                }
            } else {
                t.syncState(view);
            }
        }
    }


    public static interface PixelScrollListener {
        public void onScroll(AbsListView view, float deltaY);
    }

    private static class TrackElement {

        private final int position;


        private TrackElement(int position) {
            this.position = position;
        }


        void syncState(AbsListView view) {
            if (view.getChildCount() > 0) {
                trackedChild = getChild(view);
                trackedChildPrevTop = getY();
                trackedChildPrevPosition = view.getPositionForView(trackedChild);
            }
        }


        void reset() {
            trackedChild = null;
        }


        boolean isSafeToTrack(AbsListView view) {
            return (trackedChild != null) &&
                    (trackedChild.getParent() == view) && (view.getPositionForView(trackedChild) == trackedChildPrevPosition);
        }


        int getDeltaY() {
            return getY() - trackedChildPrevTop;
        }


        private View getChild(AbsListView view) {
            switch (position) {
                case 0:
                    return view.getChildAt(0);
                case 1:
                case 2:
                    return view.getChildAt(view.getChildCount() / 2);
                case 3:
                    return view.getChildAt(view.getChildCount() - 1);
                default:
                    return null;
            }
        }


        private int getY() {
            if (position <= 1) {
                return trackedChild.getBottom();
            } else {
                return trackedChild.getTop();
            }
        }


        View trackedChild;
        int trackedChildPrevPosition;
        int trackedChildPrevTop;
    }
}
