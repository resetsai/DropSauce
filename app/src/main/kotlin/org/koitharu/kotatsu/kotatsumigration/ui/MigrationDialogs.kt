package org.koitharu.kotatsu.kotatsumigration.ui

import android.content.Context
import org.koitharu.kotatsu.R
import org.koitharu.kotatsu.core.ui.dialog.showConfirmDialog

fun Context.showMissingSourcesDialog(missingSources: List<String>) {
	if (missingSources.isEmpty()) {
		return
	}
	showConfirmDialog(
		context = this,
		icon = R.drawable.ic_info_outline,
		title = getString(R.string.offline_sources_title),
		message = getString(
			R.string.offline_sources_message,
			missingSources.joinToString("\n• ", prefix = "• "),
		),
		onConfirm = {},
	)
}
