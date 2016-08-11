package com.wangxinarhat.mvp.ganks;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;

import com.wangxinarhat.mvp.R;
import com.wangxinarhat.mvp.data.source.GanksRepository;
import com.wangxinarhat.mvp.data.source.local.GanksLocalDataSource;
import com.wangxinarhat.mvp.data.source.remote.GanksRemoteDataSource;
import com.wangxinarhat.mvp.utils.ActivityUtils;

import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;

public class GanksActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String CURRENT_FILTERING_KEY = "CURRENT_FILTERING_KEY";
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.contentFrame)
    FrameLayout mContentFrame;
    @BindView(R.id.fab)
    FloatingActionButton mFab;
    @BindView(R.id.nav_view)
    NavigationView mNavView;
    @BindView(R.id.drawer_layout)
    DrawerLayout mDrawerLayout;




    private GanksPresenter mGanksPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ganks);
        ButterKnife.bind(this);

        initView(savedInstanceState);

    }

    private void initView(Bundle savedInstanceState) {
        setSupportActionBar(mToolbar);
        ActionBar ab = getSupportActionBar();
        ab.setHomeAsUpIndicator(R.drawable.ic_menu);
        ab.setDisplayHomeAsUpEnabled(true);

        // Set up the navigation drawer.
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerLayout.setStatusBarBackground(R.color.colorPrimaryDark);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawerLayout, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawerLayout.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


//Set up the FloatingActionButton


        GanksFragment ganksFragment =
                (GanksFragment) getSupportFragmentManager().findFragmentById(R.id.contentFrame);
        if (ganksFragment == null) {
            // Create the fragment
            ganksFragment = GanksFragment.newInstance();
            ActivityUtils.addFragmentToActivity(
                    getSupportFragmentManager(), ganksFragment, R.id.contentFrame);
        }

//        创建后的fragment实例作为presenter的构造函数参数被传入，这样就可以在presenter中调用view中的方法了

        // Create the presenter
//        mGanksPresenter = new GanksPresenter(
//                Injection.provideGanksRepository(getApplicationContext()), ganksFragment);
        mGanksPresenter = new GanksPresenter(GanksRepository.getInstance(GanksRemoteDataSource.getInstance(new Date(System.currentTimeMillis())), GanksLocalDataSource.getInstance(this)), ganksFragment);
        // Load previously saved state, if available.
        if (savedInstanceState != null) {
            GanksFilterType currentFiltering =
                    (GanksFilterType) savedInstanceState.getSerializable(CURRENT_FILTERING_KEY);
            mGanksPresenter.setFiltering(currentFiltering);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putSerializable(CURRENT_FILTERING_KEY, mGanksPresenter.getFiltering());

        super.onSaveInstanceState(outState);
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.daily, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
