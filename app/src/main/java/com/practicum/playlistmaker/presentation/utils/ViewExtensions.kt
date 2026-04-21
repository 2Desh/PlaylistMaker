package com.practicum.playlistmaker.presentation.utils

import android.view.View
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Вспомогательные функции для View
fun View.setDebouncedOnClickListener(
    delayMs: Long = 1000L,
    onClick: (View) -> Unit
) {
    var debounceJob: Job? = null

    setOnClickListener { view ->
        if (debounceJob?.isActive == true) {
            return@setOnClickListener
        }
        debounceJob = findViewTreeLifecycleOwner()?.lifecycleScope?.launch {
            onClick(view)
            delay(delayMs)
        }
    }
}