package dev.betterhunter.search.model

/** User and lifecycle requests accepted by the filter store. */
sealed interface SearchFilterIntent {
    data object Initialize : SearchFilterIntent
    data object ToggleWithSalary : SearchFilterIntent
    data object Reset : SearchFilterIntent
    data object Apply : SearchFilterIntent
}

