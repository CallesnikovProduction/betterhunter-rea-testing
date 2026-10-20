package dev.betterhunter.vacancy

import dev.betterhunter.vacancy.data.mock.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class MockVacancyRepositoryTest {
    private val repository = MockVacancyRepository()

    @Test fun returnsStableSyntheticFixtureSet() {
        val first = assertIs<VacancyLoadResult.Success>(repository.load()).vacancies
        val second = assertIs<VacancyLoadResult.Success>(repository.load()).vacancies
        assertEquals(18, first.size)
        assertEquals(first, second)
        assertEquals(18, first.map { it.id }.distinct().size)
        assertEquals(true, first.mapNotNull { it.salary?.currencyCode }.distinct().size >= 3)
        assertEquals(true, first.mapNotNull { it.employmentTypes.firstOrNull() }.distinct().size >= 3)
    }

    @Test fun exposesLoadingOutcomeScenariosAsData() {
        assertEquals(emptyList(), assertIs<VacancyLoadResult.Success>(repository.load(MockScenario.EMPTY)).vacancies)
        assertEquals("Не удалось загрузить вакансии", assertIs<VacancyLoadResult.Failure>(repository.load(MockScenario.ERROR)).message)
        assertEquals("v-016", assertIs<VacancyLoadResult.Success>(repository.load(MockScenario.INCOMPLETE)).vacancies.single().id)
        val long = assertIs<VacancyLoadResult.Success>(repository.load(MockScenario.LONG_DESCRIPTION)).vacancies.single()
        kotlin.test.assertTrue(long.details!!.description!!.length > 10_000)
    }
}
