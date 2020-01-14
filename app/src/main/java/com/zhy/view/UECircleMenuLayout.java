package com.zhy.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.zhy.sample_circlemenu.R;

import java.lang.ref.WeakReference;

/**
 * Created by zsg on 2020-01-09.
 * Desc:
 * <p>
 */
public class UECircleMenuLayout extends ViewGroup {

    public static final String TAG = UECircleMenuLayout.class.getSimpleName();

    private int mRadius;
    private double mStartAngle = 0;
    private double mTargetAngle = 0;
    private double mCurrentAngle; //选中的Item
    private String[] mItemTexts;
    private int[] mItemImgs;
    private int mMenuItemCount;
    private int mSelectIndex = 0;
    private int mInitSelectIndex = -1;
    private Point bgCenter;

    /**
     * 该容器内child item的默认尺寸
     */
    private static final float RADIO_DEFAULT_CHILD_DIMENSION = 1 / 3f;
    /**
     * 菜单的中心child的默认尺寸
     */
    private float RADIO_DEFAULT_CENTERITEM_DIMENSION = 1 / 3f;
    /**
     * 该容器的内边距,无视padding属性，如需边距请用该变量
     */
    private static final float RADIO_PADDING_LAYOUT = 1 / 28f;
    /**
     * 当每秒移动角度达到该值时，认为是快速移动
     */
    private static final int FLINGABLE_VALUE = 300;

    /**
     * 如果移动角度达到该值，则屏蔽点击
     */
    private static final int NOCLICK_VALUE = 3;

    /**
     * 当每秒移动角度达到该值时，认为是快速移动
     */
    private int mFlingableValue = FLINGABLE_VALUE;
    /**
     * 该容器的内边距,无视padding属性，如需边距请用该变量
     */
    private float mPadding;
    /**
     * 检测按下到抬起时旋转的角度
     */
    private float mTmpAngle;
    /**
     * 检测按下到抬起时使用的时间
     */
    private long mDownTime;

    /**
     * 判断是否正在自动滚动
     */
    private boolean isFling;

    private int selectIndexLeft;
    private int selectIndexTop;

    private AutoPlayTask mAutoPlayTask;
    private boolean mAutoPlayAble = true;
    private int mAutoPlayInterval = 3000;

//    private Position[] mPositions;

    private OnMenuItemClickListener mOnMenuItemClickListener;

    private int mMenuItemLayoutId = R.layout.view_circle_menu_item;
    private int mLastMultiple;


    public UECircleMenuLayout(Context context) {
        this(context, null);
    }

    public UECircleMenuLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public UECircleMenuLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setPadding(0, 0, 0, 0);
        mAutoPlayTask = new AutoPlayTask(this);
        initLayout(context);
    }

    private void initLayout(Context context) {

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int resWidth = 0;
        int resHeight = 0;

        /**
         * 根据传入的参数，分别获取测量模式和测量值
         */
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);

        int height = MeasureSpec.getSize(heightMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);


        Log.i(TAG, "width:" + width);
        Log.i(TAG, "height:" + height);

        /**
         * 如果宽或者高的测量模式非精确值
         */
        if (widthMode != MeasureSpec.EXACTLY || heightMode != MeasureSpec.EXACTLY) {
            // 主要设置为背景图的高度
            resWidth = getSuggestedMinimumWidth();
            // 如果未设置背景图片，则设置为屏幕宽高的默认值
            resWidth = resWidth == 0 ? getDefaultWidth() : resWidth;

            resHeight = getSuggestedMinimumHeight();
            // 如果未设置背景图片，则设置为屏幕宽高的默认值
            resHeight = resHeight == 0 ? getDefaultWidth() : resHeight;
        } else {
            // 如果都设置为精确值，则直接取小值；
            resWidth = resHeight = Math.min(width, height);
        }


        Log.i(TAG, "resWidth:" + resWidth);
        Log.i(TAG, "resHeight:" + resHeight);
        setMeasuredDimension(resWidth, resHeight);

        // 获得半径
        mRadius = Math.max(getMeasuredWidth(), getMeasuredHeight());
        Log.i(TAG, "mRadius:" + mRadius);

        // menu item数量
        final int count = getChildCount();
        // menu item尺寸
        int childSize = (int) (mRadius * RADIO_DEFAULT_CHILD_DIMENSION);
        // menu item测量模式
        int childMode = MeasureSpec.EXACTLY;

        // 迭代测量
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);

            if (child.getVisibility() == GONE) {
                continue;
            }

            // 计算menu item的尺寸；以及和设置好的模式，去对item进行测量
            int makeMeasureSpec = -1;

            if (child.getId() == R.id.id_circle_menu_item_center) {
                makeMeasureSpec = MeasureSpec.makeMeasureSpec((int) (mRadius * RADIO_DEFAULT_CENTERITEM_DIMENSION), childMode);
            } else {
                makeMeasureSpec = MeasureSpec.makeMeasureSpec(childSize, childMode);
            }

