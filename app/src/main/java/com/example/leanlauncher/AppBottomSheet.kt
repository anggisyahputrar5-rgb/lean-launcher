package com.example.leanlauncher

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.leanlauncher.databinding.BottomSheetAppBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class AppBottomSheet : BottomSheetDialogFragment() {
    private lateinit var b: BottomSheetAppBinding
    private var app: AppInfo? = null
    private var isPinned = false

    fun setApp(appInfo: AppInfo) { this.app = appInfo }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        b = BottomSheetAppBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val a = app ?: return
        val ctx = requireContext()

        b.bsLabel.text = a.label
        b.bsIcon.setImageDrawable(ctx.packageManager.getApplicationIcon(a.packageName))

        isPinned = SettingsManager.getPinnedApps(ctx).contains(a.packageName)
        b.btnPin.text = if (isPinned) "Hapus dari Layar Utama" else "Tambahkan ke Layar Utama"

        b.btnPin.setOnClickListener {
            val pinned = SettingsManager.getPinnedApps(ctx).toMutableSet()
            if (isPinned) pinned.remove(a.packageName) else pinned.add(a.packageName)
            SettingsManager.setPinnedApps(ctx, pinned)
            Toast.makeText(ctx, if (isPinned) "Dihapus dari Home" else "Ditambahkan ke Home", Toast.LENGTH_SHORT).show()
            dismiss()
            // Note: HomeFragment akan refresh saat onResume
        }

        b.btnInfo.setOnClickListener {
            val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            intent.data = Uri.parse("package:${a.packageName}")
            startActivity(intent)
            dismiss()
        }

        b.btnUninstall.setOnClickListener {
            val intent = Intent(Intent.ACTION_DELETE)
            intent.data = Uri.parse("package:${a.packageName}")
            startActivity(intent)
            dismiss()
        }
    }
}
