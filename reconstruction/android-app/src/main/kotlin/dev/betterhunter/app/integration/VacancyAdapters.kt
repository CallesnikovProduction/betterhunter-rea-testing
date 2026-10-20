package dev.betterhunter.app.integration

import dev.betterhunter.salary.SalaryLabels
import dev.betterhunter.salary.SalaryRange
import dev.betterhunter.salary.SalaryRangeFormatter
import dev.betterhunter.search.engine.SearchDocumentAdapter
import dev.betterhunter.search.model.SearchDocument
import dev.betterhunter.vacancy.domain.model.Vacancy
import dev.betterhunter.vacancy.presentation.model.SalaryTextFormatter
import java.math.BigDecimal

class VacancyToSearchDocumentAdapter : SearchDocumentAdapter<Vacancy> {
    override fun adapt(value: Vacancy): SearchDocument {
        val salary = value.salary
        return SearchDocument(
            id = value.id,
            title = value.title,
            employer = value.employer?.name,
            description = value.details?.description,
            location = value.address?.displayText ?: value.address?.city,
            salaryFrom = salary?.lowerBound?.takeIf { it > 0 }?.let(BigDecimal::valueOf),
            salaryTo = salary?.upperBound?.takeIf { it > 0 }?.let(BigDecimal::valueOf),
            currencyCode = salary?.currencyCode,
            experience = value.experience?.name,
            employmentForms = value.employmentTypes.map { it.name }.toSet(),
            workFormats = value.workFormat?.let { setOf(it.name) }.orEmpty(),
            workSchedules = value.schedules.map { it.name }.toSet(),
        )
    }
}

class VacancySalaryFormatterAdapter(labels: SalaryLabels) : SalaryTextFormatter {
    private val formatter = SalaryRangeFormatter(labels)
    override fun format(range: dev.betterhunter.vacancy.domain.model.SalaryRange): String =
        formatter.format(SalaryRange(range.lowerBound, range.upperBound)) + (range.currencyCode?.let { " $it" } ?: "")
}
