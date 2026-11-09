package org.koitharu.kotatsu.core.parser

import android.content.Context
import androidx.annotation.AnyThread
import androidx.collection.ArrayMap
import dagger.hilt.android.qualifiers.ApplicationContext
import org.koitharu.kotatsu.core.cache.MemoryContentCache
import org.koitharu.kotatsu.core.model.LocalMangaSource
import org.koitharu.kotatsu.core.model.MangaSource as ResolveMangaSource
import org.koitharu.kotatsu.core.model.MissingMangaSource
import org.koitharu.kotatsu.core.model.MangaSourceInfo
import org.koitharu.kotatsu.core.model.UnknownMangaSource
import org.koitharu.kotatsu.local.data.LocalMangaRepository
import org.koitharu.kotatsu.lnreader.LnMangaRepository
import org.koitharu.kotatsu.lnreader.LnPluginManager
import org.koitharu.kotatsu.lnreader.js.JsHost
import org.koitharu.kotatsu.lnreader.model.LnMangaSource
import org.koitharu.kotatsu.mihon.LazyMihonMangaRepository
import org.koitharu.kotatsu.mihon.MihonExtensionManager
import org.koitharu.kotatsu.mihon.MihonMangaRepository
import org.koitharu.kotatsu.mihon.model.MihonMangaSource
import org.koitharu.kotatsu.parsers.model.Manga
import org.koitharu.kotatsu.parsers.model.MangaChapter
import org.koitharu.kotatsu.parsers.model.MangaListFilter
import org.koitharu.kotatsu.parsers.model.MangaListFilterCapabilities
import org.koitharu.kotatsu.parsers.model.MangaListFilterOptions
import okhttp3.Headers
import okhttp3.Response
import org.koitharu.kotatsu.parsers.model.MangaPage
import org.koitharu.kotatsu.parsers.model.MangaSource
import org.koitharu.kotatsu.parsers.model.SortOrder
import java.lang.ref.WeakReference
import javax.inject.Inject
import javax.inject.Singleton

interface MangaRepository {

	val source: MangaSource

	val sortOrders: Set<SortOrder>

	var defaultSortOrder: SortOrder

	val filterCapabilities: MangaListFilterCapabilities

	suspend fun getList(offset: Int, order: SortOrder?, filter: MangaListFilter?): List<Manga>

	suspend fun getDetails(manga: Manga): Manga

	/**
	 * [manga] is the one [chapter] belongs to. Like Mihon, a chapter url is only unique within its
	 * manga (AllAnime uses the bare chapter number), so per-chapter state is keyed by both.
	 */
	suspend fun getPages(manga: Manga, chapter: MangaChapter): List<MangaPage>

	suspend fun getPageUrl(page: MangaPage): String

	/** Public web URL for a single [chapter] of [manga], or null for sources without a browsable chapter page. */
	suspend fun getChapterUrl(manga: Manga, chapter: MangaChapter): String? = null

	/**
	 * Returns [chapter]'s content as html for text sources (web novels), or null for image sources.
	 * Consumed by the native epub reader, which renders the html into a Spanned.
	 */
	suspend fun getChapterHtml(chapter: MangaChapter): String? = null

	/** Returns extension-specific HTTP headers for the image at [imageUrl], or null for non-extension sources. */
	suspend fun getImageRequestHeaders(imageUrl: String, page: MangaPage): Headers? = null

	/**
	 * Returns the full image [Response] for [pageUrl], or null to fall back to the default fetch path.
	 * Extensions may override [HttpSource.getImage] to decrypt or transform scrambled images;
	 * this hook ensures those transforms are applied before the bytes hit the disk cache.
	 */
	suspend fun getImageStream(pageUrl: String, page: MangaPage): Response? = null

	/**
	 * Fetches a non-page image (e.g. a cover/thumbnail) through the source's own client + headers,
	 * or null to fall back to the default fetch path. Mirrors how Mihon downloads covers via the
	 * source client — some sources (e.g. Comick) 403 the app's shared client on their cover CDN even
	 * though reading works. Used by code paths that fetch covers OUTSIDE Coil (Coil already routes
	 * Mihon covers through MihonImageFetcher).
	 */
	suspend fun getCoverStream(url: String): Response? = null

	suspend fun getFilterOptions(): MangaListFilterOptions

	suspend fun getRelated(seed: Manga): List<Manga>

	suspend fun find(manga: Manga): Manga? {
		val list = getList(0, SortOrder.RELEVANCE, MangaListFilter(query = manga.title))
		return list.find { x -> x.id == manga.id }
	}

	@Singleton
	class Factory @Inject constructor(
		private val localMangaRepository: LocalMangaRepository,
	) {
		@AnyThread
		fun create(source: MangaSource): MangaRepository {
			val actual = if (source is MangaSourceInfo) source.mangaSource else source
			return if (actual == LocalMangaSource) localMangaRepository else EmptyMangaRepository(actual)
		}

	}
}

/**
 * A repository that can bypass its in-memory details cache for update checks.
 *
 * This is separate from [MangaRepository.getDetails] because ordinary details screens should still
 * benefit from caching, while chapter checks must always ask the remote source for its latest list.
 */
interface FreshMangaDetailsRepository {

	suspend fun getFreshDetails(manga: Manga): Manga
}
