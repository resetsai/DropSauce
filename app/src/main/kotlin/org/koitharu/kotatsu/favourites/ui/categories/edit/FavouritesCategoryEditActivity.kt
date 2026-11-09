package org.koitharu.kotatsu.favourites.ui.categories.edit

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.view.WindowInsetsCompat
import dagger.hilt.android.AndroidEntryPoint
import org.koitharu.kotatsu.R
import org.koitharu.kotatsu.core.model.FavouriteCategory
import org.koitharu.kotatsu.core.ui.BaseActivity
import org.koitharu.kotatsu.core.util.ext.consumeAllSystemBarsInsets
import org.koitharu.kotatsu.core.util.ext.getDisplayMessage
import org.koitharu.kotatsu.core.util.ext.getSerializableCompat
import org.koitharu.kotatsu.core.util.ext.observe
import org.koitharu.kotatsu.core.util.ext.observeEvent
import org.koitharu.kotatsu.core.util.ext.systemBarsInsets
import org.koitharu.kotatsu.databinding.ActivityCategoryEditBinding
import org.koitharu.kotatsu.list.domain.ListSortOrder
import org.koitharu.kotatsu.settings.compose.DropSauceTheme

/**
 * Create/edit a favourites category. The activity owns the window — toolbar, Save button and the
 * form state — while [FavouritesCategoryEditScreen] renders everything below the toolbar.
 */
@AndroidEntryPoint
class FavouritesCategoryEditActivity :
	BaseActivity<ActivityCategoryEditBinding>(),
	View.OnClickListener {

	private val viewModel by viewModels<FavouritesCategoryEditViewModel>()

	private val name = mutableStateOf("")
	// Lazy: the view model (and the default sort it holds) is only reachable once the activity is attached.
	private val sortOrder by lazy { mutableStateOf(viewModel.defaultSortOrder) }
	private val isShelfEnabled = mutableStateOf(true)
	private val error = mutableStateOf<String?>(null)
	private val bottomInset = mutableIntStateOf(0)

	/** The category loads asynchronously; seed the form from it once, never over user edits. */
	private var isSeeded = false

	/**
	 * What the form looked like when it opened, so Save can stay disabled until something actually
	 * differs. Re-read from the category on every emission — after a config change the form is
	 * restored from the bundle, but the baseline still has to come from the stored category.
	 */
	private lateinit var baseline: FormState

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(ActivityCategoryEditBinding.inflate(layoutInflater))
		setDisplayHomeAsUp(isEnabled = true, showUpAsClose = true)
		baseline = FormState(sortOrder = viewModel.defaultSortOrder)
		viewBinding.buttonDone.setOnClickListener(this)
		viewBinding.composeView.setViewCompositionStrategy(
			ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed,
		)
		viewBinding.composeView.setContent {
			DropSauceTheme {
				val density = LocalDensity.current
				val isLoading by viewModel.isLoading.collectAsState()

				FavouritesCategoryEditScreen(
					name = name.value,
					onNameChange = {
						name.value = it
						updateDoneButton()
					},
					sortOrder = sortOrder.value,
					onSortOrderChange = {
						sortOrder.value = it
						updateDoneButton()
					},
					isShelfEnabled = isShelfEnabled.value,
					onShelfChange = {
						isShelfEnabled.value = it
						updateDoneButton()
					},
					error = error.value,
					enabled = !isLoading,
					bottomInset = with(density) { bottomInset.intValue.toDp() },
				)
			}
		}

		viewModel.onSaved.observeEvent(this) { finishAfterTransition() }
		viewModel.category.observe(this, ::onCategoryChanged)
		viewModel.isLoading.observe(this) { updateDoneButton() }
		viewModel.onError.observeEvent(this) { e ->
			error.value = e.getDisplayMessage(resources)
		}
	}

	override fun onApplyWindowInsets(
		v: View,
		insets: WindowInsetsCompat
	): WindowInsetsCompat {
		val barsInsets = insets.systemBarsInsets
		// The bottom inset is handed to Compose instead so the form can scroll under the nav bar.
		viewBinding.root.setPadding(barsInsets.left, barsInsets.top, barsInsets.right, 0)
		bottomInset.intValue = barsInsets.bottom
		return insets.consumeAllSystemBarsInsets()
	}

	override fun onSaveInstanceState(outState: Bundle) {
		super.onSaveInstanceState(outState)
		outState.putString(KEY_NAME, name.value)
		outState.putSerializable(KEY_SORT_ORDER, sortOrder.value)
		outState.putBoolean(KEY_SHELF, isShelfEnabled.value)
	}

	override fun onRestoreInstanceState(savedInstanceState: Bundle) {
		super.onRestoreInstanceState(savedInstanceState)
		name.value = savedInstanceState.getString(KEY_NAME).orEmpty()
		savedInstanceState.getSerializableCompat<ListSortOrder>(KEY_SORT_ORDER)?.let {
			sortOrder.value = it
		}
		isShelfEnabled.value = savedInstanceState.getBoolean(KEY_SHELF, true)
		isSeeded = true
		updateDoneButton()
	}

	override fun onClick(v: View) {
		when (v.id) {
			R.id.button_done -> viewModel.save(
				title = name.value.trim(),
				sortOrder = sortOrder.value,
				isVisibleOnShelf = isShelfEnabled.value,
			)
		}
	}

	private fun onCategoryChanged(category: FavouriteCategory?) {
		setTitle(if (category == null) R.string.create_category else R.string.edit_category)
		if (category == null) {
			// The flow starts at null and only later emits the loaded category, so a null here is
			// "not loaded yet" — seeding from it would blank the form for good.
			return
		}
		baseline = FormState(
			name = category.title,
			sortOrder = category.order,
			isShelfEnabled = category.isVisibleInLibrary,
		)
		if (!isSeeded) {
			isSeeded = true
			name.value = baseline.name
			sortOrder.value = baseline.sortOrder
			isShelfEnabled.value = baseline.isShelfEnabled
		}
		updateDoneButton()
	}

	private fun currentFormState() = FormState(
		name = name.value.trim(),
		sortOrder = sortOrder.value,
		isShelfEnabled = isShelfEnabled.value,
	)

	private fun updateDoneButton() {
		viewBinding.buttonDone.isEnabled = name.value.isNotBlank() &&
			!viewModel.isLoading.value &&
			currentFormState() != baseline
	}

	/** Snapshot of the editable fields, compared by value to tell whether anything was changed. */
	private data class FormState(
		val name: String = "",
		val sortOrder: ListSortOrder,
		val isShelfEnabled: Boolean = true,
	)

	companion object {

		const val NO_ID = -1L
		private const val KEY_SORT_ORDER = "sort"
		private const val KEY_NAME = "name"
		private const val KEY_SHELF = "shelf"
	}
}
