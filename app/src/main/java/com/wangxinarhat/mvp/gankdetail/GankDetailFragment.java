package com.wangxinarhat.mvp.gankdetail;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import com.wangxinarhat.mvp.R;
import com.wangxinarhat.mvp.utils.CommonUtils;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by wang on 2016/8/10.
 */
public class GankDetailFragment extends Fragment implements GankDetailContract.View, SwipeRefreshLayout.OnRefreshListener {

    private static final String ARGUMENT_GANK_URL = "GANK_URL";
    private static final String ARGUMENT_GANK_TITLE = "GANK_TITLE";
    @BindView(R.id.webview)
    WebView mWebview;
    @BindView(R.id.swipe)
    SwipeRefreshLayout mSwipe;
    private GankDetailContract.Presenter mPresenter;


    public static GankDetailFragment newInstance(String url, String title) {

        Bundle arguments = new Bundle();
        arguments.putString(ARGUMENT_GANK_URL, url);
        arguments.putString(ARGUMENT_GANK_TITLE, title);
        GankDetailFragment fragment = new GankDetailFragment();
        fragment.setArguments(arguments);
        return fragment;
    }


    @Override
    public void onResume() {
        super.onResume();
        mPresenter.subscribe();
    }

    @Override
    public void onPause() {
        super.onPause();
        mPresenter.unsubscribe();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.frag_gank_detail, container, false);
        ButterKnife.bind(this, root);
        return root;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mPresenter.setUpWebView(mWebview);
        Bundle bundle = getArguments();
        mPresenter.loadUrl(mWebview,bundle.getString(ARGUMENT_GANK_URL));

        mSwipe.setOnRefreshListener(this);
    }

    @Override
    public void setPresenter(GankDetailContract.Presenter presenter) {
        mPresenter = checkNotNull(presenter);
    }

    @Override
    public void setLoadingIndicator(final boolean active) {

        if (getView() == null) {
            return;
        }
        // Make sure setRefreshing() is called after the layout is done with everything else.
        mSwipe.post(new Runnable() {
            @Override
            public void run() {
                mSwipe.setRefreshing(active);
            }
        });

    }

    @Override
    public void showMissingGank() {
        showMessage("加载失败@!");
    }

    @Override
    public void showGankMarkedComplete() {

    }

    @Override
    public void showGankMarkedActive() {

    }

    @Override
    public boolean isActive() {
        return isAdded();
    }

    private void showMessage(String message) {
        CommonUtils.showShortSnackbar(getView(), message);
    }

    @Override
    public void onRefresh() {
        mSwipe.setRefreshing(true);
        mPresenter.loadUrl(mWebview,getArguments().getString(ARGUMENT_GANK_URL));
    }
}
