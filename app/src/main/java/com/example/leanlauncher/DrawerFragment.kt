package com.example.leanlauncher

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.example.leanlauncher.databinding.FragmentDrawerBinding
import kotlinx.coroutines.launch

class DrawerFragment : Fragment() {
    private var _binding: FragmentDrawerBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: LauncherAdapter
    private var allApps: List<AppInfo> = emptyList()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDrawerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        adapter = LauncherAdapter(lifecycleScope, { app -> 
            AppRepository.launch(requireContext(), app) 
        }, { app ->
            // Aksi Long Press -> Tampilkan BottomSheet
            val sheet = AppBottomSheet().apply { setApp(app) }
            sheet.show(parentFragmentManager, "AppSheet")
        })

        binding.rvApps.layoutManager = GridLayoutManager(requireContext(), SettingsManager.getGridColumns(requireContext()))
        binding.rvApps.adapter = adapter

        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
            override fun onTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {
                val q = s?.toString()?.trim()?.lowercase().orEmpty()
                applyFilter(q)
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        loadApps()
    }

    private fun loadApps() {
        lifecycleScope.launch {
            allApps = AppRepository.load(requireContext(), force = true)
            applyFilter("")
        }
    }

    private fun applyFilter(query: String) {
        val list = if (query.isEmpty()) allApps else allApps.filter { it.label.lowercase().contains(query) }
        adapter.submitList(list)
        adapter.showLabels = SettingsManager.showLabels(requireContext())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
