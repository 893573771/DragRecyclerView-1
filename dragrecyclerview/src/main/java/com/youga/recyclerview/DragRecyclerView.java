package com.youga.recyclerview;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.PopupWindow;

import com.youga.recyclerview.adapter.RecyclerAdapter;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Youga on 2016/2/17.
 */
public class DragRecyclerView extends RecyclerView implements RecyclerViewOnScroll.OnScrollListener {
    private static final String TAG = "SwipeRecyclerView";
    private OnScrollListener mOnScrollListener;
    private static final int REQUEST_COUNT = 10;
    private Handler mHandler;
    private View mToolbar;
    private Context mContext;
    private PopupWindow mPopupWindow;

    public DragRecyclerView(Context context) {
        super(context);
        init(context);
    }

    public DragRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public DragRecyclerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        mContext = context;
        mHandler = new Handler(Looper.getMainLooper());

        addOnScrollListener(new RecyclerViewOnScroll(this));

        setLayoutManager(new LinearLayoutManager(mContext));

        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                RecyclerAdapter adapter = (RecyclerAdapter) DragRecyclerView.this.getAdapter();
                if (adapter != null)
                    adapter.setLayoutParams(DragRecyclerView.this.getWidth(), DragRecyclerView.this.getHeight());
            }
        });
    }

    @Override
    public void setAdapter(Adapter adapter) {
        setAdapter(adapter, false);
    }

    public void setAdapter(Adapter adapter, boolean loadMore) {
        super.setAdapter(new RecyclerAdapter(adapter, loadMore));
    }

    private static Boolean twoClick = false;

    public void initToolbar(View toolbar) {
        mToolbar = toolbar;
        if (mToolbar == null) return;
        mToolbar.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Timer tExit;
                if (!twoClick) {
                    twoClick = true;
                    tExit = new Timer();
                    tExit.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            twoClick = false;
                        }
                    }, 1000);
                } else {
                    DragRecyclerView.this.scrollToPosition(0);
                }
            }
        });
    }

    public void showLoadingView() {
        RecyclerAdapter adapter = (RecyclerAdapter) getAdapter();
        adapter.showLoadingView();
    }

    public void showEmptyView(@NonNull String emptyTips) {
        RecyclerAdapter adapter = (RecyclerAdapter) getAdapter();
        adapter.showEmptyView(emptyTips);
    }

    public void showErrorView(@NonNull String errorTips) {
        RecyclerAdapter adapter = (RecyclerAdapter) getAdapter();
        adapter.showErrorView(errorTips);
    }

    public void showItemView() {
        RecyclerAdapter adapter = (RecyclerAdapter) getAdapter();
        adapter.showItemView();
    }

    public void onDragState() {
        showItemView();
    }

    public void onDragState(int resultCount) {
        RecyclerAdapter adapter = (RecyclerAdapter) getAdapter();
        if (resultCount < 0) {
            adapter.setFootState(RecyclerAdapter.FOOT_FAULT);
            Log.i(TAG, "onLoadComplete() -- > FOOT_FAULT");
        } else if (resultCount >= 0 && resultCount < REQUEST_COUNT) {
            adapter.setFootState(RecyclerAdapter.FOOT_NOT_MORE);
            Log.i(TAG, "onLoadComplete() -- > FOOT_NOT_MORE");
        } else {
            adapter.setFootState(RecyclerAdapter.FOOT_MORE);
            Log.i(TAG, "onLoadComplete() -- > FOOT_MORE");
        }
        showItemView();
    }

    @Override
    public void onFastScroll() {
        if (mToolbar != null && mPopupWindow == null) mPopupWindow = createTopPopupWindow(mToolbar);
        if (mPopupWindow != null && !mPopupWindow.isShowing()) {
            mPopupWindow.showAsDropDown(mToolbar, 0, -mToolbar.getHeight());
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mPopupWindow.dismiss();
                }
            }, 1000);
        }
    }

    @Override
    public void hide() {
        if (mOnScrollListener != null) mOnScrollListener.hide();
    }

    @Override
    public void show() {
        if (mOnScrollListener != null) mOnScrollListener.show();
    }


    public interface OnScrollListener {

        void hide();

        void show();
    }

    public void setOnScrollListener(OnScrollListener mOnScrollListener) {
        this.mOnScrollListener = mOnScrollListener;
    }

    public interface OnDragListener {
        void onLoadMore();
    }

    public void setOnDragListener(OnDragListener onDragListener) {
        RecyclerAdapter adapter = (RecyclerAdapter) getAdapter();
        adapter.setOnDragListener(onDragListener);
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        RecyclerAdapter adapter = (RecyclerAdapter) getAdapter();
        adapter.setOnItemClickListener(onItemClickListener);
    }

    private PopupWindow createTopPopupWindow(View toolbar) {
        View rootView = View.inflate(mContext, R.layout.popup_top, null);
        PopupWindow popupWindow = new PopupWindow(rootView, toolbar.getWidth(), toolbar.getHeight());
        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#00000000")));
        return popupWindow;
    }
}
