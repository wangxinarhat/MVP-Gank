package com.wangxinarhat.mvp.ganks;

import com.wangxinarhat.mvp.data.Gank;

/**
 * Created by wang on 2016/8/2.
 */
public interface GanksItemListener {


        void onGankClick(Gank clickedGank);

        void onCompleteGankClick(Gank completedGank);

        void onActivateGankClick(Gank activatedGank);
    

}
