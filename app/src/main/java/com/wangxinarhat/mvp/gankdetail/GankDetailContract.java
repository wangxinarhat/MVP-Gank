package com.wangxinarhat.mvp.gankdetail;

import com.wangxinarhat.mvp.base.BasePresenter;
import com.wangxinarhat.mvp.base.BaseView;

/**
 * Created by wang on 2016/8/9.
 */
public interface GankDetailContract {
    interface View extends BaseView<Presenter> {
        void setLoadingIndicator(boolean active);

        void showGankMarkedComplete();

        void showGankMarkedActive();

        boolean isActive();

    }

    interface Presenter extends BasePresenter {
        void activateGank();

        void completeGank();
    }
}
