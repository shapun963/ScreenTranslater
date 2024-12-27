package com.shapun.screentranslater.service

import android.accessibilityservice.AccessibilityServiceInfo
import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Configuration
import android.graphics.PixelFormat
import android.graphics.Rect
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import com.mannan.translateapi.Language
import com.mannan.translateapi.TranslateAPI
import com.shapun.screentranslater.activity.AskPermissionActivity
import com.shapun.screentranslater.activity.SelectLanguageActivity
import com.shapun.screentranslater.databinding.DirectTranslateBinding
import com.shapun.screentranslater.preferences.Preferences
import com.shapun.screentranslater.util.AccessibilityServiceUtils
import com.shapun.screentranslater.util.Utils
import org.json.JSONArray
import java.util.*
import kotlin.concurrent.schedule

class TranslateScreenService(service: MainService) {

    private lateinit var binding: DirectTranslateBinding
    private val mService : MainService = service
    private var mTranslateApi: TranslateAPI? = null
    private var mConfigListener: ((Configuration) -> Unit)? = null
    private var mAccessibilityEventListener: ((AccessibilityEvent) -> Unit)? = null

    fun start(){
        service.setTheme((Preferences.getTheme(service)))
        binding = DirectTranslateBinding.inflate(LayoutInflater.from(service))
        val containerParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )
        service.serviceInfo = AccessibilityServiceInfo()
        val markableView  = binding.markableView

        binding.btnRetry.setOnClickListener {
            startTranslating()
        }
        binding.imgLanguage.setOnClickListener {
            stop()
            service.startActivity(Intent(service,SelectLanguageActivity::class.java))
        }
        markableView.post {
            Timer().schedule(1000) {
                ContextCompat.getMainExecutor(service).execute {
                    startTranslating()
                    val info = AccessibilityServiceInfo()
                    info.eventTypes = AccessibilityEvent.TYPE_WINDOWS_CHANGED or
                            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
                    info.feedbackType = AccessibilityServiceInfo.FEEDBACK_ALL_MASK
                    service.serviceInfo = info
                }
            }
        }
        mAccessibilityEventListener = { stop() }
        service.addAccessibilityEventListener(mAccessibilityEventListener)
        mConfigListener = {
            stop()
            start()
        }
        service.addConfigChangeListener(mConfigListener)
        binding.imgClose.setOnClickListener {
            stop()
        }
        try {
            service.windowManager.addView(binding.markableView, containerParams)
        } catch (e: Exception) {
            Log.e(TAG, e.toString())
            //Most of the times turning on and off Accessibility switch will solve the problem do yeah :) a bad solution
            stop()
            service.disablePermission()
            service.startActivity(Intent(service, AskPermissionActivity::class.java))
        }
    }

    @SuppressLint("SetTextI18n", "WrongConstant")
    private fun startTranslating() {
        binding.tvStatus.text = "Translating . . . "
        binding.btnRetry.visibility = View.GONE
        val nodeList = AccessibilityServiceUtils.getNodesWithText(service.rootInActiveWindow)
        val textList = ArrayList<String>(nodeList.size)
        val rootRect = Rect()
        service.rootInActiveWindow.getBoundsInScreen(rootRect)
        val insets = ViewCompat.getRootWindowInsets(binding.markableView)
        Utils.toast(service,insets)
        val heightDiff =if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
                insets?.getInsetsIgnoringVisibility(WindowInsets.Type.systemBars())?.bottom ?:0
            } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                insets?.stableInsetTop ?: 0
            } else {
                0
            }
        }

        // (rootRect.bottom - rootRect.top) - binding.markableView.measuredHeight
        nodeList.forEach {
            val rect = Rect()
            it.getBoundsInScreen(rect)
            if (heightDiff > 0) {
                rect.top -= heightDiff
                rect.bottom -= heightDiff
            }
            binding.markableView.addText(rect, "loading . . .")
            textList.add(it.text.toString())
        }
        try {
            val originalJsonArray = JSONArray(textList)
            mTranslateApi = TranslateAPI(
                service,
                Language.AUTO_DETECT,
                Preferences.getLanguage(service).code,
                originalJsonArray.toString()
            )
            mTranslateApi!!.setTranslateListener(
                object : TranslateAPI.TranslateListener {
                    override fun onSuccess(translatedText: String) {
                        val translatedJsonArray = JSONArray(translatedText)
                        for (i in 0 until translatedJsonArray.length()) {
                            //map["original_text"] = originalJsonArray[i]
                            val rect = Rect()
                            nodeList[i].getBoundsInScreen(rect)
                            if (heightDiff > 0) {
                                rect.top -= heightDiff
                                rect.bottom -= heightDiff
                            }
                            binding.markableView.updateText(rect, translatedJsonArray.getString(i))
                        }
                    }
                    override fun onFailure(errorText: String) {
                        binding.btnRetry.visibility = View.VISIBLE
                        binding.tvStatus.text = errorText
                        Utils.toast(service, "Failed to translate . $errorText")
                    }
                })
        } catch (e: Exception) {
            binding.btnRetry.visibility = View.VISIBLE
            binding.tvStatus.text = e.message
            Utils.toast(service, "Failed to translate . ${e.message}")
        }
    }

    fun stop(){
        //sometimes view not be attached so to fix it just wrap it with try catch
        try {
            service.windowManager.removeViewImmediate(binding.markableView)
        }catch (e: Exception){}
        mTranslateApi?.setTranslateListener(null)
        service.serviceInfo = AccessibilityServiceInfo()
        service.removeConfigChangeListener(mConfigListener)
        service.removeAccessibilityEventListener(mAccessibilityEventListener)
    }

    private val service get() = mService

    companion object {
        private const val TAG = "TranslateScreenService"
    }
}