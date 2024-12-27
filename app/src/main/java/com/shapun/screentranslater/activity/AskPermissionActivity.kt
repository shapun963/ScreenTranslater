package com.shapun.screentranslater.activity

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.shapun.screentranslater.preferences.Preferences

class AskPermissionActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(Preferences.getTheme(this))
        AlertDialog.Builder(this)
            .setTitle("Permission Missing")
            .setMessage("Accessibility permission denied or you may have to disable and enabled again if its already enabled. You need to enable Accessibility Permission in order to use this app.")
            .setPositiveButton("Grant"){_,_->
                startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            }
            .setNegativeButton("Cancel",null)
            .create()
            .show()
    }
}