package com.shapun.screentranslater.theme

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.color.MaterialColors
import com.shapun.screentranslater.R
import com.shapun.screentranslater.util.Utils
import com.shapun.screentranslater.widget.ThemeView

class ThemeAdapter(ctx: Context?, private val mData: List<Theme>) :
    RecyclerView.Adapter<ThemeAdapter.ViewHolder>() {
    private var mOnRecyclerViewItemClickListener: OnRecyclerViewItemClickListener? = null
    private var mSelectedThemePos = -1
    private val mColorPrimary: Int = MaterialColors.getColor(
        ctx!!,
        android.R.attr.colorPrimary,
        ContextCompat.getColor(ctx, R.color.colorPrimary)
    )

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val themeView: ThemeView = view.findViewById(R.id.theme_view)
    }

    fun setRecyclerViewItemClickListener(listener: OnRecyclerViewItemClickListener?) {
        mOnRecyclerViewItemClickListener = listener
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val context = viewGroup.context
        val view = LayoutInflater.from(context).inflate(R.layout.row_theme, viewGroup, false)
        view.clipToOutline = true
        val params = RecyclerView.LayoutParams(
            RecyclerView.LayoutParams.WRAP_CONTENT,
            RecyclerView.LayoutParams.WRAP_CONTENT
        )
        val margin = Utils.dpToPx(context, 5)
        params.setMargins(margin, margin, margin, margin)
        view.layoutParams = params
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val themeView = viewHolder.themeView
        val context = themeView.context
        themeView.setPrimaryColor(mData[position].primaryColor)
        themeView.setAccentColor(mData[position].accentColor)
        if (mSelectedThemePos == position) {
            themeView.setStrokeColor(mColorPrimary)
        } else {
            themeView.setStrokeColor(ContextCompat.getColor(context, R.color.colorGrey))
        }
        themeView.setOnClickListener {
            mOnRecyclerViewItemClickListener!!.onRecyclerViewItemClicked(position)
        }
    }

    fun setSelectedTheme(pos: Int) {
        mSelectedThemePos = pos
    }

    override fun getItemCount(): Int {
        return mData.size
    }

    interface OnRecyclerViewItemClickListener {
        fun onRecyclerViewItemClicked(position: Int)
    }

}