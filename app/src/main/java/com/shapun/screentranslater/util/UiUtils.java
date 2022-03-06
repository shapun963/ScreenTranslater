package com.shapun.screentranslater.util;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import androidx.annotation.ColorInt;

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
}
