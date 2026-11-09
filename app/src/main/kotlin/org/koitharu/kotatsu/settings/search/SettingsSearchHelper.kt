package org.koitharu.kotatsu.settings.search

import android.annotation.SuppressLint
import android.content.Context
import androidx.fragment.app.Fragment
import dagger.Reusable
import org.koitharu.kotatsu.R
import org.koitharu.kotatsu.backup.local.ui.periodical.PeriodicalBackupSettingsFragment
import org.koitharu.kotatsu.core.LocalizedAppContext
import org.koitharu.kotatsu.core.prefs.AppSettings
import org.koitharu.kotatsu.settings.AppearanceSettingsFragment
import org.koitharu.kotatsu.settings.BackupSettingsFragment
import org.koitharu.kotatsu.settings.ReaderSettingsFragment
import org.koitharu.kotatsu.settings.appearance.PreviewSettingsFragment
import javax.inject.Inject

@Reusable
@SuppressLint("RestrictedApi")
class SettingsSearchHelper @Inject constructor(
	@LocalizedAppContext private val context: Context,
) {

	fun inflatePreferences(): List<SettingsItem> {
		val result = ArrayList<SettingsItem>()
		val ctx = context

		fun addItem(
			key: String,
			titleRes: Int,
			summaryRes: Int? = null,
			breadcrumbs: List<String>,
			fragmentClass: Class<out Fragment>,
			keywordRes: IntArray = IntArray(0),
			keywordArrayRes: IntArray = IntArray(0),
			keywordText: List<String> = emptyList(),
		) {
			val title = ctx.getString(titleRes)
			val summary = summaryRes?.let(ctx::getString).orEmpty()
			val keywords = keywordRes.map(ctx::getString) + keywordArrayRes.flatMap { ctx.resources.getStringArray(it).asList() } + keywordText
			val searchText = buildSearchText(title, key, summary, breadcrumbs, keywords)
			result.add(
				SettingsItem(
					key = key,
					title = title,
					breadcrumbs = breadcrumbs,
					searchText = searchText,
					fragmentClass = fragmentClass,
				),
			)
		}

		fun section(
			titleRes: Int,
			fragmentClass: Class<out Fragment>,
			block: (List<String>) -> Unit,
		) {
			val title = ctx.getString(titleRes)
			block(listOf(title))
		}

		fun group(sectionCrumbs: List<String>, groupTitle: String, block: (List<String>) -> Unit) {
			block(sectionCrumbs + groupTitle)
		}

		section(R.string.appearance, AppearanceSettingsFragment::class.java) { sectionCrumbs ->
			group(sectionCrumbs, "Theme") { crumbs ->
				addItem(AppSettings.KEY_COLOR_THEME, R.string.color_theme, breadcrumbs = crumbs, fragmentClass = AppearanceSettingsFragment::class.java)
				addItem(AppSettings.KEY_THEME, R.string.theme, breadcrumbs = crumbs, fragmentClass = AppearanceSettingsFragment::class.java, keywordArrayRes = intArrayOf(R.array.themes))
				addItem(AppSettings.KEY_THEME_AMOLED, R.string.black_dark_theme, R.string.black_dark_theme_summary, crumbs, AppearanceSettingsFragment::class.java)
				addItem(AppSettings.KEY_APP_LOCALE, R.string.language, breadcrumbs = crumbs, fragmentClass = AppearanceSettingsFragment::class.java)
				addItem(AppSettings.KEY_HIDE_STATUS_BAR, R.string.hide_status_bar, R.string.hide_status_bar_summary, crumbs, AppearanceSettingsFragment::class.java)
				addItem(AppSettings.KEY_UI_SCALE, R.string.ui_scale, breadcrumbs = crumbs, fragmentClass = AppearanceSettingsFragment::class.java, keywordRes = intArrayOf(R.string.ui_scale_smallest, R.string.ui_scale_smaller, R.string.ui_scale_default, R.string.ui_scale_larger, R.string.ui_scale_largest))
				addItem(AppSettings.KEY_HAPTIC_FEEDBACK, R.string.haptic_feedback, R.string.haptic_feedback_summary, crumbs, AppearanceSettingsFragment::class.java)
			}
			group(sectionCrumbs, ctx.getString(R.string.manga_list)) { crumbs ->
				addItem(AppSettings.KEY_LIST_MODE, R.string.list_mode, breadcrumbs = crumbs, fragmentClass = AppearanceSettingsFragment::class.java, keywordArrayRes = intArrayOf(R.array.list_modes))
				addItem(AppSettings.KEY_GRID_SIZE, R.string.grid_size, breadcrumbs = crumbs, fragmentClass = AppearanceSettingsFragment::class.java)
				addItem(AppSettings.KEY_QUICK_FILTER, R.string.show_quick_filters, R.string.show_quick_filters_summary, crumbs, AppearanceSettingsFragment::class.java)
				addItem(AppSettings.KEY_LIST_CHECKPOINT, R.string.list_checkpoint_settings, R.string.list_checkpoint_settings_summary, crumbs, AppearanceSettingsFragment::class.java)
				addItem(AppSettings.KEY_PROGRESS_INDICATORS, R.string.show_reading_indicators, breadcrumbs = crumbs, fragmentClass = AppearanceSettingsFragment::class.java)
				addItem(AppSettings.KEY_MANGA_LIST_BADGES, R.string.badges_in_lists, breadcrumbs = crumbs, fragmentClass = AppearanceSettingsFragment::class.java, keywordArrayRes = intArrayOf(R.array.list_badges))
			}
			group(sectionCrumbs, ctx.getString(R.string.details)) { crumbs ->
				addItem(AppSettings.KEY_COLLAPSE_DESCRIPTION, R.string.collapse_long_description, breadcrumbs = crumbs, fragmentClass = AppearanceSettingsFragment::class.java)
				addItem(AppSettings.KEY_PAGES_TAB, R.string.show_pages_thumbs, R.string.show_pages_thumbs_summary, crumbs, AppearanceSettingsFragment::class.java)
				addItem(AppSettings.KEY_DETAILS_TAB, R.string.default_tab, breadcrumbs = crumbs, fragmentClass = AppearanceSettingsFragment::class.java, keywordArrayRes = intArrayOf(R.array.details_tabs))
				addItem("details_appearance_nav", R.string.details_appearance, R.string.details_appearance_summary, crumbs, AppearanceSettingsFragment::class.java)
				addItem(AppSettings.KEY_DETAILS_UI, R.string.details_ui, breadcrumbs = crumbs + ctx.getString(R.string.details_appearance), fragmentClass = PreviewSettingsFragment::class.java, keywordRes = intArrayOf(R.string.details_ui_compact, R.string.details_ui_expressive))
				addItem(AppSettings.KEY_DETAILS_BACKDROP, R.string.details_backdrop, R.string.details_backdrop_summary, crumbs + ctx.getString(R.string.details_appearance), PreviewSettingsFragment::class.java)
				addItem(AppSettings.KEY_DETAILS_BACKDROP_BLUR_AMOUNT, R.string.details_backdrop_blur, breadcrumbs = crumbs + ctx.getString(R.string.details_appearance), fragmentClass = PreviewSettingsFragment::class.java)
			}
			group(sectionCrumbs, ctx.getString(R.string.main_screen)) { crumbs ->
				addItem(AppSettings.KEY_SEARCH_SUGGESTION_TYPES, R.string.search_suggestions, breadcrumbs = crumbs, fragmentClass = AppearanceSettingsFragment::class.java)
				addItem(AppSettings.KEY_NAV_MAIN, R.string.main_screen_sections, breadcrumbs = crumbs, fragmentClass = AppearanceSettingsFragment::class.java)
				addItem(AppSettings.KEY_MAIN_FAB, R.string.main_screen_fab, R.string.main_screen_fab_summary, crumbs, AppearanceSettingsFragment::class.java)
				addItem(AppSettings.KEY_NAV_LABELS, R.string.show_labels_in_navbar, breadcrumbs = crumbs, fragmentClass = AppearanceSettingsFragment::class.java)
				addItem(AppSettings.KEY_NAV_LEGACY, R.string.use_legacy_navigation_bar, R.string.use_legacy_navigation_bar_summary, crumbs, AppearanceSettingsFragment::class.java)
				addItem(AppSettings.KEY_BAR_BLUR, R.string.bar_blur, R.string.bar_blur_summary, crumbs, AppearanceSettingsFragment::class.java)
				addItem(AppSettings.KEY_NAV_PINNED, R.string.pin_navigation_ui, R.string.pin_navigation_ui_summary, crumbs, AppearanceSettingsFragment::class.java)
				addItem(AppSettings.KEY_EXIT_CONFIRM, R.string.exit_confirmation, R.string.exit_confirmation_summary, crumbs, AppearanceSettingsFragment::class.java)
				addItem(AppSettings.KEY_SHORTCUTS, R.string.history_shortcuts, R.string.history_shortcuts_summary, crumbs, AppearanceSettingsFragment::class.java)
			}
			group(sectionCrumbs, ctx.getString(R.string.privacy)) { crumbs ->
				addItem(AppSettings.KEY_PROTECT_APP, R.string.require_unlock, R.string.require_unlock_summary, crumbs, AppearanceSettingsFragment::class.java)
				addItem(AppSettings.KEY_PROTECT_APP_TIMEOUT, R.string.require_unlock_after, breadcrumbs = crumbs, fragmentClass = AppearanceSettingsFragment::class.java)
				addItem(AppSettings.KEY_SCREENSHOTS_POLICY, R.string.screenshots_policy, breadcrumbs = crumbs, fragmentClass = AppearanceSettingsFragment::class.java, keywordArrayRes = intArrayOf(R.array.screenshots_policy))
			}
		}


		section(R.string.reader_settings, ReaderSettingsFragment::class.java) { sectionCrumbs ->
			group(sectionCrumbs, "Mode") { crumbs ->
				addItem(AppSettings.KEY_READER_MODE, R.string.default_mode, breadcrumbs = crumbs, fragmentClass = ReaderSettingsFragment::class.java, keywordArrayRes = intArrayOf(R.array.reader_modes))
				addItem(AppSettings.KEY_READER_MODE_DETECT, R.string.detect_reader_mode, R.string.detect_reader_mode_summary, crumbs, ReaderSettingsFragment::class.java)
				addItem(AppSettings.KEY_TITLE_TAP_TO_READ, R.string.title_tap_to_read, R.string.title_tap_to_read_summary, crumbs, ReaderSettingsFragment::class.java)
				addItem(AppSettings.KEY_CHECK_DUPLICATES, R.string.duplicates_check, R.string.duplicates_check_summary, crumbs, ReaderSettingsFragment::class.java)
				addItem("manage_categories_nav", R.string.manage_categories, breadcrumbs = crumbs, fragmentClass = ReaderSettingsFragment::class.java, keywordRes = intArrayOf(R.string.categories, R.string.favourites))
			}
			group(sectionCrumbs, "Zoom") { crumbs ->
				addItem(AppSettings.KEY_ZOOM_MODE, R.string.scale_mode, breadcrumbs = crumbs, fragmentClass = ReaderSettingsFragment::class.java, keywordArrayRes = intArrayOf(R.array.zoom_modes))
				addItem(AppSettings.KEY_READER_DOUBLE_TAP_FILL, R.string.double_tap_zoom, R.string.double_tap_zoom_summary, crumbs, ReaderSettingsFragment::class.java)
				addItem(AppSettings.KEY_READER_ZOOM_BUTTONS, R.string.reader_zoom_buttons, R.string.reader_zoom_buttons_summary, crumbs, ReaderSettingsFragment::class.java)
				addItem(AppSettings.KEY_WEBTOON_ZOOM, R.string.webtoon_zoom, R.string.webtoon_zoom_summary, crumbs, ReaderSettingsFragment::class.java)
				addItem(AppSettings.KEY_WEBTOON_ZOOM_OUT, R.string.default_webtoon_zoom_out, breadcrumbs = crumbs, fragmentClass = ReaderSettingsFragment::class.java)
				addItem(AppSettings.KEY_WEBTOON_GAPS, R.string.webtoon_gaps, R.string.webtoon_gaps_summary, crumbs, ReaderSettingsFragment::class.java)
			}
			group(sectionCrumbs, "Controls") { crumbs ->
				addItem(AppSettings.KEY_READER_CONTROLS, R.string.reader_controls_in_bottom_bar, R.string.reader_controls_in_bottom_bar_summary, crumbs, ReaderSettingsFragment::class.java, keywordArrayRes = intArrayOf(R.array.reader_controls))
				addItem("reader_tap_actions", R.string.reader_actions, R.string.reader_actions_summary, crumbs, ReaderSettingsFragment::class.java)
				addItem(AppSettings.KEY_READER_CONTROL_LTR, R.string.reader_control_ltr, R.string.reader_control_ltr_summary, crumbs, ReaderSettingsFragment::class.java)
				addItem(AppSettings.KEY_READER_VOLUME_BUTTONS, R.string.switch_pages_volume_buttons, R.string.switch_pages_volume_buttons_summary, crumbs, ReaderSettingsFragment::class.java)
				addItem(AppSettings.KEY_READER_NAVIGATION_INVERTED, R.string.reader_navigation_inverted, R.string.reader_navigation_inverted_summary, crumbs, ReaderSettingsFragment::class.java)
				addItem(AppSettings.KEY_READER_ANIMATION, R.string.pages_animation, breadcrumbs = crumbs, fragmentClass = ReaderSettingsFragment::class.java, keywordArrayRes = intArrayOf(R.array.reader_animation))
				addItem(AppSettings.KEY_WEBTOON_PULL_GESTURE, R.string.enable_pull_gesture_title, R.string.enable_pull_gesture_summary, crumbs, ReaderSettingsFragment::class.java)
			}
			group(sectionCrumbs, "Image") { crumbs ->
				addItem(AppSettings.KEY_32BIT_COLOR, R.string.enhanced_colors, R.string.enhanced_colors_summary, crumbs, ReaderSettingsFragment::class.java)
				addItem(AppSettings.KEY_READER_OPTIMIZE, R.string.reader_optimize, R.string.reader_optimize_summary, crumbs, ReaderSettingsFragment::class.java)
				addItem(AppSettings.KEY_READER_UPSCALE, R.string.reader_upscale, R.string.reader_upscale_summary, crumbs, ReaderSettingsFragment::class.java)
				addItem(AppSettings.KEY_READER_CROP, R.string.crop_pages, breadcrumbs = crumbs, fragmentClass = ReaderSettingsFragment::class.java, keywordArrayRes = intArrayOf(R.array.reader_crop))
				addItem(AppSettings.KEY_READER_ROTATE_WIDE, R.string.rotate_wide_pages, R.string.rotate_wide_pages_summary, crumbs, ReaderSettingsFragment::class.java)
				addItem(AppSettings.KEY_READER_WIDE_SPLIT, R.string.wide_pages_method, R.string.wide_pages_split, crumbs, ReaderSettingsFragment::class.java)
				addItem(AppSettings.KEY_READER_ROTATE_WIDE_INVERT, R.string.rotate_wide_pages_direction, R.string.rotation_counter_clockwise, crumbs, ReaderSettingsFragment::class.java)
			}
			group(sectionCrumbs, "Display") { crumbs ->
				addItem(AppSettings.KEY_READER_FULLSCREEN, R.string.fullscreen_mode, R.string.reader_fullscreen_summary, crumbs, ReaderSettingsFragment::class.java)
				addItem(AppSettings.KEY_READER_ORIENTATION, R.string.screen_orientation, breadcrumbs = crumbs, fragmentClass = ReaderSettingsFragment::class.java, keywordArrayRes = intArrayOf(R.array.screen_orientations))
				addItem(AppSettings.KEY_READER_SCREEN_ON, R.string.keep_screen_on, R.string.keep_screen_on_summary, crumbs, ReaderSettingsFragment::class.java)
				addItem(AppSettings.KEY_READER_MULTITASK, R.string.reader_multitask, R.string.reader_multitask_summary, crumbs, ReaderSettingsFragment::class.java)
				addItem(AppSettings.KEY_CHAPTER_JUMP_DIALOG, R.string.chapter_jump_setting_title, R.string.chapter_jump_setting_summary, crumbs, ReaderSettingsFragment::class.java)
			}
			group(sectionCrumbs, "Reading info") { crumbs ->
				addItem(AppSettings.KEY_READER_BAR, R.string.reader_info_bar, R.string.reader_info_bar_summary, crumbs, ReaderSettingsFragment::class.java)
			}
			group(sectionCrumbs, "Background") { crumbs ->
				addItem(AppSettings.KEY_READER_BACKGROUND, R.string.background, breadcrumbs = crumbs, fragmentClass = ReaderSettingsFragment::class.java, keywordArrayRes = intArrayOf(R.array.reader_backgrounds))
				addItem(AppSettings.KEY_PAGES_NUMBERS, R.string.show_pages_numbers, R.string.show_pages_numbers_summary, crumbs, ReaderSettingsFragment::class.java)
			}
		}

		section(R.string.backup_restore, BackupSettingsFragment::class.java) { sectionCrumbs ->
			group(sectionCrumbs, ctx.getString(R.string.backup_restore)) { crumbs ->
				addItem("create_backup", R.string.create_backup, R.string.create_backup_summary, crumbs, BackupSettingsFragment::class.java)
				addItem("restore_local_backup", R.string.restore_backup, R.string.restore_summary, crumbs, BackupSettingsFragment::class.java)
				addItem("backup_periodic_screen", R.string.periodic_backups, R.string.periodic_backups_summary, crumbs, BackupSettingsFragment::class.java)
				val periodicCrumbs = crumbs + ctx.getString(R.string.periodic_backups)
				addItem(AppSettings.KEY_BACKUP_PERIODICAL_ENABLED, R.string.periodic_backups_enable, breadcrumbs = periodicCrumbs, fragmentClass = PeriodicalBackupSettingsFragment::class.java)
				addItem(AppSettings.KEY_BACKUP_PERIODICAL_OUTPUT, R.string.backups_output_directory, breadcrumbs = periodicCrumbs, fragmentClass = PeriodicalBackupSettingsFragment::class.java)
				addItem(AppSettings.KEY_BACKUP_PERIODICAL_FREQ, R.string.backup_frequency, breadcrumbs = periodicCrumbs, fragmentClass = PeriodicalBackupSettingsFragment::class.java)
				addItem(AppSettings.KEY_BACKUP_PERIODICAL_TRIM, R.string.delete_old_backups, R.string.delete_old_backups_summary, periodicCrumbs, PeriodicalBackupSettingsFragment::class.java)
				addItem(AppSettings.KEY_BACKUP_PERIODICAL_COUNT, R.string.max_backups_count, breadcrumbs = periodicCrumbs, fragmentClass = PeriodicalBackupSettingsFragment::class.java)
			}
			group(sectionCrumbs, ctx.getString(R.string.other_apps)) { crumbs ->
				addItem("restore_backup", R.string.restore_from_tachiyomi, R.string.restore_tachiyomi_summary, crumbs, BackupSettingsFragment::class.java)
				addItem("migrate_from_kotatsu", R.string.migrate_from_kotatsu, breadcrumbs = crumbs, fragmentClass = BackupSettingsFragment::class.java, keywordText = listOf("kotatsu"))
				addItem("export_to_mihon", R.string.export_to_mihon, R.string.export_to_mihon_summary, crumbs, BackupSettingsFragment::class.java, keywordText = listOf("mihon", "tachiyomi", "tachibk", "export"))
			}
		}

		return result
	}

	private fun buildSearchText(
		title: CharSequence,
		key: String,
		summary: String,
		breadcrumbs: List<String>,
		keywords: List<String>,
	): String = buildString {
		append(title)
		append(' ')
		append(key)
		if (summary.isNotBlank()) {
			append(' ')
			append(summary)
		}
		if (breadcrumbs.isNotEmpty()) {
			append(' ')
			append(breadcrumbs.joinToString(" "))
		}
		if (keywords.isNotEmpty()) {
			append(' ')
			append(keywords.joinToString(" "))
		}
	}
}
