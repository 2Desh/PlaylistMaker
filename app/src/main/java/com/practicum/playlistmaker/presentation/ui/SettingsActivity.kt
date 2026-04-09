package com.practicum.playlistmaker.presentation.ui

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.switchmaterial.SwitchMaterial
import com.practicum.playlistmaker.App
import com.practicum.playlistmaker.R

// View-компонент экрана настроек. Подписывается на LiveData состояния темы и передает UI события во ViewModel.
class SettingsActivity : AppCompatActivity() {

    private lateinit var themeSwitcher: SwitchMaterial
    private lateinit var viewModel: SettingsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // Инициализируем ViewModel с помощью фабрики
        viewModel = ViewModelProvider(this, SettingsViewModelFactory(this))[SettingsViewModel::class.java]

        initWindowInsets()
        initViews()
        setupListeners()
        observeViewModel()
    }

    private fun initWindowInsets() {
        val rootView = findViewById<View>(R.id.settings)
        ViewCompat.setOnApplyWindowInsetsListener(rootView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updatePadding(
                top = systemBars.top,
                bottom = systemBars.bottom,
                left = systemBars.left,
                right = systemBars.right
            )
            insets
        }
    }

    private fun initViews() {
        themeSwitcher = findViewById(R.id.switch_darkmode)

        val toolbar = findViewById<MaterialToolbar>(R.id.title_settings)
        toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupListeners() {
        findViewById<TextView>(R.id.btn_share).setOnClickListener {
            viewModel.shareApp()
        }

        findViewById<TextView>(R.id.btn_support).setOnClickListener {
            viewModel.openSupport()
        }

        findViewById<TextView>(R.id.btn_eula).setOnClickListener {
            viewModel.openTerms()
        }

        themeSwitcher.setOnCheckedChangeListener { _, isChecked ->
            viewModel.updateTheme(isChecked)
        }
    }

    private fun observeViewModel() {
        viewModel.themeState.observe(this) { isDark ->
            if (themeSwitcher.isChecked != isDark) {
                themeSwitcher.isChecked = isDark
            }

            (applicationContext as App).applyTheme(isDark)
        }
    }
}