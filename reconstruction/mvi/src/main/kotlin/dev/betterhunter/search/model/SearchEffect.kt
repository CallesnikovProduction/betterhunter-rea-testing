package dev.betterhunter.search.model

sealed interface SearchEffect {
    data object OpenFilters : SearchEffect
    data class OpenVacancy(val id: String) : SearchEffect
    data object NavigateBack : SearchEffect
}
