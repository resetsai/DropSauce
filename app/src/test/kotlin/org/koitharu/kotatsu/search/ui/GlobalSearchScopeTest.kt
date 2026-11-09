package org.koitharu.kotatsu.search.ui

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class GlobalSearchScopeTest {

	@Test
	fun `global search uses a transparent selection handle window`() {
		val layouts = listOf(
			"layout/activity_main.xml",
			"layout-w600dp-h480dp-land/activity_main.xml",
			"layout-w840dp-h480dp/activity_main.xml",
		)

		assertTrue(layouts.all { resource(it).contains("@style/ThemeOverlay.Kotatsu.SearchText") })
		assertTrue(resource("values/styles.xml").contains("""android:popupBackground">@android:color/transparent"""))
		assertTrue(resource("values/styles.xml").contains("""android:popupElevation">0dp"""))
	}

	@Test
	fun `offline search menu offers query kinds without remote source filters`() {
		val menu = resource("menu/popup_search_kind.xml")
		for (kind in listOf("simple", "title", "author", "tag")) {
			assertTrue(menu.contains("@+id/action_kind_$kind"))
		}
		assertFalse(menu.contains("action_filter_pinned_only"))
		assertFalse(menu.contains("action_filter_local_only"))
	}

	@Test
	fun `offline results include favorites history and local storage regardless of source kind`() {
		val search = source("search/ui/multi/SearchViewModel.kt")
		for (section in listOf("searchFavorites", "searchHistory", "searchLocal")) {
			assertTrue(search.contains("appendResult($section())"))
		}
		assertFalse(search.contains("isNovelSource"))
		assertFalse(search.contains("isNovelScope"))
	}

	private fun resource(relativePath: String): String {
		return sequenceOf(
			File("src/main/res", relativePath),
			File("app/src/main/res", relativePath),
		).firstOrNull(File::isFile)?.readText()
			?: error("Cannot find production resource: $relativePath")
	}

	private fun source(relativePath: String): String {
		return sequenceOf(
			File("src/main/kotlin/org/koitharu/kotatsu", relativePath),
			File("app/src/main/kotlin/org/koitharu/kotatsu", relativePath),
		).firstOrNull(File::isFile)?.readText()
			?.replace(Regex("""//[^\r\n]*"""), "")
			?.replace(Regex("""\s+"""), "")
			?: error("Cannot find production source: $relativePath")
	}
}
