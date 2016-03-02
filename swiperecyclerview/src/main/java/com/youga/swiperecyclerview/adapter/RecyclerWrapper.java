package com.youga.swiperecyclerview.adapter;

/**
 * Created by Youga on 2015/9/2.
 */

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.support.v7.widget.RecyclerView.AdapterDataObserver;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.ViewGroup;

public class RecyclerWrapper extends Adapter {

    private final Adapter<ViewHolder> wrapped;

    public RecyclerWrapper(Adapter<ViewHolder> wrapped) {
        this.wrapped = wrapped;
        this.wrapped.registerAdapterDataObserver(new AdapterDataObserver() {
            public void onChanged() {
                RecyclerWrapper.this.notifyDataSetChanged();
            }

            public void onItemRangeChanged(int positionStart, int itemCount) {
                RecyclerWrapper.this.notifyItemRangeChanged(positionStart, itemCount);
            }

            public void onItemRangeInserted(int positionStart, int itemCount) {
                RecyclerWrapper.this.notifyItemRangeInserted(positionStart, itemCount);
            }

            public void onItemRangeRemoved(int positionStart, int itemCount) {
                RecyclerWrapper.this.notifyItemRangeRemoved(positionStart, itemCount);
            }

            public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
                RecyclerWrapper.this.notifyItemMoved(fromPosition, toPosition);
            }
        });
    }

    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return this.wrapped.onCreateViewHolder(parent, viewType);
    }

    public void onBindViewHolder(ViewHolder holder, int position) {
        this.wrapped.onBindViewHolder(holder, position);
    }

    public int getItemCount() {
        return this.wrapped.getItemCount();
    }

    public int getItemViewType(int position) {
        return this.wrapped.getItemViewType(position);
    }

    public void setHasStableIds(boolean hasStableIds) {
        this.wrapped.setHasStableIds(hasStableIds);
    }

    public long getItemId(int position) {
        return this.wrapped.getItemId(position);
    }

    public void onViewRecycled(ViewHolder holder) {
        this.wrapped.onViewRecycled(holder);
    }

    public boolean onFailedToRecycleView(ViewHolder holder) {
        return this.wrapped.onFailedToRecycleView(holder);
    }

    public void onViewAttachedToWindow(ViewHolder holder) {
        this.wrapped.onViewAttachedToWindow(holder);
    }

    public void onViewDetachedFromWindow(ViewHolder holder) {
        this.wrapped.onViewDetachedFromWindow(holder);
    }

    public void registerAdapterDataObserver(AdapterDataObserver observer) {
        this.wrapped.registerAdapterDataObserver(observer);
    }

    public void unregisterAdapterDataObserver(AdapterDataObserver observer) {
        this.wrapped.unregisterAdapterDataObserver(observer);
    }

    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        this.wrapped.onAttachedToRecyclerView(recyclerView);
    }

    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        this.wrapped.onDetachedFromRecyclerView(recyclerView);
    }

    public Adapter<ViewHolder> getWrappedAdapter() {
        return this.wrapped;
    }
}
