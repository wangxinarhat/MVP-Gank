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

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import rx.Observable;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;

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
    private GanksRepository(@NonNull GanksDataSource GanksRemoteDataSource,
                            @NonNull GanksDataSource GanksLocalDataSource) {
        mGanksRemoteDataSource = checkNotNull(GanksRemoteDataSource);
        mGanksLocalDataSource = checkNotNull(GanksLocalDataSource);
    }

    /**
     * Returns the single instance of this class, creating it if necessary.
     *
     * @param GanksRemoteDataSource the backend data source
     * @param GanksLocalDataSource  the device storage data source
     * @return the {@link GanksRepository} instance
     */
    public static GanksRepository getInstance(GanksDataSource GanksRemoteDataSource,
                                              GanksDataSource GanksLocalDataSource) {
        if (INSTANCE == null) {
            INSTANCE = new GanksRepository(GanksRemoteDataSource, GanksLocalDataSource);
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
    public Observable<List<Gank>> getGanks() {
        // Respond immediately with cache if available and not dirty
        if (mCachedGanks != null && !mCacheIsDirty) {
            return Observable.from(mCachedGanks.values()).toList();
        } else if (mCachedGanks == null) {
            mCachedGanks = new LinkedHashMap<>();
        }

        Observable<List<Gank>> remoteGanks = mGanksRemoteDataSource
                .getGanks()
                .flatMap(new Func1<List<Gank>, Observable<Gank>>() {
                    @Override
                    public Observable<Gank> call(List<Gank> Ganks) {
                        return Observable.from(Ganks);
                    }
                })
                .doOnNext(new Action1<Gank>() {
                    @Override
                    public void call(Gank Gank) {
                        mGanksLocalDataSource.saveGank(Gank);
                        mCachedGanks.put(Gank.id, Gank);
                    }
                })
                .toList()
                .doOnCompleted(new Action0() {
                    @Override
                    public void call() {
                        mCacheIsDirty = false;
                    }
                });
        if (mCacheIsDirty) {
            return remoteGanks;
        } else {
            // Query the local storage if available. If not, query the network.
            Observable<List<Gank>> localGanks = mGanksLocalDataSource.getGanks();
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
        mCachedGanks.put(Gank.id, Gank);
    }

    @Override
    public void completeGank(@NonNull Gank Gank) {
        checkNotNull(Gank);
        mGanksRemoteDataSource.completeGank(Gank);
        mGanksLocalDataSource.completeGank(Gank);

        Gank completedGank = new Gank(Gank.who, Gank.desc, Gank.id, true);

        // Do in memory cache update to keep the app UI up to date
        if (mCachedGanks == null) {
            mCachedGanks = new LinkedHashMap<>();
        }
        mCachedGanks.put(Gank.id, completedGank);
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

        Gank activeGank = new Gank(Gank.who, Gank.desc, Gank.id);

        // Do in memory cache update to keep the app UI up to date
        if (mCachedGanks == null) {
            mCachedGanks = new LinkedHashMap<>();
        }
        mCachedGanks.put(Gank.id, activeGank);
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
            if (entry.getValue().isRead) {
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
                        mCachedGanks.put(Gank.id, Gank);
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
