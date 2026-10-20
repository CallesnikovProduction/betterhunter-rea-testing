package dev.betterhunter.search.store

import dev.betterhunter.search.model.SearchFilterResult
import dev.betterhunter.search.model.SearchFilterState

/** Pure state transition. Side effects are represented and handled outside the reducer. */
object SearchFilterReducer {
    fun reduce(state: SearchFilterState, result: SearchFilterResult): SearchFilterState =
        when (result) {
            SearchFilterResult.InitializationStarted -> state.copy(errorMessage = null)
            is SearchFilterResult.Initialized -> state.copy(
                criteria = result.criteria,
                isInitialized = true,
                isApplying = false,
                errorMessage = null,
            )

            is SearchFilterResult.SalaryToggled -> state.copy(
                criteria = state.criteria.copy(withSalary = result.withSalary),
                errorMessage = null,
            )

            SearchFilterResult.FiltersReset -> state.copy(
                criteria = SearchFilterState.Initial.criteria,
                isApplying = false,
                errorMessage = null,
            )

            SearchFilterResult.ApplyStarted -> state.copy(
                isApplying = true,
                errorMessage = null,
            )

            is SearchFilterResult.ApplySucceeded -> state.copy(
                criteria = result.criteria,
                isInitialized = true,
                isApplying = false,
                errorMessage = null,
            )

            is SearchFilterResult.Failed -> state.copy(
                isApplying = false,
                errorMessage = result.message,
            )
        }
}

