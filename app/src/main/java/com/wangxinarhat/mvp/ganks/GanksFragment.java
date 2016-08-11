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

package com.wangxinarhat.mvp.ganks;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.wangxinarhat.mvp.R;
import com.wangxinarhat.mvp.data.Gank;
import com.wangxinarhat.mvp.gankdetail.GankDetailActivity;
import com.wangxinarhat.mvp.utils.CommonUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Display a grid of {@link Gank}s. User can choose to view all, active or completed ganks.
 */
public class GanksFragment extends Fragment implements GanksContract.View, OnRecyclerViewItemClickListener {

    @BindView(R.id.filteringLabel)
    TextView mFilteringLabel;
    @BindView(R.id.recycler)
    RecyclerView mRecycler;
    @BindView(R.id.ganks_container)
    LinearLayout mGanksContainer;
    @BindView(R.id.no_ganks_icon)
    ImageView mNoGanksIcon;
    @BindView(R.id.no_ganks_main)
    TextView mNoGanksMain;
    @BindView(R.id.no_ganks_add)
    TextView mNoGanksAdd;
    @BindView(R.id.no_ganks)
    LinearLayout mNoGanks;
    @BindView(R.id.swipe)
    ScrollChildSwipeRefreshLayout mSwipe;


    private GanksContract.Presenter mPresenter;

    private GanksAdapter mAdapter;

    public GanksFragment() {
        // Requires empty public constructor
    }

    public static GanksFragment newInstance() {
        return new GanksFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdapter = new GanksAdapter(new ArrayList<Gank>(0), mItemListener);
    }

    //presenter开始获取数据并调用view中方法改变界面显示，其调用时机是在Fragment类的onResume方法中
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

    //将presenter实例传入view中，其调用时机是presenter实现类的构造函数中。
    @Override
    public void setPresenter(@NonNull GanksContract.Presenter presenter) {
        mPresenter = checkNotNull(presenter);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        mPresenter.result(requestCode, resultCode);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.ganks_frag, container, false);

        ButterKnife.bind(this, root);
        // Set up ganks view
        final LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        mRecycler.setLayoutManager(layoutManager);
        mRecycler.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener(this);

        // Set up  no ganks view
        mNoGanksAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showReloadGank();
            }
        });

        // Set up floating action button
        FloatingActionButton fab = (FloatingActionButton) getActivity().findViewById(R.id.fab);

        fab.setImageResource(R.drawable.ic_add);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPresenter.addNewGank();
            }
        });

        // Set up progress indicator
        mSwipe.setColorSchemeColors(
                ContextCompat.getColor(getActivity(), R.color.colorPrimary),
                ContextCompat.getColor(getActivity(), R.color.colorAccent),
                ContextCompat.getColor(getActivity(), R.color.colorPrimaryDark)
        );
        // Set the scrolling view in the custom SwipeRefreshLayout.
        mSwipe.setScrollUpChild(mRecycler);

        mSwipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
