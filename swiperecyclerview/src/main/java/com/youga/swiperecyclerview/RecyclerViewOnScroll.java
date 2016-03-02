package com.youga.swiperecyclerview;

import android.support.v7.widget.RecyclerView;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by WuXiaolong on 2015/7/7.
 */
public class RecyclerViewOnScroll extends RecyclerView.OnScrollListener {

    private static final String TAG = "RecyclerViewOnScroll";
    private Timer mTimer;
    private int mRealY, mDeprecatedY;
    private OnFastScrollListener mListener;

    public RecyclerViewOnScroll(OnFastScrollListener listener) {
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
                    Log.i(TAG, "快速滑动");
                    if (mListener != null) mListener.onFastScroll();
                }
                break;
        }
    }

    public interface OnFastScrollListener {
        void onFastScroll();
    }
}
