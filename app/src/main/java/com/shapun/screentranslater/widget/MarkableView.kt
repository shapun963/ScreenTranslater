package com.shapun.screentranslater.widget

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import com.shapun.screentranslater.R

@SuppressLint("ResourceType")
class MarkableView @JvmOverloads constructor(
    ctx: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : FrameLayout(ctx,attrs,defStyleAttr,defStyleRes) {

    private val mRects: MutableSet<Rect> = HashSet()
    private val mPaint: Paint = Paint()
    private val mBorderPaint = Paint()
    private val mBorderRect = Rect()

    init {
        var strokeColor = ContextCompat.getColor(ctx, android.R.color.darker_gray)
        if (attrs != null) {
            val a = ctx.obtainStyledAttributes(attrs, R.styleable.MarkableView, defStyleAttr, defStyleRes)
            mPaint.color = a.getColor(R.styleable.MarkableView_highlightColor,0xFF000000.toInt())
            mBorderPaint.color = a.getColor(R.styleable.MarkableView_borderColor, 0xFF000000.toInt())
            // *2 is temporary fix
            mBorderPaint.strokeWidth = (a.getDimensionPixelSize(R.styleable.MarkableView_borderWidth,0) * 2.0).toFloat()
            a.recycle()
        }
        mBorderPaint.style = Paint.Style.STROKE
        val valueAnim = ValueAnimator.ofInt(255 / 2, 255)
        valueAnim.duration = 800
        valueAnim.repeatCount = ValueAnimator.INFINITE
        valueAnim.repeatMode = ValueAnimator.REVERSE
        valueAnim.addUpdateListener {
            mBorderPaint.color = ColorUtils.setAlphaComponent(
                mBorderPaint.color, it.animatedValue as Int
            )
            invalidate()
        }
        valueAnim.start()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        for (rect in mRects) {
            canvas.drawRect(rect, mPaint)
        }
        getLocalVisibleRect(mBorderRect)
        canvas.drawRect(mBorderRect, mBorderPaint)
    }

    fun setBorderColor(@ColorInt color: Int) {
        mBorderPaint.color = color
        invalidate()
    }

    fun setBorderWidth(width: Int) {
        // *2 is temporary fix
        mBorderPaint.strokeWidth = (width * 2).toFloat()
        invalidate()
    }

    @get:ColorInt
    var highlightedColor: Int
        get() = mPaint.color
        set(color) {
            mPaint.color = color
        }

    fun addRect(rect: Rect) {
        mRects.add(rect)
        invalidate()
    }

    fun removeRect(rect: Rect) {
        mRects.remove(rect)
        invalidate()
    }
}