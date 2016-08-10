package com.wangxinarhat.mvp.gankdetail;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.wangxinarhat.mvp.R;
import com.wangxinarhat.mvp.global.BaseApplication;
import com.wangxinarhat.mvp.utils.ActivityUtils;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by wang on 2016/8/10.
 */
public class GankDetailActivity extends AppCompatActivity {

    public static final String EXTRA_GANK_URL = "GANK_URL";
    private static final String EXTRA_GANK_TITLE = "GANK_TITLE";

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    public static Intent getIntent(String url, String title) {

        Intent intent = new Intent(BaseApplication.getApplication(), GankDetailActivity.class);
        intent.putExtra(EXTRA_GANK_URL, url);
        intent.putExtra(EXTRA_GANK_TITLE, title);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_gank_detail);
        ButterKnife.bind(this);

        initView();
    }

    private void initView() {
        setSupportActionBar(mToolbar);
        ActionBar ab = getSupportActionBar();
        ab.setHomeButtonEnabled(true); //设置返回键可用
        ab.setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        ab.setTitle(intent.getStringExtra(EXTRA_GANK_TITLE));


        GankDetailFragment gankDetailFragment =
                (GankDetailFragment) getSupportFragmentManager().findFragmentById(R.id.contentFrame);
        if (gankDetailFragment == null) {
            // Create the fragment
            gankDetailFragment = GankDetailFragment.newInstance(intent.getStringExtra(EXTRA_GANK_URL), intent.getStringExtra(EXTRA_GANK_TITLE));
            ActivityUtils.addFragmentToActivity(
                    getSupportFragmentManager(), gankDetailFragment, R.id.contentFrame);
        }

        new GankDetailPresenter(gankDetailFragment);
    }
}
