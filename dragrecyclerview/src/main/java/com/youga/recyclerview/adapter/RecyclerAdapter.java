package com.youga.recyclerview.adapter;


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

import com.youga.recyclerview.R;
import com.youga.recyclerview.DragRecyclerView.OnDragListener;
import com.youga.recyclerview.DragRecyclerView.OnItemClickListener;

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

    //STATE_DEFAULT is show Item
    public static final int STATE_DEFAULT = -904291158;
    public static final int STATE_LOADING = 904291159;
    public static final int STATE_EMPTY = 904291160;
    public static final int STATE_ERROR = 904291161;
    public static final int STATE_FOOT = 904291162;

    @IntDef({FOOT_MORE, FOOT_NOT_MORE, FOOT_FAULT})
    @Retention(RetentionPolicy.SOURCE)
    public @interface FootState {
    }

    public static final int FOOT_MORE = 904291164;
    public static final int FOOT_NOT_MORE = 904291165;
    public static final int FOOT_FAULT = 904291166;

    private int mState = STATE_DEFAULT;
    private int mFootState = FOOT_MORE;

    private OnDragListener mOnDragListener;
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
        setState(STATE_LOADING);
    }

    public void showEmptyView(@NonNull String emptyTips) {
        mTips[0] = emptyTips;
        setState(STATE_EMPTY);
    }

    public void showErrorView(@NonNull String errorTips) {
        mTips[1] = errorTips;
        setState(STATE_ERROR);
    }

    public void showItemView() {
        setState(STATE_DEFAULT);
    }

    @Override
    public int getItemCount() {
        switch (mState) {
            case STATE_LOADING:
            case STATE_EMPTY:
            case STATE_ERROR:
                return 1;
            default:
                return mLoadMore && mFootState != FOOT_NOT_MORE && getSuperItemCount() > 0 ? getSuperItemCount() + 1 : getSuperItemCount();
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
                return mLoadMore && position == getSuperItemCount() ? STATE_FOOT : super.getItemViewType(position);
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
            case STATE_FOOT:
                return new FootViewHolder(View.inflate(parent.getContext(), R.layout.foot_view, null));
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
            switch (mFootState) {
                case FOOT_MORE:
                    footHolder.load();
                    Log.i(TAG, " footHolder.load()");
                    break;
                case FOOT_FAULT:
                    footHolder.fault();
                    Log.i(TAG, " footHolder.fault()");
                    break;
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
            mFootState = -904291163;//loading
            mFootViewLoading.setVisibility(View.VISIBLE);
            mFootViewFault.setVisibility(View.INVISIBLE);
        }

        public void fault() {
            mFootViewLoading.setVisibility(View.INVISIBLE);
            mFootViewFault.setVisibility(View.VISIBLE);
        }

        public void bindLoadMore() {
            if (mOnDragListener != null)
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mOnDragListener.onLoadMore();
                    }
                }, 1000);
        }
    }

    public void setFootState(@FootState int state) {
        this.mFootState = state;
        getWrappedAdapter().notifyDataSetChanged();
        notifyDataSetChanged();
    }

    public void setOnDragListener(OnDragListener onDragListener) {
        this.mOnDragListener = onDragListener;
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
