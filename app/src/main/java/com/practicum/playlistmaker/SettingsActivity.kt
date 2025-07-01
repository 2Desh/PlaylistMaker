package com.practicum.playlistmaker

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import androidx.core.net.toUri
import com.google.android.material.switchmaterial.SwitchMaterial
import android.content.Context

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val toolbar = findViewById<MaterialToolbar>(R.id.title_settings)

        // Кнопка Назад и закрытие активности
        toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Кнопка Поделиться приложением
        val shareButton = findViewById<TextView>(R.id.btn_share)
        shareButton.setOnClickListener {
            shareApp()
        }

        // Кнопка Написать в поддержку
        val supportButton = findViewById<TextView>(R.id.btn_support)
        supportButton.setOnClickListener {
            writeToSupport()
        }

        // Кнопка Пользовательское соглашение
        val agreementButton = findViewById<TextView>(R.id.btn_eula)
        agreementButton.setOnClickListener {
            openUserAgreement()
        }

        val themeSwitcher = findViewById<SwitchMaterial>(R.id.switch_darkmode)
        val app = applicationContext as App

        val currentThemeIsDark = getSharedPreferences(App.PLAYLIST_MAKER_PREFERENCES, Context.MODE_PRIVATE)
            .getBoolean(App.THEME_SWITCHER_KEY, false)

        themeSwitcher.isChecked = currentThemeIsDark

        themeSwitcher.setOnCheckedChangeListener { switcher, isChecked ->
            app.applyTheme(isChecked)
        }

    }


    // Функция для запуска системного диалога "Поделиться"
    private fun shareApp() {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, getString(R.string.app_share_link))
        }
        startActivity(Intent.createChooser(shareIntent, "Выбери приложение"))
    }

    // Функция для отправки письма в ТП
    private fun writeToSupport() {
        val recipient = "mailto:aleksandrchh@yandex.ru"

        // Создаём Intent для отправки email
        val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
            data = recipient.toUri()
            putExtra(Intent.EXTRA_SUBJECT, getString(R.string.email_subject))
            putExtra(Intent.EXTRA_TEXT, getString(R.string.email_body))
        }
        // Илюзия выбора
        startActivity(Intent.createChooser(emailIntent, "Написать письмо в"))
    }

    // Открытие польз соглашения в браузере по умолчанию
    private fun openUserAgreement() {
        val agreementUrl = getString(R.string.user_agreement_url)
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(agreementUrl))
        startActivity(browserIntent)
    }
}


