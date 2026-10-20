package dev.betterhunter.search

import dev.betterhunter.search.engine.*
import dev.betterhunter.search.model.*
import dev.betterhunter.search.presentation.*
import java.math.BigDecimal
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.coroutines.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class SearchGatewayTest {
    private val docs = listOf(
        SearchDocument("a", "Разработчик Kotlin", "Альфа", "Создание сервиса", "Москва", BigDecimal("100"), BigDecimal("200"), "RUB", "ONE_TO_THREE_YEARS", setOf("FULL_TIME"), setOf("REMOTE"), publishedAtEpochMillis = 3000),
        SearchDocument("b", "Kotlin engineer", "Бета", "Поддержка платформы", "Казань", null, null, experience = "UNKNOWN", workFormats = setOf("HYBRID"), publishedAtEpochMillis = 1000),
        SearchDocument("c", "Kotlin инженер", "Гамма", "Разработка", "Москва", BigDecimal("200"), BigDecimal("300"), "RUB", "THREE_TO_SIX_YEARS", setOf("INTERNSHIP"), setOf("REMOTE"), publishedAtEpochMillis = 2000),
    )

    @Test fun unicodeAndRepeatedQueriesReturnStableMatches() {
        val gateway = MockSearchGateway(docs)
        val request = SearchRequest(VacancySearchCriteria(text = "РАЗРАБО"), pageSize = 10)
        assertEquals(listOf("a", "c"), runSuspend { gateway.search(request) }.items.map { it.id })
        assertEquals(runSuspend { gateway.search(request) }, runSuspend { gateway.search(request) })
    }

    @Test fun filtersAndCurrencyPolicyAreExplicit() {
        val gateway = MockSearchGateway(docs)
        val withLocation = VacancySearchCriteria(areas = listOf("Москва", "Казань"), workFormats = listOf("REMOTE"), salary = SalaryFilter("150", BigDecimal("150")), currencyCode = "RUB")
        assertEquals(listOf("a", "c"), runSuspend { gateway.search(SearchRequest(withLocation, pageSize = 10)) }.items.map { it.id })
        val noCurrency = withLocation.copy(currencyCode = null)
        assertEquals(listOf("a", "c"), runSuspend { gateway.search(SearchRequest(noCurrency, pageSize = 10)) }.items.map { it.id })
        assertTrue(runSuspend { gateway.search(SearchRequest(withLocation.copy(currencyCode = "EUR"), pageSize = 10)) }.items.isEmpty())
    }

    @Test fun sortingIsStableAndPagesHaveNoDuplicatesOrGaps() {
        val gateway = MockSearchGateway(docs)
        val relevance = runSuspend { gateway.search(SearchRequest(VacancySearchCriteria(text = "Kotlin"), pageSize = 1)) }
        val second = runSuspend { gateway.search(SearchRequest(VacancySearchCriteria(text = "Kotlin"), pageIndex = 1, pageSize = 1)) }
        val third = runSuspend { gateway.search(SearchRequest(VacancySearchCriteria(text = "Kotlin"), pageIndex = 2, pageSize = 1)) }
        assertEquals(listOf("a", "b", "c"), (relevance.items + second.items + third.items).map { it.id })
        assertFalse(third.hasNextPage)
        val newest = runSuspend { gateway.search(SearchRequest(VacancySearchCriteria(sort = SearchSort.Known("publication_time")), pageSize = 10)) }
        assertEquals(listOf("a", "c", "b"), newest.items.map { it.id })
    }

    @Test fun filterApplyValidationAndResetHandleUnknownValues() {
        val engine = SearchFilterEngine()
        val selected = SearchFilterSelection().with("area", setOf("Москва", "Казань")).with("work_format", setOf("TELEPORT"))
        val errors = engine.validate(VacancySearchCriteria(), selected).errors
        assertTrue(errors.any { it.code == SearchValidationError.Code.UNKNOWN_ENUM })
        val clean = selected.with("work_format", emptySet())
        assertEquals(listOf("Москва", "Казань"), engine.apply(VacancySearchCriteria(), clean).areas)
        assertEquals(SearchFilterSelection(), engine.reset())
        assertEquals(engine.apply(VacancySearchCriteria(), clean), engine.apply(engine.apply(VacancySearchCriteria(), clean), clean))
        val salaryCriteria = engine.apply(VacancySearchCriteria(), clean.copy(salaryMinimum = BigDecimal("125"), currencyCode = "RUB"))
        assertEquals(BigDecimal("125"), salaryCriteria.salary?.amount)
        assertEquals("RUB", salaryCriteria.currencyCode)
        assertEquals(BigDecimal("125"), SearchFilterSelection.from(salaryCriteria).salaryMinimum)
        assertTrue(engine.validate(VacancySearchCriteria(workFormats = listOf("UNKNOWN_FORMAT"))).errors.any { it.code == SearchValidationError.Code.UNKNOWN_ENUM })
    }

    @Test fun emptyErrorAndContentStatesMapCorrectly() {
        val mapper = SearchResultUiMapper()
        val criteria = VacancySearchCriteria(text = "missing")
        val empty = mapper.map(SearchResultPage(emptyList(), 0, 10, 0), criteria)
        assertIs<SearchUiState.Empty>(empty)
        val content = mapper.map(SearchResultPage(listOf(docs.first()), 0, 1, 2), criteria)
        assertIs<SearchUiState.Content>(content)
        assertTrue((content as SearchUiState.Content).value.hasNextPage)
        val failure = runCatching { runSuspend { MockSearchGateway(docs, MockSearchScenario.ERROR).search(SearchRequest()) } }.exceptionOrNull()
        assertIs<SearchGatewayException>(failure)
        val errorState = mapper.mapFailure(criteria, failure!!, (content as SearchUiState.Content).value)
        assertEquals((content as SearchUiState.Content).value, errorState.previous)
    }

    private fun <T> runSuspend(block: suspend () -> T): T {
        var outcome: Result<T>? = null
        val latch = CountDownLatch(1)
        block.startCoroutine(object : Continuation<T> {
            override val context: CoroutineContext = EmptyCoroutineContext
            override fun resumeWith(result: Result<T>) { outcome = result; latch.countDown() }
        })
        check(latch.await(5, TimeUnit.SECONDS)) { "suspending test timed out" }
        return outcome!!.getOrThrow()
    }
}
