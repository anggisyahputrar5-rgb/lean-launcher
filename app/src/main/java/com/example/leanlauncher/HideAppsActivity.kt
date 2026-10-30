package com.example.leanlauncher

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.leanlauncher.databinding.ActivityHideAppsBinding
import com.example.leanlauncher.databinding.ItemHideAppBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HideAppsActivity : ComponentActivity() {
    private lateinit var b: ActivityHideAppsBinding
    private var allApps: List<AppInfo> = emptyList()
    private val hiddenSet = mutableSetOf<String>()
    private lateinit var adapter: HideAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityHideAppsBinding.inflate(layoutInflater)
        setContentView(b.root)
        b.btnBack.setOnClickListener { finish() }

        hiddenSet.addAll(SettingsManager.getHiddenApps(this))

        // Ambil SEMUA aplikasi tanpa filter (bypass)
        lifecycleScope.launch {
            val pm = packageManager
            val intent = android.content.Intent(android.content.Intent.ACTION_MAIN).addCategory(android.content.Intent.CATEGORY_LAUNCHER)
            val resolved = withContext(Dispatchers.IO) { pm.queryIntentActivities(intent, 0) }
            allApps = resolved.map {
                AppInfo(it.loadLabel(pm).toString(), it.activityInfo.packageName, it.activityInfo.name)
            }.filter { it.packageName != packageName }
             .sortedBy { it.label.lowercase() }

            adapter = HideAdapter(allApps, hiddenSet) { pkg, isHidden ->
                if (isHidden) hiddenSet.add(pkg) else hiddenSet.remove(pkg)
                SettingsManager.setHiddenApps(this@HideAppsActivity, hiddenSet)
                AppRepository.clearCache()
            }
            b.rvHideApps.layoutManager = LinearLayoutManager(this@HideAppsActivity)
            b.rvHideApps.adapter = adapter
        }
    }
}

class HideAdapter(
    private val apps: List<AppInfo>,
    private val hidden: Set<String>,
    private val onToggle: (String, Boolean) -> Unit
) : RecyclerView.Adapter<HideAdapter.VH>() {
    class VH(val b: ItemHideAppBinding) : RecyclerView.ViewHolder(b.root)
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = VH(ItemHideAppBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    
    override fun onBindViewHolder(holder: VH, position: Int) {
        val app = apps[position]
        holder.b.label.text = app.label
        holder.b.icon.setImageDrawable(holder.itemView.context.packageManager.getApplicationIcon(app.packageName))
        holder.b.swHide.setOnCheckedChangeListener(null)
        holder.b.swHide.isChecked = hidden.contains(app.packageName)
        holder.b.swHide.setOnCheckedChangeListener { _, isChecked ->
            onToggle(app.packageName, isChecked)
        }
    }
    override fun getItemCount() = apps.size
}
