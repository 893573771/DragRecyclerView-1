package com.youga.recyclerview;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;

import com.youga.recyclerview.adapter.RecyclerAdapter;
import com.youga.recyclerview.model.Fill;
import com.youga.recyclerview.model.Foot;

/**
 * Created by Youga on 2016/2/17.
 */
public class DragRecyclerView extends RecyclerView {
    private static final String TAG = "DragRecyclerView";
    private int mRequestCount = 10;
    private Context mContext;

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
        setAdapter(adapter, loadMore, null);
    }

    public void setAdapter(final Adapter adapter, boolean loadMore, LayoutManager layout) {
        super.setAdapter(new RecyclerAdapter(adapter, mContext, loadMore));
        if (layout == null) {
            setLayoutManager(new LinearLayoutManager(mContext));
        } else {
            setLayoutManager(layout);
        }
        if (layout instanceof GridLayoutManager) {
            final GridLayoutManager manager = (GridLayoutManager) layout;
            manager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override

                public int getSpanSize(int position) {
                    return position == adapter.getItemCount() ? manager.getSpanCount() : 1;
                }
            });
        }
        RecyclerAdapter recyclerAdapter = (RecyclerAdapter) getAdapter();
        recyclerAdapter.setLayoutManager(layout);
    }

    public void setRequestCount(int requestCount) {
        this.mRequestCount = requestCount;
    }

    public void showLoadingView() {
        RecyclerAdapter adapter = (RecyclerAdapter) getAdapter();
        adapter.showView(new Fill(null, RecyclerAdapter.TYPE_LOADING, 0));
    }

    public void showEmptyView(@NonNull String emptyTips) {
        showEmptyView(emptyTips, R.mipmap.empty);
    }

    public void showEmptyView(@NonNull String emptyTips, int resId) {
        RecyclerAdapter adapter = (RecyclerAdapter) getAdapter();
        adapter.showView(new Fill(emptyTips, RecyclerAdapter.TYPE_EMPTY, resId));
    }

    public void showErrorView(@NonNull String errorTips) {
        showErrorView(errorTips, R.mipmap.error);
    }

    public void showErrorView(@NonNull String errorTips, int resId) {
        RecyclerAdapter adapter = (RecyclerAdapter) getAdapter();
        adapter.showView(new Fill(errorTips, RecyclerAdapter.TYPE_ERROR, resId));
    }

    public void onDragState(int resultCount) {
        RecyclerAdapter adapter = (RecyclerAdapter) getAdapter();
        if (resultCount < 0) {
            adapter.showFoot(new Foot(RecyclerAdapter.TYPE_FOOT_FAULT));
        } else if (resultCount >= 0 && resultCount < mRequestCount) {
            adapter.showFoot(null);
        } else {
            adapter.showFoot(new Foot(RecyclerAdapter.TYPE_FOOT_LOAD));
        }
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
}
