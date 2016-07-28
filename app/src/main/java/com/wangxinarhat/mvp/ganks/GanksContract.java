/*
 * Copyright 2016, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.wangxinarhat.mvp.ganks;

import android.support.annotation.NonNull;

import com.wangxinarhat.mvp.base.BasePresenter;
import com.wangxinarhat.mvp.base.BaseView;
import com.wangxinarhat.mvp.data.Gank;

import java.util.List;

/**
 * This specifies the contract between the view and the presenter.
 */
public interface GanksContract {

    interface View extends BaseView<Presenter> {

        void setLoadingIndicator(boolean active);

        void showGanks(List<Gank> Ganks);

        void showAddGank();

        void showGankDetailsUi(String GankId);

        void showGankMarkedComplete();

        void showGankMarkedActive();

        void showCompletedGanksCleared();

        void showLoadingGanksError();

        void showNoGanks();

        void showActiveFilterLabel();

        void showCompletedFilterLabel();

        void showAllFilterLabel();

        void showNoActiveGanks();

        void showNoCompletedGanks();

        void showSuccessfullySavedMessage();

        boolean isActive();

        void showFilteringPopUpMenu();
    }

    interface Presenter extends BasePresenter {

        void result(int requestCode, int resultCode);

        void loadGanks(boolean forceUpdate);

        void addNewGank();

        void openGankDetails(@NonNull Gank requestedGank);

        void completeGank(@NonNull Gank completedGank);

        void activateGank(@NonNull Gank activeGank);

        void clearCompletedGanks();

        void setFiltering(GanksFilterType requestType);

        GanksFilterType getFiltering();
    }
}
