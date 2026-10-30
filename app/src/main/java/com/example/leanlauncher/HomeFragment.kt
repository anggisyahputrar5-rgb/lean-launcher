package com.example.leanlauncher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.example.leanlauncher.databinding.FragmentHomeBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: LauncherAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        adapter = LauncherAdapter(
            scope = lifecycleScope, 
            onLaunch = { app -> AppRepository.launch(requireContext(), app) }
        )
        
        binding.rvPinnedApps.layoutManager = GridLayoutManager(requireContext(), 4)
        binding.rvPinnedApps.adapter = adapter
        
        startClock()
    }

    override fun onResume() {
        super.onResume()
        loadPinnedApps()
    }

    private fun loadPinnedApps() {
        val ctx = requireContext()
        lifecycleScope.launch {
            val allApps = AppRepository.load(ctx)
            val pinnedSet = SettingsManager.getPinnedApps(ctx)
            val pinnedList = allApps.filter { pinnedSet.contains(it.packageName) }
            adapter.submitList(pinnedList)
        }
    }

    private fun startClock() {
        viewLifecycleOwner.lifecycleScope.launch {
            val sdfTime = SimpleDateFormat("HH:mm", Locale.getDefault())
            val sdfDate = SimpleDateFormat("EEEE, d MMMM", Locale("id", "ID"))
            while (true) {
                binding.tvClock.text = sdfTime.format(Date())
                binding.tvDate.text = sdfDate.format(Date())
                delay(10000)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
