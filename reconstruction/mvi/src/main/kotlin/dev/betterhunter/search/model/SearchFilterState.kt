package dev.betterhunter.search.model

/** The query filters owned by this first Search Domain slice. */
data class SearchFilterCriteria(
    val withSalary: Boolean = false,
)

/** Immutable store state. Status fields describe the local pipeline, not server state. */
data class SearchFilterState(
    val criteria: SearchFilterCriteria = SearchFilterCriteria(),
    val isInitialized: Boolean = false,
    val isApplying: Boolean = false,
    val errorMessage: String? = null,
) {
    companion object {
        val Initial = SearchFilterState()
    }
}

