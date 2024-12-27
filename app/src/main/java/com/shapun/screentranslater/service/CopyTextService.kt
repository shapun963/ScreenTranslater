package com.shapun.screentranslater.service

import android.accessibilityservice.AccessibilityServiceInfo
import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Configuration
import android.graphics.PixelFormat
import android.graphics.Rect
import android.os.Build
import android.util.Log
import android.view.*
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import androidx.core.graphics.ColorUtils
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.shapun.screentranslater.activity.AskPermissionActivity
import com.shapun.screentranslater.activity.TranslateTextActivity
import com.shapun.screentranslater.adapter.SelectedTextsAdapter
import com.shapun.screentranslater.databinding.DialogSelectedTextsBinding
import com.shapun.screentranslater.preferences.Preferences
import com.shapun.screentranslater.util.AccessibilityServiceUtils
import com.shapun.screentranslater.util.Utils
import java.util.*
import kotlin.concurrent.schedule


class CopyTextService (service: MainService): View.OnTouchListener, GestureDetector.OnGestureListener {
    private var mAccessibilityEventListener: ((AccessibilityEvent) -> Unit)? = null
    private var mConfigChangeListener: ((Configuration) -> Unit)? = null
    private var mGestureDetector: GestureDetector? = null
    private var binding: DialogSelectedTextsBinding? = null
    private var mSelectedNodes: ArrayList<AccessibilityNodeInfo> = ArrayList()
    private var mSelectedTextAdapter: SelectedTextsAdapter? = null
    private var mWindowView: View? = null
    private val mService : MainService = service


