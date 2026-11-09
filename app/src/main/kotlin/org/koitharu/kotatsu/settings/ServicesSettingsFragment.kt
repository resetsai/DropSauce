package org.koitharu.kotatsu.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Switch
import androidx.compose.material3.VerticalDivider
import androidx.compose.ui.Alignment
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koitharu.kotatsu.R
import org.koitharu.kotatsu.core.nav.router
import org.koitharu.kotatsu.core.prefs.AppSettings
import org.koitharu.kotatsu.core.ui.dialog.showConfirmDialog
import org.koitharu.kotatsu.core.util.ext.getDisplayMessage
import org.koitharu.kotatsu.core.util.ext.printStackTraceDebug
import org.koitharu.kotatsu.scrobbling.common.domain.model.ScrobblerService
import org.koitharu.kotatsu.reader.ui.eyeReminderDuration
import org.koitharu.kotatsu.settings.compose.ActionSettingsItem
import org.koitharu.kotatsu.settings.compose.ChoiceDialog
import org.koitharu.kotatsu.settings.compose.BaseComposeSettingsFragment
import org.koitharu.kotatsu.settings.compose.DropSauceTheme
import org.koitharu.kotatsu.settings.compose.NavigationSettingsItem
import org.koitharu.kotatsu.settings.compose.SettingsGroup
import org.koitharu.kotatsu.settings.compose.SettingsItem
import org.koitharu.kotatsu.settings.compose.SettingsScaffold
import org.koitharu.kotatsu.settings.compose.SwitchSettingsItem
import org.koitharu.kotatsu.settings.compose.rememberBooleanPref
import org.koitharu.kotatsu.settings.compose.rememberIntPref
import javax.inject.Inject

@AndroidEntryPoint
class ServicesSettingsFragment : BaseComposeSettingsFragment(R.string.services) {

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?,
	): View = ComposeView(requireContext()).apply {
		setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
		setContent {
			DropSauceTheme {
				ServicesScreen(router::openStatistic)
			}
		}
	}

}

@Composable
private fun ServicesScreen(onOpenStatistics: () -> Unit) {
	var statsEnabled by rememberBooleanPref(AppSettings.KEY_STATS_ENABLED, true)
	var readingTime by rememberBooleanPref(AppSettings.KEY_READING_TIME, true)

	var eyeReminder by rememberBooleanPref(AppSettings.KEY_EYE_REMINDER, false)
	var eyeReminderInterval by rememberIntPref(
		AppSettings.KEY_EYE_REMINDER_INTERVAL,
		AppSettings.EYE_REMINDER_DEFAULT_INTERVAL,
	)
	var showIntervalDialog by remember { mutableStateOf(false) }
	val resources = LocalContext.current.resources

	val enabledLabel = stringResource(R.string.enabled)
	val disabledLabel = stringResource(R.string.disabled)

	if (showIntervalDialog) {
		val intervals = listOf(15 * 60, 20 * 60, 30 * 60, 45 * 60, 60 * 60)
		ChoiceDialog(
			title = stringResource(R.string.eye_reminder_interval),
			entries = intervals.map { eyeReminderDuration(resources, it) },
			selectedIndex = intervals.indexOf(eyeReminderInterval),
			onSelect = {
				eyeReminderInterval = intervals[it]
				eyeReminder = true
			},
			onDismiss = { showIntervalDialog = false },
			icon = R.drawable.ic_eye_tracking,
		)
	}

	SettingsScaffold {
		item {
			SettingsGroup(title = "General") {
				item { pos ->
					// Combined row: tapping the row opens the statistics page, the trailing
					// switch (split off by a divider) toggles collection on/off.
					SettingsItem(
						title = stringResource(R.string.reading_stats),
						subtitle = if (statsEnabled) enabledLabel else disabledLabel,
						icon = R.drawable.ic_timelapse,
						shape = pos.shape,
						onClick = onOpenStatistics,
						trailing = {
							Row(verticalAlignment = Alignment.CenterVertically) {
								VerticalDivider(modifier = Modifier.height(32.dp))
								Spacer(Modifier.width(14.dp))
								Switch(
									checked = statsEnabled,
									onCheckedChange = { statsEnabled = it },
								)
							}
						},
					)
				}
				item { pos ->
					SwitchSettingsItem(
						title = stringResource(R.string.reading_time_estimation),
						subtitle = stringResource(R.string.reading_time_estimation_summary),
						checked = readingTime,
						onCheckedChange = { readingTime = it },
						icon = R.drawable.ic_timer,

						shape = pos.shape,
					)
				}
				item { pos ->
					// Same split row as reading stats: the row picks the interval, the switch toggles.
					SettingsItem(
						title = stringResource(R.string.eye_reminder),
						subtitle = if (eyeReminder) {
							stringResource(R.string.eye_reminder_summary, eyeReminderDuration(resources, eyeReminderInterval))
						} else {
							disabledLabel
						},
						icon = R.drawable.ic_eye_tracking,
						shape = pos.shape,
						onClick = { showIntervalDialog = true },
						trailing = {
							Row(verticalAlignment = Alignment.CenterVertically) {
								VerticalDivider(modifier = Modifier.height(32.dp))
								Spacer(Modifier.width(14.dp))
								Switch(
									checked = eyeReminder,
									onCheckedChange = { eyeReminder = it },
								)
							}
						},
					)
				}
			}
		}
		item { Spacer(Modifier.height(24.dp).fillMaxWidth()) }
	}
}
