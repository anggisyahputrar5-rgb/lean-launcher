package com.example.leanlauncher
import android.graphics.drawable.Drawable
data class AppInfo(
    val label: String,
    val packageName: String,
    val componentName: String,
    @Volatile var icon: Drawable? = null
)
