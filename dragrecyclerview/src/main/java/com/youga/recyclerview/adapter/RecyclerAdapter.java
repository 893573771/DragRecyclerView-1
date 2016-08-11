package com.youga.recyclerview.adapter;


import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import android.view.ViewGroup.LayoutParams;

import com.youga.recyclerview.R;
import com.youga.recyclerview.DragRecyclerView.OnDragListener;
import com.youga.recyclerview.DragRecyclerView.OnItemClickListener;
import com.youga.recyclerview.model.Fill;
import com.youga.recyclerview.model.Foot;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


/**
 * Created by Youga on 2015/9/2.
 */
public class RecyclerAdapter extends RecyclerWrapper {

    private static final String TAG = "RecyclerAdapter";
    //是否再加更多
    private final boolean mLoadMore;
    private RecyclerView.LayoutManager mLayoutManager;
    //是否还有更多数据
    private boolean mShouldMore;
    private final Context mContext;
    private DisplayMetrics mMetrics;

    public static final int TYPE_ITEM = 160809000;
    public static final int TYPE_LOADING = 160809001;
    public static final int TYPE_EMPTY = 160809002;
    public static final int TYPE_ERROR = 160809003;
    public static final int TYPE_FOOT_LOAD = 160809004;
    public static final int TYPE_FOOT_FAULT = 160809005;

    private OnDragListener mOnDragListener;
    private OnItemClickListener mOnItemClickListener;
    private LayoutParams mLayoutParams;
    private RecyclerView.Adapter mAdapter;
    //填充对象
    private Fill mFill;
    //foot对象
    private Foot mFoot;


    public RecyclerAdapter(@NonNull RecyclerView.Adapter adapter, Context context,
                           boolean loadMore) {
        super(adapter);
        mContext = context;
        mMetrics = context.getResources().getDisplayMetrics();
        mLoadMore = loadMore;
        mAdapter = adapter;
    }

    @Override
    public int getItemCount() {
        if (mFill != null) {
            return 1;
        } else {
            return mLoadMore & mShouldMore ? mAdapter.getItemCount() + 1 : mAdapter.getItemCount();
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0 && mFill != null) {
            return mFill.getViewType();
        } else if (position == mAdapter.getItemCount()) {
            return mFoot.getViewType();
        } else {
            return TYPE_ITEM;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_LOADING || viewType == TYPE_EMPTY || viewType == TYPE_ERROR) {
            return new FillViewHolder(View.inflate(parent.getContext(), R.layout.fill_view, null));
        } else if (viewType == TYPE_FOOT_LOAD || viewType == TYPE_FOOT_FAULT) {
            return new FootViewHolder(View.inflate(parent.getContext(), R.layout.foot_view, null));
        } else {
            return mAdapter.onCreateViewHolder(parent, viewType);
        }
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof FillViewHolder) {
            LayoutParams params = getLayoutParams();
            if (params != null) holder.itemView.setLayoutParams(params);
            FillViewHolder fillHolder = (FillViewHolder) holder;
            fillHolder.bindView(mFill);
        } else if (holder instanceof FootViewHolder) {
            int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48, mMetrics);
            FootViewHolder footHolder = (FootViewHolder) holder;
            footHolder.bindView(mFoot);
            if (mLayoutManager instanceof StaggeredGridLayoutManager) {
                StaggeredGridLayoutManager.LayoutParams params = new StaggeredGridLayoutManager.LayoutParams(getLayoutParams().width, height);
                params.setFullSpan(true);
                holder.itemView.setLayoutParams(params);
            } else {
                LayoutParams params = new LayoutParams(getLayoutParams().width, height);
                holder.itemView.setLayoutParams(params);
            }
        } else {
            if (mOnItemClickListener != null)
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mOnItemClickListener.onItemClick(holder.itemView, holder.getAdapterPosition());
                    }
                });
            mAdapter.onBindViewHolder(holder, position);
        }
    }

    public void showView(Fill fill) {
        mFill = fill;
        mShouldMore = true;
        notifyDataSetChanged();
        getWrappedAdapter().notifyDataSetChanged();
    }

    public void showFoot(Foot foot) {
        mFill = null;
        mFoot = foot;
        if (foot == null) mShouldMore = false;
        notifyDataSetChanged();
        getWrappedAdapter().notifyDataSetChanged();
    }

    public class FillViewHolder extends RecyclerView.ViewHolder {

        private final TextView mTipsView;
        private final ProgressBar mPbLoading;

        public FillViewHolder(View itemView) {
            super(itemView);
            mTipsView = (TextView) itemView.findViewById(R.id.tips_view);
            mPbLoading = (ProgressBar) itemView.findViewById(R.id.pb_loading);
        }

        public void bindView(Fill fill) {
            if (fill.getViewType() == TYPE_LOADING) {
                mPbLoading.setVisibility(View.VISIBLE);
                mTipsView.setVisibility(View.INVISIBLE);
            } else {
                mPbLoading.setVisibility(View.INVISIBLE);
                mTipsView.setVisibility(View.VISIBLE);
                Drawable top = mContext.getResources().getDrawable(fill.getIconId());
                if (top != null)
                    top.setBounds(0, 0, top.getIntrinsicWidth(), top.getIntrinsicHeight());
                mTipsView.setCompoundDrawables(null, top, null, null);
                mTipsView.setText(fill.getTips());
            }
        }
    }

    public class FootViewHolder extends RecyclerView.ViewHolder {
        private LinearLayout mViewLoading, mViewFault;

        public FootViewHolder(View itemView) {
            super(itemView);
            mViewLoading = (LinearLayout) itemView.findViewById(R.id.view_loading);
            mViewFault = (LinearLayout) itemView.findViewById(R.id.view_fault);
        }

        public void bindView(final Foot foot) {
            if (foot.getViewType() == TYPE_FOOT_LOAD) {
                showLoad(foot);
            } else {
                foot.setLoading(false);
                mViewLoading.setVisibility(View.INVISIBLE);
                mViewFault.setVisibility(View.VISIBLE);
            }

            mViewFault.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showLoad(foot);
                }
            });
        }

        private void showLoad(Foot foot) {
            mViewLoading.setVisibility(View.VISIBLE);
            mViewFault.setVisibility(View.INVISIBLE);
            if (!foot.isLoading()) {
                foot.setLoading(true);
                mViewLoading.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mOnDragListener.onLoadMore();
                    }
                }, 1000);
            }
        }
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
            mLayoutParams = new LayoutParams(width, height);
            notifyDataSetChanged();
            getWrappedAdapter().notifyDataSetChanged();
        }
    }

    public void setLayoutManager(RecyclerView.LayoutManager layoutManager) {
        mLayoutManager = layoutManager;
    }
}
