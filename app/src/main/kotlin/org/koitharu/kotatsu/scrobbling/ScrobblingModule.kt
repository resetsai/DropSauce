package org.koitharu.kotatsu.scrobbling

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.Multibinds
import org.koitharu.kotatsu.scrobbling.common.domain.Scrobbler

@Module
@InstallIn(SingletonComponent::class)
interface ScrobblingModule {
	@Multibinds
	fun scrobblers(): Set<@JvmSuppressWildcards Scrobbler>
}
