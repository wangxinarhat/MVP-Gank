package com.wangxinarhat.mvp.ganks;

import android.view.View;

import com.wangxinarhat.mvp.data.Gank;


/**
 * Created by wang on 2016/7/25.
 */
public interface OnHolderClickListener {


    void onHolderClick(View itemView, int position, int itemViewType, Gank gank, View viewImage, View viewText);


}
