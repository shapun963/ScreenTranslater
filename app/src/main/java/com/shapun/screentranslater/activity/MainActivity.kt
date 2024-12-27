package com.shapun.screentranslater.activity

import android.animation.LayoutTransition
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.res.Resources
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.animation.AnimationUtils
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ShareCompat
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.shapun.screentranslater.R
import com.shapun.screentranslater.databinding.ActivityMainBinding
import com.shapun.screentranslater.dialog.SelectThemeBottomSheetDialogFragment
import com.shapun.screentranslater.preferences.Preferences
import com.shapun.screentranslater.service.MainService
import com.shapun.screentranslater.util.AccessibilityServiceUtils
import com.shapun.screentranslater.util.Utils

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var mSwitchEnabled = false

    private val mActivityLauncherSelectLanguage = registerForActivityResult(StartActivityForResult()){
        if (it != null) {
            binding.tvSelectedLanguage.text = Preferences.getLanguage(this).name
        }
    }
    private val mActivityLauncherAccessibilitySettings = registerForActivityResult(StartActivityForResult()) {
        if (it != null) {
            if (hasAccessibilityPermission()) {
                setChecked(true)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //startService(Intent(this,MainService::class.java).setAction(MainService.ACTION_START_OCR_MODE))
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.switchParent.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
        binding.tvSelectedLanguage.text = Preferences.getLanguage(this).name
        setUpSwitch()
        val permissionGiven =hasAccessibilityPermission()
        setChecked(Preferences.isServiceEnabled(this) && permissionGiven)
        binding.llLanguage.setOnClickListener {
            mActivityLauncherSelectLanguage.launch(
                Intent(this, SelectLanguageActivity::class.java)
            )
        }
        binding.llTheme.setOnClickListener {
            val themeDialog = SelectThemeBottomSheetDialogFragment()
            themeDialog.show(
                supportFragmentManager,
                SelectThemeBottomSheetDialogFragment::class.java.name
            )
        }
        if (savedInstanceState != null) {
            val behavior = BottomSheetBehavior.from(
                binding.sheetContent
            )
            behavior.state =
                savedInstanceState.getInt("bottom_sheet_state", BottomSheetBehavior.STATE_COLLAPSED)
        }
        binding.llSheetTitle.setOnClickListener {
            val behavior = BottomSheetBehavior.from(binding.sheetContent)
            if (behavior.state == BottomSheetBehavior.STATE_EXPANDED) {
                behavior.setState(BottomSheetBehavior.STATE_COLLAPSED)
            } else {
                behavior.setState(BottomSheetBehavior.STATE_EXPANDED)
            }
        }
        binding.llShare.setOnClickListener {
            val shareIntent = ShareCompat.IntentBuilder(this)
                .setType("text/plain")
                .setText("https://play.google.com/store/apps/details?id=$packageName")
                .intent
            if (shareIntent.resolveActivity(packageManager) != null) {
                startActivity(shareIntent)
            } else {
                Utils.toast(this, "Couldn't find any app that shares text")
            }
        }
        binding.llRate.setOnClickListener {
            try {
                val intent = Intent()
                intent.data = Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
                startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                Utils.toast(this, "PlayStore not found")
            }
        }
        /*
        val textRecognizer =  TextRecognizer.Builder(this).build()
        val bitmap = (resources.getDrawable(R.drawable.test) as BitmapDrawable).bitmap
        val frame = Frame.Builder()
            .setBitmap(bitmap)
            .build()
        val textBlocks = textRecognizer.detect(frame)
        var s = ""
        textBlocks.forEach { key, value -> s+= value.value  }
         */
    }

    override fun onSaveInstanceState(bundle: Bundle) {
        super.onSaveInstanceState(bundle)
        bundle.putInt("bottom_sheet_state", BottomSheetBehavior.from(binding.sheetContent).state)
    }

    private fun setUpSwitch() {
        val scaleAnim = AnimationUtils.loadAnimation(this, R.anim.scale_anim)
        binding.switchButton.startAnimation(scaleAnim)
        val layoutTransition = binding.switchButton.layoutTransition
        layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
        binding.switchButton.setOnClickListener { setChecked(!mSwitchEnabled) }
    }

    override fun getTheme(): Resources.Theme {
        val theme = super.getTheme()
        theme.applyStyle(Preferences.getTheme(this), true)
        return theme
    }

    private fun setChecked(b: Boolean) {
        mSwitchEnabled = b
        if (mSwitchEnabled) {
            if (hasAccessibilityPermission()) {
                Preferences.setServiceEnabled(this, true)
                val intent = Intent(this, MainService::class.java)
                intent.action = MainService.ACTION_SHOW_NOTIFICATION
                startService(intent)
            } else {
                MaterialAlertDialogBuilder(this)
                    .setTitle("Permission Required")
                    .setMessage("${getString(R.string.app_name)} requires Accessibility Service permission in order to extract texts from the screen. Need some suggestions on what should be written"
                    )
                    .setPositiveButton("Grant") { _, _->
                        mActivityLauncherAccessibilitySettings.launch(
                            Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                        )
                    }
                    .setNegativeButton("Later", null)
                    .create()
                    .show()
                mSwitchEnabled = false
                return
            }
        } else {
            Preferences.setServiceEnabled(this, false)
            val intent = Intent(this, MainService::class.java)
            intent.action = MainService.ACTION_HIDE_NOTIFICATION
            startService(intent)
        }
        binding.tvSwitchText.setTextColor(if (mSwitchEnabled) -0x1 else -0x424243)
        binding.tvSwitchText.text = if (mSwitchEnabled) "Service Enabled" else "Service Disabled"
        binding.tvShortNote.setText(if (mSwitchEnabled) R.string.service_enabled_info else R.string.service_disabled_info)
        binding.switchButton.setBackgroundResource(if (mSwitchEnabled) R.drawable.switch_enabled else R.drawable.switch_disabled)
    }

    private fun hasAccessibilityPermission(): Boolean{
        return AccessibilityServiceUtils.isAccessibilityServiceEnabled(
            this, MainService::class.java
        )
    }
}