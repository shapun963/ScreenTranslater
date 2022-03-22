package com.shapun.screentranslater.util;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import androidx.annotation.ColorInt;
import androidx.core.content.ContextCompat;

import com.google.android.material.color.MaterialColors;
import com.shapun.screentranslater.R;

public final class UiUtils {
    public static Drawable getDrawable(
            @ColorInt int color, int topLeft, int topRight, int bottomRight, int bottomLeft) {
        GradientDrawable gd = new GradientDrawable();
        gd.setColor(color);
        gd.setCornerRadii(
                new float[] {
                    topLeft,
                    topLeft,
                    topRight,
                    topRight,
                    bottomRight,
                    bottomRight,
                    bottomLeft,
                    bottomLeft
                });
        return gd;
    }

    public static Drawable getDrawable(@ColorInt int color, int corner) {
        return getDrawable(color, corner, corner, corner, corner);
    }
	public static Drawable getStrokedDrawable(@ColorInt int color, @ColorInt int strokeColor,int strokeWidth) {
        GradientDrawable gd = new GradientDrawable();
        gd.setColor(color);
		gd.setStroke(strokeWidth,strokeColor);
		return gd;
    }
    @ColorInt
    public static int getColorPrimary(Context ctx){
        return MaterialColors.getColor(
                ctx,
                android.R.attr.colorPrimary,
                ContextCompat.getColor(ctx, R.color.colorPrimary)
        );
    }

    @ColorInt
    public static int getColorControlNormal(Context ctx){
        return MaterialColors.getColor(
                ctx,
                android.R.attr.colorControlNormal,
                ContextCompat.getColor(ctx, R.color.colorControlNormal));
    }
}
