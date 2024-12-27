package com.shapun.screentranslater.activity

import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.shapun.screentranslater.service.MainService

class ScreenCapturePermission : AppCompatActivity() {
    private val mMediaProjectionPermission = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it != null) {
            if (it.resultCode == RESULT_OK) {
                if (it.data != null) {
                    val intent = Intent(this, MainService::class.java)
                    intent.action = MainService.ACTION_START_OCR_MODE
                    intent.putExtra("intent", it.data)
                    startService(intent)
                }
            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intent = (getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager).createScreenCaptureIntent()
        mMediaProjectionPermission.launch(intent)
    }
}