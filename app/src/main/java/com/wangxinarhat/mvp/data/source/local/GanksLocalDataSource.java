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

package com.wangxinarhat.mvp.data.source.local;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.squareup.sqlbrite.BriteDatabase;
import com.squareup.sqlbrite.SqlBrite;
import com.wangxinarhat.mvp.data.Gank;
import com.wangxinarhat.mvp.data.source.GanksDataSource;

import java.util.Date;
import java.util.List;

import rx.Observable;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.wangxinarhat.mvp.data.source.local.GanksPersistenceContract.GankEntry;


/**
 * Concrete implementation of a data source as a db.
 */
public class GanksLocalDataSource implements GanksDataSource {

    private static GanksLocalDataSource INSTANCE;
    private final BriteDatabase mDatabaseHelper;
    private Func1<Cursor, Gank> mGankMapperFunction;

    // Prevent direct instantiation.
    private GanksLocalDataSource(@NonNull Context context) {
        checkNotNull(context);
        GanksDbHelper dbHelper = new GanksDbHelper(context);
        SqlBrite sqlBrite = SqlBrite.create();
        mDatabaseHelper = sqlBrite.wrapDatabaseHelper(dbHelper, Schedulers.io());
        mGankMapperFunction = new Func1<Cursor, Gank>() {
            @Override
            public Gank call(Cursor c) {
                String itemId = c.getString(c.getColumnIndexOrThrow(GanksPersistenceContract.GankEntry.COLUMN_NAME_ENTRY_ID));
                String title = c.getString(c.getColumnIndexOrThrow(GanksPersistenceContract.GankEntry.COLUMN_NAME_TITLE));
                String description =
                        c.getString(c.getColumnIndexOrThrow(GanksPersistenceContract.GankEntry.COLUMN_NAME_DESCRIPTION));
                boolean completed =
                        c.getInt(c.getColumnIndexOrThrow(GanksPersistenceContract.GankEntry.COLUMN_NAME_COMPLETED)) == 1;
                return new Gank(title, description, itemId, completed);
            }
        };
    }

    public static GanksLocalDataSource getInstance(@NonNull Context context) {
        if (INSTANCE == null) {
            INSTANCE = new GanksLocalDataSource(context);
        }
        return INSTANCE;
    }

    @Override
    public Observable<List<Gank>> getGanks(Date date) {
        String[] projection = {
                GankEntry.COLUMN_NAME_ENTRY_ID,
                GankEntry.COLUMN_NAME_TITLE,
                GankEntry.COLUMN_NAME_DESCRIPTION,
                GankEntry.COLUMN_NAME_COMPLETED
        };
        String sql = String.format("SELECT %s FROM %s", TextUtils.join(",", projection), GankEntry.TABLE_NAME);
        return mDatabaseHelper.createQuery(GankEntry.TABLE_NAME, sql)
                .mapToList(mGankMapperFunction);
    }

    @Override
    public Observable<Gank> getGank(@NonNull String gankId) {
        String[] projection = {
                GankEntry.COLUMN_NAME_ENTRY_ID,
                GankEntry.COLUMN_NAME_TITLE,
                GankEntry.COLUMN_NAME_DESCRIPTION,
                GankEntry.COLUMN_NAME_COMPLETED
        };
        String sql = String.format("SELECT %s FROM %s WHERE %s LIKE ?",
                TextUtils.join(",", projection), GankEntry.TABLE_NAME, GankEntry.COLUMN_NAME_ENTRY_ID);
        return mDatabaseHelper.createQuery(GankEntry.TABLE_NAME, sql, gankId)
                .mapToOneOrDefault(mGankMapperFunction, null);
    }

    @Override
    public void saveGank(@NonNull Gank gank) {
        checkNotNull(gank);
        ContentValues values = new ContentValues();
        values.put(GankEntry.COLUMN_NAME_ENTRY_ID, gank.getId());
        values.put(GankEntry.COLUMN_NAME_TITLE, gank.getTitle());
        values.put(GankEntry.COLUMN_NAME_DESCRIPTION, gank.getDescription());
        values.put(GankEntry.COLUMN_NAME_COMPLETED, gank.isCompleted());
        mDatabaseHelper.insert(GankEntry.TABLE_NAME, values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    @Override
    public void completeGank(@NonNull Gank gank) {
        completeGank(gank.getId());
    }

    @Override
    public void completeGank(@NonNull String gankId) {
        ContentValues values = new ContentValues();
        values.put(GankEntry.COLUMN_NAME_COMPLETED, true);

        String selection = GankEntry.COLUMN_NAME_ENTRY_ID + " LIKE ?";
        String[] selectionArgs = {gankId};
        mDatabaseHelper.update(GankEntry.TABLE_NAME, values, selection, selectionArgs);
    }

    @Override
    public void activateGank(@NonNull Gank gank) {
        activateGank(gank.getId());
    }

    @Override
    public void activateGank(@NonNull String gankId) {
        ContentValues values = new ContentValues();
        values.put(GankEntry.COLUMN_NAME_COMPLETED, false);

        String selection = GankEntry.COLUMN_NAME_ENTRY_ID + " LIKE ?";
        String[] selectionArgs = {gankId};
        mDatabaseHelper.update(GankEntry.TABLE_NAME, values, selection, selectionArgs);
    }

    @Override
    public void clearCompletedGanks() {
        String selection = GankEntry.COLUMN_NAME_COMPLETED + " LIKE ?";
        String[] selectionArgs = {"1"};
        mDatabaseHelper.delete(GankEntry.TABLE_NAME, selection, selectionArgs);
    }

    @Override
    public void refreshGanks() {
        // Not required because the {@link GanksRepository} handles the logic of refreshing the
        // ganks from all the available data sources.
    }

    @Override
    public void deleteAllGanks() {
        mDatabaseHelper.delete(GankEntry.TABLE_NAME, null);
    }

    @Override
    public void deleteGank(@NonNull String gankId) {
        String selection = GankEntry.COLUMN_NAME_ENTRY_ID + " LIKE ?";
        String[] selectionArgs = {gankId};
        mDatabaseHelper.delete(GankEntry.TABLE_NAME, selection, selectionArgs);
    }
}
