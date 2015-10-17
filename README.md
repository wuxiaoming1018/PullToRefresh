# PullToRefresh
下拉刷新上拉加载更多
========================================================
# layout中：
-----------------------------------------------------------
                <com.quanquan.pulltorefresh.view.RefreshListView
                     android:layout_width="match_parent"
                     android:layout_height="match_parent"
                     android:id="@+id/refreshListView">

                </com.quanquan.pulltorefresh.view.RefreshListView>
        
# activity中:
-----------------------------------------------------
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
                
# 请求数据成功后：
-------------------------------------------
              private Handler handler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    adapter.notifyDataSetChanged();
                    //请求服务器数据成功后，在UI线程中调用此方法
                    refreshListView.completeRefresh();
                }
            };
            
