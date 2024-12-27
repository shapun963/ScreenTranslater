package com.shapun.screentranslater.widget

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.os.Build
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.widget.FrameLayout
import androidx.annotation.ColorInt
import androidx.core.graphics.ColorUtils
import com.shapun.screentranslater.R
import com.shapun.screentranslater.util.Utils
import kotlin.math.absoluteValue


@SuppressLint("ResourceType")
class MarkableView @JvmOverloads constructor(
    ctx: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : FrameLayout(ctx,attrs,defStyleAttr,defStyleRes) {

    private var mMinTextSize: Float
    private var mMaxTextSize: Float
    private var mTextColor: Int = Color.BLACK
    private val mRects: MutableSet<Rect> = HashSet()
    private val mOverlayTextList: ArrayList<OverlayText> = ArrayList()
    private val mPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mTextBgPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mBorderRect = Rect()

    init {
        setBackgroundColor(0)
        if (attrs != null) {
            val a = ctx.obtainStyledAttributes(attrs, R.styleable.MarkableView, defStyleAttr, defStyleRes)
            mPaint.color = a.getColor(R.styleable.MarkableView_highlightColor,0xFF000000.toInt())
            mBorderPaint.color = a.getColor(R.styleable.MarkableView_borderColor, 0xFF000000.toInt())
            // *2 is temporary fix
            mBorderPaint.strokeWidth = (a.getDimensionPixelSize(R.styleable.MarkableView_borderWidth,0) * 2.0).toFloat()
            a.recycle()
        }
        mBorderPaint.style = Paint.Style.STROKE
        mTextBgPaint.color = Color.WHITE
        mMinTextSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 8f, context.resources.displayMetrics)
        mMaxTextSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 30f, context.resources.displayMetrics)
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
        mOverlayTextList.forEach {
            canvas.drawRect(it.rect,mTextBgPaint)
            canvas.save()
            //val padding = if(it.paddingPossible) mDefaultPadding else 0
            //ToDo get minimum width needed to draw text
            val padHorizontal = (it.rect.width() - it.textWidth).absoluteValue/2
            val padVertical = (it.rect.height() - it.staticLayout.height).absoluteValue/2
            //Toast.makeText(context, padVertical.toString()+" "+padHorizontal.toString(), Toast.LENGTH_SHORT).show()
            canvas.translate((it.rect.left+padHorizontal), (it.rect.top+padVertical).toFloat())
            it.staticLayout.paint.color = mTextColor
            it.staticLayout.draw(canvas)
            canvas.restore()
        }
    }

    @Suppress("unused")
    fun setBorderColor(@ColorInt color: Int) {
        mBorderPaint.color = color
        invalidate()
    }

    @Suppress("unused")
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
    fun setMaximumTextSize(size: Float){
        mMaxTextSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, size, context.resources.displayMetrics)
    }
    fun removeRect(rect: Rect) {
        mRects.remove(rect)
        invalidate()
    }

    @Suppress("unused")
    fun setTextColor(@ColorInt color:Int){
        mTextColor = color
        invalidate()
    }

    @Suppress("unused")
    fun setMinTextSize(size: Float){
        mMinTextSize = size
    }

    @Suppress("unused")
    fun setTextBgColor(@ColorInt color: Int){
        mTextBgPaint.color = color
        invalidate()
    }

    fun addText(rect: Rect,text: CharSequence){
        mOverlayTextList.add(OverlayText(rect,text))
        invalidate()
    }

    @Suppress("unused")
    fun removeText(rect: Rect){
        mOverlayTextList.forEachIndexed { index, overlayText ->
            if(overlayText.rect == rect) mOverlayTextList.removeAt(index)
        }
        invalidate()
    }
    fun updateText(rect: Rect,text: String){
        mOverlayTextList.forEach{
            if(it.rect == rect) it.updateText(text)
        }
        invalidate()
    }
    fun clearAllTexts(){
        mOverlayTextList.clear()
        invalidate()
    }
    private  inner class OverlayText(val rect: Rect, var text: CharSequence) {
        var textWidth: Float = rect.width().toFloat()
        lateinit var staticLayout: StaticLayout
        val textPaint: TextPaint = TextPaint(TextPaint.ANTI_ALIAS_FLAG)
        private val mDefaultPadding: Int

        init {
            mDefaultPadding = Utils.dpToPx(context,3)
            textPaint.textSize = mMinTextSize
            textPaint.color = Color.BLACK
            calculate()
        }

        fun updateText(_text: CharSequence) {
            text = _text
            calculate()
        }

        private fun calculate() {
            var padding = mDefaultPadding * 2
            var width = rect.width().absoluteValue
            var height = rect.height().absoluteValue
            //Add padding only if there is enough room,check if height is enough,width is enough
            // and finally if there is enough room available for smallest text size
            if(width-padding > 0 && getStaticLayout(width-padding,mMinTextSize).height < height-padding ){
                width -= padding
                height -= padding
            }else{
                padding = 0
            }
            // If we cant fit text with minimum text size , append an ellipsis
            if (text.isNotEmpty() && getStaticLayout(width, mMinTextSize).height > height) {
                try {
                val ellipseText = "..."
                // Draw using a static layout
                // use a copy of TextPaint for measuring
                val paint = TextPaint(textPaint)
                val layout = getStaticLayout(text,paint)
                    // Check that we have a least one line of rendered text
                if (layout.lineCount > 0) {
                    // Since the line at the specific vertical position would be cut off,
                    // we must trim up to the previous line
                    val lastLine = layout.getLineForVertical(height) - 1
                    // If the text would not even fit on a single line, clear it
                    if (lastLine < 0) {
                        text = ""
                    }
                    // Otherwise, trim to the previous line and add an ellipsis
                    else {
                        val start = layout.getLineStart(lastLine)
                        var end = layout.getLineEnd(lastLine)
                        var lineWidth = layout.getLineWidth(lastLine)
                        val ellipseWidth = textPaint.measureText(ellipseText)
                        // Trim characters off until we have enough room to draw the ellipsis
                        while (width < lineWidth + ellipseWidth) {
                            lineWidth =
                                textPaint.measureText(text.subSequence(start, --end + 1).toString())
                        }

                            text = text.subSequence(0, end).toString() + ellipseText

                    }
                }
                }catch (e: Exception){
                    //If for any reason an exception occurs we will ignore it
                    Log.e(javaClass.name,text.toString()+e.toString())
                }
            }
            var size = mMinTextSize - 1
            @Suppress("ControlFlowWithEmptyBody")
            while (getStaticLayout(width, ++size).height <= height && size < mMaxTextSize+1);
            staticLayout = getStaticLayout(width, --size)
            textWidth = (textPaint.measureText(text.toString()) - padding).coerceAtMost(width.toFloat())
        }

        @Suppress("DEPRECATION")
        private fun getStaticLayout(width: Int, textSize: Float): StaticLayout {
            textPaint.textSize = textSize
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                StaticLayout.Builder.obtain(text, 0, text.length, textPaint, width).build()
            } else {
                StaticLayout(text,textPaint,width,Layout.Alignment.ALIGN_NORMAL,1.0f,0.0f,true)
            }
        }

        @Suppress("DEPRECATION")
        private fun getStaticLayout(text: CharSequence, textPaint: TextPaint): StaticLayout {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                 StaticLayout.Builder.obtain(text, 0, text.length, textPaint, width).build()
            } else {
                 StaticLayout(text,textPaint,width,Layout.Alignment.ALIGN_NORMAL,1.0f,0.0f,true)
            }
        }
    }
}