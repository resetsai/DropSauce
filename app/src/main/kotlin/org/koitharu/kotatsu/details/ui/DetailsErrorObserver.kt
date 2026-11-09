package org.koitharu.kotatsu.details.ui

import com.google.android.material.snackbar.Snackbar
import org.koitharu.kotatsu.R
import org.koitharu.kotatsu.core.exceptions.UnsupportedSourceException
import org.koitharu.kotatsu.core.exceptions.resolve.ErrorObserver
import org.koitharu.kotatsu.core.exceptions.resolve.ExceptionResolver
import org.koitharu.kotatsu.core.exceptions.resolve.addCopyErrorAction
import org.koitharu.kotatsu.core.util.ext.getDisplayMessage
import org.koitharu.kotatsu.core.util.ext.isNetworkError
import org.koitharu.kotatsu.core.util.ext.isSerializable
import org.koitharu.kotatsu.parsers.exception.NotFoundException
import org.koitharu.kotatsu.parsers.exception.ParseException

class DetailsErrorObserver(
	override val activity: androidx.fragment.app.FragmentActivity,
	private val snackbarHost: android.view.View,
	private val bottomSheet: android.view.View?,
	private val viewModel: DetailsViewModel,
	resolver: ExceptionResolver?,
) : ErrorObserver(
	snackbarHost, null, resolver,
	{ isResolved ->
		if (isResolved) {
			viewModel.reload()
		}
	},
) {

	override suspend fun emit(value: Throwable) {
		val displayMessage = if (value is UnsupportedSourceException) host.context.getString(R.string.offline_source_unavailable)
		else value.getDisplayMessage(host.context.resources)

		val snackbar = Snackbar.make(host, displayMessage, Snackbar.LENGTH_SHORT)
		snackbar.setAnchorView(bottomSheet)
		if (value is NotFoundException || value is UnsupportedSourceException) {
			snackbar.duration = Snackbar.LENGTH_INDEFINITE
		}
		when {
			value !is UnsupportedSourceException && canResolve(value) -> {
				snackbar.setAction(ExceptionResolver.getResolveStringId(value)) {
					resolve(value)
				}
			}

			value.isNetworkError() -> {
				snackbar.setAction(R.string.try_again) {
					viewModel.reload()
				}
			}
		}
		if (value.isSerializable()) {
			snackbar.addCopyErrorAction(value)
		}
		snackbar.show()
	}
}
