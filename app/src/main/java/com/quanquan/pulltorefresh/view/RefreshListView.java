package com.quanquan.pulltorefresh.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.quanquan.pulltorefresh.R;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Administrator on 2015/10/17.
 */
public class RefreshListView extends ListView implements AbsListView.OnScrollListener {

    private View headerView, footerView;
    private ImageView iv_arrow;
    private ProgressBar pb_rotate;
    private TextView tv_refresh, tv_time;
    private int headerViewHeight;
    private int startY;


    private RotateAnimation upRotate, downRotate;


    private final int PULL_REFRESH = 0;
    private final int RELEASE_REFRESH = 1;
    private final int REFRESHING = 2;
    private int refreshState = PULL_REFRESH;
    private int footerViewHeight;


    private boolean isLoadingMore = false;

    public RefreshListView(Context context) {
        super(context);
        init();
    }


    public RefreshListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }


    private void init() {
        initHeaderView();
        initFooterView();
        initRotateAnimation();
        setOnScrollListener(this);
    }

    /**
     * 底部上拉加载更多
     */
    private void initFooterView() {
        footerView = View.inflate(getContext(), R.layout.layout_footer, null);

        footerView.measure(0, 0);
        footerViewHeight = footerView.getMeasuredHeight();
        footerView.setPadding(0, 0, 0, -footerViewHeight);
        addFooterView(footerView);

    }


    /**
     * 顶部下拉刷新
     */
    private void initHeaderView() {
        headerView = View.inflate(getContext(), R.layout.layout_header, null);
        iv_arrow = (ImageView) headerView.findViewById(R.id.iv_arrow);
        pb_rotate = (ProgressBar) headerView.findViewById(R.id.pb_rotate);
        tv_refresh = (TextView) headerView.findViewById(R.id.tv_refresh);
        tv_time = (TextView) headerView.findViewById(R.id.tv_time);

        headerView.measure(0, 0);//通知系统测量
        headerViewHeight = headerView.getMeasuredHeight();
        headerView.setPadding(0, -headerViewHeight, 0, 0);

        addHeaderView(headerView);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startY = (int) ev.getY();
                break;
            case MotionEvent.ACTION_MOVE:

                if (refreshState == REFRESHING) {
                    break;
                }

                int deltaY = (int) (ev.getY() - startY);

                int paddingTop = -headerViewHeight + deltaY;


                if (paddingTop > -headerViewHeight && getFirstVisiblePosition() == 0) {
                    headerView.setPadding(0, paddingTop, 0, 0);
                    if (paddingTop >= 0 && refreshState == PULL_REFRESH) {
                        //从下拉刷新状态进入松开刷新状态
                        refreshState = RELEASE_REFRESH;
                        refreshHeaderView();
                    } else if (paddingTop < 0 && refreshState == RELEASE_REFRESH) {
                        //进去下拉刷新状态
                        refreshState = PULL_REFRESH;
                        refreshHeaderView();
                    }
                    return true;//拦截TouchMove，不让listview处理该次move事件,会造成listview无法滑动
                }
                break;
            case MotionEvent.ACTION_UP:
                if (refreshState == PULL_REFRESH) {
                    headerView.setPadding(0, -headerViewHeight, 0, 0);
                } else if (refreshState == RELEASE_REFRESH) {
                    //进入正在刷新状态
                    headerView.setPadding(0, 0, 0, 0);
                    refreshState = REFRESHING;
                    refreshHeaderView();

                    if (listener != null) {
                        listener.onPullRefresh();
                    }
                }

                break;

        }
        return super.onTouchEvent(ev);


    }

    /**
     * 刷新HeaderView
     */
    private void refreshHeaderView() {
        switch (refreshState) {
            case PULL_REFRESH:
                tv_refresh.setText("下拉刷新");
                iv_arrow.startAnimation(downRotate);
                break;
            case RELEASE_REFRESH:
                tv_refresh.setText("松开刷新");
                iv_arrow.startAnimation(upRotate);
                break;
            case REFRESHING:
                iv_arrow.clearAnimation();
                iv_arrow.setVisibility(INVISIBLE);
                pb_rotate.setVisibility(VISIBLE);
                tv_refresh.setText("正在刷新...");
                break;

        }
    }


    private OnRefreshListener listener;

    public void setOnRefreshListener(OnRefreshListener listener) {
        this.listener = listener;
    }

    public interface OnRefreshListener {
        void onPullRefresh();

        void onLoadingMore();
    }


    /**
     * 初始化动画
     */
    private void initRotateAnimation() {
        upRotate = new RotateAnimation(0, 180, Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        upRotate.setDuration(300);
        upRotate.setFillAfter(true);
        downRotate = new RotateAnimation(180, 360, Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        downRotate.setDuration(300);
        downRotate.setFillAfter(true);
    }

    /**
     * 当刷新数据完成后，在UI线程中调用此方法
     */
    public void completeRefresh() {
        if (isLoadingMore) {
            footerView.setPadding(0, 0, 0, -footerViewHeight);
            isLoadingMore = false;
        } else {
            headerView.setPadding(0, -headerViewHeight, 0, 0);
            refreshState = PULL_REFRESH;
            iv_arrow.setVisibility(VISIBLE);
            pb_rotate.setVisibility(INVISIBLE);
            tv_refresh.setText("下拉刷新");
            tv_time.setText("最后刷新：" + getCurrentTime());
        }
    }

    /**
     * 获取当前时间
     *
     * @return
     */
    private String getCurrentTime() {
        SimpleDateFormat format = new SimpleDateFormat("yy-MM-dd HH:mm:ss");
        return format.format(new Date());
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (scrollState == SCROLL_STATE_IDLE && getLastVisiblePosition()
                == (getCount() - 1) && !isLoadingMore) {
            isLoadingMore = true;
            footerView.setPadding(0, 0, 0, 0);//显示出footerView
            setSelection(getCount());//将最后一个item显示在第一个

            if (listener != null) {
                listener.onLoadingMore();
            }
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

    }
}
