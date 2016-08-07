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

package com.wangxinarhat.mvp.data.source;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.wangxinarhat.mvp.data.Gank;

import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import rx.Observable;
import rx.functions.Action1;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Concrete implementation to load Ganks from the data sources into a cache.
 * <p/>
 * For simplicity, this implements a dumb synchronisation between locally persisted data and data
 * obtained from the server, by using the remote data source only if the local database doesn't
 * exist or is empty.
 */
public class GanksRepository implements GanksDataSource {

    private static GanksRepository INSTANCE = null;

    private final GanksDataSource mGanksRemoteDataSource;

    private final GanksDataSource mGanksLocalDataSource;

    /**
     * This variable has package local visibility so it can be accessed from tests.
     */
    Map<String, Gank> mCachedGanks;

    /**
     * Marks the cache as invalid, to force an update the next time data is requested. This variable
     * has package local visibility so it can be accessed from tests.
     */
    boolean mCacheIsDirty = false;

    // Prevent direct instantiation.
    private GanksRepository(@NonNull GanksDataSource ganksRemoteDataSource,
                            @NonNull GanksDataSource ganksLocalDataSource) {
        mGanksRemoteDataSource = checkNotNull(ganksRemoteDataSource);
        mGanksLocalDataSource = checkNotNull(ganksLocalDataSource);
    }

    /**
     * Returns the single instance of this class, creating it if necessary.
     *
     * @param ganksRemoteDataSource the backend data source
     * @param ganksLocalDataSource  the device storage data source
     * @return the {@link GanksRepository} instance
     */
    public static GanksRepository getInstance(GanksDataSource ganksRemoteDataSource,
                                              GanksDataSource ganksLocalDataSource) {
        if (INSTANCE == null) {
            INSTANCE = new GanksRepository(ganksRemoteDataSource, ganksLocalDataSource);
        }
        return INSTANCE;
    }

    /**
     * Used to force {@link #getInstance(GanksDataSource, GanksDataSource)} to create a new instance
     * next time it's called.
     */
    public static void destroyInstance() {
        INSTANCE = null;
    }

    /**
     * Gets Ganks from cache, local data source (SQLite) or remote data source, whichever is
     * available first.
     */
    @Override
    public Observable<List<Gank>> getGanks(Date date) {
        // Respond immediately with cache if available and not dirty
        if (mCachedGanks != null && !mCacheIsDirty) {
            return Observable.from(mCachedGanks.values()).toList();
        } else if (mCachedGanks == null) {
            mCachedGanks = new LinkedHashMap<>();
        }

        Observable<List<Gank>> remoteGanks = mGanksRemoteDataSource
                .getGanks(date);
                /*.flatMap(new Func1<List<Gank>, Observable<Gank>>() {//Observable.flatMap()接收一个Observable的输出作为输入，同时输出另外一个Observable
                    @Override
                    public Observable<Gank> call(List<Gank> Ganks) {//接收一个集合作为输入，然后每次输出一个元素给subscriber
                        return Observable.from(Ganks);
                    }
                })
                .doOnNext(new Action1<Gank>() {//允许我们在每次输出一个元素之前做一些额外的事情，调试、保存、缓存网络结果（直到doOnNext里的方法在新线程执行完毕，subscribe里的call才有机会在主线程执行）
                    @Override
                    public void call(Gank Gank) {
                        mGanksLocalDataSource.saveGank(Gank);
                        mCachedGanks.put(Gank.getId(), Gank);
                    }
                })
                .toList()//toList操作符让Observable将多项数据组合成一个List，然后调用一次onNext方法传递整个列表。
                         //如果原始Observable没有发射任何数据就调用了onCompleted，toList返回的Observable会在调用onCompleted之前发射一个空列表
                         //如果原始Observable调用了onError，toList返回的Observable会立即调用它的观察者的onError方法
                .doOnCompleted(new Action0() {//操作符注册一个动作，当它产生的Observable正常终止调用onCompleted时会被调用。
                    @Override
                    public void call() {
                        mCacheIsDirty = false;
                    }
                });*/
        if (mCacheIsDirty) {
            return remoteGanks;
        } else {
            // Query the local storage if available. If not, query the network.
            Observable<List<Gank>> localGanks = mGanksLocalDataSource.getGanks(date);
            return Observable.concat(localGanks, remoteGanks).first();
        }
    }

