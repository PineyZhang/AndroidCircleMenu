package com.zhy.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathDashPathEffect;
import android.graphics.PathEffect;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import com.zhy.sample_circlemenu.R;

/**
 * Created by zsg on 2020-01-02.
 * Desc:
 * <p>
 * Copyright (c) 2020 UePay.mo All rights reserved.
 */
public class CircleViewIndicator extends View {

    private int mCircleRadius;
    private int mCircleColor;
    private int mCircleBorderType;
    private int mCircleBorderWidth;
    private int mCircleRotateDirection;
    private boolean mCircleAutoPlay;

    private int mSpeed;
    private Paint mPaint;

    public CircleViewIndicator(Context context) {
        this(context, null);
    }

    public CircleViewIndicator(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CircleViewIndicator(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray ta = context.getTheme().obtainStyledAttributes(attrs, R.styleable.CircleViewIndicator, defStyleAttr, 0);
        int n = ta.getIndexCount();
        for (int i = 0; i < n; i++) {
            int attr = ta.getIndex(i);
            switch (attr) {
                case R.styleable.CircleViewIndicator_circleRadius:
                    mCircleRadius = ta.getDimensionPixelSize(attr, dp2px(getContext(), 10));
                    break;
                case R.styleable.CircleViewIndicator_circleColor:
                    mCircleColor = ta.getColor(attr, Color.GREEN);
                    break;
                case R.styleable.CircleViewIndicator_circleAutoPlay:
                    mCircleAutoPlay = ta.getBoolean(attr, false);
                    break;
                case R.styleable.CircleViewIndicator_circleBorderType:
                    mCircleBorderType = ta.getInteger(attr, 0);
                    break;
                case R.styleable.CircleViewIndicator_circleBorderWidth:
                    mCircleBorderWidth = ta.getInteger(attr, 5);
                    break;
                case R.styleable.CircleViewIndicator_circleRotateDirection:
                    mCircleRotateDirection = ta.getInteger(attr, 0);
                    break;
                default:
                    break;
            }
        }
        ta.recycle();
        initView();
    }

    private void initView() {
        mPaint = new Paint();

    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }


    @Override
    protected void onLayout(boolean b, int i, int i1, int i2, int i3) {

    }

    int phase;

    @Override
    protected void onDraw(Canvas canvas) {
        int center = getWidth() / 2;
        int radius = mCircleRadius;
        mPaint.setStrokeWidth(mCircleBorderWidth);
        mPaint.setAntiAlias(true);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(mCircleColor);

        Path path1 = new Path();
        path1.addCircle(0,0,3, Path.Direction.CCW);
        PathEffect pathEffect1 = new PathDashPathEffect(path1,12,phase, PathDashPathEffect.Style.ROTATE);

//        PathEffect dashPathEffect = new DashPathEffect(new float[]{8,8},1);
        mPaint.setPathEffect(pathEffect1);
        canvas.drawCircle(center, center, radius, mPaint);

        mPaint.reset();
        mPaint.setStrokeWidth(mCircleBorderWidth);
        mPaint.setAntiAlias(true);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(mCircleColor);

        Path path = new Path();
        path.addCircle(0,0,3, Path.Direction.CW);
        PathEffect pathEffect = new PathDashPathEffect(path,12,phase, PathDashPathEffect.Style.ROTATE);

//        PathEffect dashPathEffect2 = new DashPathEffect(new float[]{6,8},1);
        mPaint.setPathEffect(pathEffect);
        canvas.drawCircle(center, center, radius + 60, mPaint);

        phase++;
        invalidate();

    }

    public static int dp2px(Context context, float dpValue) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpValue, context.getResources().getDisplayMetrics());
    }

    public static int sp2px(Context context, float spValue) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, spValue, context.getResources().getDisplayMetrics());
    }

}
