package com.shapun.screentranslater.service;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.os.Build;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.color.MaterialColors;
import com.shapun.screentranslater.R;
import com.shapun.screentranslater.activity.TranslateTextActivity;
import com.shapun.screentranslater.adapter.SelectedTextsAdapter;
import com.shapun.screentranslater.preferences.Preferences;
import com.shapun.screentranslater.util.AccessibilityServiceUtils;
import com.shapun.screentranslater.util.Utils;
import com.shapun.screentranslater.widget.MarkableView;

import java.util.ArrayList;
import java.util.List;

public class ScreenTranslateService extends AccessibilityService
        implements View.OnTouchListener, GestureDetector.OnGestureListener {
    private static final String TAG = "ScreenTranslateService";
    public static final int NOTIFICATION_ID = 100101;
    public static final String ACTION_HIDE_NOTIFICATION = "hide_notification";
    public static final String ACTION_SHOW_NOTIFICATION = "show_notification";
	public static final String ACTION_START_TRANSLATE_MODE ="start_translate_mode";
    // Variables must be set to null after use
    private WindowManager mWindowManager;
    private MarkableView mMarkableView;
    private WindowManager.LayoutParams mContainerParams;
    private GestureDetector mGestureDetector;
    private CoordinatorLayout mCoordinatorLayout;
    private TextView mTextViewSelectedCount;
    private RecyclerView mRecyclerViewSelectedText;
    private ArrayList<String> mSelectedTexts;
    private ArrayList<AccessibilityNodeInfo> mSelectedNodes;
    private SelectedTextsAdapter mSelectedTextAdapter;
    private int mSelectedWindowId;

    @Override
    protected void onServiceConnected() {
        Log.i(TAG, "Service Connected");
        setTheme(R.style.AppTheme);
        setServiceInfo(new AccessibilityServiceInfo());
        showNotification();
    }

    @Override
    public boolean onSingleTapUp(MotionEvent ev) {
        mSelectedWindowId = getRootInActiveWindow().getWindowId();
        List<AccessibilityNodeInfo> list = new ArrayList<>();
        getTexts(list, getRootInActiveWindow(), (int) ev.getRawX(), (int) ev.getRawY());
        if (list.size() == 0) return false;
        AccessibilityNodeInfo node = list.get(list.size() - 1);
        if (mSelectedNodes.contains(node)) {
            removeNode(node);
        } else {
            addNode(node);
        }
        return true;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (mSelectedWindowId != 0) {
            stopTranslateService();
            startTranslateService();
        }
    }

    private void showNotification() {
        final String CHANNEL = "main";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Translate Service ";
            String description = "This is primary notification used to display translate options";
            int importance = NotificationManager.IMPORTANCE_MIN;
            NotificationChannel channel = new NotificationChannel(CHANNEL, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
        Intent intent = new Intent(this, ScreenTranslateService.class);
        intent.setAction(ACTION_START_TRANSLATE_MODE);
		int color = MaterialColors.getColor(
                        this,
                        android.R.attr.colorPrimary,
                        ContextCompat.getColor(this,R.color.colorPrimary));
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this, CHANNEL)
                        .setContentTitle(getString(R.string.app_name))
                        .setContentText("Tap to translate or copy text on screen")
                        .setContentIntent(PendingIntent.getService(this, 0, intent, 0))
                        .setSmallIcon(R.drawable.default_image)
                        .setOngoing(true)
                        .setColor(color)
                        .setPriority(NotificationCompat.PRIORITY_LOW)
                        .setAutoCancel(false);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) return super.onStartCommand(intent, flags, startId);
				switch (intent.getAction()) {
                    case ACTION_START_TRANSLATE_MODE:
                        startTranslateService();
                        break;
                    case ACTION_HIDE_NOTIFICATION:
                        ((NotificationManager) getSystemService(NOTIFICATION_SERVICE))
                                .cancel(NOTIFICATION_ID);
                        break;
                    case ACTION_SHOW_NOTIFICATION:
						setTheme(Preferences.getTheme(this));
                        showNotification();
                        break;
                    default:
                        break;
                }
        return START_STICKY;
    }

    private void startTranslateService() {
        mSelectedTexts = new ArrayList<>();
        mSelectedNodes = new ArrayList<>();
		setTheme(Preferences.getTheme(this));
        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
	    setUpContainer();
        setUpBottomSheet();
        setUpRecyclerView();
        Intent it = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        sendBroadcast(it);
		try{
			mWindowManager.addView(mMarkableView, mContainerParams);
        }catch(Exception e){
			boolean permissionGiven = AccessibilityServiceUtils.isAccessibilityServiceEnabled(
                                this, ScreenTranslateService.class);
			Utils.toast(this,String.valueOf(getWindows()));
			Log.e(TAG,e.toString());
			Log.i(TAG,String.valueOf(permissionGiven));
			
		}
		mMarkableView.setAlpha(0);
        mMarkableView.animate().alpha(1).setDuration(1000);
        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        info.eventTypes = AccessibilityEvent.WINDOWS_CHANGE_ACTIVE;
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_ALL_MASK;
        setServiceInfo(info);
    }

    private void setUpBottomSheet() {
        View view =
                LayoutInflater.from(this).inflate(R.layout.dialog_selected_texts, mMarkableView);
        mTextViewSelectedCount = view.findViewById(R.id.selected_text_count);
        View sheetContent = view.findViewById(R.id.sheet_content);
        BottomSheetBehavior.from(sheetContent).setState(BottomSheetBehavior.STATE_COLLAPSED);
        ImageView img_close = view.findViewById(R.id.img_close);

        img_close.setOnClickListener(v -> stopTranslateService());
        view.findViewById(R.id.img_copy)
                .setOnClickListener(
                        v -> {
                            if (mSelectedTexts.isEmpty()) {
                                Utils.showSnackbar(
                                        mMarkableView,
                                        "No texts were selected . Click on texts you want copy");
                                return;
                            }
                            StringBuilder sb = new StringBuilder();
                            for (String str : mSelectedTexts) {
                                sb.append(str);
                                sb.append("\n");
                            }
                            Utils.copyToClipboard(this, sb);
                            Utils.showSnackbar(mMarkableView, "Copied to clipboard");
                        });
        view.findViewById(R.id.img_translate)
                .setOnClickListener(
                        v -> {
                            if (mSelectedTexts.isEmpty()) {
                                Utils.showSnackbar(
                                        mMarkableView,
                                        "No texts were selected . Click on texts you want translate");
                                return;
                            }
                            Intent intent = TranslateTextActivity.newIntent(this, mSelectedTexts);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            stopTranslateService();
                        });

        view.findViewById(R.id.img_select_all)
                .setOnClickListener(
                        v -> {
                            List<AccessibilityNodeInfo> list = new ArrayList<>();
                            AccessibilityServiceUtils.getAllNodesWithText(
                                    list, this, getRootInActiveWindow());
                            for (AccessibilityNodeInfo info : list) {
                                addNode(info);
                            }
                        });
    }

    private void setUpRecyclerView() {
        mRecyclerViewSelectedText = mMarkableView.findViewById(R.id.recyclerview);
        mRecyclerViewSelectedText.setLayoutManager(new LinearLayoutManager(this));
        mSelectedTextAdapter = new SelectedTextsAdapter(mSelectedTexts);
        mRecyclerViewSelectedText.setAdapter(mSelectedTextAdapter);
        mSelectedTextAdapter.setOnCloseClickListener(
                pos -> {
                    removeNode(mSelectedNodes.get(pos));
                });
    }

    private void setUpContainer() {
        mMarkableView = new MarkableView(this);
        mContainerParams = new WindowManager.LayoutParams();
        mContainerParams.type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY;
        mContainerParams.format = PixelFormat.TRANSLUCENT;
		mContainerParams.flags |= WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                        | WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS;
        mContainerParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        mContainerParams.height = WindowManager.LayoutParams.MATCH_PARENT;
        int border = Utils.dpToPx(this, 5);
        int color = MaterialColors.getColor(
                        this,
                        android.R.attr.colorControlNormal,
                ContextCompat.getColor(this,R.color.colorControlNormal));
        mMarkableView.setHighilightColor(ColorUtils.setAlphaComponent(color, 255 / 4));
        mMarkableView.setBackgroundColor(0);
        mMarkableView.setBorderWidth(border);
        mMarkableView.setBorderColor(color);
        mMarkableView.setPadding(border, border, border, border);
        mMarkableView.setOnTouchListener(this);
        mGestureDetector = new GestureDetector(this, this);
    }

    private void addNode(AccessibilityNodeInfo accessibilityNodeInfo) {
        if (mSelectedNodes.contains(accessibilityNodeInfo)) return;
        mSelectedNodes.add(accessibilityNodeInfo);
        Rect rect = new Rect();
        accessibilityNodeInfo.getBoundsInScreen(rect);
        mMarkableView.addRect(rect);
        mSelectedTexts.add(accessibilityNodeInfo.getText().toString());
        mSelectedTextAdapter.notifyItemInserted(mSelectedTexts.size() - 1);
        refreshSelectedCount();
    }

    private void removeNode(AccessibilityNodeInfo node) {
        int index = mSelectedNodes.indexOf(node);
        if (index != -1) {
            Rect rect = new Rect();
            node.getBoundsInScreen(rect);
            mMarkableView.removeRect(rect);
            mSelectedNodes.remove(index);
            mSelectedTexts.remove(index);
            mSelectedTextAdapter.notifyItemRemoved(index);
            mSelectedTextAdapter.notifyItemRangeChanged(index, mSelectedTextAdapter.getItemCount());
            refreshSelectedCount();
        }
    }

    private void refreshSelectedCount() {
        TextView textView = mTextViewSelectedCount;
        String prefix =
                mSelectedTexts.size() == 0 ? "No text" : String.valueOf(mSelectedTexts.size());
        mTextViewSelectedCount.setText(prefix + "  selected");
    }

    @Nullable
    private void getTexts(
            List<AccessibilityNodeInfo> list, AccessibilityNodeInfo node, int x, int y) {
        if (node == null) return;
        if (node.getText() != null) {
            Rect rect = new Rect();
            node.getBoundsInScreen(rect);
            if (rect.contains(x, y)) {
                list.add(node);
            }
        }
        for (int i = 0; i < node.getChildCount(); i++) {
            AccessibilityNodeInfo child = node.getChild(i);
            getTexts(list, child, x, y);
        }
    }

    public void stopTranslateService() {
        mWindowManager.removeView(mMarkableView);
        setServiceInfo(new AccessibilityServiceInfo());
        mSelectedWindowId = 0;
        mWindowManager = null;
        mMarkableView = null;
        mContainerParams = null;
        mGestureDetector = null;
        mCoordinatorLayout = null;
        mTextViewSelectedCount = null;
        mRecyclerViewSelectedText = null;
        mSelectedTexts = null;
        mSelectedNodes = null;
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event != null) {
            if (mSelectedWindowId != 0) {
                // Window changed sostop the service
                if (mSelectedWindowId != event.getWindowId()) {
                    stopTranslateService();
                }
            }
        }
    }

    @Override
    public void onDestroy() {
		super.onDestroy();
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel(NOTIFICATION_ID);
        Log.v(TAG, "Service Destroyed");
		Utils.toast(this,"destroyed");
    }

    @Override
    public void onInterrupt() {
        Log.v(TAG, "onInterrupt");
    }

    @Override
    public boolean onTouch(View v, MotionEvent e) {
        return mGestureDetector.onTouchEvent(e);
    }

    @Override
    public void onLongPress(MotionEvent e) {}

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {}

    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }
}