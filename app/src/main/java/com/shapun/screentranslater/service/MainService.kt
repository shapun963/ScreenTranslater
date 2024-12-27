package com.shapun.screentranslater.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.annotation.SuppressLint
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.ServiceInfo
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.ImageFormat
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.media.ImageReader
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.util.Log
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.widget.ImageView
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.shapun.screentranslater.R
import com.shapun.screentranslater.preferences.Preferences
import com.shapun.screentranslater.util.NotificationUtils
import com.shapun.screentranslater.util.UiUtils
import com.shapun.screentranslater.util.Utils

class MainService : AccessibilityService() {
    //These Variables must be set to null after use
    private val mConfigChangeListeners:ArrayList<(Configuration)-> Unit> = ArrayList()
    private val mAccessibilityEventListeners:ArrayList<(AccessibilityEvent)-> Unit> = ArrayList()

    override fun onServiceConnected() {
        Log.i(TAG, "Service Connected")
        serviceInfo = AccessibilityServiceInfo()
        setTheme(R.style.AppTheme)
        showNotification()
    }


    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        mConfigChangeListeners.forEach{ it(newConfig) }
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
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
        val intent = Intent(this, MainService::class.java)
        intent.action = ACTION_START
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
        val translateIntent = Intent(this, MainService::class.java)
        translateIntent.action = ACTION_START_TRANSLATE_MODE
        val translateServiceIntent = PendingIntent.getService(
            this,
            0,
            translateIntent,
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
            .addAction(0,"Translate",translateServiceIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setAutoCancel(false)
        NotificationManagerCompat.from(this).notify(NOTIFICATION_ID, builder.build())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START->{
                closeNotificationPanel()
                CopyTextService(this).start()
            }
            ACTION_START_TRANSLATE_MODE ->{
                closeNotificationPanel()
                TranslateScreenService(this).start()
            }
            ACTION_HIDE_NOTIFICATION ->
                (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).cancel(NOTIFICATION_ID)
            ACTION_SHOW_NOTIFICATION -> {
                setTheme(Preferences.getTheme(this))
                showNotification()
            }
            ACTION_START_OCR_MODE->{
                val notif = NotificationUtils.getNotification(this)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    startForeground(notif.first,notif.second, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION)
                }else{
                    startForeground(notif.first,notif.second)
                }

                    val mediaProjectionManager =
                        getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
                    val mediaProjection = mediaProjectionManager.getMediaProjection(
                        Activity.RESULT_OK,
                        intent.getParcelableExtra<Intent>("intent")!!
                    )
                    val metrics = resources.displayMetrics
                    val imageReader = ImageReader.newInstance(
                        metrics.widthPixels,
                        metrics.heightPixels,
                        ImageFormat.JPEG,
                        5
                    )
                val containerParams = WindowManager.LayoutParams(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT
                )
                imageReader.setOnImageAvailableListener({
                    it.acquireLatestImage().use { image ->
                        val planes = image.planes
                        val buffer = planes[0].buffer
                        val pixelStride = planes[0].pixelStride
                        val rowStride = planes[0].rowStride
                        val rowPadding = rowStride - pixelStride * metrics.widthPixels
                        val bitmap = Bitmap.createBitmap(
                            metrics.widthPixels+rowPadding/pixelStride,
                            metrics.heightPixels,
                            Bitmap.Config.ARGB_8888
                        )
                        bitmap.copyPixelsFromBuffer(buffer)
                        Log.e("jhgj",bitmap.toString())
                        windowManager.addView(
                            ImageView(this@MainService).also { img -> img.setImageBitmap(bitmap) },
                            containerParams
                        )
                    }
                }, null)
                    val virtualDisplay = mediaProjection.createVirtualDisplay(
                        "disp",
                        metrics.widthPixels,
                        metrics.heightPixels,
                        metrics.densityDpi,
                        DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR or DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC,
                        imageReader.surface,
                        null,
                        null
                    )

            }
            else -> {}
        }
        return START_STICKY
    }


    fun disablePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            disableSelf()
        }
    }



    @SuppressLint("MissingPermission")
    fun closeNotificationPanel() {
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.S){
            performGlobalAction(GLOBAL_ACTION_DISMISS_NOTIFICATION_SHADE)
        }else{
            @Suppress("DEPRECATION")
            sendBroadcast(Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS))
        }
    }

    fun addAccessibilityEventListener(block: ((AccessibilityEvent)->Unit)?){
        if(block!=null) {
            mAccessibilityEventListeners.add(block)
        }
    }
    fun removeAccessibilityEventListener(block: ((AccessibilityEvent)->Unit)?){
        if(block!=null) {
            mAccessibilityEventListeners.remove(block)
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        Log.i(TAG,event.toString())
        mAccessibilityEventListeners.forEach { it(event) }
    }

    override fun onDestroy() {
        super.onDestroy()
        // (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).cancel(NOTIFICATION_ID)
        Log.v(TAG, "Service Destroyed")
        Utils.toast(this, "destroyed")
    }

    fun addConfigChangeListener(block: ((Configuration)-> Unit)?){
        if(block != null) {
            mConfigChangeListeners.add(block)
        }
    }
    fun removeConfigChangeListener(block: ((Configuration)-> Unit)?) {
        if(block!=null) {
            mConfigChangeListeners.remove(block)
        }
    }

    override fun onInterrupt() {
        Log.v(TAG, "onInterrupt")
    }

    val windowManager get() = getSystemService(WINDOW_SERVICE) as WindowManager


    companion object {
        private const val TAG = "ScreenTranslateService"
        const val NOTIFICATION_ID = 100101
        const val ACTION_HIDE_NOTIFICATION = "hide_notification"
        const val ACTION_SHOW_NOTIFICATION = "show_notification"
        const val ACTION_START = "start"
        const val ACTION_START_TRANSLATE_MODE = "start_translate_mode"
        const val ACTION_START_OCR_MODE = "start_ocr_mode"
    }
}