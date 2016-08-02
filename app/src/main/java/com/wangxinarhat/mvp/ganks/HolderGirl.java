package com.wangxinarhat.mvp.ganks;

import android.view.View;
import android.widget.TextView;

import com.wangxinarhat.mvp.R;
import com.wangxinarhat.mvp.base.BaseHolder;
import com.wangxinarhat.mvp.data.Gank;
import com.wangxinarhat.mvp.utils.DateUtil;
import com.wangxinarhat.mvp.utils.GlideUtils;
import com.wangxinarhat.mvp.widget.RatioImageView;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by wang on 2016/7/22.
 */
public class HolderGirl extends BaseHolder {
    @BindView(R.id.tv_video_name)
    TextView mTvTime;
    @BindView(R.id.iv_index_photo)
    RatioImageView mImageView;

    private Gank mGank;

    public HolderGirl(View itemView, OnHolderClickListener listener) {
        super(itemView, listener);
        ButterKnife.bind(this, itemView);
        mImageView.setOriginalSize(200, 100);
    }

    @Override
    public void bindData(Gank gank) {
        mGank = gank;
        mTvTime.setText(DateUtil.toDate(gank.getPublishedAt()));

        GlideUtils.loadImage(gank.getUrl(), mImageView);

        mTvTime.setText(DateUtil.toDate(gank.getPublishedAt()));
    }

    @Override
    public void onHolderClick(View itemView, int position, int itemViewType) {
        mlistener.onHolderClick(itemView, position, itemViewType, mGank, mImageView, mTvTime);
    }
}
