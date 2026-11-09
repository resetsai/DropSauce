package org.koitharu.kotatsu.settings.work

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koitharu.kotatsu.backup.local.ui.periodical.PeriodicalBackupWorker
import org.koitharu.kotatsu.core.prefs.AppSettings
import org.koitharu.kotatsu.core.util.ext.processLifecycleScope
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkScheduleManager @Inject constructor(
	private val settings: AppSettings,
	private val backupScheduler: PeriodicalBackupWorker.Scheduler,
	@ApplicationContext private val context: Context,
) : SharedPreferences.OnSharedPreferenceChangeListener {

	override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
		when (key) {
			AppSettings.KEY_BACKUP_PERIODICAL_ENABLED,
			AppSettings.KEY_BACKUP_PERIODICAL_FREQ,
			AppSettings.KEY_BACKUP_PERIODICAL_OUTPUT -> updateWorker(
				scheduler = backupScheduler,
				isEnabled = settings.isPeriodicalBackupEnabled && settings.periodicalBackupDirectory != null,
				force = key != AppSettings.KEY_BACKUP_PERIODICAL_ENABLED,
			)


		}
	}

	fun init() {
		settings.subscribe(this)
		val work = androidx.work.WorkManager.getInstance(context)
		for (name in listOf("tracking", "tracking_oneshot", "suggestions", "suggestions_oneshot",
			"gdrive_sync_periodic", "gdrive_sync_soon", "extension_auto_updates", "extension_auto_updates_now")) {
			work.cancelUniqueWork(name)
		}
		processLifecycleScope.launch(Dispatchers.Default) {
			updateWorkerImpl(
				scheduler = backupScheduler,
				isEnabled = settings.isPeriodicalBackupEnabled && settings.periodicalBackupDirectory != null,
				force = false,
			)

		}
	}

	private fun updateWorker(scheduler: PeriodicWorkScheduler, isEnabled: Boolean, force: Boolean) {
		processLifecycleScope.launch(Dispatchers.Default) {
			updateWorkerImpl(scheduler, isEnabled, force)
		}
	}

	private suspend fun updateWorkerImpl(scheduler: PeriodicWorkScheduler, isEnabled: Boolean, force: Boolean) {
		if (force || scheduler.isScheduled() != isEnabled) {
			if (isEnabled) {
				scheduler.schedule()
			} else {
				scheduler.unschedule()
			}
		}
	}
}
