package org.koitharu.kotatsu.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.koitharu.kotatsu.settings.compose.BaseComposeSettingsFragment
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import dagger.hilt.android.AndroidEntryPoint
import org.koitharu.kotatsu.BuildConfig
import org.koitharu.kotatsu.R
import org.koitharu.kotatsu.core.nav.router
import org.koitharu.kotatsu.settings.search.SettingsSearchMenuProvider
import org.koitharu.kotatsu.settings.search.SettingsSearchViewModel
import org.koitharu.kotatsu.settings.compose.CategoryPalette
import org.koitharu.kotatsu.settings.compose.DropSauceTheme
import org.koitharu.kotatsu.settings.compose.PlainInfoSettingsItem
import org.koitharu.kotatsu.settings.compose.SettingsGroup
import org.koitharu.kotatsu.settings.compose.SettingsItem
import org.koitharu.kotatsu.settings.compose.SettingsScaffold
import org.koitharu.kotatsu.settings.tracker.TrackerSettingsFragment
import javax.inject.Inject

/**
 * Redesigned settings landing screen — Compose-based, modeled after PixelPlayer's settings.
 * Uses M3 LargeTopAppBar (collapses on scroll) and a single rounded SettingsGroup with the
 * dynamic-corner-radius pattern for the 9 section cards.
 *
 * Hides the host SettingsActivity's MaterialToolbar while attached, since we draw our own
 * top bar inside Compose. Sub-screens still use the activity's toolbar.
 */
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
	val ctx = LocalContext.current
	SettingsScaffold {
		item {
			SettingsGroup {
				// SettingsGroup's DSL block is NOT @Composable — @Composable calls
				// (stringResource, MaterialTheme.colorScheme, …) only happen inside
				// each `item { pos -> ... }` body which IS @Composable.
				SettingsSection.values().forEach { section ->
					item { pos ->
						val subtitle = section.summaryRes.joinToString { ctx.getString(it) }
						SettingsItem(
							title = stringResource(section.titleRes),
							subtitle = subtitle,
							icon = section.iconRes,
							iconColors = CategoryPalette.forKey(section.paletteKey),
							tintIcon = section.tintIcon,
							shape = pos.shape,
							onClick = { onSectionClick(section) },
						)
					}
				}
			}
		}
		item { Spacer(Modifier.height(8.dp).fillMaxWidth()) }
		item { Spacer(Modifier.height(24.dp).fillMaxWidth()) }
	}
}

