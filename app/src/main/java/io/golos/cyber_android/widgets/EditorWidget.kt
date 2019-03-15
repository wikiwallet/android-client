package io.golos.cyber_android.widgets

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import io.golos.cyber_android.R
import kotlinx.android.synthetic.main.view_editor_widget.view.*

/**
 * Custom view which represents Editor Widget. It may produce two types of events -
 * gallery button click (handled via [galleryClickListener]) and widget click itself (handled via [widgetClickListener]).
 * Also widget can display user avatar (via [loadUserAvatar] method).
 * This view doesn't support any custom xml attributes.
 */
internal class EditorWidget : LinearLayout {

    /**
     * Click listener for gallery button
     */
    var galleryClickListener: (() -> Unit)? = null

    /**
     * Click listener for all of the view except gallery button
     */
    var widgetClickListener: (() -> Unit)? = null

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(context)
    }

    private fun init(context: Context) {
        inflate(context, R.layout.view_editor_widget, this)

        galleryButton.setOnClickListener { galleryClickListener?.invoke() }
        rootView.setOnClickListener { widgetClickListener?.invoke() }
    }

    /**
     * Loads user avatar into avatar ImageView
     */
    fun loadUserAvatar(url: String) {
        //TODO replace with real implementation
        avatar.setImageResource(R.drawable.img_example_avatar)
    }
}