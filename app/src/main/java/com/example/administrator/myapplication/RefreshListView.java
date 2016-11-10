package com.example.administrator.myapplication;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListView;
import android.widget.TextView;

/**
 * @author yhw(email:861574834@qq.com)
 * @date 2016-10-11 23:46
 * @package com.customdemo
 * @description RefreshListView  TODO(界面功能描述)
 * @params TODO(进入界面传参描述)
 */
public class RefreshListView extends ListView implements OnScrollListener {

    //下拉的状态
    private static final int PULL_DOWN = 0;//下拉的状态
    private static final int RELEASE_FRESH = 1;//松开刷新
    private static final int REFRESHING = 2;//正在刷新
    private static final int REMIND_REFRESHED = 3;//提示刷新成功
    //上拉的状态
    private static final int PULL_UP = 4;// 上拉的状态
    private static final int RELEASE_LOAD = 5;//松开加载
    private static final int LOADING = 6;//正在加载
    //正常状态
    private static final int NORMAL = -1;//初始状态 也是正常状态
    private static final String TAG = "RefreshListView";

    private int mViewStatus = NORMAL;//控件状态 默认就是正常状态

    //顶部的header
    private View mHeaderView;//顶部布局视图
    private TextView mTvHeadRemind;//顶部的提示语
    //底部的footer
    private View mFooterView;
    private TextView mTvFootRemind;

    private boolean isInTop = true;//当前listView是否在顶部
    private boolean isInBottom = false;//当前的listview是否在底部

    private int mDownY;//按下的Y轴坐标
    private int mHeaderHeight = 0;//头控件的高度
    private int mFooterHeight = 0;//底控件的高度

    private OnRefreshListener mRefreshListener;

    public RefreshListView(Context context) {
        super(context, null);
    }

    public RefreshListView(Context context, AttributeSet attrs) {
        super(context, attrs);

        //初始化头部布局
        mHeaderView = LayoutInflater.from(context).inflate(R.layout.layout_refreshlist_header, null);
        mTvHeadRemind = ((TextView) mHeaderView.findViewById(R.id.list_head_remind));
        int w = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        int h = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        mHeaderView.measure(w, h);
        mHeaderHeight = mHeaderView.getMeasuredHeight();
        mHeaderView.setPadding(0, -mHeaderHeight, 0, 0);//初始化状态为隐藏
        addHeaderView(mHeaderView);

        //初始化底部布局
        mFooterView = LayoutInflater.from(context).inflate(R.layout.layout_refreshlist_footer, null);
        mTvFootRemind = ((TextView) mFooterView.findViewById(R.id.list_footer_remind));
        mFooterView.measure(w, h);
        mFooterHeight = mFooterView.getMeasuredHeight();
        mFooterView.setPadding(0, 0, 0, -mFooterHeight);
        addFooterView(mFooterView);

        setOnScrollListener(this);
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

        //firstVisibleItem 只要出现在屏幕中,哪怕一部分 firstVisibleItem也会为0;会出现滑动到顶部但不精确的问题,需要进一步判断
        if (firstVisibleItem == 0) {
            View firstVisibleItemView = getChildAt(0);
            isInTop = firstVisibleItemView != null && firstVisibleItemView.getTop() == 0;
        } else {
            isInTop = false;
        }

        //判断底部也是同样的道理
        if (firstVisibleItem + visibleItemCount == totalItemCount) {
            View lastVisibleItemView = getChildAt(getChildCount() - 1);
            isInBottom = lastVisibleItemView != null && lastVisibleItemView.getBottom() == getHeight();
        } else {
            isInBottom = false;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mDownY = (int) ev.getRawY();
                break;
            case MotionEvent.ACTION_MOVE:
                int currentY = (int) ev.getRawY();

                int offsetY = currentY - mDownY;

                //向下拉的动作
                if (offsetY > 0 && isInTop) {
                    if (offsetY < mHeaderHeight * 3) {
                        mViewStatus = PULL_DOWN;
                    } else {
                        mViewStatus = RELEASE_FRESH;
                    }
                    mHeaderView.setPadding(0, -mHeaderHeight + offsetY / 3, 0, 0);
                    updateHeaderStatus();
                }
                //向上拉的动作
                else if (offsetY < 0 && isInBottom) {
                    if (offsetY > -mFooterHeight * 3) {
                        mViewStatus = PULL_UP;
                    } else {
                        mViewStatus = RELEASE_LOAD;
                    }
                    mFooterView.setPadding(0, 0, 0, -mFooterHeight - offsetY / 3);
                    updateHeaderStatus();
                }
                break;
            case MotionEvent.ACTION_UP:
                //下拉刷新
                if (mViewStatus == PULL_DOWN) {
                    mHeaderView.setPadding(0, -mHeaderHeight, 0, 0);
                }
                //释放刷新
                else if (mViewStatus == RELEASE_FRESH) {
                    mViewStatus = REFRESHING;
                    if (mRefreshListener != null) {
                        mRefreshListener.onRefreshing();
                    }
                    mHeaderView.setPadding(0, 0, 0, 0);
                }

                //上拉加载
                else if (mViewStatus == PULL_UP) {
                    mFooterView.setPadding(0, 0, 0, -mFooterHeight);
                }

                //松开加载更多
                else if (mViewStatus == RELEASE_LOAD) {
                    mViewStatus = LOADING;
                    if (mRefreshListener != null) {
                        mRefreshListener.onLoading();
                    }
                    mFooterView.setPadding(0, 0, 0, 0);
                }

                //更新指示条的状态
                updateHeaderStatus();
                break;
        }
        return super.onTouchEvent(ev);
    }

