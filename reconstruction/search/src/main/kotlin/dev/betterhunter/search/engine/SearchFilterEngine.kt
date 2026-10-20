package dev.betterhunter.search.engine

import dev.betterhunter.search.model.*

class SearchFilterEngine {
    val definitions: List<SearchFilterDefinition> = listOf(
        SearchFilterDefinition("area", "Регион"), SearchFilterDefinition("experience", "Опыт", options = EXPERIENCE),
        SearchFilterDefinition("employment_form", "Занятость", options = EMPLOYMENT),
        SearchFilterDefinition("work_format", "Формат", options = FORMATS),
        SearchFilterDefinition("work_schedule_by_days", "График", options = SCHEDULES),
        SearchFilterDefinition("with_salary", "Только с зарплатой", options = listOf(SearchFilterOption("true", "Только с зарплатой")), allowsMultiple = false),
        SearchFilterDefinition("salary", "Минимальная зарплата", allowsMultiple = false),
    )

    fun validate(criteria: VacancySearchCriteria, selection: SearchFilterSelection = SearchFilterSelection()): SearchValidationResult {
        val errors = mutableListOf<SearchValidationError>()
        if (criteria.salary?.amount?.let { it < java.math.BigDecimal.ZERO } == true) errors += error("salary", SearchValidationError.Code.INVALID_SALARY, "Зарплата не может быть отрицательной")
        if (criteria.salary?.amount == null && criteria.salary != null) errors += error("salary", SearchValidationError.Code.INVALID_SALARY, "Укажите числовое значение зарплаты")
        if (selection.salaryMinimum?.let { it < java.math.BigDecimal.ZERO } == true) errors += error("salary", SearchValidationError.Code.INVALID_SALARY, "Зарплата не может быть отрицательной")
        if (criteria.periodDays != null && criteria.periodDays < 0) errors += error("period", SearchValidationError.Code.CONFLICTING_FILTERS, "Период не может быть отрицательным")
        val known = definitions.map { it.id }.toSet()
        selection.values.keys.filterNot { it in known }.forEach { errors += error(it, SearchValidationError.Code.UNKNOWN_FILTER, "Неизвестный фильтр") }
        definitions.filterNot { it.allowsMultiple }.forEach { definition ->
            if (selection.selected(definition.id).size > 1) errors += error(definition.id, SearchValidationError.Code.CONFLICTING_FILTERS, "Выберите не более одного значения")
        }
        val allowed = mapOf("experience" to EXPERIENCE, "employment_form" to EMPLOYMENT, "work_format" to FORMATS, "work_schedule_by_days" to SCHEDULES)
        val criteriaValues = mapOf(
            "experience" to criteria.experience,
            "employment_form" to criteria.employmentForms,
            "work_format" to criteria.workFormats,
            "work_schedule_by_days" to criteria.workScheduleByDays,
        )
        criteriaValues.forEach { (field, values) ->
            val validIds = allowed.getValue(field).map { it.id }.toSet()
            values.filterNot { it in validIds }.forEach { errors += error(field, SearchValidationError.Code.UNKNOWN_ENUM, "Неизвестное значение: $it") }
        }
        selection.values.forEach { (field, values) ->
            val validIds = allowed[field]?.map { it.id }?.toSet()
            values.filterNot { it in validIds.orEmpty() }.forEach { errors += error(field, SearchValidationError.Code.UNKNOWN_ENUM, "Неизвестное значение: $it") }
        }
        if (criteria.sort is SearchSort.Unknown) errors += error("order_by", SearchValidationError.Code.UNKNOWN_ENUM, "Неизвестная сортировка: ${criteria.sort.rawValue}")
        return SearchValidationResult(errors.distinct())
    }

    /** Applying a selection replaces only the represented criteria fields; repeated apply is idempotent. */
    fun apply(criteria: VacancySearchCriteria, selection: SearchFilterSelection): VacancySearchCriteria = criteria.copy(
        areas = selection.selected("area").toList(),
        experience = selection.selected("experience").toList(),
        employmentForms = selection.selected("employment_form").toList(),
        workFormats = selection.selected("work_format").toList(),
        workScheduleByDays = selection.selected("work_schedule_by_days").toList(),
        withSalaryOnly = selection.selected("with_salary").contains("true"),
        salary = selection.salaryMinimum?.let { SalaryFilter(it.toPlainString(), it) },
        currencyCode = selection.currencyCode?.trim()?.takeIf(String::isNotEmpty),
    )

    fun reset(): SearchFilterSelection = SearchFilterSelection()

    fun definitions(facets: List<SearchFacet>): List<SearchFilterDefinition> = definitions.map { definition ->
        val facet = facets.firstOrNull { it.filterId == definition.id }
        if (facet == null) definition else definition.copy(options = facet.options)
    }

    fun facets(documents: List<SearchDocument>): List<SearchFacet> = listOf(
        facet("area", documents.mapNotNull { it.location }), facet("experience", documents.mapNotNull { it.experience }),
        facet("employment_form", documents.flatMap { it.employmentForms }), facet("work_format", documents.flatMap { it.workFormats }),
        facet("work_schedule_by_days", documents.flatMap { it.workSchedules }),
    )

    private fun facet(id: String, values: List<String>) = SearchFacet(id, values.groupingBy { it }.eachCount().entries
        .sortedBy { it.key }.map { SearchFilterOption(it.key, it.key, it.value) })
    private fun error(field: String, code: SearchValidationError.Code, message: String) = SearchValidationError(field, code, message)

    companion object {
        private val EXPERIENCE = listOf("NO_EXPERIENCE", "ONE_TO_THREE_YEARS", "THREE_TO_SIX_YEARS", "MORE_THAN_SIX_YEARS").map { SearchFilterOption(it, it) }
        private val EMPLOYMENT = listOf("FULL_TIME", "PART_TIME", "PROJECT", "INTERNSHIP", "VOLUNTEER").map { SearchFilterOption(it, it) }
        private val FORMATS = listOf("ON_SITE", "HYBRID", "REMOTE").map { SearchFilterOption(it, it) }
        private val SCHEDULES = listOf("FIVE_TWO", "SIX_ONE", "FLEXIBLE", "SHIFT", "ROTATIONAL").map { SearchFilterOption(it, it) }
    }
}
