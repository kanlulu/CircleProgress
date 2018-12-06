package com.kanlulu.customview.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import com.kanlulu.customview.R;

/**
 * Created by kanlulu
 * DATE: 2018/12/4 9:17
 */
public class CircleProgress extends View {
    private static final String TAG = "CircleProgress";

    private Paint mPaint;
    private int backCircleWith = 20;//圆环宽度
    private int radius;//半径
    private float currentAngle = 0;
    private float currentPointAngle = 0;
    private float sweepAngle = 0;

    public CircleProgress(Context context) {
        super(context);
        init(context, null);
    }

    public CircleProgress(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public CircleProgress(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, @Nullable AttributeSet attrs) {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);

        if (attrs != null) {
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.CircleProgress);
            sweepAngle = 360 * typedArray.getFloat(R.styleable.CircleProgress_progress, 0) / 100;
            typedArray.recycle();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        radius = (getHeight() - backCircleWith) / 2;//半径
        //绘制背景圆环
        paintBackCircle(canvas);
        //绘制进度
        paintProgress(canvas);
        //绘制圆点
        paintProgressPoint(canvas);
        if (currentAngle != sweepAngle || currentPointAngle != sweepAngle) invalidate();//重新绘制
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }

    /**
     * 背景圆环
     *
     * @param canvas
     */
    private void paintBackCircle(Canvas canvas) {
        mPaint.setColor(getResources().getColor(R.color.circleBackColor));
        mPaint.setStrokeWidth(backCircleWith);
        mPaint.setStyle(Paint.Style.STROKE);
        canvas.drawCircle(getWidth() / 2, getHeight() / 2, radius, mPaint);
    }

    /**
     * 画进度圆环
     *
     * @param canvas
     */
    private void paintProgress(Canvas canvas) {
        mPaint.setColor(getResources().getColor(R.color.circleProgressColor));
        mPaint.setStrokeWidth(backCircleWith);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeCap(Paint.Cap.ROUND);

        RectF rectF = new RectF(backCircleWith / 2, backCircleWith / 2, 2 * radius + backCircleWith / 2, 2 * radius + backCircleWith / 2);
        currentAngle += 5;
        if (currentAngle >= sweepAngle) currentAngle = sweepAngle;
        canvas.drawArc(rectF, 180, currentAngle, false, mPaint);
    }

    /**
     * 画进度前端的圆点
     *
     * @param canvas
     */
    private void paintProgressPoint(Canvas canvas) {
        mPaint.setColor(getResources().getColor(R.color.circlePoint));
        mPaint.setStrokeWidth(backCircleWith - 8);
        mPaint.setStyle(Paint.Style.STROKE);
        if (sweepAngle > 359 || sweepAngle <= 0) return;

        currentPointAngle += 5;
        if (currentPointAngle >= sweepAngle) currentPointAngle = sweepAngle;
        //绘制点的x坐标
        double x = getWidth() / 2 - radius * Math.cos(currentPointAngle * Math.PI / 180);
        //绘制点的y坐标
        double y = getHeight() / 2 - radius * Math.sin(currentPointAngle * Math.PI / 180);
        canvas.drawPoint((float) x, (float) y, mPaint);
    }

    public void setProgress(float creditProgress) {
        currentPointAngle = 0;
        currentAngle = 0;
        sweepAngle = 360 * creditProgress / 100;
        invalidate();
    }
}
