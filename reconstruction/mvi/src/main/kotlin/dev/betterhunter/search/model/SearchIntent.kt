package dev.betterhunter.search.model

sealed interface SearchIntent {
    data object Initialize : SearchIntent
    data class SearchTextChanged(val value: String) : SearchIntent
    data object SearchSubmitted : SearchIntent
    data object FiltersOpened : SearchIntent
    data class FiltersApplied(val selection: SearchFilterSelection) : SearchIntent
    data object FiltersReset : SearchIntent
    data class SortChanged(val sort: SearchSort) : SearchIntent
    data object NextPageRequested : SearchIntent
    data class VacancySelected(val id: String) : SearchIntent
    data object BackNavigation : SearchIntent
    data object RetryRequested : SearchIntent
}
