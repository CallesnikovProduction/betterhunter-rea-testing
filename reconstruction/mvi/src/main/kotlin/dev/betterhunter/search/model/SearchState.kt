package dev.betterhunter.search.model

import dev.betterhunter.search.presentation.SearchUiState

data class SearchState(
    val queryText: String = "",
    val criteria: VacancySearchCriteria = VacancySearchCriteria(),
    val filterSelection: SearchFilterSelection = SearchFilterSelection(),
    val isEditingFilters: Boolean = false,
    val ui: SearchUiState = SearchUiState.Initial,
    val requestId: Long = 0,
    val selectedVacancyId: String? = null,
    val isLoadingNextPage: Boolean = false,
) {
    companion object { val Initial = SearchState() }
}
