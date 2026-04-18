package com.practicum.playlistmaker.presentation.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.practicum.playlistmaker.App
import com.practicum.playlistmaker.databinding.FragmentSettingsBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

// Фрагмент Настроек
class SettingsFragment : Fragment() {

    private val viewModel by viewModel<SettingsViewModel>()

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.titleSettings.navigationIcon = null

        initListeners()
        observeViewModel()
    }

    private fun initListeners() {
        binding.btnShare.setOnClickListener { viewModel.shareApp() }
        binding.btnSupport.setOnClickListener { viewModel.openSupport() }
        binding.btnEula.setOnClickListener { viewModel.openTerms() }

        binding.switchDarkmode.setOnCheckedChangeListener { _, isChecked ->
            viewModel.updateTheme(isChecked)
        }
    }

    private fun observeViewModel() {
        viewModel.themeState.observe(viewLifecycleOwner) { isDark ->
            binding.switchDarkmode.isChecked = isDark
            (requireActivity().applicationContext as App).applyTheme(isDark)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}