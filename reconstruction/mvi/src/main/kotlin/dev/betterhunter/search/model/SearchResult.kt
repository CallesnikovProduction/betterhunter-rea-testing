package dev.betterhunter.search.model

import dev.betterhunter.search.presentation.SearchUiState

sealed interface SearchResult {
    data class QueryChanged(val value: String) : SearchResult
    data object FiltersOpened : SearchResult
    data class FiltersDraftChanged(val selection: SearchFilterSelection) : SearchResult
    data class CriteriaChanged(val criteria: VacancySearchCriteria) : SearchResult
    data object SearchStarted : SearchResult
    data class SearchSucceeded(val ui: SearchUiState, val requestId: Long) : SearchResult
    data class SearchFailed(val message: String, val requestId: Long) : SearchResult
    data object NextPageStarted : SearchResult
    data class NextPageSucceeded(val ui: SearchUiState, val requestId: Long) : SearchResult
    data class NextPageFailed(val message: String, val requestId: Long) : SearchResult
    data class SortUpdated(val sort: SearchSort) : SearchResult
    data class VacancyChosen(val id: String) : SearchResult
    data object ReturnedToSearch : SearchResult
}
