package com.wangxinarhat.mvp.ganks.opreators;

import com.wangxinarhat.mvp.data.Gank;
import com.wangxinarhat.mvp.data.GankData;

import java.util.ArrayList;
import java.util.List;

import rx.functions.Func1;

/**
 * Created by wang on 2016/4/5.
 */
public class GankData2Ganks implements Func1<GankData, List<Gank>> {


    public static GankData2Ganks newInstance() {
        return new GankData2Ganks();
    }


    @Override
    public List<Gank> call(GankData gankData) {

        List<Gank> androidList = gankData.results.androidList;
        List<Gank> restList = gankData.results.restList;
        List<Gank> iOSList = gankData.results.iOSList;
        List<Gank> welfareList = gankData.results.welfareList;
        List<Gank> expandList = gankData.results.expandList;
        List<Gank> recommendList = gankData.results.recommendList;
        List<Gank> appList = gankData.results.appList;

        List<Gank> gankList = new ArrayList<>();

        gankList.addAll(welfareList);
        gankList.addAll(androidList);
        gankList.addAll(iOSList);
        gankList.addAll(appList);
        gankList.addAll(recommendList);
        gankList.addAll(expandList);
        gankList.addAll(restList);
        return gankList;

    }
}
