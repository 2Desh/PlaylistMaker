package com.practicum.playlistmaker.presentation.ui

import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.switchmaterial.SwitchMaterial
import com.practicum.playlistmaker.App
import com.practicum.playlistmaker.Creator
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.domain.api.SettingsInteractor
import com.practicum.playlistmaker.domain.api.SharingInteractor

class SettingsActivity : AppCompatActivity() {

    private lateinit var sharingInteractor: SharingInteractor
    private lateinit var settingsInteractor: SettingsInteractor

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        initWindowInsets()
        initInteractors()
        initViews()
    }

    private fun initWindowInsets() {
        val rootView = findViewById<android.view.View>(R.id.settings)
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

    private fun initInteractors() {
        sharingInteractor = Creator.provideSharingInteractor(this)
        settingsInteractor = Creator.provideSettingsInteractor(this)
    }

    private fun initViews() {
        // настройка Toolbar
        val toolbar = findViewById<MaterialToolbar>(R.id.title_settings)
        toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        setupSharingButtons()
        setupThemeSwitcher()
    }

    private fun setupSharingButtons() {
        findViewById<TextView>(R.id.btn_share).setOnClickListener {
            sharingInteractor.shareApp()
        }

        findViewById<TextView>(R.id.btn_support).setOnClickListener {
            sharingInteractor.openSupport()
        }

        findViewById<TextView>(R.id.btn_eula).setOnClickListener {
            sharingInteractor.openTerms()
        }
    }

    private fun setupThemeSwitcher() {
        val themeSwitcher = findViewById<SwitchMaterial>(R.id.switch_darkmode)
        themeSwitcher.isChecked = settingsInteractor.getThemeSettings()
        themeSwitcher.setOnCheckedChangeListener { _, isChecked ->
            // сохраняем новое состояние в SharedPreferences через интерактор
            settingsInteractor.updateThemeSetting(isChecked)
            (applicationContext as App).applyTheme(isChecked)
        }
    }
}