    @Override
    public void saveGank(@NonNull Gank Gank) {
        checkNotNull(Gank);
        mGanksRemoteDataSource.saveGank(Gank);
        mGanksLocalDataSource.saveGank(Gank);

        // Do in memory cache update to keep the app UI up to date
        if (mCachedGanks == null) {
            mCachedGanks = new LinkedHashMap<>();
        }
        mCachedGanks.put(Gank.getId(), Gank);
    }

    @Override
    public void completeGank(@NonNull Gank Gank) {
        checkNotNull(Gank);
        mGanksRemoteDataSource.completeGank(Gank);
        mGanksLocalDataSource.completeGank(Gank);

        Gank completedGank = new Gank(Gank.getTitle(), Gank.getDescription(), Gank.getId(), true);

        // Do in memory cache update to keep the app UI up to date
        if (mCachedGanks == null) {
            mCachedGanks = new LinkedHashMap<>();
        }
        mCachedGanks.put(Gank.getId(), completedGank);
    }

    @Override
    public void completeGank(@NonNull String GankId) {
        checkNotNull(GankId);
        Gank GankWithId = getGankWithId(GankId);
        if (GankWithId != null) {
            completeGank(GankWithId);
        }
    }

    @Override
    public void activateGank(@NonNull Gank Gank) {
        checkNotNull(Gank);
        mGanksRemoteDataSource.activateGank(Gank);
        mGanksLocalDataSource.activateGank(Gank);

        Gank activeGank = new Gank(Gank.getTitle(), Gank.getDescription(), Gank.getId());

        // Do in memory cache update to keep the app UI up to date
        if (mCachedGanks == null) {
            mCachedGanks = new LinkedHashMap<>();
        }
        mCachedGanks.put(Gank.getId(), activeGank);
    }

    @Override
    public void activateGank(@NonNull String GankId) {
        checkNotNull(GankId);
        Gank GankWithId = getGankWithId(GankId);
        if (GankWithId != null) {
            activateGank(GankWithId);
        }
    }

    @Override
    public void clearCompletedGanks() {
        mGanksRemoteDataSource.clearCompletedGanks();
        mGanksLocalDataSource.clearCompletedGanks();

        // Do in memory cache update to keep the app UI up to date
        if (mCachedGanks == null) {
            mCachedGanks = new LinkedHashMap<>();
        }
        Iterator<Map.Entry<String, Gank>> it = mCachedGanks.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Gank> entry = it.next();
            if (entry.getValue().isCompleted()) {
                it.remove();
            }
        }
    }

    /**
     * Gets Ganks from local data source (sqlite) unless the table is new or empty. In that case it
     * uses the network data source. This is done to simplify the sample.
     */
    @Override
    public Observable<Gank> getGank(@NonNull final String GankId) {
        checkNotNull(GankId);

        final Gank cachedGank = getGankWithId(GankId);

        // Respond immediately with cache if available
        if (cachedGank != null) {
            return Observable.just(cachedGank);
        }

        // Load from server/persisted if needed.

        // Is the Gank in the local data source? If not, query the network.
        Observable<Gank> localGank = mGanksLocalDataSource
                .getGank(GankId)
                .doOnNext(new Action1<Gank>() {
                    @Override
                    public void call(Gank Gank) {
                        mCachedGanks.put(GankId, Gank);
                    }
                });
        Observable<Gank> remoteGank = mGanksRemoteDataSource
                .getGank(GankId)
                .doOnNext(new Action1<Gank>() {
                    @Override
                    public void call(Gank Gank) {
                        mGanksLocalDataSource.saveGank(Gank);
                        mCachedGanks.put(Gank.getId(), Gank);
                    }
                });

        return Observable.concat(localGank, remoteGank).first();
    }

    @Override
    public void refreshGanks() {
        mCacheIsDirty = true;
    }

    @Override
    public void deleteAllGanks() {
        mGanksRemoteDataSource.deleteAllGanks();
        mGanksLocalDataSource.deleteAllGanks();

        if (mCachedGanks == null) {
            mCachedGanks = new LinkedHashMap<>();
        }
        mCachedGanks.clear();
    }

    @Override
    public void deleteGank(@NonNull String GankId) {
        mGanksRemoteDataSource.deleteGank(checkNotNull(GankId));
        mGanksLocalDataSource.deleteGank(checkNotNull(GankId));

        mCachedGanks.remove(GankId);
    }

    @Nullable
    private Gank getGankWithId(@NonNull String id) {
        checkNotNull(id);
        if (mCachedGanks == null || mCachedGanks.isEmpty()) {
            return null;
        } else {
            return mCachedGanks.get(id);
        }
    }
}
