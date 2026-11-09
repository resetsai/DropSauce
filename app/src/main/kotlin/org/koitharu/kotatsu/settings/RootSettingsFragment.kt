package org.koitharu.kotatsu.settings

import android.os.Bundle
import org.koitharu.kotatsu.core.ui.ReadingPanes
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.koitharu.kotatsu.settings.compose.BaseComposeSettingsFragment
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import dagger.hilt.android.AndroidEntryPoint
import org.koitharu.kotatsu.R
import org.koitharu.kotatsu.settings.compose.CategoryPalette
import org.koitharu.kotatsu.settings.compose.DropSauceTheme
import org.koitharu.kotatsu.settings.compose.SettingsGroup
import org.koitharu.kotatsu.settings.compose.SettingsItem
import org.koitharu.kotatsu.settings.compose.SettingsScaffold

@AndroidEntryPoint
class RootSettingsFragment : BaseComposeSettingsFragment(R.string.settings) {

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?,
	): View = ComposeView(requireContext()).apply {
		setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
		setContent {
			DropSauceTheme {
				RootSettingsContent(::openSection)
			}
		}
	}

	private fun openSection(section: SettingsSection) {
		val activity = activity as? SettingsActivity ?: return
		activity.openFragment(section.fragmentClass, null, isFromRoot = true)
	}
}

private enum class SettingsSection(
	val titleRes: Int,
	val iconRes: Int,
	val paletteKey: String,
	val summaryRes: IntArray,
	val fragmentClass: Class<out Fragment>,
	val tintIcon: Boolean = true,
) {
	APPEARANCE(
		R.string.appearance, R.drawable.ic_appearance, "appearance",
		intArrayOf(R.string.theme, R.string.list_mode, R.string.language),
		AppearanceSettingsFragment::class.java,
	),
	READER(
		R.string.reader_settings, R.drawable.ic_book_page, "reader",
		intArrayOf(R.string.read_mode, R.string.scale_mode, R.string.switch_pages),
		ReaderSettingsFragment::class.java,
	),
	STORAGE(
		R.string.storage_usage, R.drawable.ic_usage, "storage",
		intArrayOf(R.string.data_removal), StorageAndNetworkSettingsFragment::class.java,
	),
	STATISTICS(
		R.string.reading_stats, R.drawable.ic_timelapse, "services",
		intArrayOf(R.string.reading_time_estimation, R.string.eye_reminder), ServicesSettingsFragment::class.java,
	),
	BACKUP(
		R.string.backup_restore, R.drawable.ic_backup_restore, "backup",
		intArrayOf(R.string.restore_backup),
		BackupSettingsFragment::class.java,
	),
}

@Composable
private fun RootSettingsContent(
	onSectionClick: (SettingsSection) -> Unit,
) {
    SettingsScaffold {
        item {
            ReadingPanes(leading = {
                SectionGroup(R.string.epub_reading_tab, listOf(SettingsSection.READER, SettingsSection.APPEARANCE, SettingsSection.STATISTICS), onSectionClick)
            }, trailing = {
                SectionGroup(R.string.local_storage, listOf(SettingsSection.STORAGE, SettingsSection.BACKUP), onSectionClick)
            })
        }
    }
}

@Composable
private fun SectionGroup(title: Int, sections: List<SettingsSection>, onClick: (SettingsSection) -> Unit) {
    val context = LocalContext.current
    SettingsGroup(modifier = Modifier.fillMaxWidth(), title = stringResource(title)) {
        sections.forEach { section ->
            item { pos ->
                SettingsItem(
                    title = stringResource(section.titleRes),
                    subtitle = section.summaryRes.joinToString { context.getString(it) },
                    icon = section.iconRes,
                    iconColors = CategoryPalette.forKey(section.paletteKey),
                    tintIcon = section.tintIcon,
                    shape = pos.shape,
                    onClick = { onClick(section) },
                )
            }
        }
    }
}
