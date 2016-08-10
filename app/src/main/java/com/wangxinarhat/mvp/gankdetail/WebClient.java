package com.wangxinarhat.mvp.gankdetail;

import android.graphics.Bitmap;
import android.net.Uri;
import android.text.TextUtils;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * Created by wang on 2016/8/10.
 */
public class WebClient extends WebViewClient {

    private GankDetailContract.View mView;
    public WebClient(GankDetailContract.View gankDetailView) {
        mView = gankDetailView;
    }

    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        if (TextUtils.isEmpty(url)) {
            return true;
        }
        if (Uri.parse(url).getHost().equals("github.com")) {
            return false;
        }
        view.loadUrl(url);
        return true;
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);
        mView.setLoadingIndicator(false);
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        super.onPageStarted(view, url, favicon);
        mView.setLoadingIndicator(true);
    }

    @Override
    public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
        super.onReceivedError(view, errorCode, description, failingUrl);
        mView.showMissingGank();
    }
}
