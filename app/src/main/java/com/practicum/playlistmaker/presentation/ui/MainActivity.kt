package com.practicum.playlistmaker.presentation.ui

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.databinding.ActivityMainBinding

// Основная и единственная активити
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Настройка отступов для системных bar'ов
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updatePadding(left = systemBars.left, top = systemBars.top, right = systemBars.right)
            insets
        }

        // Navigation Component
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.main_container_view) as NavHostFragment
        val navController = navHostFragment.navController

        binding.bottomNavigation.setupWithNavController(navController)

        // Видимость нижней панели
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                // для плеера: скрываем навигацию
                R.id.playerFragment -> {
                    binding.bottomNavigation.isVisible = false
                    binding.navSeparator.isVisible = false
                }
                // для остальных экранов: показываем навигацию
                else -> {
                    binding.bottomNavigation.isVisible = true
                    binding.navSeparator.isVisible = true
                }
            }
        }
    }
}