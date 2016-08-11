package com.youga.recyclerview.model;

/**
 * Created by YougaKing on 2016/8/11.
 */
public class Foot {
    boolean loading;
    int viewType;

    public Foot(int viewType) {
        this.viewType = viewType;
    }

    public boolean isLoading() {
        return loading;
    }

    public void setLoading(boolean loading) {
        this.loading = loading;
    }

    public int getViewType() {
        return viewType;
    }
}
