package com.example.leanlauncher

import android.app.admin.DevicePolicyManager
import android.content.*
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.example.leanlauncher.databinding.ActivityMainBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: LauncherAdapter
    private var allApps: List<AppInfo> = emptyList()
    private var query: String = ""
    
    private val packageReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) { AppRepository.clearCache(); reloadApps() }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupSettings()
        setupGestures()

        adapter = LauncherAdapter(lifecycleScope) { app -> AppRepository.launch(this, app) }
        binding.apps.adapter = adapter
        binding.apps.setHasFixedSize(true)
        binding.apps.setItemViewCacheSize(20)

        binding.search.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
            override fun onTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {
                query = s?.toString()?.trim()?.lowercase().orEmpty()
                applyFilter()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addAction(Intent.ACTION_PACKAGE_REMOVED)
            addAction(Intent.ACTION_PACKAGE_CHANGED)
            addDataScheme("package")
        }
        ContextCompat.registerReceiver(this, packageReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED)
        
        startClock()
        reloadApps()
    }

    private fun setupSettings() {
        val isDark = SettingsManager.isDarkTheme(this)
        val bgColor = if (isDark) 0xFF000000.toInt() else 0xFFFFFFFF.toInt()
        val textColor = if (isDark) 0xFFFFFFFF.toInt() else 0xFF000000.toInt()
        binding.rootLayout.setBackgroundColor(bgColor)
        binding.tvClock.setTextColor(textColor)
        binding.apps.layoutManager = GridLayoutManager(this, SettingsManager.getGridColumns(this))
        binding.tvClock.visibility = if (SettingsManager.showClock(this)) View.VISIBLE else View.GONE
        binding.search.visibility = if (SettingsManager.hideSearch(this)) View.GONE else View.VISIBLE
        binding.apps.setPadding(
            binding.apps.paddingLeft, binding.apps.paddingTop, 
            binding.apps.paddingRight, if (SettingsManager.hideSearch(this)) 24 else 80
        )
        binding.apps.itemAnimator = null
    }

    private fun setupGestures() {
        val gestureDetector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            override fun onLongPress(e: MotionEvent) {
                startActivity(Intent(this@MainActivity, SettingsActivity::class.java))
            }
            override fun onDoubleTap(e: MotionEvent): Boolean {
                if (SettingsManager.doubleTapLock(this@MainActivity)) {
                    val dpm = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
                    val compName = ComponentName(this@MainActivity, MyDeviceAdminReceiver::class.java)
                    if (dpm.isAdminActive(compName)) dpm.lockNow()
                    else Toast.makeText(this@MainActivity, "Aktifkan izin Admin dulu di Setting HP", Toast.LENGTH_SHORT).show()
                    return true
                }
                return false
            }
        })
        
        val touchListener = View.OnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
            false
        }
        binding.rootLayout.setOnTouchListener(touchListener)
        binding.apps.setOnTouchListener(touchListener)
    }

    private fun startClock() {
        lifecycleScope.launch {
            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            while (true) {
                binding.tvClock.text = sdf.format(Date())
                delay(10000)
            }
        }
    }

    private fun reloadApps() {
        lifecycleScope.launch {
            allApps = AppRepository.load(this@MainActivity, force = true)
            applyFilter()
        }
    }

    private fun applyFilter() {
        val list = if (query.isEmpty()) allApps else allApps.filter { it.label.lowercase().contains(query) }
        adapter.submitList(list)
        adapter.showLabels = SettingsManager.showLabels(this)
        adapter.iconShape = SettingsManager.getIconShape(this)
        binding.empty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
    }

    override fun onResume() {
        super.onResume()
        setupSettings()
        if (binding.search.text?.isNotEmpty() == true) binding.search.text?.clear()
        binding.search.clearFocus()
        getSystemService(InputMethodManager::class.java)?.hideSoftInputFromWindow(binding.search.windowToken, 0)
    }

    override fun onDestroy() {
        super.onDestroy()
        try { unregisterReceiver(packageReceiver) } catch (_: Exception) {}
        IconCache.clear()
    }
}
