package com.shapun.screentranslater.util;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.util.TypedValue;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Utils {

    public static void toast(Context context, Object obj) {
        Toast.makeText(context, String.valueOf(obj), Toast.LENGTH_SHORT).show();
    }

    public static void copyToClipboard(Context context, CharSequence str) {
        ((ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE))
                .setPrimaryClip(ClipData.newPlainText("clipboard", str.toString()));
    }

    public static int dpToPx(Context context, int input) {
        return (int)
                TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,
                        input,
                        context.getResources().getDisplayMetrics());
    }
    public void closeNotificationPanel(Context context){
        try {
            @SuppressLint("WrongConstant")
            Object sbservice = context.getSystemService("statusbar");
            Class<?> statusbarManager;
            statusbarManager = Class.forName("android.app.StatusBarManager");
            Method showsb;
            if (Build.VERSION.SDK_INT >= 17) {
                showsb = statusbarManager.getMethod("collapsePanels");
            } else {
                showsb = statusbarManager.getMethod("collapse");
            }
            showsb.invoke(sbservice);
        } catch (ClassNotFoundException | IllegalArgumentException | IllegalAccessException _e) {
            _e.printStackTrace();
        } catch (NoSuchMethodException _e) {
            _e.printStackTrace();
        } catch (InvocationTargetException _e) {
            _e.printStackTrace();
        }
    }
	public static void showSnackbar(View v,CharSequence text){
		Snackbar.make(v,text,Snackbar.LENGTH_SHORT).show();
	}
}
