package org.koitharu.kotatsu.list.ui

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewTreeObserver
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import org.koitharu.kotatsu.R
import kotlin.math.roundToInt

/** Positions portrait empty-state artwork relative to the window shared by the tabs. */
class EmptyStateLayout(context: Context, attrs: AttributeSet) :
	LinearLayout(context, attrs), ViewTreeObserver.OnPreDrawListener {

	private val position = IntArray(2)
	private val icon by lazy { findViewById<View>(R.id.icon) }
	private var list: RecyclerView? = null

	override fun onAttachedToWindow() {
		super.onAttachedToWindow()
		var ancestor = parent
		while (ancestor != null && ancestor !is RecyclerView) ancestor = ancestor.parent
		list = ancestor as? RecyclerView
		viewTreeObserver.addOnPreDrawListener(this)
	}

	override fun onDetachedFromWindow() {
		viewTreeObserver.removeOnPreDrawListener(this)
		list = null
		super.onDetachedFromWindow()
	}

	override fun onPreDraw(): Boolean {
		// Let long descriptions scroll naturally when the user has scrolled away from the top.
		if (!isShown || list?.canScrollVertically(-1) == true) return true
		getLocationInWindow(position)
		val anchor = (rootView.height * ARTWORK_CENTER_FRACTION).roundToInt()
		val halfIcon = if (icon.visibility == GONE) 0 else icon.height / 2
		val top = (anchor - position[1] - halfIcon).coerceAtLeast(0)
		if (paddingTop == top) return true
		setPadding(paddingLeft, top, paddingRight, paddingBottom)
		return false
	}

	private companion object {
		const val ARTWORK_CENTER_FRACTION = 0.43f
	}
}
