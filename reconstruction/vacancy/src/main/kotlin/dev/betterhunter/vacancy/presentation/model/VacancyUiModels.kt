package dev.betterhunter.vacancy.presentation.model

data class VacancyCardUiModel(
    val id: String,
    val title: String,
    val salary: String?,
    val employer: String?,
    val location: String?,
    val experience: String?,
    val employment: String?,
    val workFormat: String?,
    val schedule: String?,
)

data class VacancyDetailsUiModel(
    val card: VacancyCardUiModel,
    val description: String?,
    val requirements: List<String>,
    val skills: List<String>,
    val additionalConditions: List<String>,
)

sealed interface VacancyDetailsUiState {
    data object Loading : VacancyDetailsUiState
    data object Empty : VacancyDetailsUiState
    data class Error(val message: String) : VacancyDetailsUiState
    data class Content(val vacancy: VacancyDetailsUiModel) : VacancyDetailsUiState
}

/** Text remains injected so original resources and app localization stay outside this module. */
interface VacancyPresentationLabels {
    fun experience(value: dev.betterhunter.vacancy.domain.model.WorkExperience): String
    fun employment(value: dev.betterhunter.vacancy.domain.model.EmploymentType): String
    fun workFormat(value: dev.betterhunter.vacancy.domain.model.WorkFormat): String
    fun schedule(value: dev.betterhunter.vacancy.domain.model.WorkSchedule): String
    fun notSpecified(): String
}

interface VacancyScreenLabels {
    fun notFound(): String
    fun retry(): String
    fun description(): String
    fun requirements(): String
    fun skills(): String
    fun conditions(): String
}

fun interface SalaryTextFormatter {
    fun format(range: dev.betterhunter.vacancy.domain.model.SalaryRange): String
}
