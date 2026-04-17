package com.hom8.app.presentation.common

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.isVisible
import com.hom8.app.R

/**
 * Top hom8 bar used across all screens.
 * Shows: optional back button | centered title | optional right action icon.
 */
class WireTopBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val btnBack: ImageButton
    private val tvTitle: TextView
    private val btnAction: ImageButton

    init {
        orientation = HORIZONTAL
        setBackgroundColor(context.getColor(R.color.colorSurface))
        minimumHeight = resources.getDimensionPixelSize(R.dimen.topBarHeight)
        gravity = android.view.Gravity.CENTER_VERTICAL

        LayoutInflater.from(context).inflate(R.layout.component_top_bar, this, true)
        btnBack = findViewById(R.id.btnTopBarBack)
        tvTitle = findViewById(R.id.tvTopBarTitle)
        btnAction = findViewById(R.id.btnTopBarAction)

        attrs?.let {
            val ta = context.obtainStyledAttributes(it, R.styleable.WireTopBar, 0, 0)
            try {
                tvTitle.text = ta.getString(R.styleable.WireTopBar_wireTitle) ?: ""
                btnBack.isVisible = ta.getBoolean(R.styleable.WireTopBar_wireShowBack, false)
                btnAction.isVisible = ta.getBoolean(R.styleable.WireTopBar_wireShowAction, false)
                val actionIcon = ta.getResourceId(R.styleable.WireTopBar_wireActionIcon, 0)
                if (actionIcon != 0) btnAction.setImageResource(actionIcon)
            } finally {
                ta.recycle()
            }
        }
    }

    fun setTitle(title: String) { tvTitle.text = title }
    fun setOnBackClick(l: () -> Unit) { btnBack.setOnClickListener { l() } }
    fun setOnActionClick(l: () -> Unit) { btnAction.setOnClickListener { l() } }
    fun showBack(show: Boolean) { btnBack.isVisible = show }
    fun showAction(show: Boolean, iconRes: Int = 0) {
        btnAction.isVisible = show
        if (iconRes != 0) btnAction.setImageResource(iconRes)
    }
}
