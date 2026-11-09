package eu.kanade.tachiyomi.ui.webview

import android.app.Activity
import android.os.Bundle

/** Compatibility endpoint for callers of the offline build. */
class WebViewActivity : Activity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setResult(RESULT_CANCELED)
		finish()
	}
}
