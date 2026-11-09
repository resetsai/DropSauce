package org.koitharu.kotatsu.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.viewModels
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import org.koitharu.kotatsu.R
import org.koitharu.kotatsu.core.util.ext.observeEvent
import org.koitharu.kotatsu.parsers.util.names
import org.koitharu.kotatsu.settings.compose.BaseComposeSettingsFragment
import org.koitharu.kotatsu.settings.compose.DropSauceTheme
import org.koitharu.kotatsu.settings.compose.NavigationSettingsItem
import org.koitharu.kotatsu.settings.compose.StorageUsageRow
import org.koitharu.kotatsu.settings.compose.SettingsGroup
import org.koitharu.kotatsu.settings.compose.SettingsScaffold
import org.koitharu.kotatsu.settings.compose.rememberBooleanPref
import org.koitharu.kotatsu.settings.compose.rememberStringPref
import org.koitharu.kotatsu.settings.userdata.storage.DataCleanupSettingsFragment
import org.koitharu.kotatsu.settings.userdata.storage.StorageUsage

@AndroidEntryPoint
class StorageAndNetworkSettingsFragment : BaseComposeSettingsFragment(R.string.storage_usage) {

	private val viewModel by viewModels<StorageAndNetworkSettingsViewModel>()
	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?,
	): View = ComposeView(requireContext()).apply {
		setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
		setContent {
			DropSauceTheme {
				val usage by viewModel.storageUsage.collectAsState(null)
				StorageNetworkScreen(
					usage = usage,
					onDataRemoval = {
						(activity as? SettingsActivity)?.openFragment(
							DataCleanupSettingsFragment::class.java,
							null,
							isFromRoot = false,
						)
					},
				)
			}
		}
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		viewModel.onError.observeEvent(viewLifecycleOwner) { err ->
			Snackbar.make(requireView(), err.message ?: "", Snackbar.LENGTH_SHORT).show()
		}
	}



}

@Composable
private fun StorageNetworkScreen(
	usage: StorageUsage?,
	onDataRemoval: () -> Unit,
) {
	SettingsScaffold {
		// Inline storage chart + legend (segmented bar). Replaces the simple Info row.
		item {
			StorageUsageRow(
				usage = usage,
				shape = androidx.compose.foundation.shape.RoundedCornerShape(
					topStart = 24.dp,
					topEnd = 24.dp,
					bottomStart = 4.dp,
					bottomEnd = 4.dp,
				),
			)
		}
		item { Spacer(Modifier.height(2.dp).fillMaxWidth()) }
		item {
			SettingsGroup {
				item { pos ->
					NavigationSettingsItem(
						title = stringResource(R.string.data_removal),
						icon = R.drawable.ic_delete,
						shape = androidx.compose.foundation.shape.RoundedCornerShape(
							topStart = 4.dp,
							topEnd = 4.dp,
							bottomStart = 24.dp,
							bottomEnd = 24.dp,
						),
						onClick = onDataRemoval,
					)
				}
			}
		}
		item { Spacer(Modifier.height(24.dp).fillMaxWidth()) }
	}
}