    private void updateHeaderStatus() {
        switch (mViewStatus) {
            case PULL_DOWN:
                mTvHeadRemind.setText("下拉刷新");
                break;
            case RELEASE_FRESH:
                mTvHeadRemind.setText("释放刷新");
                break;
            case REFRESHING:
                mTvHeadRemind.setText("正在刷新");
                break;
            case REMIND_REFRESHED:
                mTvHeadRemind.setText("刷新成功");
                break;
            case PULL_UP:
                mTvFootRemind.setText("上拉加载");
                break;
            case RELEASE_LOAD:
                mTvFootRemind.setText("松开加载");
                break;
            case LOADING:
                mTvFootRemind.setText("正在加载");
                break;
            case NORMAL:
                mTvHeadRemind.setText("下拉刷新");
                mTvFootRemind.setText("上拉加载");
                break;
        }
    }

    /**
     * 刷新完成
     */
    public void onRefreshed() {
        mViewStatus = REMIND_REFRESHED;
        postDelayed(new Runnable() {
            @Override
            public void run() {
                mViewStatus = NORMAL;
                updateHeaderStatus();
//                mHeaderView.setPadding(0, -mHeaderHeight, 0, 0);
                mFooterView.setPadding(0, 0, 0, -mFooterHeight);
                HeaderHideAnim();
            }
        }, 500);
        updateHeaderStatus();
    }

    /**
     * 上拉加载完成
     */
    public void onLoaded() {
        mViewStatus = NORMAL;
        updateHeaderStatus();
        mHeaderView.setPadding(0, -mHeaderHeight, 0, 0);
//        mFooterView.setPadding(0, 0, 0, -mFooterHeight);
        footerHideAnim();
    }

    public void HeaderHideAnim() {
        final ValueAnimator anim = ObjectAnimator.ofFloat(0.0f, 1.0f).setDuration(300);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float cVal = (Float) animation.getAnimatedValue();

                int height = mHeaderView.getMeasuredHeight();
                    height = (int) (0 - (mHeaderHeight + height) * cVal);
                    Log.e(TAG, "height:" + height);
                    mHeaderView.setPadding(0, height, 0, 0);
            }
        });
        anim.start();
    }

    public void footerHideAnim() {
        final ValueAnimator anim = ObjectAnimator.ofFloat(0.0f, 1.0f).setDuration(300);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float cVal = (Float) animation.getAnimatedValue();

                int height = mFooterView.getMeasuredHeight();
                height = (int) (0 - (mFooterHeight + height) * cVal);
                Log.e(TAG, "mFooterView==height:" + height);
                mFooterView.setPadding(0, 0, 0,height);
            }
        });
        anim.start();
    }

    public void setRefreshListener(OnRefreshListener refreshListener) {
        mRefreshListener = refreshListener;
    }

    public interface OnRefreshListener {
        void onRefreshing();

        void onLoading();
    }
}
