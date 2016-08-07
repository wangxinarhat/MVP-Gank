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

package com.wangxinarhat.mvp.data.source.remote;

import android.support.annotation.NonNull;

import com.wangxinarhat.mvp.api.GankFactory;
import com.wangxinarhat.mvp.api.GankService;
import com.wangxinarhat.mvp.data.Gank;
import com.wangxinarhat.mvp.data.GankData;
import com.wangxinarhat.mvp.data.Results;
import com.wangxinarhat.mvp.data.source.GanksDataSource;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.functions.Func1;

/**
 * Implementation of the data source that adds a latency simulating network.
 */
public class GanksRemoteDataSource implements GanksDataSource {
    private static final String TAG = GanksRemoteDataSource.class.getCanonicalName();

    private static GanksRemoteDataSource INSTANCE;

    private static final int SERVICE_LATENCY_IN_MILLIS = 5000;

    private final static Map<String, Gank> TASKS_SERVICE_DATA;

    private Date mCurrentDate;

    static {
        TASKS_SERVICE_DATA = new LinkedHashMap<>(2);
        addGank("Build tower in Pisa", "Ground looks good, no foundation work required.");
        addGank("Finish bridge in Tacoma", "Found awesome girders at half the cost!");
    }

    private GanksRemoteDataSource(Date date) {
        mCurrentDate = date;
    }

    public static GanksRemoteDataSource getInstance(Date date) {
        if (INSTANCE == null) {
            INSTANCE = new GanksRemoteDataSource(date);
        }
        return INSTANCE;
    }

    // Prevent direct instantiation.
    private GanksRemoteDataSource() {
    }

    private static void addGank(String title, String description) {
        Gank newGank = new Gank();
        TASKS_SERVICE_DATA.put(newGank.getId(), newGank);
    }


    public static final GankService mGankService = GankFactory.getGankService();

    @Override
    public Observable<List<Gank>> getGanks(Date date) {

        mCurrentDate = date;

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(mCurrentDate);
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);


        Observable<List<Gank>> observable = mGankService.getGankData(2016, 8, 5)
                .map(new Func1<GankData, Results>() {
                    @Override
                    public Results call(GankData gankData) {
                        return gankData.results;
                    }
                })
                .map(new Func1<Results, List<Gank>>() {
                    @Override
                    public List<Gank> call(Results results) {
                        return addAllResults(results);
                    }
                });
        return observable;

    }

    private List<Gank> addAllResults(Results results) {
        List<Gank> gankList = new ArrayList<>();
        if (results.androidList != null) gankList.addAll(results.androidList);
        if (results.iOSList != null) gankList.addAll(results.iOSList);
        if (results.appList != null) gankList.addAll(results.appList);
        if (results.expandList != null) gankList.addAll(results.expandList);
        if (results.recommendList != null) gankList.addAll(results.recommendList);
        if (results.restList != null) gankList.addAll(results.restList);
        // make meizi data is in first gankList
        if (results.welfareList != null) gankList.addAll(0, results.welfareList);
        return gankList;
    }


    @Override
    public Observable<Gank> getGank(@NonNull String gankId) {
        final Gank gank = TASKS_SERVICE_DATA.get(gankId);
        if (gank != null) {
            return Observable.just(gank).delay(SERVICE_LATENCY_IN_MILLIS, TimeUnit.MILLISECONDS);
        } else {
            return Observable.empty();
        }
    }

    @Override
    public void saveGank(Gank gank) {
        TASKS_SERVICE_DATA.put(gank.getId(), gank);
    }

    @Override
    public void completeGank(Gank gank) {
        Gank completedGank = new Gank(gank.getTitle(), gank.getDescription(), gank.getId(), true);
        TASKS_SERVICE_DATA.put(gank.getId(), completedGank);
    }

    @Override
    public void completeGank(@NonNull String gankId) {
        // Not required for the remote data source because the {@link GanksRepository} handles
        // converting from a {@code gankId} to a {@link gank} using its cached data.
    }

    @Override
    public void activateGank(Gank gank) {
        Gank activeGank = new Gank(gank.getTitle(), gank.getDescription(), gank.getId());
        TASKS_SERVICE_DATA.put(gank.getId(), activeGank);
    }

    @Override
    public void activateGank(@NonNull String gankId) {
        // Not required for the remote data source because the {@link GanksRepository} handles
        // converting from a {@code gankId} to a {@link gank} using its cached data.
    }

    @Override
    public void clearCompletedGanks() {
        Iterator<Map.Entry<String, Gank>> it = TASKS_SERVICE_DATA.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Gank> entry = it.next();
            if (entry.getValue().isCompleted()) {
                it.remove();
            }
        }
    }

    @Override
    public void refreshGanks() {
        // Not required because the {@link GanksRepository} handles the logic of refreshing the
        // ganks from all the available data sources.
    }

    @Override
    public void deleteAllGanks() {
        TASKS_SERVICE_DATA.clear();
    }

    @Override
    public void deleteGank(String gankId) {
        TASKS_SERVICE_DATA.remove(gankId);
    }
}
