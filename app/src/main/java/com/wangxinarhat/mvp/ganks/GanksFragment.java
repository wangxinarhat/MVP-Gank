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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Display a grid of {@link Gank}s. User can choose to view all, active or completed ganks.
 */
public class GanksFragment extends Fragment implements GanksContract.View, OnRecyclerViewItemClickListener {

    private GanksContract.Presenter mPresenter;

    private GanksAdapter mAdapter;

    private View mNoGanksView;

    private ImageView mNoGankIcon;

    private TextView mNoGankMainView;

    private TextView mNoGankAddView;

    private LinearLayout mGanksView;

    private TextView mFilteringLabelView;

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

        // Set up ganks view
        RecyclerView recyclerView = (RecyclerView) root.findViewById(R.id.ganks_list);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener(this);
        mFilteringLabelView = (TextView) root.findViewById(R.id.filteringLabel);
        mGanksView = (LinearLayout) root.findViewById(R.id.ganksLL);


        // Set up  no ganks view
        mNoGanksView = root.findViewById(R.id.noGanks);
        mNoGankIcon = (ImageView) root.findViewById(R.id.noGanksIcon);
        mNoGankMainView = (TextView) root.findViewById(R.id.noGanksMain);
        mNoGankAddView = (TextView) root.findViewById(R.id.noGanksAdd);
        mNoGankAddView.setOnClickListener(new View.OnClickListener() {
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
        final ScrollChildSwipeRefreshLayout swipeRefreshLayout =
                (ScrollChildSwipeRefreshLayout) root.findViewById(R.id.refresh_layout);
        swipeRefreshLayout.setColorSchemeColors(
                ContextCompat.getColor(getActivity(), R.color.colorPrimary),
                ContextCompat.getColor(getActivity(), R.color.colorAccent),
                ContextCompat.getColor(getActivity(), R.color.colorPrimaryDark)
        );
        // Set the scrolling view in the custom SwipeRefreshLayout.
        swipeRefreshLayout.setScrollUpChild(recyclerView);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
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
        final SwipeRefreshLayout srl =
                (SwipeRefreshLayout) getView().findViewById(R.id.refresh_layout);

        // Make sure setRefreshing() is called after the layout is done with everything else.
        srl.post(new Runnable() {
            @Override
            public void run() {
                srl.setRefreshing(active);
            }
        });
    }

    @Override
    public void showGanks(List<Gank> ganks) {
        mAdapter.replaceData(ganks);

        mGanksView.setVisibility(View.VISIBLE);
        mNoGanksView.setVisibility(View.GONE);
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
        mGanksView.setVisibility(View.GONE);
        mNoGanksView.setVisibility(View.VISIBLE);

        mNoGankMainView.setText(mainText);
        mNoGankIcon.setImageDrawable(getResources().getDrawable(iconRes));
        mNoGankAddView.setVisibility(showAddView ? View.VISIBLE : View.GONE);
    }

    @Override
    public void showActiveFilterLabel() {
        mFilteringLabelView.setText(getResources().getString(R.string.label_active));
    }

    @Override
    public void showCompletedFilterLabel() {
        mFilteringLabelView.setText(getResources().getString(R.string.label_completed));
    }

    @Override
    public void showAllFilterLabel() {
        mFilteringLabelView.setText(getResources().getString(R.string.label_all));
    }

    @Override
    public void showReloadGank() {
//        Intent intent = new Intent(getContext(), AddEditGankActivity.class);
//        startActivityForResult(intent, AddEditGankActivity.REQUEST_ADD_TASK);

        Snackbar.make(getView(), "重新加载！", Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void showGankDetailsUi(String gankId) {
        // in it's own Activity, since it makes more sense that way and it gives us the flexibility
        // to show some Intent stubbing.
//        Intent intent = new Intent(getContext(), GankDetailActivity.class);
//        intent.putExtra(GankDetailActivity.EXTRA_TASK_ID, gankId);
//        startActivity(intent);
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
        Snackbar.make(getView(), message, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public boolean isActive() {
        return isAdded();
    }


    @Override
    public void onItemClick(View itemView, int position, int itemViewType, Gank gank, View viewImage, View viewText) {

    }
}
