package com.wangxinarhat.mvp.ganks;

import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.wangxinarhat.mvp.R;
import com.wangxinarhat.mvp.base.BaseHolder;
import com.wangxinarhat.mvp.data.Gank;
import com.wangxinarhat.mvp.global.GankCategory;

import java.util.Date;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by wang on 2016/8/1.
 */

public class GanksAdapter extends RecyclerView.Adapter<BaseHolder> implements OnHolderClickListener {
    /**
     * The listener that receives notifications when an item is clicked.
     */
    OnRecyclerViewItemClickListener mOnItemClickListener;
    private List<Gank> mGanks;
    private GanksItemListener mItemListener;

    public GanksAdapter(List<Gank> ganks, GanksItemListener itemListener) {
        setList(ganks);
        mItemListener = itemListener;
    }

    public void replaceData(List<Gank> ganks) {
        setList(ganks);
        notifyDataSetChanged();
    }

    private void setList(List<Gank> ganks) {
        mGanks = checkNotNull(ganks);
    }

    @Override
    public BaseHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view;

        BaseHolder holder;
        if (viewType == ItemType.ITEM_TYPE_GIRL.ordinal()) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_girl, null);
            holder = new HolderGirl(view, this);

        } else if (viewType == ItemType.ITEM_TYPE_CATEGOTY.ordinal()) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category, null);
            holder = new HolderCategory(view, this);
        } else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_normal, null);
            holder = new HolderNormal(view, this);
        }

        return holder;
    }


    @Override
    public void onBindViewHolder(BaseHolder holder, int position) {
        holder.bindData(mGanks.get(position));
    }

    @Override
    public int getItemCount() {
        return null == mGanks ? 0 : mGanks.size();
    }

    @Override
    public int getItemViewType(int position) {
        Gank gank = mGanks.get(position);
        if (gank.isWelfare()) {
            return ItemType.ITEM_TYPE_GIRL.ordinal();
        } else if (gank.isHeader()) {
            return ItemType.ITEM_TYPE_CATEGOTY.ordinal();
        } else {
            return ItemType.ITEM_TYPE_NORMAL.ordinal();
        }
    }


    /**
     * before add data , it will remove history data
     *
     * @param data
     */
    public void updateWithClear(List<Gank> data) {
        mGanks.clear();
        update(data);
    }

    /**
     * add data append to history data*
     *
     * @param data new data
     */
    public void update(List<Gank> data) {
        formatGankData(data);
        notifyDataSetChanged();
    }

    /**
     * filter list and add category entity into list
     *
     * @param data source data
     */
    private void formatGankData(List<Gank> data) {
        //Insert headers into list of items.
        String lastHeader = "";
        for (int i = 0; i < data.size(); i++) {
            Gank gank = data.get(i);
            String header = gank.getType();
            if (!gank.isWelfare() && !TextUtils.equals(lastHeader, header)) {
                // Insert new header view.
                Gank gankHeader = gank.clone();
                lastHeader = header;
                gankHeader.setHeader(true);
                mGanks.add(gankHeader);
            }
            gank.setHeader(false);
            mGanks.add(gank);
        }
    }


    /**
     * get a init Gank entity
     *
     * @return gank entity
     */
    private Gank getDefGankGirl() {
        Gank gank = new Gank();
        gank.setPublishedAt(new Date(System.currentTimeMillis()));
        gank.setUrl("empty");
        gank.setType(GankCategory.福利.name());
        return gank;
    }


    public void setOnItemClickListener(@Nullable OnRecyclerViewItemClickListener listener) {
        mOnItemClickListener = listener;
    }



    @Override
    public void onHolderClick(View itemView, int position, int itemViewType, Gank gank, View viewImage, View viewText) {
        mOnItemClickListener.onItemClick(itemView, position, itemViewType, gank, viewImage, viewText);
    }
}