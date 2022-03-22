package com.shapun.screentranslater.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.res.Configuration
import android.graphics.PixelFormat
import android.graphics.Rect
import android.os.Build
import android.util.Log
import android.view.*
import android.view.View.OnTouchListener
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.graphics.ColorUtils
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.shapun.screentranslater.R
import com.shapun.screentranslater.activity.TranslateTextActivity.Companion.newIntent
import com.shapun.screentranslater.adapter.SelectedTextsAdapter
import com.shapun.screentranslater.databinding.DialogSelectedTextsBinding
import com.shapun.screentranslater.preferences.Preferences
import com.shapun.screentranslater.util.AccessibilityServiceUtils
import com.shapun.screentranslater.util.UiUtils
import com.shapun.screentranslater.util.Utils

class ScreenTranslateService : AccessibilityService(), OnTouchListener,
    GestureDetector.OnGestureListener {
    //These Variables must be set to null after use
    private var mWindowManager: WindowManager? = null
    //private var mContainerParams: WindowManager.LayoutParams? = null
    private var mGestureDetector: GestureDetector? = null
    private var binding: DialogSelectedTextsBinding? = null
    private var mSelectedTexts: ArrayList<String>? = null
    private var mSelectedNodes: ArrayList<AccessibilityNodeInfo>? = null
    private var mSelectedTextAdapter: SelectedTextsAdapter? = null
    private var mSelectedWindowId = 0

    override fun onServiceConnected() {
        Log.i(TAG, "Service Connected")
        setTheme(R.style.AppTheme)
        serviceInfo = AccessibilityServiceInfo()
        showNotification()
    }

    override fun onSingleTapUp(ev: MotionEvent): Boolean {
        mSelectedWindowId = rootInActiveWindow.windowId
        val list = getTexts(rootInActiveWindow, ev.rawX.toInt(), ev.rawY.toInt())
        if (list.size == 0) return false
        val node = list[list.size - 1]
        if (mSelectedNodes!!.contains(node)) {
            removeNode(node)
        } else {
            addNode(node)
        }
        return true
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (mSelectedWindowId != 0) {
            stopTranslateService()
            startTranslateService()
        }
    }

    @SuppressLint("LaunchActivityFromNotification")
    private fun showNotification() {
        val channelName = "main"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Translate Service "
            val description = "This is primary notification used to display translate options"
            val importance = NotificationManager.IMPORTANCE_MIN
            val channel = NotificationChannel(channelName, name, importance)
            channel.description = description
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
        val intent = Intent(this, ScreenTranslateService::class.java)
        intent.action = ACTION_START_TRANSLATE_MODE
        val color = UiUtils.getColorPrimary(this)
        val serviceIntent = PendingIntent.getService(
            this,
            0,
            intent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
        )
        val builder = NotificationCompat.Builder(this, channelName)
            .setContentTitle(getString(R.string.app_name))
            .setContentText("Tap to translate or copy text on screen")
            .setContentIntent(serviceIntent)
            .setSmallIcon(R.drawable.default_image)
            .setOngoing(true)
            .setColor(color)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setAutoCancel(false)
        NotificationManagerCompat.from(this).notify(NOTIFICATION_ID, builder.build())
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        when (intent.action) {
            ACTION_START_TRANSLATE_MODE -> startTranslateService()
            ACTION_HIDE_NOTIFICATION ->
                (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).cancel(NOTIFICATION_ID)
            ACTION_SHOW_NOTIFICATION -> {
                setTheme(Preferences.getTheme(this))
                showNotification()
            }
            else -> {}
        }
        return START_STICKY
    }

    @SuppressLint("MissingPermission")
    private fun startTranslateService() {
        mSelectedTexts = ArrayList()
        mSelectedNodes = ArrayList()
        setTheme(Preferences.getTheme(this))
        mWindowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        setUpContainer()
        setUpBottomSheet()
        setUpRecyclerView()
        binding!!.markableView.alpha = 0f
        binding!!.markableView.animate().alpha(1f).duration = 1000
        val info = AccessibilityServiceInfo()
        info.eventTypes = AccessibilityEvent.TYPE_WINDOWS_CHANGED or
                AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or
                AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_ALL_MASK
        serviceInfo = info
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setUpBottomSheet() {
        val mContainerParams = WindowManager.LayoutParams()
        mContainerParams.type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY
        mContainerParams.format = PixelFormat.TRANSLUCENT
        mContainerParams.flags =
            mContainerParams.flags or (WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                    or WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        mContainerParams.width = WindowManager.LayoutParams.MATCH_PARENT
        mContainerParams.height = WindowManager.LayoutParams.MATCH_PARENT
        binding = DialogSelectedTextsBinding.inflate(LayoutInflater.from(this))
        binding!!.markableView.highlightedColor =
            ColorUtils.setAlphaComponent(binding!!.markableView.highlightedColor, 255 / 4)
        binding!!.markableView.setBackgroundColor(0)
        binding!!.markableView.setOnTouchListener(this)
        mGestureDetector = GestureDetector(this, this)
        try {
            mWindowManager!!.addView(binding!!.markableView, mContainerParams)
        } catch (e: Exception) {
            val permissionGiven = AccessibilityServiceUtils.isAccessibilityServiceEnabled(
                this, ScreenTranslateService::class.java
            )
            Utils.toast(this, windows.toString())
            Log.e(TAG, e.toString())
            Log.i(TAG, permissionGiven.toString())
        }

        BottomSheetBehavior.from(binding!!.sheetContent).state = BottomSheetBehavior.STATE_COLLAPSED
        binding!!.imgClose.setOnClickListener { stopTranslateService() }
        binding!!.imgCopy.setOnClickListener {
            if (mSelectedTexts!!.isEmpty()) {
                Utils.showSnackbar(binding!!.markableView,"No texts were selected . Click on texts you want copy")
                return@setOnClickListener
            }
            val sb = StringBuilder()
            mSelectedTexts!!.forEach { sb.append(it + "\n") }
            Utils.copyToClipboard(this, sb)
            Utils.showSnackbar(binding!!.markableView, "Copied to clipboard")
        }
        binding!!.imgTranslate.setOnClickListener {
            if (mSelectedTexts!!.isEmpty()) {
                Utils.showSnackbar(binding!!.markableView,"No texts were selected . Click on texts you want translate")
                return@setOnClickListener
            }else {
                val intent = newIntent(this, mSelectedTexts!!)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                stopTranslateService()
            }
        }
        binding!!.imgSelectAll.setOnClickListener {
            val list: List<AccessibilityNodeInfo> = ArrayList()
            AccessibilityServiceUtils.getAllNodesWithText(this, rootInActiveWindow)
            for (info in list) {
                addNode(info)
            }
        }
    }

    private fun setUpRecyclerView() {
        val rv = binding!!.recyclerview
        rv.layoutManager = LinearLayoutManager(this)
        mSelectedTextAdapter = SelectedTextsAdapter(mSelectedTexts)
        rv.adapter = mSelectedTextAdapter
        mSelectedTextAdapter!!.setOnCloseClickListener { pos: Int -> removeNode(mSelectedNodes!![pos]) }
    }

    private fun setUpContainer() {


    }

    private fun addNode(accessibilityNodeInfo: AccessibilityNodeInfo) {
        if (mSelectedNodes!!.contains(accessibilityNodeInfo)) return
        mSelectedNodes!!.add(accessibilityNodeInfo)
        val rect = Rect()
        accessibilityNodeInfo.getBoundsInScreen(rect)
        binding!!.markableView.addRect(rect)
        mSelectedTexts!!.add(accessibilityNodeInfo.text.toString())
        mSelectedTextAdapter!!.notifyItemInserted(mSelectedTexts!!.size - 1)
        refreshSelectedCount()
    }

    private fun removeNode(node: AccessibilityNodeInfo) {
        val index = mSelectedNodes!!.indexOf(node)
        if (index != -1) {
            val rect = Rect()
            node.getBoundsInScreen(rect)
            binding!!.markableView.removeRect(rect)
            mSelectedNodes!!.removeAt(index)
            mSelectedTexts!!.removeAt(index)
            mSelectedTextAdapter!!.notifyItemRemoved(index)
            mSelectedTextAdapter!!.notifyItemRangeChanged(index, mSelectedTextAdapter!!.itemCount)
            refreshSelectedCount()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun refreshSelectedCount() {
        val prefix = if (mSelectedTexts!!.size == 0) "No text" else mSelectedTexts!!.size.toString()
        binding!!.selectedTextCount.text = "$prefix  selected"
    }

    private fun getTexts(node: AccessibilityNodeInfo?, x: Int, y: Int): MutableList<AccessibilityNodeInfo> {
        val list : MutableList<AccessibilityNodeInfo> = mutableListOf()
        if (node == null) return list
        if (node.text != null) {
            val rect = Rect()
            node.getBoundsInScreen(rect)
            if (rect.contains(x, y)) {
                list.add(node)
            }
        }
        for (i in 0 until node.childCount) {
            val child = node.getChild(i)
            list.addAll(getTexts(child, x, y))
        }
        return list
    }

    private fun stopTranslateService() {
        mWindowManager!!.removeView(binding!!.markableView)
        serviceInfo = AccessibilityServiceInfo()
        mSelectedWindowId = 0
        mWindowManager = null
        binding = null
        mGestureDetector = null
        mSelectedTexts = null
        mSelectedNodes = null
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
            if (mSelectedWindowId != 0) {
                // Window changed so stop the service
                if (mSelectedWindowId != event.windowId) {
                    stopTranslateService()
                }
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).cancel(NOTIFICATION_ID)
        Log.v(TAG, "Service Destroyed")
        Utils.toast(this, "destroyed")
    }

    override fun onInterrupt() {
        Log.v(TAG, "onInterrupt")
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(v: View, e: MotionEvent): Boolean {
        return mGestureDetector!!.onTouchEvent(e)
    }

    override fun onLongPress(e: MotionEvent) {}
    override fun onScroll(e1: MotionEvent, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean = false
    override fun onFling(e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean = false
    override fun onShowPress(e: MotionEvent) {}
    override fun onDown(e: MotionEvent): Boolean = false

    companion object {
        private const val TAG = "ScreenTranslateService"
        const val NOTIFICATION_ID = 100101
        const val ACTION_HIDE_NOTIFICATION = "hide_notification"
        const val ACTION_SHOW_NOTIFICATION = "show_notification"
        const val ACTION_START_TRANSLATE_MODE = "start_translate_mode"
    }
}