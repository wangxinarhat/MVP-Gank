package com.wangxinarhat.mvp.ganks;

import android.view.View;
import android.widget.TextView;

import com.wangxinarhat.mvp.R;
import com.wangxinarhat.mvp.base.BaseHolder;
import com.wangxinarhat.mvp.base.OnHolderClickListener;
import com.wangxinarhat.mvp.data.Gank;
import com.wangxinarhat.mvp.utils.StringStyleUtils;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by wang on 2016/7/22.
 */
public class HolderNormal extends BaseHolder<Gank> {
    @BindView(R.id.item_normal_title)
    TextView mTitle;

    private Gank mGank;

    public HolderNormal(View itemView, OnHolderClickListener listener) {
        super(itemView, listener);
        ButterKnife.bind(this, itemView);
    }

    @Override
    public void bindData(Gank gank) {
        mGank = gank;
        mTitle.setText(StringStyleUtils.getGankInfoSequence(gank));

    }



}
