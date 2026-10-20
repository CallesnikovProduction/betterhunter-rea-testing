package dev.betterhunter.search.model

import java.math.BigDecimal

/** Immutable input for one local or remote search operation. pageIndex is zero based. */
data class SearchRequest(
    val criteria: VacancySearchCriteria = VacancySearchCriteria(),
    val pageIndex: Int = 0,
    val pageSize: Int = DEFAULT_PAGE_SIZE,
) {
    init { require(pageIndex >= 0) { "pageIndex must be non-negative" }; require(pageSize > 0) { "pageSize must be positive" } }
    companion object { const val DEFAULT_PAGE_SIZE = 20 }
}

data class SearchResultPage(
    val items: List<SearchDocument>,
    val pageIndex: Int,
    val pageSize: Int,
    val totalCount: Int,
) {
    val hasNextPage: Boolean get() = (pageIndex + 1L) * pageSize < totalCount
    val nextPageIndex: Int? get() = if (hasNextPage) pageIndex + 1 else null
}

/** Search-side projection. Integrators adapt their Vacancy model without a reverse dependency. */
data class SearchDocument(
    val id: String,
    val title: String,
    val employer: String? = null,
    val description: String? = null,
    val location: String? = null,
    val salaryFrom: BigDecimal? = null,
    val salaryTo: BigDecimal? = null,
    val currencyCode: String? = null,
    val experience: String? = null,
    val employmentForms: Set<String> = emptySet(),
    val workFormats: Set<String> = emptySet(),
    val workSchedules: Set<String> = emptySet(),
    val labels: Set<String> = emptySet(),
    val publishedAtEpochMillis: Long? = null,
)

data class SearchFilterDefinition(
    val id: String,
    val title: String,
    val options: List<SearchFilterOption> = emptyList(),
    val allowsMultiple: Boolean = true,
)

data class SearchFilterOption(val id: String, val title: String, val resultCount: Int? = null)
data class SearchFilterSelection(
    val values: Map<String, Set<String>> = emptyMap(),
    val salaryMinimum: BigDecimal? = null,
    val currencyCode: String? = null,
) {
    fun selected(filterId: String): Set<String> = values[filterId].orEmpty()
    fun with(filterId: String, selected: Set<String>) = copy(values = values.toMutableMap().apply {
        if (selected.isEmpty()) remove(filterId) else put(filterId, selected.toSet())
    })

    companion object {
        fun from(criteria: VacancySearchCriteria) = SearchFilterSelection(
            values = buildMap {
                if (criteria.areas.isNotEmpty()) put("area", criteria.areas.toSet())
                if (criteria.experience.isNotEmpty()) put("experience", criteria.experience.toSet())
                if (criteria.employmentForms.isNotEmpty()) put("employment_form", criteria.employmentForms.toSet())
                if (criteria.workFormats.isNotEmpty()) put("work_format", criteria.workFormats.toSet())
                if (criteria.workScheduleByDays.isNotEmpty()) put("work_schedule_by_days", criteria.workScheduleByDays.toSet())
                if (criteria.withSalaryOnly) put("with_salary", setOf("true"))
            },
            salaryMinimum = criteria.salary?.amount,
            currencyCode = criteria.currencyCode,
        )
    }
}

data class SearchFacet(val filterId: String, val options: List<SearchFilterOption>)
data class SearchSuggestion(val text: String, val kind: Kind = Kind.QUERY) {
    enum class Kind { QUERY, LOCATION, EMPLOYER }
}

data class SearchValidationResult(val errors: List<SearchValidationError> = emptyList()) {
    val isValid: Boolean get() = errors.isEmpty()
}
data class SearchValidationError(val field: String, val code: Code, val message: String) {
    enum class Code { INVALID_PAGE, INVALID_SALARY, UNKNOWN_FILTER, CONFLICTING_FILTERS, UNKNOWN_ENUM }
}
