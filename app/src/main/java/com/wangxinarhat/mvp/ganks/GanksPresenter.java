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

import com.wangxinarhat.mvp.api.GankFactory;
import com.wangxinarhat.mvp.api.GankService;
import com.wangxinarhat.mvp.data.Gank;
import com.wangxinarhat.mvp.data.GankData;
import com.wangxinarhat.mvp.data.Results;
import com.wangxinarhat.mvp.data.source.GanksDataSource;
import com.wangxinarhat.mvp.data.source.GanksRepository;
import com.wangxinarhat.mvp.utils.EspressoIdlingResource;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Listens to user actions from the UI ({@link GanksFragment}), retrieves the data and updates the
 * UI as required.
 */
public class GanksPresenter implements GanksContract.Presenter {


    public static final GankService mGankService = GankFactory.getGankService();

    private final GanksRepository mGanksRepository;

    private final GanksContract.View mGanksView;

    private GanksFilterType mCurrentFiltering = GanksFilterType.ALL_GankS;

    private boolean mFirstLoad = true;
    private CompositeSubscription mSubscriptions;

    public GanksPresenter(@NonNull GanksRepository GanksRepository, @NonNull GanksContract.View GanksView) {
        mGanksRepository = checkNotNull(GanksRepository, "GanksRepository cannot be null");
        mGanksView = checkNotNull(GanksView, "GanksView cannot be null!");
        mSubscriptions = new CompositeSubscription();
        mGanksView.setPresenter(this);
    }

    @Override
    public void subscribe() {
        loadGanks(false);
    }

    @Override
    public void unsubscribe() {
        mSubscriptions.clear();
    }

    @Override
    public void result(int requestCode, int resultCode) {
        // If a Gank was successfully added, show snackbar
        //FIXME
//        if (AddEditGankActivity.REQUEST_ADD_Gank == requestCode && Activity.RESULT_OK == resultCode) {
//            mGanksView.showSuccessfullySavedMessage();
//        }
    }

    @Override
    public void loadGanks(boolean forceUpdate) {
        // Simplification for sample: a network reload will be forced on first load.
        loadGanks(forceUpdate || mFirstLoad, true);
        mFirstLoad = false;
    }

    private Date mCurrentDate=new Date(System.currentTimeMillis());

    public void loadGanks(boolean forceUpdate, final boolean showLoadingUI, final Date date) {
        mCurrentDate = date;
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        mGankService.getGankData(year, month, day)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(new Func1<GankData, Results>() {
                    @Override
                    public Results call(GankData gankData) {
                        return gankData.results;
                    }
                })
                .map(new Func1<Results, List<Gank>>() {
                    @Override
                    public List<Gank> call(Results result) {
                        return addAllResults(result);
                    }
                })
                .subscribe(new Subscriber<List<Gank>>() {
                    @Override
                    public void onCompleted() {
                        // after get data complete, need put off time one day
                        mCurrentDate = new Date(date.getTime() - DAY_OF_MILLISECOND);
                        mView.getDataFinish();
                    }

                    @Override
                    public void onError(Throwable e) {
                        mView.getDataFinish();
                    }

                    @Override
                    public void onNext(List<Gank> ganks) {
                        // some day the data will be return empty like sunday, so we need get after day data

                        if (ganks.isEmpty()) {
                            getData(new Date(date.getTime() - DAY_OF_MILLISECOND));
                        } else {
                            mCountOfGetMoreDataEmpty = 0;
                            mView.fillData(ganks);
                        }
                        mView.getDataFinish();
                    }
                });
    }

    /**
     * @param forceUpdate   Pass in true to refresh the data in the {@link GanksDataSource}
     * @param showLoadingUI Pass in true to display a loading icon in the UI
     */
    private void loadGanks(boolean forceUpdate, final boolean showLoadingUI) {
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
                .getGanks()
                .flatMap(new Func1<List<Gank>, Observable<Gank>>() {
                    @Override
                    public Observable<Gank> call(List<Gank> Ganks) {
                        return Observable.from(Ganks);
                    }
                })
                .filter(new Func1<Gank, Boolean>() {
                    @Override
                    public Boolean call(Gank Gank) {
                        switch (mCurrentFiltering) {
                            case ACTIVE_GankS:
                                return Gank.isActive();
                            case COMPLETED_GankS:
                                return Gank.isCompleted();
                            case ALL_GankS:
                            default:
                                return true;
                        }
                    }
                })
                .toList()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<Gank>>() {
                    @Override
                    public void onCompleted() {
                        mGanksView.setLoadingIndicator(false);
                    }

                    @Override
                    public void onError(Throwable e) {
                        mGanksView.showLoadingGanksError();
                    }

                    @Override
                    public void onNext(List<Gank> Ganks) {
                        processGanks(Ganks);
                    }
                });
        mSubscriptions.add(subscription);
    }






    private void processGanks(List<Gank> Ganks) {
        if (Ganks.isEmpty()) {
            // Show a message indicating there are no Ganks for that filter type.
            processEmptyGanks();
        } else {
            // Show the list of Ganks
            mGanksView.showGanks(Ganks);
            // Set the filter label's text.
            showFilterLabel();
        }
    }

    private void showFilterLabel() {
        switch (mCurrentFiltering) {
            case ACTIVE_GankS:
                mGanksView.showActiveFilterLabel();
                break;
            case COMPLETED_GankS:
                mGanksView.showCompletedFilterLabel();
                break;
            default:
                mGanksView.showAllFilterLabel();
                break;
        }
    }

    private void processEmptyGanks() {
        switch (mCurrentFiltering) {
            case ACTIVE_GankS:
                mGanksView.showNoActiveGanks();
                break;
            case COMPLETED_GankS:
                mGanksView.showNoCompletedGanks();
                break;
            default:
                mGanksView.showNoGanks();
                break;
        }
    }

    @Override
    public void addNewGank() {
        mGanksView.showAddGank();
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
        loadGanks(false, false);
    }

    @Override
    public void activateGank(@NonNull Gank activeGank) {
        checkNotNull(activeGank, "activeGank cannot be null!");
        mGanksRepository.activateGank(activeGank);
        mGanksView.showGankMarkedActive();
        loadGanks(false, false);
    }

    @Override
    public void clearCompletedGanks() {
        mGanksRepository.clearCompletedGanks();
        mGanksView.showCompletedGanksCleared();
        loadGanks(false, false);
    }

    /**
     * Sets the current Gank filtering type.
     *
     * @param requestType Can be {@link GanksFilterType#ALL_GankS},
     *                    {@link GanksFilterType#COMPLETED_GankS}, or
     *                    {@link GanksFilterType#ACTIVE_GankS}
     */
    @Override
    public void setFiltering(GanksFilterType requestType) {
        mCurrentFiltering = requestType;
    }

    @Override
    public GanksFilterType getFiltering() {
        return mCurrentFiltering;
    }

}
