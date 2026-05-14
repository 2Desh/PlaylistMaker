package com.practicum.playlistmaker.presentation.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.RectF
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.annotation.StyleRes
import androidx.core.content.ContextCompat
import com.practicum.playlistmaker.R
import androidx.core.graphics.createBitmap

// Кнопка Играть/Пауза на экране плеера
class PlaybackButtonView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    @AttrRes defStyleAttr: Int = 0,
    @StyleRes defStyleRes: Int = 0
) : View(context, attrs, defStyleAttr, defStyleRes) {

    private var playBitmap: Bitmap? = null
    private var pauseBitmap: Bitmap? = null
    private val imageRect = RectF()

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
                    playBitmap = getBitmapFromVectorDrawable(context, playImageResId, tintColor)
                }
                if (pauseImageResId != 0) {
                    pauseBitmap = getBitmapFromVectorDrawable(context, pauseImageResId, tintColor)
                }
            } finally {
                recycle()
            }
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        imageRect.left = 0f
        imageRect.top = 0f
        imageRect.right = w.toFloat()
        imageRect.bottom = h.toFloat()
    }

    override fun onDraw(canvas: Canvas) {
        val bitmapToDraw = if (isPlaying) pauseBitmap else playBitmap
        if (bitmapToDraw != null) {
            canvas.drawBitmap(bitmapToDraw, null, imageRect, null)
        }
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

    private fun getBitmapFromVectorDrawable(context: Context, drawableId: Int, @ColorInt tint: Int): Bitmap? {
        val drawable = ContextCompat.getDrawable(context, drawableId) ?: return null

        drawable.mutate()
        drawable.colorFilter = PorterDuffColorFilter(tint, PorterDuff.Mode.SRC_IN)

        val bitmap = createBitmap(drawable.intrinsicWidth.takeIf { it > 0 } ?: 1,
            drawable.intrinsicHeight.takeIf { it > 0 } ?: 1)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)

        return bitmap
    }
}