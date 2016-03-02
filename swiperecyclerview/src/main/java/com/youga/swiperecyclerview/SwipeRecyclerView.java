package com.youga.swiperecyclerview;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.PopupWindow;

import com.youga.swiperecyclerview.adapter.RecyclerAdapter;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Youga on 2016/2/17.
 */
public class SwipeRecyclerView extends FrameLayout implements SwipeRefreshLayout.OnRefreshListener,
        RecyclerViewOnScroll.OnFastScrollListener {
    private static final String TAG = "SwipeRecyclerView";
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView mRecyclerView;
    private OnSwipeListener mOnSwipeListener;
    private static final int REQUEST_COUNT = 20;
    private Handler mHandler;
    private Toolbar mToolbar;
    private Context mContext;
    private PopupWindow mPopupWindow;

    public SwipeRecyclerView(Context context) {
        super(context);
        init(context);
    }

    public SwipeRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public SwipeRecyclerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        mContext = context;
        mHandler = new Handler(Looper.getMainLooper());

        View view = LayoutInflater.from(context).inflate(R.layout.swipe_recycler_view, null);
        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeRefreshLayout);
        mSwipeRefreshLayout.setColorSchemeResources(
                R.color.holo_green_light, R.color.holo_red_light,
                R.color.holo_blue_light, R.color.holo_orange_light);
        mSwipeRefreshLayout.setOnRefreshListener(this);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        mRecyclerView.setVerticalScrollBarEnabled(true);
        mRecyclerView.addOnScrollListener(new RecyclerViewOnScroll(this));

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        mRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        addView(view);

        mRecyclerView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                RecyclerAdapter adapter = (RecyclerAdapter) mRecyclerView.getAdapter();
                if (adapter != null)
                    adapter.setLayoutParams(mRecyclerView.getWidth(), mRecyclerView.getHeight());
            }
        });
    }


    public void setLayoutManager(RecyclerView.LayoutManager manager) {
        mRecyclerView.setLayoutManager(manager);
    }

    public void setAdapter(@NonNull RecyclerView.Adapter<RecyclerView.ViewHolder> adapter) {
        setAdapter(adapter, true);
    }

    public void setAdapter(@NonNull RecyclerView.Adapter<RecyclerView.ViewHolder> adapter, boolean loadMore) {
        mRecyclerView.setAdapter(new RecyclerAdapter(adapter, loadMore));
    }

    private static Boolean twoClick = false;

    public void initToolbar(Toolbar toolbar) {
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
                    mRecyclerView.scrollToPosition(0);
                }
            }
        });
    }

    private void showLoadingView() {
        RecyclerAdapter adapter = (RecyclerAdapter) mRecyclerView.getAdapter();
        adapter.showLoadingView();
    }

    public void showEmptyView(@NonNull String emptyTips) {
        RecyclerAdapter adapter = (RecyclerAdapter) mRecyclerView.getAdapter();
        adapter.showEmptyView(emptyTips);
    }

    public void showErrorView(@NonNull String errorTips) {
        RecyclerAdapter adapter = (RecyclerAdapter) mRecyclerView.getAdapter();
        adapter.showErrorView(errorTips);
    }

    @Override
    public void onRefresh() {
        mSwipeRefreshLayout.setRefreshing(true);
        mSwipeRefreshLayout.setEnabled(false);
        final RecyclerAdapter adapter = (RecyclerAdapter) mRecyclerView.getAdapter();
        if (mOnSwipeListener != null && adapter != null)
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mOnSwipeListener.onRefresh(adapter.getSuperItemCount() == 0);
                }
            }, 1000);
    }

    public void onRefreshComplete(int resultCount, boolean refresh) {
        onRefreshEnabled();
        if (refresh) onLoadMoreComplete(resultCount);
        RecyclerAdapter adapter = (RecyclerAdapter) mRecyclerView.getAdapter();
        adapter.showItemView();
    }

    public void onRefreshLoad() {
        showLoadingView();
        mSwipeRefreshLayout.setRefreshing(true);
        mSwipeRefreshLayout.setEnabled(false);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mOnSwipeListener.onRefresh(true);
            }
        }, 1000);
    }

    public void onRefreshEnabled() {
        mSwipeRefreshLayout.setRefreshing(false);
        mSwipeRefreshLayout.setEnabled(true);
    }

    public void onLoadMoreComplete(int resultCount) {
        RecyclerAdapter adapter = (RecyclerAdapter) mRecyclerView.getAdapter();
        if (resultCount < 0) {
            adapter.setFootState(RecyclerAdapter.FOOT_FAULT);
            Log.i(TAG, "onLoadComplete() -- > FOOT_FAULT");
        } else if (resultCount >= REQUEST_COUNT) {
            adapter.setFootState(RecyclerAdapter.FOOT_GONE);
            Log.i(TAG, "onLoadComplete() -- > MORE");
        } else {
            adapter.setFootState(RecyclerAdapter.FOOT_NOT_MORE);
            Log.i(TAG, "onLoadComplete() -- > NOT_MORE");
        }
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

    public interface OnSwipeListener {

        void onRefresh(boolean refresh);

        void onLoadMore();
    }


    public void setOnSwipeListener(OnSwipeListener onSwipeListener) {
        this.mOnSwipeListener = onSwipeListener;

        mSwipeRefreshLayout.setRefreshing(true);
        mSwipeRefreshLayout.setEnabled(false);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mOnSwipeListener.onRefresh(true);
            }
        }, 1000);

        RecyclerAdapter adapter = (RecyclerAdapter) mRecyclerView.getAdapter();
        adapter.setOnSwipeListener(mOnSwipeListener);
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        RecyclerAdapter adapter = (RecyclerAdapter) mRecyclerView.getAdapter();
        adapter.setOnItemClickListener(onItemClickListener);
    }


    private PopupWindow createTopPopupWindow(Toolbar toolbar) {
        View rootView = View.inflate(mContext, R.layout.popup_top, null);
        PopupWindow popupWindow = new PopupWindow(rootView, toolbar.getWidth(), toolbar.getHeight());
        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#00000000")));
        return popupWindow;
    }
}
