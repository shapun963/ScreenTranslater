package com.shapun.screentranslater.util;

import android.accessibilityservice.AccessibilityService;
import android.content.Context;
import android.graphics.Rect;
import android.provider.Settings;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.ArrayList;
import java.util.List;

public class AccessibilityServiceUtils {

	public static List<AccessibilityNodeInfo> getNodesWithText(AccessibilityNodeInfo node) {
	    List<AccessibilityNodeInfo> list  = new ArrayList<>();
        if (node.getText() != null) list.add(node);
        for (int i = 0; i < node.getChildCount(); i++) {
            list.addAll(getNodesWithText(node.getChild(i)));
        }
        return  list;
    }
	public static boolean isAccessibilityServiceEnabled(
            Context context, Class<? extends AccessibilityService> clz) {

        String prefString =
                Settings.Secure.getString(
                        context.getContentResolver(),
                        Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);

        return prefString != null && prefString.contains(clz.getName());
    }

    public static List<AccessibilityNodeInfo> getNodesWithText(AccessibilityNodeInfo node,int x,int y)  {
	    List<AccessibilityNodeInfo> list  = new ArrayList<>();
        if (node == null) return list;
        if (node.getText() != null) {
            Rect rect = new Rect();
            node.getBoundsInScreen(rect);
            if (rect.contains(x, y)) {
                list.add(node);
            }
        }
        for (int i =0;i<node.getChildCount();i++) {
            var child = node.getChild(i);
            list.addAll(getNodesWithText(child, x, y));
        }
        return list;
    }


}