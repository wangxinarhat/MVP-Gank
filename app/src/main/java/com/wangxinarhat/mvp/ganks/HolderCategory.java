package com.wangxinarhat.mvp.ganks;

import android.view.View;
import android.widget.TextView;

import com.wangxinarhat.mvp.R;
import com.wangxinarhat.mvp.base.BaseHolder;
import com.wangxinarhat.mvp.data.Gank;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by wang on 2016/7/22.
 */
public class HolderCategory extends BaseHolder {
    @BindView(R.id.item_category_text)
    TextView mTvCategory;
    public HolderCategory(View itemView,OnHolderClickListener listener) {
        super(itemView,listener);
        ButterKnife.bind(this, itemView);
    }

    @Override
    public void bindData(Gank gank) {

        mTvCategory.setText(gank.getType());
    }


}
