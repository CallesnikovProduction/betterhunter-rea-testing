package dev.betterhunter.search.presentation

data class SearchFilterUiState(
    val withSalaryChecked: Boolean,
    val isReady: Boolean,
    val isApplying: Boolean,
    val errorMessage: String?,
)

