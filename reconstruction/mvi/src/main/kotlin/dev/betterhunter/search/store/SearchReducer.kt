package dev.betterhunter.search.store

import dev.betterhunter.search.model.*
import dev.betterhunter.search.presentation.SearchUiState

object SearchReducer {
    fun reduce(state: SearchState, result: SearchResult): SearchState = when (result) {
        is SearchResult.QueryChanged -> state.copy(queryText = result.value)
        SearchResult.FiltersOpened -> state.copy(isEditingFilters = true, filterSelection = SearchFilterSelection.from(state.criteria))
        is SearchResult.FiltersDraftChanged -> state.copy(filterSelection = result.selection)
        is SearchResult.CriteriaChanged -> state.copy(criteria = result.criteria)
        SearchResult.SearchStarted -> state.copy(requestId = state.requestId + 1, isLoadingNextPage = false,
            criteria = state.criteria.copy(text = state.queryText), ui = SearchUiState.Loading(state.criteria.copy(text = state.queryText)))
        is SearchResult.SearchSucceeded -> if (result.requestId != state.requestId) state else state.copy(ui = result.ui, isLoadingNextPage = false)
        is SearchResult.SearchFailed -> if (result.requestId != state.requestId) state else state.copy(
            ui = SearchUiState.Error(state.criteria, result.message, (state.ui as? SearchUiState.Content)?.value), isLoadingNextPage = false)
        SearchResult.NextPageStarted -> state.copy(isLoadingNextPage = true, ui = (state.ui as? SearchUiState.Content)?.value?.let(SearchUiState::LoadingNextPage) ?: state.ui)
        is SearchResult.NextPageSucceeded -> if (result.requestId != state.requestId) state else state.copy(ui = result.ui, isLoadingNextPage = false)
        is SearchResult.NextPageFailed -> if (result.requestId != state.requestId) state else state.copy(
            ui = SearchUiState.Error(state.criteria, result.message, (state.ui as? SearchUiState.LoadingNextPage)?.content ?: (state.ui as? SearchUiState.Content)?.value), isLoadingNextPage = false)
        is SearchResult.SortUpdated -> state.copy(criteria = state.criteria.copy(sort = result.sort))
        is SearchResult.VacancyChosen -> state.copy(selectedVacancyId = result.id)
        SearchResult.ReturnedToSearch -> state.copy(selectedVacancyId = null, isEditingFilters = false)
    }
}
