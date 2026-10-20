package dev.betterhunter.vacancy.domain.model

/** BetterHunter domain model; it is not a copy of an upstream transport DTO. */
data class Vacancy(
    val id: String,
    val title: String,
    val employer: Employer? = null,
    val salary: SalaryRange? = null,
    val address: VacancyAddress? = null,
    val experience: WorkExperience? = null,
    val employmentTypes: List<EmploymentType> = emptyList(),
    val schedules: List<WorkSchedule> = emptyList(),
    val workFormat: WorkFormat? = null,
    val details: VacancyDetails? = null,
)

data class Employer(val name: String, val logoUrl: String? = null, val verified: Boolean? = null)

data class VacancyAddress(
    val city: String? = null,
    val street: String? = null,
    val building: String? = null,
    val displayText: String? = null,
)

enum class WorkExperience { NO_EXPERIENCE, ONE_TO_THREE_YEARS, THREE_TO_SIX_YEARS, MORE_THAN_SIX_YEARS, UNKNOWN }
enum class EmploymentType { FULL_TIME, PART_TIME, PROJECT, INTERNSHIP, VOLUNTEER, UNKNOWN }
enum class WorkSchedule { FIVE_TWO, SIX_ONE, FLEXIBLE, SHIFT, ROTATIONAL, UNKNOWN }
enum class WorkFormat { ON_SITE, HYBRID, REMOTE, UNKNOWN }

data class VacancyDetails(
    val description: String? = null,
    val requirements: List<String> = emptyList(),
    val skills: List<VacancySkill> = emptyList(),
    val additionalConditions: List<String> = emptyList(),
)

data class VacancySkill(val name: String, val required: Boolean? = null)
