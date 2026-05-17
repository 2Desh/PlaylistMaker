package com.practicum.playlistmaker.presentation.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import androidx.annotation.AttrRes
import androidx.annotation.StyleRes
import androidx.core.content.ContextCompat
import com.practicum.playlistmaker.R

// Кнопка Играть/Пауза на экране плеера
class PlaybackButtonView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    @AttrRes defStyleAttr: Int = 0,
    @StyleRes defStyleRes: Int = 0
) : View(context, attrs, defStyleAttr, defStyleRes) {

    private var playDrawable: Drawable? = null
    private var pauseDrawable: Drawable? = null

    var isPlaying: Boolean = false
        private set

    init {
        // функция для получения цвета темы
        fun resolveColorAttribute(@AttrRes attr: Int): Int {
            val typedValue = TypedValue()
            context.theme.resolveAttribute(attr, typedValue, true)
            return typedValue.data
        }

        val tintColor = resolveColorAttribute(android.R.attr.colorPrimary)

        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.PlaybackButtonView,
            defStyleAttr,
            defStyleRes
        ).apply {
            try {
                val playImageResId = getResourceId(R.styleable.PlaybackButtonView_playImage, 0)
                val pauseImageResId = getResourceId(R.styleable.PlaybackButtonView_pauseImage, 0)

                if (playImageResId != 0) {
                    playDrawable = ContextCompat.getDrawable(context, playImageResId)?.apply {
                        mutate()
                        colorFilter = PorterDuffColorFilter(tintColor, PorterDuff.Mode.SRC_IN)
                    }
                }

                if (pauseImageResId != 0) {
                    pauseDrawable = ContextCompat.getDrawable(context, pauseImageResId)?.apply {
                        mutate()
                        colorFilter = PorterDuffColorFilter(tintColor, PorterDuff.Mode.SRC_IN)
                    }
                }
            } finally {
                recycle()
            }
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        playDrawable?.setBounds(0, 0, w, h)
        pauseDrawable?.setBounds(0, 0, w, h)
    }

    override fun onDraw(canvas: Canvas) {
        val drawableToDraw = if (isPlaying) pauseDrawable else playDrawable
        drawableToDraw?.draw(canvas)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isEnabled) return false

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                return true
            }
            MotionEvent.ACTION_UP -> {
                performClick()
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    override fun performClick(): Boolean {
        super.performClick()
        isPlaying = !isPlaying
        invalidate()
        return true
    }

    fun setState(playing: Boolean) {
        if (isPlaying != playing) {
            isPlaying = playing
            invalidate()
        }
    }
}