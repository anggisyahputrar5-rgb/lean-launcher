package com.example.leanlauncher

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.leanlauncher.databinding.ItemAppBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LauncherAdapter(
    private val scope: CoroutineScope,
    private val onLaunch: (AppInfo) -> Unit,
    private val onLongClick: ((AppInfo) -> Unit)? = null
) : ListAdapter<AppInfo, LauncherAdapter.VH>(DIFF) {

    var showLabels: Boolean = true

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemAppBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding, parent.context, scope, onLaunch, onLongClick, this)
    }

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(getItem(position))

    class VH(
        private val binding: ItemAppBinding,
        private val context: Context,
        private val scope: CoroutineScope,
        private val onLaunch: (AppInfo) -> Unit,
        private val onLongClick: ((AppInfo) -> Unit)?,
        private val adapter: LauncherAdapter
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(app: AppInfo) {
            binding.label.text = app.label
            binding.label.visibility = if (adapter.showLabels) View.VISIBLE else View.GONE
            binding.icon.tag = app.packageName

            val cached = IconCache.get(app.packageName)
            if (cached != null) {
                binding.icon.setImageDrawable(cached)
            } else {
                binding.icon.setImageDrawable(null)
                scope.launch {
                    val icon = withContext(Dispatchers.IO) {
                        try { context.packageManager.getApplicationIcon(app.packageName) } catch (_: Exception) { null }
                    }
                    if (icon != null) {
                        IconCache.put(app.packageName, icon)
                        if (binding.icon.tag == app.packageName) binding.icon.setImageDrawable(icon)
                    }
                }
            }
            binding.root.setOnClickListener { onLaunch(app) }
            binding.root.setOnLongClickListener { 
                onLongClick?.invoke(app)
                true 
            }
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<AppInfo>() {
            override fun areItemsTheSame(a: AppInfo, b: AppInfo) = a.packageName == b.packageName
            override fun areContentsTheSame(a: AppInfo, b: AppInfo) = a.label == b.label
        }
    }
}
