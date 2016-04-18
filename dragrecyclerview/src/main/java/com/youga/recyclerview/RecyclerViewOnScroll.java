package com.youga.recyclerview;

import android.support.v7.widget.RecyclerView;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;


public class RecyclerViewOnScroll extends RecyclerView.OnScrollListener {

    private static final String TAG = "RecyclerViewOnScroll";
    private Timer mTimer;
    private int mRealY, mDeprecatedY;
    private OnScrollListener mListener;

    private static final float HIDE_THRESHOLD = 100;
    private static final float SHOW_THRESHOLD = 50;

    int scrollDist = 0;
    private boolean isVisible = true;

    public RecyclerViewOnScroll(OnScrollListener listener) {
        mTimer = new Timer();
        this.mListener = listener;
    }

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);
        if (dy < 0) {
            mRealY += -dy;
        } else {
            mRealY = 0;
            mDeprecatedY = 0;
        }

        //  Check scrolled distance against the minimum
        if (isVisible && scrollDist > HIDE_THRESHOLD) {
            //  Hide fab & reset scrollDist
            if (mListener != null) mListener.hide();
            scrollDist = 0;
            isVisible = false;
        }
        //  -MINIMUM because scrolling up gives - dy values
        else if (!isVisible && scrollDist < -SHOW_THRESHOLD) {
            //  Show fab & reset scrollDist
            if (mListener != null) mListener.show();

            scrollDist = 0;
            isVisible = true;
        }

        //  Whether we scroll up or down, calculate scroll distance
        if ((isVisible && dy > 0) || (!isVisible && dy < 0)) {
            scrollDist += dy;
        }

    }

    @Override
    public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
        super.onScrollStateChanged(recyclerView, newState);
        switch (newState) {
            case RecyclerView.SCROLL_STATE_IDLE:
                mTimer.cancel();
                break;
            case RecyclerView.SCROLL_STATE_DRAGGING:
                break;
            case RecyclerView.SCROLL_STATE_SETTLING:
                mTimer = new Timer();
                mTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        mDeprecatedY = mRealY;
                    }
                }, 1000);
                if (mDeprecatedY != 0 && mRealY - mDeprecatedY > 500) {
                    if (mListener != null) mListener.onFastScroll();
                }
                break;
        }
    }

    public interface OnScrollListener {
        void onFastScroll();

        void hide();

        void show();
    }
}
