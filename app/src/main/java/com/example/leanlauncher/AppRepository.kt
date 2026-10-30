package com.example.leanlauncher

import android.content.Context
import android.content.Intent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object AppRepository {
    @Volatile private var cached: List<AppInfo> = emptyList()
    @Volatile private var loaded = false

    suspend fun load(context: Context, force: Boolean = false): List<AppInfo> =
        withContext(Dispatchers.IO) {
            if (loaded && !force) return@withContext cached
            val pm = context.packageManager
            val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
            val resolved = pm.queryIntentActivities(intent, 0)
            val list = ArrayList<AppInfo>(resolved.size)
            for (ri in resolved) {
                val pkg = ri.activityInfo.packageName
                if (pkg == context.packageName) continue
                list.add(
                    AppInfo(
                        label = ri.loadLabel(pm).toString(),
                        packageName = pkg,
                        componentName = ri.activityInfo.name
                    )
                )
            }
            list.sortWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.label })
            cached = list
            loaded = true
            cached
        }

    fun launch(context: Context, app: AppInfo) {
        // Menggunakan cara standar Android yang lebih aman
        val intent = context.packageManager.getLaunchIntentForPackage(app.packageName)
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
            try { context.startActivity(intent) } catch (_: Exception) {}
        }
    }
}
