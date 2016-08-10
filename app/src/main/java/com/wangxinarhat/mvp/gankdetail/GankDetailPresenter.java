package com.wangxinarhat.mvp.gankdetail;

import android.support.annotation.NonNull;
import android.webkit.WebSettings;
import android.webkit.WebView;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by wang on 2016/8/10.
 */
public class GankDetailPresenter implements GankDetailContract.Presenter {

    private final GankDetailContract.View mGankDetailView;


    public GankDetailPresenter(@NonNull GankDetailContract.View gankDetailView) {
        mGankDetailView = checkNotNull(gankDetailView, "gankDetailView cannot be null!");
        mGankDetailView.setPresenter(this);
    }


    @Override
    public void activateGank() {

    }

    @Override
    public void completeGank() {

    }

    @Override
    public void subscribe() {

    }

    @Override
    public void unsubscribe() {
    }

    @Override
    public void setUpWebView(WebView webView) {
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setLoadWithOverviewMode(true);
        settings.setAppCacheEnabled(true);
        settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        settings.setSupportZoom(true);
        webView.setWebViewClient(new WebClient(mGankDetailView));
    }

    @Override
    public void loadUrl(WebView webView, String url) {
        webView.loadUrl(url);
    }

}
