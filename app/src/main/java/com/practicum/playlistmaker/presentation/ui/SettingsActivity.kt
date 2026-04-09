package com.practicum.playlistmaker.presentation.ui

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.switchmaterial.SwitchMaterial
import com.practicum.playlistmaker.App
import com.practicum.playlistmaker.R
import org.koin.androidx.viewmodel.ext.android.viewModel

class SettingsActivity : AppCompatActivity() {
    private val viewModel by viewModel<SettingsViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        initWindowInsets()
        initViews()
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
        val toolbar = findViewById<MaterialToolbar>(R.id.title_settings)
        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        findViewById<TextView>(R.id.btn_share).setOnClickListener { viewModel.shareApp() }
        findViewById<TextView>(R.id.btn_support).setOnClickListener { viewModel.openSupport() }
        findViewById<TextView>(R.id.btn_eula).setOnClickListener { viewModel.openTerms() }

        val themeSwitcher = findViewById<SwitchMaterial>(R.id.switch_darkmode)

        viewModel.themeState.observe(this) { isDark ->
            themeSwitcher.isChecked = isDark
            (applicationContext as App).applyTheme(isDark)
        }

        themeSwitcher.setOnCheckedChangeListener { _, isChecked ->
            viewModel.updateTheme(isChecked)
        }
    }
}