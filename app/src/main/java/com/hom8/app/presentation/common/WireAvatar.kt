package com.hom8.app.presentation.common

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.hom8.app.R

/**
 * Circular avatar that shows either an image URL (loaded via Coil)
 * or initials on a colored background.
 */
class WireAvatar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    enum class AvatarSize(val dp: Int) {
        XS(20), SM(28), MD(40), LG(56)
    }

    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = resources.getDimension(R.dimen.avatarBorderWidth)
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        color = ContextCompat.getColor(context, R.color.avatarText)
        typeface = Typeface.DEFAULT_BOLD
    }

    var initials: String = ""
        set(value) { field = value.take(2).uppercase(); invalidate() }

    var avatarSize: AvatarSize = AvatarSize.MD
        set(value) { field = value; requestLayout() }

    var showRing: Boolean = false
        set(value) {
            field = value
            borderPaint.strokeWidth = if (value) 3f * resources.displayMetrics.density
            else resources.getDimension(R.dimen.avatarBorderWidth)
            borderPaint.color = if (value) ContextCompat.getColor(context, R.color.colorPrimary)
            else ContextCompat.getColor(context, R.color.avatarBorder)
            invalidate()
        }

    private val memberColors = intArrayOf(
        R.color.memberColor1, R.color.memberColor2, R.color.memberColor3,
        R.color.memberColor4, R.color.memberColor5, R.color.memberColor6
    )

    init {
        bgPaint.color = ContextCompat.getColor(context, R.color.avatarBackground)
        borderPaint.color = ContextCompat.getColor(context, R.color.avatarBorder)
        borderPaint.style = Paint.Style.STROKE

        attrs?.let {
            val ta = context.obtainStyledAttributes(it, R.styleable.WireAvatar, 0, 0)
            try {
                initials = ta.getString(R.styleable.WireAvatar_wireInitials) ?: ""
                val sizeOrdinal = ta.getInt(R.styleable.WireAvatar_wireAvatarSize, AvatarSize.MD.ordinal)
                avatarSize = AvatarSize.entries[sizeOrdinal]
                showRing = ta.getBoolean(R.styleable.WireAvatar_wireShowRing, false)
                val customColor = ta.getColor(R.styleable.WireAvatar_wireAvatarColor, 0)
                if (customColor != 0) bgPaint.color = customColor
            } finally {
                ta.recycle()
            }
        }
    }

    fun setColorForIndex(index: Int) {
        val colorRes = memberColors[index % memberColors.size]
        val color = ContextCompat.getColor(context, colorRes)
        // Use a lighter version for background
        bgPaint.color = color and 0x33FFFFFF or 0x1A000000
        textPaint.color = color
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val size = (avatarSize.dp * resources.displayMetrics.density).toInt()
        setMeasuredDimension(size, size)
    }

    override fun onDraw(canvas: Canvas) {
        val cx = width / 2f
        val cy = height / 2f
        val radius = minOf(cx, cy) - borderPaint.strokeWidth / 2f
        canvas.drawCircle(cx, cy, radius, bgPaint)
        canvas.drawCircle(cx, cy, radius, borderPaint)
        if (initials.isNotEmpty()) {
            textPaint.textSize = width * 0.33f
            val textY = cy - (textPaint.descent() + textPaint.ascent()) / 2f
            canvas.drawText(initials, cx, textY, textPaint)
        }
    }
}
