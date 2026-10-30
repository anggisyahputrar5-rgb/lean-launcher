package com.example.leanlauncher

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.example.leanlauncher.databinding.ActivityMainBinding
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: LauncherAdapter
    private var allApps: List<AppInfo> = emptyList()
    private var query: String = ""

    private val GRID_COLUMNS = 5

    private val packageReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) = reloadApps()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = LauncherAdapter(lifecycleScope) { app ->
            AppRepository.launch(this, app)
        }

        binding.apps.layoutManager = GridLayoutManager(this, GRID_COLUMNS)
        binding.apps.adapter = adapter
        binding.apps.setHasFixedSize(true)
        binding.apps.setItemViewCacheSize(20)
        binding.apps.itemAnimator = null

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
        registerReceiver(packageReceiver, filter)

        reloadApps()
    }

    private fun reloadApps() {
        lifecycleScope.launch {
            allApps = AppRepository.load(this@MainActivity, force = true)
            applyFilter()
        }
    }

    private fun applyFilter() {
        val list = if (query.isEmpty()) allApps
        else allApps.filter { it.label.lowercase().contains(query) }
        adapter.submitList(list)
        binding.empty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
    }

    override fun onResume() {
        super.onResume()
        if (binding.search.text?.isNotEmpty() == true) binding.search.text?.clear()
        binding.search.clearFocus()
        getSystemService(InputMethodManager::class.java)
            ?.hideSoftInputFromWindow(binding.search.windowToken, 0)
    }

    override fun onDestroy() {
        super.onDestroy()
        try { unregisterReceiver(packageReceiver) } catch (_: Exception) {}
        IconCache.clear()
    }
}
