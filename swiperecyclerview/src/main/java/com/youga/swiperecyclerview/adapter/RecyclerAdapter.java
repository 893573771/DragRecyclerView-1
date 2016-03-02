package com.youga.swiperecyclerview.adapter;


import android.os.Handler;
import android.os.Looper;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import android.view.ViewGroup.LayoutParams;

import com.youga.swiperecyclerview.SwipeRecyclerView.OnSwipeListener;
import com.youga.swiperecyclerview.SwipeRecyclerView.OnItemClickListener;
import com.youga.swiperecyclerview.R;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


/**
 * Created by Youga on 2015/9/2.
 */
public class RecyclerAdapter extends RecyclerWrapper {

    private static final String TAG = "RecyclerAdapter";
    private final Handler mHandler;
    private String[] mTips = new String[2];
    private DisplayMetrics mMetrics;

    public static final int STATE_LOADING = 904291159;
    public static final int STATE_EMPTY = 904291160;
    public static final int STATE_ERROR = 904291161;

    public static final int TYPE_ITEM = 904291162;
    public static final int TYPE_FOOT = 904291163;

    @IntDef({FOOT_GONE, FOOT_LOAD, FOOT_FAULT, FOOT_NOT_MORE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface FootState {
    }

    public static final int FOOT_GONE = 904291164;
    public static final int FOOT_LOAD = 904291165;
    public static final int FOOT_FAULT = 904291166;
    public static final int FOOT_NOT_MORE = 904291167;

    private int mState = STATE_LOADING;
    private int mFootState = FOOT_GONE;

    private OnSwipeListener mOnSwipeListener;
    private OnItemClickListener mOnItemClickListener;
    private boolean mLoadMore;
    private LayoutParams mLayoutParams;

    public RecyclerAdapter(@NonNull RecyclerView.Adapter<RecyclerView.ViewHolder> wrapped, boolean loadMore) {
        super(wrapped);
        this.mLoadMore = loadMore;
        mHandler = new Handler(Looper.getMainLooper());
    }

    private void setState(int state) {
        this.mState = state;
        getWrappedAdapter().notifyDataSetChanged();
        notifyDataSetChanged();
    }

    public void showLoadingView() {
        setState(RecyclerAdapter.STATE_LOADING);
    }

    public void showEmptyView(@NonNull String emptyTips) {
        mTips[0] = emptyTips;
        setState(RecyclerAdapter.STATE_EMPTY);
    }

    public void showErrorView(@NonNull String errorTips) {
        mTips[1] = errorTips;
        setState(RecyclerAdapter.STATE_ERROR);
    }

    public void showItemView() {
        setState(-904291158);
    }

    @Override
    public int getItemCount() {
        switch (mState) {
            case STATE_LOADING:
            case STATE_EMPTY:
            case STATE_ERROR:
                return 1;
            default:
                return mLoadMore && getSuperItemCount() > 0 && mFootState != FOOT_NOT_MORE ? getSuperItemCount() + 1 : getSuperItemCount();
        }
    }

    public int getSuperItemCount() {
        return super.getItemCount();
    }

    @Override
    public int getItemViewType(int position) {
        switch (mState) {
            case STATE_LOADING:
            case STATE_EMPTY:
            case STATE_ERROR:
                return mState;
            default:
                return position == getSuperItemCount() ? TYPE_FOOT : TYPE_ITEM;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mMetrics = parent.getResources().getDisplayMetrics();
        switch (viewType) {
            case STATE_LOADING:
                return new LoadingViewHolder(View.inflate(parent.getContext(), R.layout.loading, null));
            case STATE_EMPTY:
                return new EmptyViewHolder(View.inflate(parent.getContext(), R.layout.empty, null));
            case STATE_ERROR:
                return new ErrorViewHolder(View.inflate(parent.getContext(), R.layout.error, null));
            case TYPE_FOOT:
                return new FootViewHolder(View.inflate(parent.getContext(), R.layout.foot_view, null));
            case TYPE_ITEM:
            default:
                return super.onCreateViewHolder(parent, viewType);
        }
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof LoadingViewHolder
                || holder instanceof EmptyViewHolder
                || holder instanceof ErrorViewHolder) {
            Log.i(TAG, "holder-->" + holder.getClass().getSimpleName());
            LayoutParams params = getLayoutParams();
            if (params != null) holder.itemView.setLayoutParams(params);
        } else if (holder instanceof FootViewHolder) {
            int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, mMetrics);
            LayoutParams params = new LayoutParams(getLayoutParams().width, height);
            holder.itemView.setLayoutParams(params);
            FootViewHolder footHolder = (FootViewHolder) holder;
            if (mFootState == FOOT_GONE) {
                footHolder.load();
            } else if (mFootState == FOOT_FAULT) {
                footHolder.fault();
            } else {
                Log.i(TAG, "footHolder state is FOOT_LOAD");
            }
        } else {
            if (mOnItemClickListener != null)
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mOnItemClickListener.onItemClick(holder.itemView, position);
                    }
                });
            super.onBindViewHolder(holder, position);
        }
    }

    public class LoadingViewHolder extends RecyclerView.ViewHolder {

        public LoadingViewHolder(View itemView) {
            super(itemView);
        }
    }

    public class EmptyViewHolder extends RecyclerView.ViewHolder {
        private final TextView emptyTips;

        public EmptyViewHolder(View itemView) {
            super(itemView);
            emptyTips = (TextView) itemView.findViewById(R.id.empty_tips);
            emptyTips.setText(mTips[0]);
        }
    }

    public class ErrorViewHolder extends RecyclerView.ViewHolder {
        private final TextView errorTips;

        public ErrorViewHolder(View itemView) {
            super(itemView);
            errorTips = (TextView) itemView.findViewById(R.id.error_tips);
            errorTips.setText(mTips[1]);
        }
    }

    public class FootViewHolder extends RecyclerView.ViewHolder {
        private LinearLayout mFootViewLoading, mFootViewFault;

        public FootViewHolder(View itemView) {
            super(itemView);
            mFootViewLoading = (LinearLayout) itemView.findViewById(R.id.footView_loading);
            mFootViewFault = (LinearLayout) itemView.findViewById(R.id.footView_fault);

            mFootViewFault.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    load();
                }
            });
        }

        public void load() {
            bindLoadMore();
            mFootState = FOOT_LOAD;
            mFootViewLoading.setVisibility(View.VISIBLE);
            mFootViewFault.setVisibility(View.INVISIBLE);
        }

        public void fault() {
            mFootViewLoading.setVisibility(View.INVISIBLE);
            mFootViewFault.setVisibility(View.VISIBLE);
        }

        public void bindLoadMore() {
            if (mOnSwipeListener != null && mFootState != FOOT_LOAD)
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mOnSwipeListener.onLoadMore();
                    }
                }, 1000);
        }
    }

    public void setFootState(@FootState int state) {
        this.mFootState = state;
        getWrappedAdapter().notifyDataSetChanged();
        notifyDataSetChanged();
    }

    public void setOnSwipeListener(OnSwipeListener onSwipeListener) {
        this.mOnSwipeListener = onSwipeListener;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.mOnItemClickListener = onItemClickListener;
    }

    public LayoutParams getLayoutParams() {
        return mLayoutParams;
    }

    public void setLayoutParams(int width, int height) {
        if (mLayoutParams == null) {
            this.mLayoutParams = new LayoutParams(width, height);
            getWrappedAdapter().notifyDataSetChanged();
            notifyDataSetChanged();
        }
    }
}
