package dev.betterhunter.vacancy.presentation.mapper

import dev.betterhunter.vacancy.domain.model.Vacancy
import dev.betterhunter.vacancy.domain.model.VacancyAddress
import dev.betterhunter.vacancy.presentation.model.SalaryTextFormatter
import dev.betterhunter.vacancy.presentation.model.VacancyCardUiModel
import dev.betterhunter.vacancy.presentation.model.VacancyDetailsUiModel
import dev.betterhunter.vacancy.presentation.model.VacancyPresentationLabels

class VacancyUiMapper(
    private val labels: VacancyPresentationLabels,
    private val salaryFormatter: SalaryTextFormatter,
) {
    fun toCard(vacancy: Vacancy): VacancyCardUiModel = VacancyCardUiModel(
        id = vacancy.id,
        title = vacancy.title,
        salary = vacancy.salary?.let(salaryFormatter::format),
        employer = vacancy.employer?.name,
        location = vacancy.address?.let(::formatAddress),
        experience = vacancy.experience?.let(labels::experience),
        employment = vacancy.employmentTypes.firstOrNull()?.let(labels::employment),
        workFormat = vacancy.workFormat?.let(labels::workFormat),
        schedule = vacancy.schedules.firstOrNull()?.let(labels::schedule),
    )

    fun toDetails(vacancy: Vacancy): VacancyDetailsUiModel {
        val details = vacancy.details
        return VacancyDetailsUiModel(
            card = toCard(vacancy),
            description = details?.description,
            requirements = details?.requirements.orEmpty(),
            skills = details?.skills.orEmpty().map { it.name },
            additionalConditions = details?.additionalConditions.orEmpty(),
        )
    }

    fun formatAddress(address: VacancyAddress): String? {
        address.displayText?.trim()?.takeIf(String::isNotEmpty)?.let { return it }
        return listOfNotNull(address.city, address.street, address.building)
            .map(String::trim)
            .filter(String::isNotEmpty)
            .distinct()
            .joinToString(", ")
            .ifEmpty { null }
    }
}