    @Suppress("DEPRECATION")
    @SuppressLint("ClickableViewAccessibility", "WrongConstant")
    fun start(){
        service.setTheme((Preferences.getTheme(service)))
        if(!AccessibilityServiceUtils.isAccessibilityServiceEnabled(service,MainService::class.java)){
            service.disablePermission()
            service.startActivity(Intent(service, AskPermissionActivity::class.java))
            return
        }
        val mContainerParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR,PixelFormat.TRANSLUCENT
        )
        binding = DialogSelectedTextsBinding.inflate(LayoutInflater.from(service))
        binding!!.markableView.highlightedColor =
            ColorUtils.setAlphaComponent(binding!!.markableView.highlightedColor, 255 / 4)
        binding!!.markableView.setOnTouchListener(this)
        mGestureDetector = GestureDetector(service,this)
        binding!!.markableView.alpha = 0f
        binding!!.markableView.animate().alpha(1f).duration = 1000
        try {
            mWindowView = binding!!.markableView
            service.windowManager.addView(mWindowView, mContainerParams)
        } catch (e: Exception) {
            Log.e(TAG, e.toString())
            //Most of the times turning on and off Accessibility switch will solve the problem do yeah :)
            service.disablePermission()
            service.startActivity(Intent(service,AskPermissionActivity::class.java))
        }
        binding!!.markableView.setOnApplyWindowInsetsListener { v, insets ->
            //val insets = ViewCompat.getRootWindowInsets(binding!!.markableView)
            val heightDiff =if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
                insets?.getInsetsIgnoringVisibility(WindowInsets.Type.systemBars())?.bottom ?:0
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    insets?.stableInsetTop ?: 0
                } else {
                    0
                }
            }
            //Utils.toast(service,insets.getInsets(WindowInsets.Type.statusBars()))
            return@setOnApplyWindowInsetsListener insets
        }
        BottomSheetBehavior.from(binding!!.sheetContent).state = BottomSheetBehavior.STATE_COLLAPSED
        binding!!.imgClose.setOnClickListener {
            BottomSheetBehavior.from(binding!!.sheetContent).apply {
                addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                    override fun onStateChanged(bottomSheet: View, newState: Int) {
                        if (newState == BottomSheetBehavior.STATE_HIDDEN) stop()
                    }
                    override fun onSlide(bottomSheet: View, slideOffset: Float) {}
                })
                isHideable = true
                setState(BottomSheetBehavior.STATE_HIDDEN)
            }
        }
        binding!!.imgCopy.setOnClickListener {
            if (mSelectedNodes.isEmpty()) {
                Utils.showSnackbar(binding!!.markableView,"No texts were selected . Click on texts you want copy")
                return@setOnClickListener
            }
            val sb = StringBuilder()
            mSelectedNodes.forEach { sb.append(it.text.toString() + "\n") }
            Utils.copyToClipboard(service, sb)
            Utils.showSnackbar(binding!!.markableView, "Copied to clipboard")
        }
        binding!!.imgTranslate.setOnClickListener {
            if (mSelectedNodes.isEmpty()) {
                Utils.showSnackbar(binding!!.markableView,"No texts were selected . Click on texts you want translate")
                return@setOnClickListener
            }else {
                val selectedTexts: ArrayList<String> = ArrayList()
                mSelectedNodes.forEach { selectedTexts.add(it.text.toString()) }
                val intent = TranslateTextActivity.newIntent(service, selectedTexts)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                service.startActivity(intent)
                stop()
            }
        }
        binding!!.imgSelectAll.setOnClickListener {
            AccessibilityServiceUtils.getNodesWithText(service.rootInActiveWindow).forEach(::addNode)
        }
        val rv = binding!!.recyclerview
        rv.layoutManager = LinearLayoutManager(service)
        mSelectedTextAdapter = SelectedTextsAdapter(mSelectedNodes)
        rv.adapter = mSelectedTextAdapter
        mSelectedTextAdapter!!.setOnCloseClickListener { removeNode(mSelectedNodes[it]) }
        Timer().schedule(1000) {
            val info = AccessibilityServiceInfo()
            info.eventTypes = AccessibilityEvent.TYPE_WINDOWS_CHANGED or
                    AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
            info.feedbackType = AccessibilityServiceInfo.FEEDBACK_ALL_MASK
            service.serviceInfo = info
        }
        mAccessibilityEventListener = {}//stop()}
        service.addAccessibilityEventListener(mAccessibilityEventListener!!)
        mConfigChangeListener = {
            stop()
            start()
        }
        service.addConfigChangeListener(mConfigChangeListener!!)
    }

    private fun addNode(node: AccessibilityNodeInfo) {
        //if (mSelectedNodes.contains(node)) return
        mSelectedNodes.add(node)
        val rect = Rect()
        node.getBoundsInScreen(rect)
        val rootRect = Rect()
        service.rootInActiveWindow.getBoundsInScreen(rootRect)
        if((rootRect.bottom - rootRect.top) > binding!!.markableView.measuredHeight){
            rect.top -= (rootRect.bottom - rootRect.top) - binding!!.markableView.measuredHeight
            rect.bottom -= (rootRect.bottom - rootRect.top) - binding!!.markableView.measuredHeight
        }
        binding!!.markableView.addRect(rect)
        mSelectedTextAdapter!!.notifyItemInserted(mSelectedNodes.lastIndex)
        refreshSelectedCount()
    }

    private fun removeNode(node: AccessibilityNodeInfo) {
        val index = mSelectedNodes.indexOf(node)
        if (index != -1) {
            val rect = Rect()
            node.getBoundsInScreen(rect)
            binding!!.markableView.removeRect(rect)
            mSelectedNodes.removeAt(index)
            mSelectedTextAdapter!!.notifyItemRemoved(index)
            mSelectedTextAdapter!!.notifyItemRangeChanged(index, mSelectedTextAdapter!!.itemCount)
            refreshSelectedCount()
        }
    }

    private fun stop() {
        service.windowManager.removeView(mWindowView)
        service.removeAccessibilityEventListener(mAccessibilityEventListener!!)
        service.removeConfigChangeListener(mConfigChangeListener!!)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        return mGestureDetector!!.onTouchEvent(event)
    }

    

    override fun onSingleTapUp(ev: MotionEvent): Boolean {
        val list = AccessibilityServiceUtils.getNodesWithText(service.rootInActiveWindow, ev.rawX.toInt(), ev.rawY.toInt())
        if (list.isEmpty()) return false
        val node = list.last()
        if (mSelectedNodes.contains(node)) {
            removeNode(node)
        } else {
            addNode(node)
        }
        return true
    }

    @SuppressLint("SetTextI18n")
    private fun refreshSelectedCount() {
        val prefix = if (mSelectedNodes.size == 0) "No text" else mSelectedNodes.size.toString()
        binding!!.selectedTextCount.text = "$prefix  selected"
    }


    private val service get() = mService

    //All unnecessary things that needs to be implemented
    override fun onLongPress(e: MotionEvent) {}
    override fun onScroll(e1: MotionEvent, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean = false
    override fun onFling(e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean = false
    override fun onShowPress(e: MotionEvent){}
    override fun onDown(e: MotionEvent): Boolean = false

    companion object {
        private const val TAG = "CopyTextService"
    }

}