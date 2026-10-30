package com.example.leanlauncher

import android.content.Context
import android.content.SharedPreferences

object SettingsManager {
    private const val PREFS = "lean_launcher_prefs"
    private fun get(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun isDarkTheme(c: Context) = get(c).getBoolean("dark_theme", true)
    fun setDarkTheme(c: Context, v: Boolean) = get(c).edit().putBoolean("dark_theme", v).apply()

    fun showClock(c: Context) = get(c).getBoolean("show_clock", true)
    fun setShowClock(c: Context, v: Boolean) = get(c).edit().putBoolean("show_clock", v).apply()

    fun getGridColumns(c: Context) = get(c).getInt("grid_columns", 5)
    fun setGridColumns(c: Context, v: Int) = get(c).edit().putInt("grid_columns", v).apply()

    fun showLabels(c: Context) = get(c).getBoolean("show_labels", true)
    fun setShowLabels(c: Context, v: Boolean) = get(c).edit().putBoolean("show_labels", v).apply()

    fun hideSearch(c: Context) = get(c).getBoolean("hide_search", false)
    fun setHideSearch(c: Context, v: Boolean) = get(c).edit().putBoolean("hide_search", v).apply()

    fun getIconShape(c: Context) = get(c).getInt("icon_shape", 0)
    fun setIconShape(c: Context, v: Int) = get(c).edit().putInt("icon_shape", v).apply()

    fun sortAscending(c: Context) = get(c).getBoolean("sort_asc", true)
    fun setSortAscending(c: Context, v: Boolean) = get(c).edit().putBoolean("sort_asc", v).apply()

    fun disableAnim(c: Context) = get(c).getBoolean("disable_anim", false)
    fun setDisableAnim(c: Context, v: Boolean) = get(c).edit().putBoolean("disable_anim", v).apply()

    fun doubleTapLock(c: Context) = get(c).getBoolean("double_tap_lock", false)
    fun setDoubleTapLock(c: Context, v: Boolean) = get(c).edit().putBoolean("double_tap_lock", v).apply()

    fun getHiddenApps(c: Context): Set<String> = get(c).getStringSet("hidden_apps", emptySet()) ?: emptySet()
    fun setHiddenApps(c: Context, v: Set<String>) = get(c).edit().putStringSet("hidden_apps", v).apply()
}
