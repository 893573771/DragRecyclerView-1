package com.youga.sample;

import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.youga.swiperecyclerview.SwipeRecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private SwipeRecyclerView mSwipeRecyclerView;
    private RecyclerViewAdapter mAdapter;
    private Toolbar toolbar;
    private boolean notMore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        init();
    }

    private void init() {

        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.refresh:
                        notMore = false;
                        mSwipeRecyclerView.onRefreshLoad();
                        break;
                    case R.id.empty:
                        mAdapter.getDataList().clear();
                        mSwipeRecyclerView.showEmptyView("神马都没有");
                        break;
                    case R.id.error:
                        mAdapter.getDataList().clear();
                        mSwipeRecyclerView.showErrorView("网络连接错误");
                        break;
                    case R.id.not_more:
                        mSwipeRecyclerView.onRefreshLoad();
                        notMore = true;
                        break;
                }
                return false;
            }
        });

        mSwipeRecyclerView = (SwipeRecyclerView) findViewById(R.id.pullLoadMoreRecyclerView);

        mAdapter = new RecyclerViewAdapter(this, null);
        mSwipeRecyclerView.setAdapter(mAdapter);
        mSwipeRecyclerView.initToolbar(toolbar);

        mSwipeRecyclerView.setOnSwipeListener(new SwipeRecyclerView.OnSwipeListener() {
            @Override
            public void onRefresh(boolean refresh) {
                if (refresh) {
                    getData("NULL");
                } else {
                    getData("UP");
                }
            }

            @Override
            public void onLoadMore() {
                notMore = false;
                getData("DOWN");
            }
        });

        mSwipeRecyclerView.setOnItemClickListener(new SwipeRecyclerView.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {

            }
        });

    }

    private void getData(final String action) {
        mAdapter.notifyDataSetChanged();
        switch (action) {
            case "NULL":
                List<String> list = setList(0);
                mAdapter.getDataList().clear();
                mAdapter.getDataList().addAll(list);
                mSwipeRecyclerView.onRefreshComplete(list.size(), true);
                break;
            case "UP":
                list = setUpList(mAdapter.getFirstNumber());
                mAdapter.getDataList().addAll(0, list);
                mSwipeRecyclerView.onRefreshComplete(list.size(), false);
                break;
            case "DOWN":
                list = setList(mAdapter.getLastNumber() + 1);
                mAdapter.getDataList().addAll(list);
                mSwipeRecyclerView.onLoadMoreComplete(list.size());
                break;
        }
    }

    private List<String> setList(int start) {
        List<String> dataList = new ArrayList<>();
        for (int i = start; i < (notMore ? 10 : 20 + start); i++) {
            dataList.add(String.valueOf(i));
        }
        return dataList;
    }

    private List<String> setUpList(int start) {
        List<String> dataList = new ArrayList<>();
        for (int i = start - 20; i < 20; i++) {
            dataList.add(String.valueOf(i));
        }
        return dataList;
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
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
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
