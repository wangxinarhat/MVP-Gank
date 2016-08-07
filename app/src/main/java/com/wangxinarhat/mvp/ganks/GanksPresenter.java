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

import com.orhanobut.logger.Logger;
import com.wangxinarhat.mvp.data.Gank;
import com.wangxinarhat.mvp.data.Results;
import com.wangxinarhat.mvp.data.source.GanksDataSource;
import com.wangxinarhat.mvp.data.source.GanksRepository;
import com.wangxinarhat.mvp.utils.EspressoIdlingResource;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Listens to user actions from the UI ({@link GanksFragment}), retrieves the data and updates the
 * UI as required.
 */
public class GanksPresenter implements GanksContract.Presenter {

    private static final String TAG = GanksPresenter.class.getCanonicalName();

    private final GanksRepository mGanksRepository;

    private final GanksContract.View mGanksView;

    private GanksFilterType mCurrentFiltering = GanksFilterType.ALL_GANKS;

    private boolean mFirstLoad = true;
    private CompositeSubscription mSubscriptions;


    List<Gank> mGankList = new ArrayList<>();

    public GanksPresenter(@NonNull GanksRepository ganksRepository, @NonNull GanksContract.View ganksView) {
        mGanksRepository = checkNotNull(ganksRepository, "ganksRepository cannot be null");
        mGanksView = checkNotNull(ganksView, "ganksView cannot be null!");
        mSubscriptions = new CompositeSubscription();
        mGanksView.setPresenter(this);
    }

    @Override
    public void subscribe() {
        loadGanks(false, new Date(System.currentTimeMillis()));
    }

    @Override
    public void unsubscribe() {
        mSubscriptions.clear();
    }

    @Override
    public void result(int requestCode, int resultCode) {
        // If a gank was successfully added, show snackbar
        // FIXME: 2016/8/2 
//        if (AddEditGankActivity.REQUEST_ADD_GANK == requestCode && Activity.RESULT_OK == resultCode) {
//            mGanksView.showSuccessfullySavedMessage();
//        }
    }

    @Override
    public void loadGanks(boolean forceUpdate, Date date) {
        // Simplification for sample: a network reload will be forced on first load.
        loadGanks(forceUpdate || mFirstLoad, date, true);
        mFirstLoad = false;
    }


    private Date mCurrentDate = new Date(System.currentTimeMillis());

    /**
     * @param forceUpdate   Pass in true to refresh the data in the {@link GanksDataSource}
     * @param showLoadingUI Pass in true to display a loading icon in the UI
     */
    private void loadGanks(boolean forceUpdate, Date date, final boolean showLoadingUI) {
        mCurrentDate = date;
        if (showLoadingUI) {
            mGanksView.setLoadingIndicator(true);
        }
        if (forceUpdate) {
            mGanksRepository.refreshGanks();
        }

        // The network request might be handled in a different thread so make sure Espresso knows
        // that the app is busy until the response is handled.
        EspressoIdlingResource.increment(); // App is busy until further notice

        mSubscriptions.clear();
        Subscription subscription = mGanksRepository
                .getGanks(date)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(getObserver());
        mSubscriptions.add(subscription);


    }

    private Observer<? super List<Gank>> getObserver() {

        Observer<List<Gank>> observer = new Observer<List<Gank>>() {
            @Override
            public void onCompleted() {
                mGanksView.setLoadingIndicator(false);
            }

            @Override
            public void onError(Throwable e) {
                Logger.e(TAG, e.getMessage());
                mGanksView.showLoadingGanksError();
            }

            @Override
            public void onNext(List<Gank> ganks) {
                processGanks(ganks);
            }
        };

        return observer;
    }

    private void processGanks(List<Gank> ganks) {
        if (ganks.isEmpty()) {
            // Show a message indicating there are no ganks for that filter type.
            processEmptyGanks();
        } else {
            // Show the list of ganks
            mGanksView.showGanks(ganks);
            // Set the filter label's text.
            showFilterLabel();
        }
    }

    private void showFilterLabel() {
        switch (mCurrentFiltering) {
            case ACTIVE_GANKS:
                mGanksView.showActiveFilterLabel();
                break;
            case COMPLETED_GANKS:
                mGanksView.showCompletedFilterLabel();
                break;
            default:
                mGanksView.showAllFilterLabel();
                break;
        }
    }

    private void processEmptyGanks() {
        switch (mCurrentFiltering) {
            case ACTIVE_GANKS:
                mGanksView.showNoActiveGanks();
                break;
            case COMPLETED_GANKS:
                mGanksView.showNoCompletedGanks();
                break;
            default:
                mGanksView.showNoGanks();
                break;
        }
    }

    @Override
    public void addNewGank() {
        mGanksView.showReloadGank();
    }

    @Override
    public void openGankDetails(@NonNull Gank requestedGank) {
        checkNotNull(requestedGank, "requestedGank cannot be null!");
        mGanksView.showGankDetailsUi(requestedGank.getId());
    }

    @Override
    public void completeGank(@NonNull Gank completedGank) {
        checkNotNull(completedGank, "completedGank cannot be null!");
        mGanksRepository.completeGank(completedGank);
        mGanksView.showGankMarkedComplete();
        loadGanks(false, mCurrentDate, false);
    }

    @Override
    public void activateGank(@NonNull Gank activeGank) {
        checkNotNull(activeGank, "activeGank cannot be null!");
        mGanksRepository.activateGank(activeGank);
        mGanksView.showGankMarkedActive();
        loadGanks(false, mCurrentDate, false);
    }

    @Override
    public void clearCompletedGanks() {
        mGanksRepository.clearCompletedGanks();
        mGanksView.showCompletedGanksCleared();
        loadGanks(false, mCurrentDate, false);
    }

    /**
     * Sets the current gank filtering type.
     *
     * @param requestType Can be {@link GanksFilterType#ALL_GANKS},
     *                    {@link GanksFilterType#COMPLETED_GANKS}, or
     *                    {@link GanksFilterType#ACTIVE_GANKS}
     */
    @Override
    public void setFiltering(GanksFilterType requestType) {
        mCurrentFiltering = requestType;
    }

    @Override
    public GanksFilterType getFiltering() {
        return mCurrentFiltering;
    }


    private List<Gank> addAllResults(Results results) {
        mGankList.clear();
        if (results.androidList != null) mGankList.addAll(results.androidList);
        if (results.iOSList != null) mGankList.addAll(results.iOSList);
        if (results.appList != null) mGankList.addAll(results.appList);
        if (results.expandList != null) mGankList.addAll(results.expandList);
        if (results.recommendList != null) mGankList.addAll(results.recommendList);
        if (results.restList != null) mGankList.addAll(results.restList);
        // make meizi data is in first position
        if (results.welfareList != null) mGankList.addAll(0, results.welfareList);
        return mGankList;
    }

}