//                mPresenter.loadGanks(false);
                mPresenter.loadGanks(true, new Date(System.currentTimeMillis()));
            }
        });

        setHasOptionsMenu(true);

        return root;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_clear:
                mPresenter.clearCompletedGanks();
                break;
            case R.id.menu_filter:
                showFilteringPopUpMenu();
                break;
            case R.id.menu_refresh:
                mPresenter.loadGanks(true, new Date(System.currentTimeMillis()));
                break;
        }
        return true;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.ganks_fragment_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void showFilteringPopUpMenu() {
        PopupMenu popup = new PopupMenu(getContext(), getActivity().findViewById(R.id.menu_filter));
        popup.getMenuInflater().inflate(R.menu.filter_ganks, popup.getMenu());

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.active:

                        mPresenter.setFiltering(GanksFilterType.ACTIVE_GANKS);
                        break;
                    case R.id.completed:
                        mPresenter.setFiltering(GanksFilterType.COMPLETED_GANKS);
                        break;
                    default:
                        mPresenter.setFiltering(GanksFilterType.ALL_GANKS);
                        break;
                }
                mPresenter.loadGanks(false, new Date(System.currentTimeMillis()));
                return true;
            }
        });

        popup.show();
    }

    /**
     * Listener for clicks on ganks in the ListView.
     */
    GanksItemListener mItemListener = new GanksItemListener() {
        @Override
        public void onGankClick(Gank clickedGank) {
            mPresenter.openGankDetails(clickedGank);
        }

        @Override
        public void onCompleteGankClick(Gank completedGank) {
            mPresenter.completeGank(completedGank);
        }

        @Override
        public void onActivateGankClick(Gank activatedGank) {
            mPresenter.activateGank(activatedGank);
        }
    };

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
    public void showGanks(List<Gank> ganks) {
        mAdapter.replaceData(ganks);

        mGanksContainer.setVisibility(View.VISIBLE);
        mNoGanks.setVisibility(View.GONE);
    }

    @Override
    public void showNoActiveGanks() {
        showNoGanksViews(
                getResources().getString(R.string.no_ganks_active),
                R.drawable.ic_check_circle_24dp,
                false
        );
    }

    @Override
    public void showNoGanks() {
        showNoGanksViews(
                getResources().getString(R.string.no_ganks_all),
                R.drawable.ic_assignment_turned_in_24dp,
                false
        );
    }

    @Override
    public void showNoCompletedGanks() {
        showNoGanksViews(
                getResources().getString(R.string.no_ganks_completed),
                R.drawable.ic_verified_user_24dp,
                false
        );
    }

    @Override
    public void showSuccessfullySavedMessage() {
        showMessage(getString(R.string.successfully_saved_gank_message));
    }

    private void showNoGanksViews(String mainText, int iconRes, boolean showAddView) {
        mGanksContainer.setVisibility(View.GONE);
        mNoGanks.setVisibility(View.VISIBLE);

        mNoGanksMain.setText(mainText);
        mNoGanksIcon.setImageDrawable(getResources().getDrawable(iconRes));
        mNoGanksAdd.setVisibility(showAddView ? View.VISIBLE : View.GONE);
    }

    @Override
    public void showActiveFilterLabel() {
        mFilteringLabel.setText(getResources().getString(R.string.label_active));
    }

    @Override
    public void showCompletedFilterLabel() {
        mFilteringLabel.setText(getResources().getString(R.string.label_completed));
    }

    @Override
    public void showAllFilterLabel() {
        mFilteringLabel.setText(getResources().getString(R.string.label_all));
    }

    @Override
    public void showReloadGank() {
//        Intent intent = new Intent(getContext(), AddEditGankActivity.class);
//        startActivityForResult(intent, AddEditGankActivity.REQUEST_ADD_TASK);

        Snackbar.make(getView(), "重新加载！", Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void showGankDetailsUi(int itemViewType, String url, String title) {
        if (itemViewType == ItemType.ITEM_TYPE_GIRL.ordinal()) {

        } else if (itemViewType == ItemType.ITEM_TYPE_NORMAL.ordinal()) {
            getContext().startActivity(GankDetailActivity.getIntent(url, title));
        } else if (itemViewType == ItemType.ITEM_TYPE_CATEGOTY.ordinal()) {
        }
    }


    @Override
    public void showGankMarkedComplete() {
        showMessage(getString(R.string.gank_marked_complete));
    }

    @Override
    public void showGankMarkedActive() {
        showMessage(getString(R.string.gank_marked_active));
    }

    @Override
    public void showCompletedGanksCleared() {
        showMessage(getString(R.string.completed_ganks_cleared));
    }

    @Override
    public void showLoadingGanksError() {
        showMessage(getString(R.string.loading_ganks_error));
    }


    private void showMessage(String message) {
        CommonUtils.showShortSnackbar(getView(), message);
    }

    @Override
    public boolean isActive() {
        return isAdded();
    }


    @Override
    public void onItemClick(View itemView, int position, int itemViewType, Gank gank, View viewImage, View viewText) {
        showGankDetailsUi(itemViewType, gank.getUrl(), gank.getTitle());
    }
}
