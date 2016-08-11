package com.youga.sample;

import android.content.Context;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by YougaKing on 2016/8/11.
 */
public abstract class BaseAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    protected String TAG = getClass().getSimpleName();
    protected Context mContext;
    protected List<String> mStringList;

    protected List<String> getDataList() {
        return mStringList;
    }

    public BaseAdapter(Context context, List<String> dataList) {
        this.mStringList = dataList == null ? new ArrayList<String>() : dataList;
        mContext = context;
    }


    @Override
    public int getItemCount() {
        return mStringList.size();
    }

    public int getFirstNumber() {
        return Integer.valueOf(mStringList.get(0));
    }

    public int getLastNumber() {
        return Integer.valueOf(mStringList.get(getItemCount() - 1));
    }
}
