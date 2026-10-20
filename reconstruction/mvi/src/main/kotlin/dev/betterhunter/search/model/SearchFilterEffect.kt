package dev.betterhunter.search.model

/** One-shot outcomes for analytics and presentation adapters. */
sealed interface SearchFilterEffect {
    data class TrackWithSalaryChange(val action: SalaryAction) : SearchFilterEffect
    data class FiltersApplied(val criteria: SearchFilterCriteria) : SearchFilterEffect
    data class ShowError(val message: String) : SearchFilterEffect
    data class NonCriticalFailure(val source: String, val message: String) : SearchFilterEffect

    enum class SalaryAction { APPLY, RESET }
}

