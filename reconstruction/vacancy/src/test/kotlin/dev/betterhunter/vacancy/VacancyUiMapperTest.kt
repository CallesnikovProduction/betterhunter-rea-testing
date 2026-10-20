package dev.betterhunter.vacancy

import dev.betterhunter.vacancy.domain.model.*
import dev.betterhunter.vacancy.presentation.mapper.VacancyUiMapper
import dev.betterhunter.vacancy.presentation.model.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class VacancyUiMapperTest {
    private val labels = object : VacancyPresentationLabels {
        override fun experience(value: WorkExperience) = value.name
        override fun employment(value: EmploymentType) = value.name
        override fun workFormat(value: WorkFormat) = value.name
        override fun schedule(value: WorkSchedule) = value.name
        override fun notSpecified() = "Not specified"
    }
    private val mapper = VacancyUiMapper(labels, SalaryTextFormatter { "salary:${it.lowerBound}:${it.upperBound}" })

    @Test fun mapsCardFieldsAndDelegatesSalaryText() {
        val vacancy = Vacancy(
            id = "id-1", title = "Engineer", employer = Employer("Studio"),
            salary = SalaryRange(10, 20, "RUB"), address = VacancyAddress(city = " Kazan ", street = "Main", building = "2"),
            experience = WorkExperience.ONE_TO_THREE_YEARS, employmentTypes = listOf(EmploymentType.FULL_TIME), schedules = listOf(WorkSchedule.FIVE_TWO), workFormat = WorkFormat.REMOTE,
        )
        val card = mapper.toCard(vacancy)
        assertEquals("salary:10:20", card.salary)
        assertEquals("Studio", card.employer)
        assertEquals("Kazan, Main, 2", card.location)
        assertEquals("ONE_TO_THREE_YEARS", card.experience)
        assertEquals("FULL_TIME", card.employment)
        assertEquals("REMOTE", card.workFormat)
    }

    @Test fun preservesExplicitAddressAndHandlesMissingAndLongContent() {
        assertEquals("Custom", mapper.formatAddress(VacancyAddress(city = "ignored", displayText = " Custom ")))
        assertNull(mapper.formatAddress(VacancyAddress()))
        val long = "x".repeat(30_000)
        val details = mapper.toDetails(Vacancy("id", "Long", details = VacancyDetails(long, listOf("Need"), listOf(VacancySkill("Kotlin")), listOf("Remote"))))
        assertEquals(long, details.description)
        assertEquals(listOf("Need"), details.requirements)
        assertEquals(listOf("Kotlin"), details.skills)
        assertEquals(listOf("Remote"), details.additionalConditions)
    }

    @Test fun mapsAbsentOptionalValuesWithoutInventingContent() {
        val card = mapper.toCard(Vacancy("id", "Title"))
        assertNull(card.salary)
        assertNull(card.employer)
        assertNull(card.location)
        val details = mapper.toDetails(Vacancy("id", "Title"))
        assertEquals(emptyList(), details.skills)
        assertNull(details.description)
    }

    @Test fun preservesExplicitUnknownEnumCase() {
        val card = mapper.toCard(Vacancy("id", "Unknown", experience = WorkExperience.UNKNOWN, workFormat = WorkFormat.UNKNOWN))
        assertEquals("UNKNOWN", card.experience)
        assertEquals("UNKNOWN", card.workFormat)
    }
}
