package com.quanquan.pulltorefresh;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.quanquan.pulltorefresh.view.RefreshListView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

    private RefreshListView refreshListView;

    private List<String> lists = new ArrayList<>();

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            adapter.notifyDataSetChanged();
            refreshListView.completeRefresh();
        }
    };
    private MyAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        initData();
    }

    /**
     *
     */
    private void initData() {
        for (int i = 0; i < 15; i++) {
            lists.add("listview原来的数据" + i);
        }
    }

    /**
     *
     */
    private void initView() {
        setContentView(R.layout.content_main);
        refreshListView = (RefreshListView) findViewById(R.id.refreshListView);
        adapter = new MyAdapter();
        refreshListView.setAdapter(adapter);

        refreshListView.setOnRefreshListener(new RefreshListView.OnRefreshListener() {
            @Override
            public void onPullRefresh() {
                //需要请求联网的数据库，然后更新UI
                requestDataFromServer(false);
            }

            @Override
            public void onLoadingMore() {
                requestDataFromServer(true);
            }
        });
    }

    /**
     * 请求数据
     */
    private void requestDataFromServer(final boolean isLoadingMore) {
        new Thread() {
            @Override
            public void run() {
                SystemClock.sleep(1000);

                if (isLoadingMore) {
                    lists.add("上拉加载更多更新的数据");
                } else {
                    lists.add(0, "下拉刷新的数据");
                }

                handler.sendEmptyMessage(0);
            }
        }.start();
    }

    class MyAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return lists.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView textView = new TextView(MainActivity.this);
            textView.setTextColor(Color.BLACK);
            textView.setPadding(20, 20, 20, 20);
            textView.setTextSize(18);
            textView.setText(lists.get(position));
            return textView;
        }
    }
}
