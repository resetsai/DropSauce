package org.koitharu.kotatsu.scrobbling.common.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import androidx.core.net.toUri
import org.koitharu.kotatsu.scrobbling.common.domain.ScrobblerRepositoryMap
import org.koitharu.kotatsu.scrobbling.common.domain.model.ScrobblerService
import org.koitharu.kotatsu.scrobbling.common.domain.model.ScrobblerUser
import javax.inject.Inject

class ScrobblerAuthHelper @Inject constructor(
	private val repositoriesMap: ScrobblerRepositoryMap,
) {

	fun isAuthorized(scrobbler: ScrobblerService) = repositoriesMap[scrobbler].isAuthorized

	fun getCachedUser(scrobbler: ScrobblerService): ScrobblerUser? {
		return repositoriesMap[scrobbler].cachedUser
	}

	suspend fun getUser(scrobbler: ScrobblerService): ScrobblerUser {
		return repositoriesMap[scrobbler].loadUser()
	}

	fun startAuth(context: Context, scrobbler: ScrobblerService): Result<Unit> =
		Result.failure(UnsupportedOperationException("Online accounts are disabled"))
}
