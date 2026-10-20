package dev.betterhunter.search.model

sealed interface SearchFilterResult {
    data object InitializationStarted : SearchFilterResult
    data class Initialized(val criteria: SearchFilterCriteria) : SearchFilterResult
    data class SalaryToggled(val withSalary: Boolean) : SearchFilterResult
    data object FiltersReset : SearchFilterResult
    data object ApplyStarted : SearchFilterResult
    data class ApplySucceeded(val criteria: SearchFilterCriteria) : SearchFilterResult
    data class Failed(val operation: Operation, val message: String) : SearchFilterResult

    enum class Operation { INITIALIZE, APPLY }
}