//            Log.i(TAG, "makeMeasureSpec:" + makeMeasureSpec);
            child.measure(makeMeasureSpec, makeMeasureSpec);
        }

        mPadding = RADIO_PADDING_LAYOUT * mRadius;
    }


    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int layoutRadius = mRadius;

        // Laying out the child views
        final int childCount = getChildCount();

        int left, top;
        // menu item 的尺寸
        int cWidth = (int) (layoutRadius * RADIO_DEFAULT_CHILD_DIMENSION);

        // 根据menu item的个数，计算角度
        float angleDelay = 360 / (getChildCount() - 1);

        // 遍历去设置menuitem的位置
        for (int i = 0; i < childCount; i++) {
            final View child = getChildAt(i);

            if (child.getId() == R.id.id_circle_menu_item_center) continue;

            if (child.getVisibility() == GONE) {
                continue;
            }

            mStartAngle %= 360;
            Log.i(TAG, "cWidth:" + cWidth);
            Log.i(TAG, "mStartAngle:" + mStartAngle);

            // 计算，中心点到menu item中心的距离
            float tmp = layoutRadius / 2f - cWidth / 2 - mPadding;

            // tmp cosa 即menu item中心点的横坐标
            left = layoutRadius / 2 + (int) Math.round(tmp * Math.cos(Math.toRadians(mStartAngle)) - 1 / 2f * cWidth);
            // tmp sina 即menu item的纵坐标
            top = layoutRadius / 2 + (int) Math.round(tmp * Math.sin(Math.toRadians(mStartAngle)) - 1 / 2f * cWidth);

            child.layout(left, top, left + cWidth, top + cWidth);

            if (mStartAngle == 180 && mInitSelectIndex == -1) {
                mInitSelectIndex = i;
                mSelectIndex = i;

                bgCenter = new Point(left + child.getMeasuredWidth() / 2, top + child.getMeasuredHeight() / 2);
            }
            // 叠加尺寸
            mStartAngle += angleDelay;
        }

        // 找到中心的view，如果存在设置onclick事件
        View cView = findViewById(R.id.id_circle_menu_item_center);
        if (cView != null) {
            cView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (mOnMenuItemClickListener != null) {
                        mOnMenuItemClickListener.itemCenterClick(v);
                    }
                }
            });
            // 设置center item位置
            int cl = layoutRadius / 2 - cView.getMeasuredWidth() / 2;
            int cr = cl + cView.getMeasuredWidth();
            cView.layout(cl, cl, cr, cr);

        }
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        Paint paint = new Paint();
        paint.setColor(Color.parseColor("#80000000"));
        canvas.drawRoundRect(new RectF(bgCenter.x - 300, bgCenter.y - 30, bgCenter.x + 120, bgCenter.y + 30), 100, 100, paint);
        super.dispatchDraw(canvas);
    }

    private void setDefaultCurrentItem() {

        int count = getChildCount();

        int selectionIndex = count / 2; // index从0开始计算

        // 根据menu item的个数，计算角度
        float angleDelay = 360 / (count - 1);

        mCurrentAngle = angleDelay * selectionIndex; // 180°

    }

    /**
     * 记录上一次的x，y坐标
     */
    private float mLastX;
    private float mLastY;

    /**
     * 自动滚动的Runnable
     */
    private AutoFlingRunnable mFlingRunnable;

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {

        handleAutoPlayActionUpOrCancel(event);

        float x = event.getX();
        float y = event.getY();

        // Log.e("TAG", "x = " + x + " , y = " + y);

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mTargetAngle = -1;
                mLastX = x;
                mLastY = y;
                mDownTime = System.currentTimeMillis();
                mTmpAngle = 0;

                // 如果当前已经在快速滚动
                if (isFling) {
                    // 移除快速滚动的回调
                    removeCallbacks(mFlingRunnable);
                    isFling = false;
                    return true;
                }

                break;
            case MotionEvent.ACTION_MOVE:
                mTargetAngle = -1;
                /**
                 * 获得开始的角度
                 */
                float start = getAngle(mLastX, mLastY);
                /**
                 * 获得当前的角度
                 */
                float end = getAngle(x, y);

                // Log.e("TAG", "start = " + start + " , end =" + end);
                // 如果是一、四象限，则直接end-start，角度值都是正值
                if (getQuadrant(x, y) == 1 || getQuadrant(x, y) == 4) {
                    mStartAngle += end - start;
                    mTmpAngle += end - start;
                } else
                // 二、三象限，色角度值是付值
                {
                    mStartAngle += start - end;
                    mTmpAngle += start - end;
                }
                // 重新布局
                requestLayout();

                mLastX = x;
                mLastY = y;

                break;
            case MotionEvent.ACTION_UP:

                // 计算手指移动的角度，是否进入下一个item还是返回原位置
                getMoveAngleToNext(mTmpAngle);

                // 计算，每秒移动的角度
                float anglePerSecond = mTmpAngle * 1000 / (System.currentTimeMillis() - mDownTime);

                // Log.e("TAG", anglePrMillionSecond + " , mTmpAngel = " +
                // mTmpAngle);

                // 如果达到该值认为是快速移动
                if (Math.abs(anglePerSecond) > mFlingableValue && !isFling) {
                    mTargetAngle = -1;
                    // post一个任务，去自动滚动
                    isFling = true;
                    post(mFlingRunnable = new AutoFlingRunnable(anglePerSecond));

                    return true;
                } else {
                    int perAngle = 360 / (getChildCount() - 1);
                    int minIndex = ((int) (mStartAngle / perAngle)) % 10;
                    int maxIndex = (minIndex + 1) % (getChildCount() - 1);
                    int volicity = 0;
                    if (mStartAngle % (perAngle) > perAngle / 2) {
                        mTargetAngle = maxIndex * perAngle;
                        volicity = 50;
                        mSelectIndex = (int) ((getChildCount() - 1) - ((mInitSelectIndex + mStartAngle / perAngle) % (getChildCount() - 1)));
                    } else {
                        mTargetAngle = minIndex * perAngle;
                        volicity = -50;
                        mSelectIndex = (int) ((getChildCount() - 1) - ((mInitSelectIndex + mStartAngle / perAngle) % (getChildCount() - 1)));
                    }
                    Log.e("xxxxxxx", " mSelectIndex111 " + mSelectIndex);
                    isFling = true;
                    post(mFlingRunnable = new AutoFlingRunnable(volicity));
                    // 重新布局
                }

                // 如果当前旋转角度超过NOCLICK_VALUE屏蔽点击
                if (Math.abs(mTmpAngle) > NOCLICK_VALUE) {
                    return true;
                }

                break;
            case MotionEvent.ACTION_CANCEL:
                getMoveAngleToNext(mTmpAngle);
                break;
        }
        return super.dispatchTouchEvent(event);
    }

    private void getMoveAngleToNext(float moveAngle) {
        int count = getChildCount() - 1;
        int angle = 360 / count;
        if (moveAngle >= 0) { // 顺时针滑動（向上）

            if (moveAngle >= (angle / 2)) {
                switchToNextItem();
            } else {
                switchToPreItem();
            }

        } else {
            // 逆時針滑動（向下）

            if (moveAngle >= (angle / 2)) {
                switchToPreItem();
            } else {
                switchToNextItem();
            }

        }
    }


    private void handleAutoPlayActionUpOrCancel(MotionEvent event) {
        if (mAutoPlayAble) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    stopAutoPlay();
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    startAutoPlay();
                    break;
            }
        }
    }

    /**
     * 主要为了action_down时，返回true
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return true;
    }

    @Override
    protected void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        // 记录当前选中Item的位置
        setDefaultCurrentItem();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        startAutoPlay();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }


    public void startAutoPlay() {
        stopAutoPlay();
        if (mAutoPlayAble) {
            postDelayed(mAutoPlayTask, mAutoPlayInterval);
        }
    }


    public void stopAutoPlay() {
        if (mAutoPlayTask != null) {
            removeCallbacks(mAutoPlayTask);
        }
    }

    /**
     * 切换到下一个Item显示
     */
    private void switchToNextItem() {

        int count = getChildCount() - 1;
        int angle = 360 / count;
        double nextAngle = mCurrentAngle / angle % 360;

        mCurrentAngle = nextAngle;
        mStartAngle %= 360;
        mTargetAngle = mStartAngle + angle;

        Log.i(TAG, "mCurrentAngle:" + mCurrentAngle);
        isFling = true;
        post(mFlingRunnable = new AutoFlingRunnable(100));

//        int selectIndex = (mSelectIndex ) % count;
//        int nextIndex = (mSelectIndex + 1) % count;
//        TextView selectChild = getChildAt(selectIndex).findViewById(R.id.id_circle_menu_item_text);
//        TextView nextChild = getChildAt(nextIndex).findViewById(R.id.id_circle_menu_item_text);
//        if (selectChild != null && nextChild != null) {
//            selectChild.setTextColor(getResources().getColor(R.color.colorAccent));
//            nextChild.setTextColor(Color.parseColor("#000000"));
//        }
    }

    /**
     * 切换到上一个Item显示
     */
    private void switchToPreItem() {
        int count = getChildCount() - 1;
        int angle = 360 / count;
        double nextAngle = mCurrentAngle / angle % 360;

        mCurrentAngle = nextAngle;
        mStartAngle %= 360;
        mTargetAngle = mStartAngle + angle;

        Log.i(TAG, "mCurrentAngle:" + mCurrentAngle);
        isFling = true;
        post(mFlingRunnable = new AutoFlingRunnable(100));
    }

    /**
     * 根据触摸的位置，计算角度
     *
     * @param xTouch
     * @param yTouch
     * @return
     */
    private float getAngle(float xTouch, float yTouch) {
        double x = xTouch - (mRadius / 2d);
        double y = yTouch - (mRadius / 2d);
        return (float) (Math.asin(y / Math.hypot(x, y)) * 180 / Math.PI);
    }

    /**
     * 根据当前位置计算象限
     *
     * @param x
     * @param y
     * @return
     */
    private int getQuadrant(float x, float y) {
        int tmpX = (int) (x - mRadius / 2);
        int tmpY = (int) (y - mRadius / 2);
        if (tmpX >= 0) {
            return tmpY >= 0 ? 4 : 1;
        } else {
            return tmpY >= 0 ? 3 : 2;
        }

    }

    /**
     * 设置菜单条目的图标和文本
     *
     * @param resIds
     */
    public void setMenuItemIconsAndTexts(int[] resIds, String[] texts) {
        mItemImgs = resIds;
        mItemTexts = texts;

        // 参数检查
        if (resIds == null && texts == null) {
            throw new IllegalArgumentException("菜单项文本和图片至少设置其一");
        }

        // 初始化mMenuCount
        mMenuItemCount = resIds == null ? texts.length : resIds.length;

        if (resIds != null && texts != null) {
            mMenuItemCount = Math.min(resIds.length, texts.length);
        }

        addMenuItems();

    }

    /**
     * 设置MenuItem的布局文件，必须在setMenuItemIconsAndTexts之前调用
     *
     * @param mMenuItemLayoutId
     */
    public void setMenuItemLayoutId(int mMenuItemLayoutId) {
        this.mMenuItemLayoutId = mMenuItemLayoutId;
    }

    /**
     * 添加菜单项
     */
    private void addMenuItems() {
        LayoutInflater mInflater = LayoutInflater.from(getContext());

        /**
         * 根据用户设置的参数，初始化view
         */
        for (int i = 0; i < mMenuItemCount; i++) {
            final int j = i;
            View view = mInflater.inflate(mMenuItemLayoutId, this, false);
            ImageView iv = (ImageView) view.findViewById(R.id.id_circle_menu_item_image);
            TextView tv = (TextView) view.findViewById(R.id.id_circle_menu_item_text);

            if (iv != null) {
                iv.setVisibility(View.VISIBLE);
                iv.setImageResource(mItemImgs[i]);
                iv.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (mOnMenuItemClickListener != null) {
                            mOnMenuItemClickListener.itemClick(v, j);
                        }
                    }
                });
            }
            if (tv != null) {
                tv.setVisibility(View.VISIBLE);
                tv.setText(mItemTexts[i]);
            }

            // 添加view到容器中
            addView(view);
        }
    }

    /**
     * 如果每秒旋转角度到达该值，则认为是自动滚动
     *
     * @param mFlingableValue
     */
    public void setFlingableValue(int mFlingableValue) {
        this.mFlingableValue = mFlingableValue;
    }

    /**
     * 设置内边距的比例
     *
     * @param mPadding
     */
    public void setPadding(float mPadding) {
        this.mPadding = mPadding;
    }


    /**
     * 获得默认该layout的尺寸
     *
     * @return
     */
    private int getDefaultWidth() {
        WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);
        return Math.min(outMetrics.widthPixels, outMetrics.heightPixels);
    }


    /**
     * 设置MenuItem的点击事件接口
     *
     * @param listener
     */
    public void setOnMenuItemClickListener(OnMenuItemClickListener listener) {
        this.mOnMenuItemClickListener = listener;
    }

    /**
     * MenuItem的点击事件接口
     */
    public interface OnMenuItemClickListener {
        void itemClick(View view, int pos);

        void itemCenterClick(View view);
    }


    /**
     * 自动滚动的任务
     *
     * @author zhy
     */
    private class AutoFlingRunnable implements Runnable {

        private float angelPerSecond;

        public AutoFlingRunnable(float velocity) {
            this.angelPerSecond = velocity;
        }

        public void run() {
            if (!isFling) {
                return;
            }
            // 如果小于20,则停止
            int perAngle = 360 / (getChildCount() - 1);
            if ((int) Math.abs(angelPerSecond) < 20) {
                isFling = false;
                if (mTargetAngle != -1 && mStartAngle != mTargetAngle) {
                    mStartAngle = mTargetAngle;
                    postDelayed(this, 30);
                    // 重新布局
                    requestLayout();
                } else if (mTargetAngle == -1) {//手动滑的情况
                    int minIndex = (int) (mStartAngle / perAngle);
                    int maxIndex = minIndex + 1;
                    if (mStartAngle % (perAngle) - perAngle > perAngle / 2) {
                        mStartAngle = maxIndex * perAngle;
                        mSelectIndex = (int) ((getChildCount() - 1) - ((mInitSelectIndex + mStartAngle / perAngle) % (getChildCount() - 1)));
                    } else {
                        mStartAngle = minIndex * perAngle;
                        mSelectIndex = (int) ((getChildCount() - 1) - ((mInitSelectIndex + mStartAngle / perAngle) % (getChildCount() - 1)));
                    }
                    Log.e("xxxxxxx", " mSelectIndex111 " + mSelectIndex);
                    postDelayed(this, 30);
                    // 重新布局
                    requestLayout();
                }
                return;
            }
            isFling = true;
            // 不断改变mStartAngle，让其滚动，/30为了避免滚动太快
            mStartAngle += (angelPerSecond / 30);
            // 逐渐减小这个值
            angelPerSecond /= 1.0666F;

            if (mTargetAngle != -1 && angelPerSecond > 0 && mStartAngle > mTargetAngle) {
                mStartAngle = mTargetAngle;
                isFling = false;
            } else if (mTargetAngle != -1 && angelPerSecond < 0 && mStartAngle < mTargetAngle) {
                mStartAngle = mTargetAngle;
                isFling = false;
            }

            int selectIndex = (int) ((getChildCount() - 1) - ((mInitSelectIndex + mStartAngle / perAngle) % (getChildCount() - 1)));
            selectIndex %= getChildCount() - 1;
            if (((int) mStartAngle / perAngle) != mLastMultiple && mSelectIndex != selectIndex) {
                UECircleMenuLayout.this.mSelectIndex = selectIndex;
                Log.e("xxxxxxx", " mSelectIndex " + UECircleMenuLayout.this.mSelectIndex);
            }
            mLastMultiple = (int) (mStartAngle / perAngle);
            postDelayed(this, 30);
            // 重新布局
            requestLayout();
        }
    }

    private static class AutoPlayTask implements Runnable {
        private final WeakReference<UECircleMenuLayout> mCircleMenuLayout;

        private AutoPlayTask(UECircleMenuLayout layout) {
            mCircleMenuLayout = new WeakReference<>(layout);
        }

        @Override
        public void run() {
            UECircleMenuLayout circleMenu = mCircleMenuLayout.get();
            if (circleMenu != null) {
                circleMenu.switchToNextItem();
                circleMenu.startAutoPlay();
            }
        }
    }

    public class Position {

        public int index;
        public double angle;
        public float scale;
        public float alpha;

        public Position(int index, double angle) {
            this.index = index;
            this.angle = angle;
        }
    }

}
