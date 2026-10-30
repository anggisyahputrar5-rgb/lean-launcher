package com.example.leanlauncher

import android.content.Intent
import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import androidx.activity.ComponentActivity
import androidx.fragment.app.Fragment
import com.example.leanlauncher.databinding.ActivityMainBinding

class MainActivity : ComponentActivity() {

    private lateinit var binding: ActivityMainBinding
    private var isDrawerOpen = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            loadFragment(HomeFragment())
        }

        val gestureDetector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            override fun onFling(e1: MotionEvent?, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
                if (e1 == null) return false
                val diffY = e2.y - e1.y
                if (Math.abs(diffY) > 100 && Math.abs(velocityY) > 100) {
                    if (diffY < 0 && !isDrawerOpen) { // Swipe Up -> Buka Drawer
                        openDrawer()
                        return true
                    } else if (diffY > 0 && isDrawerOpen) { // Swipe Down -> Tutup Drawer
                        closeDrawer()
                        return true
                    }
                }
                return false
            }

            override fun onDoubleTap(e: MotionEvent): Boolean {
                if (!isDrawerOpen && SettingsManager.doubleTapLock(this@MainActivity)) {
                    val dpm = getSystemService(DEVICE_POLICY_SERVICE) as android.app.admin.DevicePolicyManager
                    val compName = android.content.ComponentName(this@MainActivity, MyDeviceAdminReceiver::class.java)
                    if (dpm.isAdminActive(compName)) dpm.lockNow()
                    return true
                }
                return false
            }
        })

        binding.mainContainer.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
            false
        }

        // Long Press di Home untuk Settings
        binding.mainContainer.setOnLongClickListener {
            if (!isDrawerOpen) {
                startActivity(Intent(this, SettingsActivity::class.java))
                true
            } else false
        }
    }

    private fun openDrawer() {
        isDrawerOpen = true
        loadFragment(DrawerFragment())
        binding.fragmentContainer.animate().translationY(0f).setDuration(0).start()
        // Animasi masuk dari bawah
        binding.fragmentContainer.translationY = 2000f
        binding.fragmentContainer.animate().translationY(0f).setDuration(300).start()
    }

    private fun closeDrawer() {
        isDrawerOpen = false
        binding.fragmentContainer.animate().translationY(2000f).setDuration(300).withEndAction {
            loadFragment(HomeFragment())
            binding.fragmentContainer.translationY = 0f
        }.start()
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(binding.fragmentContainer.id, fragment)
            .commit()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (isDrawerOpen) {
            closeDrawer()
        } else {
            super.onBackPressed()
        }
    }
}
