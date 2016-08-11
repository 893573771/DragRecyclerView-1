package com.youga.sample;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;


import com.youga.recyclerview.DragRecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private DragRecyclerView mDragRecyclerView;
    private BaseAdapter mAdapter;
    private Toolbar toolbar;
    private boolean notMore;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

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
                        getData("NULL");
                        mDragRecyclerView.showLoadingView();//加载显示ProgressBar
                        break;
                    case R.id.empty:
                        mAdapter.getDataList().clear();
                        mDragRecyclerView.showEmptyView("神马都没有");//显示请求结果为空时显示
                        break;
                    case R.id.error:
                        mAdapter.getDataList().clear();
                        mDragRecyclerView.showErrorView("网络连接错误", R.mipmap.ic_launcher);//显示请求错误时显示
                        break;
                    case R.id.not_more:
                        mDragRecyclerView.showLoadingView();//加载显示ProgressBar
                        notMore = true;
                        getData("NULL");
                        break;
                }
                return false;
            }
        });

        mDragRecyclerView = (DragRecyclerView) findViewById(R.id.dragRecyclerView);
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);

        mAdapter = new RecyclerViewAdapter(this, null);

//        mDragRecyclerView.setAdapter(mAdapter); 不执行加载更多 默认设置LinearLayoutManager VERTICAL
//        mDragRecyclerView.setAdapter(mAdapter,boolean b); b==true?执行加载更多:不执行加载更多 默认设置LinearLayoutManager VERTICAL
//        mDragRecyclerView.setAdapter(mAdapter,boolean b,LayoutManager manager); b==true?执行加载更多:不执行加载更多 manager 自己设置 LayoutManager
        mDragRecyclerView.setAdapter(mAdapter, true, new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mDragRecyclerView.showLoadingView();//加载显示ProgressBar
        mDragRecyclerView.setRequestCount(10);//设置每次请求的数量,默认10
        getData("NULL");

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (mAdapter.getItemCount() == 0) {
                    getData("NULL");
                } else {
                    getData("UP");
                }
            }
        });

        mDragRecyclerView.setOnDragListener(new DragRecyclerView.OnDragListener() {
            @Override
            public void onLoadMore() {
                notMore = false;
                getData("DOWN");
            }
        });


        mDragRecyclerView.setOnItemClickListener(new DragRecyclerView.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Toast.makeText(MainActivity.this, "position:" + position, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * 模拟网络请求
     *
     * @param action 请求动作
     */
    private void getData(final String action) {
        if (!"DOWN".equals(action)) {
            mSwipeRefreshLayout.setEnabled(false);
            mSwipeRefreshLayout.setRefreshing(true);
        }
        mSwipeRefreshLayout.postDelayed(new Runnable() {
            @Override
            public void run() {
                switch (action) {
                    case "NULL"://刷新
                        List<String> list = setList(0);
                        mAdapter.getDataList().clear();
                        mAdapter.getDataList().addAll(list);
                        mDragRecyclerView.onDragState(list.size());//传入请求结果的个数,自动判断是否还有更多数据
                        if (list.size() == 0) {
                            mDragRecyclerView.showEmptyView("神马都没有");//显示请求结果为空时显示
                        } else {
                            mAdapter.getDataList().addAll(0, list);
                            mAdapter.notifyDataSetChanged();
                        }
                        //如果请求失败
                        //mDragRecyclerView.showErrorView("网络连接错误");//显示请求错误时显示
                        break;
                    case "UP"://请求新的数据 动作下拉
                        list = setUpList(mAdapter.getFirstNumber());
                        mAdapter.getDataList().addAll(0, list);
                        mAdapter.notifyDataSetChanged();
                        break;
                    case "DOWN"://加载更多 动作上滑
                        list = setList(mAdapter.getLastNumber() + 1);
                        mAdapter.getDataList().addAll(list);
                        mDragRecyclerView.onDragState(list.size());//传入请求结果的个数,自动判断是否还有更多数据
                        //如果请求失败
                        //mDragRecyclerView.onDragState(-1)
                        //footer 可点击继续请求
                        break;
                }
                if (!"DOWN".equals(action)) {
                    mSwipeRefreshLayout.setEnabled(true);
                    mSwipeRefreshLayout.setRefreshing(false);
                }
            }
        }, 1000);

    }

    private List<String> setList(int start) {
        List<String> dataList = new ArrayList<>();
        for (int i = start; i < (notMore ? 9 : 10 + start); i++) {
            dataList.add(String.valueOf(i));
        }
        return dataList;
    }

    private List<String> setUpList(int start) {
        List<String> dataList = new ArrayList<>();
        for (int i = start - 10; i < 10; i++) {
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
        int id = item.getItemId();
        if (id == R.id.lm_hor) {
            mAdapter = new RecyclerViewAdapter(this, null);
            mDragRecyclerView.setAdapter(mAdapter, true, new LinearLayoutManager(this,
                    LinearLayoutManager.HORIZONTAL, false));
        } else if (id == R.id.lm_ver) {
            mAdapter = new RecyclerViewAdapter(this, null);
            mDragRecyclerView.setAdapter(mAdapter, true, new LinearLayoutManager(this,
                    LinearLayoutManager.VERTICAL, false));
        } else if (id == R.id.glm_hor) {
            mAdapter = new StaggeredGridAdapter(this, null, false);
            mDragRecyclerView.setAdapter(mAdapter, true, new GridLayoutManager(this, 4,
                    GridLayoutManager.HORIZONTAL, false));
        } else if (id == R.id.glm_ver) {
            mAdapter = new StaggeredGridAdapter(this, null, false);
            mDragRecyclerView.setAdapter(mAdapter, true, new GridLayoutManager(this, 4,
                    GridLayoutManager.VERTICAL, false));
        } else if (id == R.id.sglm_hor) {
            mAdapter = new StaggeredGridAdapter(this, null, true);
            mDragRecyclerView.setAdapter(mAdapter, true,
                    new StaggeredGridLayoutManager(4, StaggeredGridLayoutManager.HORIZONTAL));
        } else if (id == R.id.sglm_ver) {
            mAdapter = new StaggeredGridAdapter(this, null, true);
            mDragRecyclerView.setAdapter(mAdapter, true,
                    new StaggeredGridLayoutManager(4, StaggeredGridLayoutManager.VERTICAL));
        }
        mDragRecyclerView.showLoadingView();
        getData("NULL");

        mDragRecyclerView.setOnDragListener(new DragRecyclerView.OnDragListener() {
            @Override
            public void onLoadMore() {
                notMore = false;
                getData("DOWN");
            }
        });


        mDragRecyclerView.setOnItemClickListener(new DragRecyclerView.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Toast.makeText(MainActivity.this, "position:" + position, Toast.LENGTH_SHORT).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
