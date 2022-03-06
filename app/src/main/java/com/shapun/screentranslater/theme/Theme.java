package com.shapun.screentranslater.theme;

import android.content.Context;
import android.content.ContextWrapper;
import androidx.annotation.ColorInt;
import androidx.annotation.StyleRes;
import androidx.core.content.ContextCompat;

import com.google.android.material.color.MaterialColors;
import com.shapun.screentranslater.R;

public class Theme {
	
	public final int resId;
	public final int primaryColor;
	public final int accentColor;

	public Theme(Context ctx,@StyleRes int resId){
		this.resId = resId;
		ContextWrapper wrapper = new ContextWrapper(ctx.getApplicationContext());
		wrapper.setTheme(resId);
		this.primaryColor = MaterialColors.getColor(wrapper,android.R.attr.colorPrimary,ContextCompat.getColor(ctx,R.color.colorPrimary));
		this.accentColor = MaterialColors.getColor(wrapper,android.R.attr.colorAccent, ContextCompat.getColor(ctx,R.color.colorAccent));
	}
	@Override
	public boolean equals(Object obj) {
		if(obj == this)return true;
		if(obj instanceof Theme){
            if (((Theme) obj).resId == resId){
                return true;
            }
		}
		return false;
	}
}