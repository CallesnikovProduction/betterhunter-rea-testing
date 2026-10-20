package dev.betterhunter.app

import dev.betterhunter.app.di.AppGraph
import dev.betterhunter.search.engine.MockSearchScenario
import dev.betterhunter.search.model.*
import dev.betterhunter.search.presentation.SearchUiState
import dev.betterhunter.search.store.SearchActor
import dev.betterhunter.search.store.SearchStore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AppIntegrationTest {
    @Test fun fixturesAdaptToSearchAndDetailsWithSalary() = runTest {
        val graph = AppGraph()
        val page = graph.gateway.search(SearchRequest())
        assertEquals(18, page.totalCount)
        assertEquals(18, page.items.map { it.id }.distinct().size)
        val vacancy = requireNotNull(graph.vacancies.find("v-001"))
        val details = graph.vacancyMapper.toDetails(vacancy)
        assertEquals("Android-разработчик", details.card.title)
        assertEquals("260 000 – 340 000 RUB", details.card.salary?.replace('\u00a0', ' '))
        assertTrue(details.skills.contains("Kotlin"))
    }

    @Test fun filtersAndSortReachTheRealMockGateway() = runTest {
        val graph = AppGraph()
        val selection = SearchFilterSelection().with("work_format", setOf("REMOTE"))
        val criteria = graph.filterEngine.apply(VacancySearchCriteria(text = "Kotlin"), selection)
        val page = graph.gateway.search(SearchRequest(criteria.copy(sort = SearchSort.Known("salary_desc"))))
        assertTrue(page.items.isNotEmpty())
        assertTrue(page.items.all { "REMOTE" in it.workFormats })
        val bounds = page.items.map { listOfNotNull(it.salaryFrom, it.salaryTo).maxOrNull() }
        assertEquals(bounds.sortedWith(compareByDescending { it }), bounds)
        val reset = graph.filterEngine.apply(criteria, graph.filterEngine.reset())
        assertTrue(reset.workFormats.isEmpty())
        assertEquals("Kotlin", reset.text)
    }

    @Test fun paginationAndBackKeepTheLoadedSnapshot() = runTest {
        val graph = AppGraph()
        val store = SearchStore(this, SearchActor(graph.gateway, graph.filterEngine, pageSize = 6))
        try {
            advanceUntilIdle()
            repeat(2) {
                store.dispatch(SearchIntent.NextPageRequested)
                advanceUntilIdle()
            }
            val before = store.state.value
            val content = (before.ui as SearchUiState.Content).value
            assertEquals(18, content.results.size)
            assertEquals(18, content.results.map { it.id }.distinct().size)
            assertFalse(content.hasNextPage)
            store.dispatch(SearchIntent.NextPageRequested)
            advanceUntilIdle()
            assertEquals(before.ui, store.state.value.ui)
            assertFalse(store.state.value.isLoadingNextPage)
            store.dispatch(SearchIntent.VacancySelected(content.results.first().id))
            store.dispatch(SearchIntent.BackNavigation)
            advanceUntilIdle()
            assertNull(store.state.value.selectedVacancyId)
            assertEquals(before.ui, store.state.value.ui)
        } finally { store.close() }
    }

    @Test fun emptyAndFailureCanRetryToContent() = runTest {
        val graph = AppGraph()
        graph.setScenario(MockSearchScenario.ERROR)
        val store = SearchStore(this, SearchActor(graph.gateway, graph.filterEngine))
        try {
            advanceUntilIdle()
            assertTrue(store.state.value.ui is SearchUiState.Error)
            graph.setScenario(MockSearchScenario.EMPTY)
            store.dispatch(SearchIntent.RetryRequested)
            advanceUntilIdle()
            assertTrue(store.state.value.ui is SearchUiState.Empty)
            graph.setScenario(MockSearchScenario.SUCCESS)
            store.dispatch(SearchIntent.RetryRequested)
            advanceUntilIdle()
            assertTrue(store.state.value.ui is SearchUiState.Content)
        } finally { store.close() }
    }
}
