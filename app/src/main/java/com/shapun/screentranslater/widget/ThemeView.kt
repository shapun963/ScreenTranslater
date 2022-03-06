package com.shapun.screentranslater.widget

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import com.shapun.screentranslater.R
import com.shapun.screentranslater.util.Utils

class ThemeView @JvmOverloads constructor(
    ctx: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : View(ctx,attrs,defStyleAttr,defStyleRes) {

    private var mStrokeWidth = 0
    private var mCornerRadius = 0
    private var mPrimaryColorCorners = FloatArray(8)
    private var mAccentColorCorners = FloatArray(8)
    private var mStrokeCorners = FloatArray(8)
    private val mPrimaryColorPaint= Paint(Paint.ANTI_ALIAS_FLAG)
    private val mAccentColorPaint= Paint(Paint.ANTI_ALIAS_FLAG)
    private val mStrokePaint= Paint(Paint.ANTI_ALIAS_FLAG)
    private val mStrokeRect = RectF()
    private val mPrimaryColorRect = RectF()
    private val mAccentColorRect = RectF()
    private val mStrokePath =  Path()
    private val mPrimaryColorPath = Path()
    private var mAccentColorPath = Path()


    init {
        mCornerRadius = Utils.dpToPx(ctx, 5)
        mStrokeWidth = 0
        var strokeColor = ContextCompat.getColor(ctx, android.R.color.darker_gray)
        if (attrs != null) {
            val a = ctx.obtainStyledAttributes(
                attrs, R.styleable.ThemeView, defStyleAttr, defStyleRes
            )
            mCornerRadius =
                a.getDimensionPixelSize(R.styleable.ThemeView_cornerRadius, mCornerRadius)
            mStrokeWidth = a.getDimensionPixelSize(R.styleable.ThemeView_strokeWidth, mStrokeWidth)
            strokeColor = a.getColor(R.styleable.ThemeView_borderColor, strokeColor)
            a.recycle()
        }
        mStrokePaint.color = strokeColor
        mStrokePaint.strokeWidth = mStrokeWidth.toFloat()
        mPrimaryColorCorners = floatArrayOf(
            mCornerRadius.toFloat(), mCornerRadius.toFloat(),
            mCornerRadius.toFloat(), mCornerRadius.toFloat(), 0f, 0f, 0f, 0f
        )
        mAccentColorCorners = floatArrayOf(
            0f,
            0f,
            0f,
            0f,
            mCornerRadius.toFloat(),
            mCornerRadius.toFloat(),
            mCornerRadius.toFloat(),
            mCornerRadius.toFloat()
        )
        mStrokeCorners = floatArrayOf(
            mCornerRadius.toFloat(), mCornerRadius.toFloat(),
            mCornerRadius.toFloat(), mCornerRadius.toFloat(),
            mCornerRadius.toFloat(), mCornerRadius.toFloat(),
            mCornerRadius.toFloat(), mCornerRadius
                .toFloat()
        )
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawPath(mStrokePath, mStrokePaint)
        canvas.drawPath(mPrimaryColorPath, mPrimaryColorPaint)
        canvas.drawPath(mAccentColorPath, mAccentColorPaint)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        mStrokePath.reset()
        mStrokeRect.set(0.0f, 0.0f, measuredWidth.toFloat(), measuredHeight.toFloat())
        mStrokePath.addRoundRect(mStrokeRect, mStrokeCorners,Path.Direction.CW)
        mPrimaryColorRect.set(mStrokeWidth.toFloat(),
            mStrokeWidth.toFloat(),
            (measuredWidth - mStrokeWidth).toFloat(),
            (measuredHeight / 2).toFloat())

        mAccentColorRect.set(
            mStrokeWidth.toFloat(),
            (measuredHeight / 2).toFloat(),
            (measuredWidth - mStrokeWidth).toFloat(),
            (measuredHeight - mStrokeWidth).toFloat()
        )
        mPrimaryColorPath.addRoundRect(
            mPrimaryColorRect,
            mPrimaryColorCorners,
            Path.Direction.CW
        )

        mAccentColorPath.addRoundRect(
            mAccentColorRect,
            mAccentColorCorners,
            Path.Direction.CW
        )
    }

    fun setPrimaryColor(@ColorInt color: Int) {
        mPrimaryColorPaint.color = color
        invalidate()
    }

    fun setStrokeColor(@ColorInt color: Int) {
        mStrokePaint.color = color
        invalidate()
    }

    fun setAccentColor(@ColorInt color: Int) {
        mAccentColorPaint.color = color
        invalidate()
    }
}