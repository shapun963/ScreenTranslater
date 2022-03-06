package com.shapun.screentranslater.activity

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.mannan.translateapi.Language
import com.mannan.translateapi.TranslateAPI
import com.mannan.translateapi.TranslateAPI.TranslateListener
import com.shapun.screentranslater.R
import com.shapun.screentranslater.activity.TranslateTextActivity
import com.shapun.screentranslater.adapter.TranslatedTextAdapter
import com.shapun.screentranslater.databinding.ActivityTranslateTextBinding
import com.shapun.screentranslater.preferences.Preferences
import com.shapun.screentranslater.util.Utils
import org.json.JSONArray

class TranslateTextActivity : AppCompatActivity() {

    private lateinit var binding : ActivityTranslateTextBinding
    private var mData: ArrayList<Map<String, Any>>? = null
    private val mActivityLauncherSelectLanguage = registerForActivityResult(
        StartActivityForResult()
    ) { result: ActivityResult? ->
        if (result != null) {
            startTranslating()
        }
    }

    public override fun onCreate(savedInstamceState: Bundle?) {
        super.onCreate(savedInstamceState)
        theme.applyStyle(Preferences.getTheme(this), true)
        binding = ActivityTranslateTextBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.root.clipToOutline = true
        setSupportActionBar(binding.toolbar)
        setFinishOnTouchOutside(true)
        startTranslating()
    }

    @SuppressLint("SetTextI18n")
    private fun startTranslating() {
        val list  = intent.getSerializableExtra(LIST_KEY) as ArrayList<String?>?
        mData = ArrayList()
        binding.tvStatus.text = "Translating . . ."
        binding.tvStatus.setOnClickListener(null)
        binding.linStatus.visibility = View.VISIBLE
        try {
            val originalJsonArray = JSONArray(list)
            val translate = TranslateAPI(
                this,
                Language.AUTO_DETECT,
                Preferences.getLanguage(this).code,
                originalJsonArray.toString()
            )
            translate.setTranslateListener(
                object : TranslateListener {
                    override fun onSuccess(translatedText: String) {
                        try {
                            val translatedJsonArray = JSONArray(translatedText)
                            for (i in 0 until translatedJsonArray.length()) {
                                val map: MutableMap<String, Any> = HashMap()
                                map["original_text"] = originalJsonArray[i]
                                map["translated_text"] = translatedJsonArray.getString(i)
                                map["show_translated"] = true
                                mData!!.add(map)
                            }
                            binding.linStatus.visibility = View.GONE
                            binding.recyclerview.layoutManager =
                                LinearLayoutManager(this@TranslateTextActivity)
                            binding.recyclerview.adapter = TranslatedTextAdapter(mData)
                        } catch (e: Exception) {
                            showErrorMessage()
                            Utils.toast(applicationContext, e)
                        }
                    }
                    override fun onFailure(ErrorText: String) {
                        showErrorMessage()
                    }
                })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun showErrorMessage() {
        Utils.toast(applicationContext, "Failed to translate ")
        binding.tvStatus.text = "Failed to translate. Tap to retry."
        binding.tvStatus.setOnClickListener { startTranslating() }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val item = menu.add("Select Language")
        item.setIcon(R.drawable.ic_language)
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
        item.setOnMenuItemClickListener {
            val intent = Intent(this, SelectLanguageActivity::class.java);
            mActivityLauncherSelectLanguage.launch(intent)
            true
        }
        return true
    }


    public override fun onStart() {
        super.onStart()
        val width = ViewGroup.LayoutParams.MATCH_PARENT
        val height = ViewGroup.LayoutParams.MATCH_PARENT
        window.setLayout(width, height)
        window.setBackgroundDrawableResource(android.R.color.transparent)
    }

    companion object {
        private const val TAG = "TranslateTextActivity"
        private const val LIST_KEY = "list_data"
        @JvmStatic
        fun newIntent(context: Context, list: ArrayList<String>): Intent {
            val intent = Intent(context, TranslateTextActivity::class.java)
            intent.putExtra(LIST_KEY, list)
            return intent
        }
    }
}