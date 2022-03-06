package com.shapun.screentranslater.widget;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.widget.FrameLayout;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.core.graphics.ColorUtils;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

public class MarkableView extends FrameLayout {

    private Set<Rect> mRects;
    private Paint mPaint;
    private int mHighilightedColor;
    private Paint mBorderPaint;
    private int mBorderColor;
    private Rect mBorderRect;

    public MarkableView(Context context) {
        super(context);
        init();
    }

    private void init() {
        mRects = new HashSet<>();
        mHighilightedColor = 0xff000000;
        mPaint = new Paint();
        mPaint.setColor(mHighilightedColor);
        mBorderPaint = new Paint();
        mBorderPaint.setStyle(Paint.Style.STROKE);
		mBorderRect = new Rect();
        ValueAnimator valueAnim = ValueAnimator.ofInt(255 / 2, 255);
        valueAnim.setDuration(800);
        valueAnim.setRepeatCount(ValueAnimator.INFINITE);
        valueAnim.setRepeatMode(ValueAnimator.REVERSE);
        valueAnim.addUpdateListener(
                animation -> {
                    mBorderPaint.setColor(
                            ColorUtils.setAlphaComponent(
                                    mBorderColor, (int) animation.getAnimatedValue()));
                    invalidate();
                });
        valueAnim.start();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (Rect rect : mRects) {
            canvas.drawRect(rect, mPaint);
        }
		getLocalVisibleRect(mBorderRect);
		canvas.drawRect(mBorderRect,mBorderPaint);
    }

    public void setBorderColor(@ColorInt int color) {
        mBorderColor = color;
        mBorderPaint.setColor(mBorderColor);
        invalidate();
    }

    public void setBorderWidth(int width) {
        // *2 is tempory fix
        mBorderPaint.setStrokeWidth(width * 2);
        invalidate();
    }

    public void setHighilightColor(@ColorInt int color) {
        mHighilightedColor = color;
        mPaint.setColor(mHighilightedColor);
    }

    @ColorInt
    public int getHighilightedColor() {
        return mHighilightedColor;
    }

    public void addRect(@NonNull Rect rect) {
        mRects.add(rect);
        invalidate();
    }

    public void removeRect(@NonNull Rect rect) {
        mRects.remove(rect);
        invalidate();
    }
}
