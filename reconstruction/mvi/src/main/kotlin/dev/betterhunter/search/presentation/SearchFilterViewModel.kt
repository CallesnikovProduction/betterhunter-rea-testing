package dev.betterhunter.search.presentation

import dev.betterhunter.search.model.SearchFilterEffect
import dev.betterhunter.search.model.SearchFilterIntent
import dev.betterhunter.search.store.SearchFilterStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/** Platform-neutral adapter; an Android ViewModel can own the supplied scope and delegate to it. */
class SearchFilterViewModel(
    private val store: SearchFilterStore,
    scope: CoroutineScope,
) {
    val uiState: StateFlow<SearchFilterUiState> = store.state
        .map { state ->
            SearchFilterUiState(
                withSalaryChecked = state.criteria.withSalary,
                isReady = state.isInitialized,
                isApplying = state.isApplying,
                errorMessage = state.errorMessage,
            )
        }
        .stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = SearchFilterUiState(
                withSalaryChecked = false,
                isReady = false,
                isApplying = false,
                errorMessage = null,
            ),
        )

    val effects: Flow<SearchFilterEffect> = store.effects

    suspend fun onIntent(intent: SearchFilterIntent) = store.dispatch(intent)
}

