package com.wangxinarhat.mvp.base;

import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.wangxinarhat.mvp.R;
import com.wangxinarhat.mvp.data.Gank;

import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnLongClick;

/**
 * Created by wang on 2016/7/22.
 */
public abstract class BaseHolder<T> extends RecyclerView.ViewHolder {

    protected OnHolderClickListener mOnHolderClickListener;
    protected OnHolderLongClickListener mOnHolderLongClickListener;

    public BaseHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }


    public BaseHolder(View itemView, OnHolderClickListener onHolderClickListener) {
        super(itemView);
        ButterKnife.bind(this, itemView);
        mOnHolderClickListener = onHolderClickListener;
    }


    public BaseHolder(View itemView, OnHolderClickListener onHolderClickListener, OnHolderLongClickListener onHolderLongClickListener) {
        super(itemView);
        ButterKnife.bind(this, itemView);
        mOnHolderClickListener = onHolderClickListener;
        mOnHolderLongClickListener = onHolderLongClickListener;
    }


    protected abstract void bindData(T t);

    @Nullable
    @OnClick(R.id.holder_root)
    public void onClick(View view) {
        if (null != mOnHolderClickListener) {
            mOnHolderClickListener.onHolderClick(view, getLayoutPosition());
        }
    }

    @Nullable
    @OnLongClick(R.id.holder_root)
    public boolean onLongClick(View view) {
        if (null != mOnHolderLongClickListener) {
            return mOnHolderLongClickListener.onHolderLongClick(view, getLayoutPosition());
        } else {
            return false;
        }
    }

}
