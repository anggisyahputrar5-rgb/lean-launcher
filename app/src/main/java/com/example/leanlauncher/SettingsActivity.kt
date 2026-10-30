package com.example.leanlauncher

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.Switch
import androidx.activity.ComponentActivity
import com.example.leanlauncher.databinding.ActivitySettingsBinding

class SettingsActivity : ComponentActivity() {
    private lateinit var b: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(b.root)

        // Load Settings
        b.swTheme.isChecked = SettingsManager.isDarkTheme(this)
        b.swClock.isChecked = SettingsManager.showClock(this)
        b.etGrid.setText(SettingsManager.getGridColumns(this).toString())
        b.swLabels.isChecked = SettingsManager.showLabels(this)
        b.swSearch.isChecked = SettingsManager.hideSearch(this)
        b.swAnim.isChecked = SettingsManager.disableAnim(this)
        b.swLock.isChecked = SettingsManager.doubleTapLock(this)
        b.swSort.isChecked = SettingsManager.sortAscending(this)

        // Listeners
        b.btnBack.setOnClickListener { finish() }
        
        b.swTheme.setOnCheckedChangeListener { _, v -> SettingsManager.setDarkTheme(this, v) }
        b.swClock.setOnCheckedChangeListener { _, v -> SettingsManager.setShowClock(this, v) }
        b.swLabels.setOnCheckedChangeListener { _, v -> SettingsManager.setShowLabels(this, v) }
        b.swSearch.setOnCheckedChangeListener { _, v -> SettingsManager.setHideSearch(this, v) }
        b.swAnim.setOnCheckedChangeListener { _, v -> SettingsManager.setDisableAnim(this, v) }
        b.swLock.setOnCheckedChangeListener { _, v -> SettingsManager.setDoubleTapLock(this, v) }
        b.swSort.setOnCheckedChangeListener { _, v -> SettingsManager.setSortAscending(this, v); AppRepository.clearCache() }

        b.etGrid.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val cols = b.etGrid.text.toString().toIntOrNull()
                if (cols != null && cols in 3..8) SettingsManager.setGridColumns(this, cols)
                else b.etGrid.setText(SettingsManager.getGridColumns(this).toString())
            }
        }

        b.btnHideApps.setOnClickListener { 
            startActivity(Intent(this, HideAppsActivity::class.java)) 
        }
    }
    
    override fun onPause() {
        super.onPause()
        // Terapkan perubahan dengan me-restart MainActivity
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }
}
