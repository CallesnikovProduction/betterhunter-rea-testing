package dev.betterhunter.search

import dev.betterhunter.search.engine.*
import dev.betterhunter.search.model.*
import dev.betterhunter.search.presentation.SearchUiState
import dev.betterhunter.search.store.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.*
import kotlin.test.*

@OptIn(ExperimentalCoroutinesApi::class)
class SearchStoreTest {
    @Test fun `bootstrap loads content and selection emits navigation`() = runTest {
        val store = newStore(this, listOf(doc("a"), doc("b")))
        try {
            advanceUntilIdle()
            assertEquals(2, (store.state.value.ui as SearchUiState.Content).value.results.size)
            val effect = backgroundScope.async { store.effects.first() }
            store.dispatch(SearchIntent.VacancySelected("a")); advanceUntilIdle()
            assertEquals(SearchEffect.OpenVacancy("a"), effect.await())
            store.dispatch(SearchIntent.BackNavigation); advanceUntilIdle()
            assertNull(store.state.value.selectedVacancyId)
            assertTrue(store.state.value.ui is SearchUiState.Content)
        } finally { store.close() }
    }

    @Test fun `filters sort and paging update result collection`() = runTest {
        val docs = (1..25).map { doc("$it", title = "Role $it", salary = it.toLong()) }
        val store = newStore(this, docs)
        try {
            advanceUntilIdle()
            store.dispatch(SearchIntent.FiltersApplied(SearchFilterSelection().with("with_salary", setOf("true"))))
            advanceUntilIdle()
            assertTrue(store.state.value.criteria.withSalaryOnly)
            store.dispatch(SearchIntent.SortChanged(SearchSort.Known("salary_desc"))); advanceUntilIdle()
            assertEquals("25", (store.state.value.ui as SearchUiState.Content).value.results.first().id)
            store.dispatch(SearchIntent.NextPageRequested)
            store.dispatch(SearchIntent.NextPageRequested)
            advanceUntilIdle()
            val content = (store.state.value.ui as SearchUiState.Content).value
            assertEquals(25, content.results.size)
            assertEquals(25, content.results.map { it.id }.distinct().size)
        } finally { store.close() }
    }

    @Test fun `gateway failure and empty page produce explicit states and retry works`() = runTest {
        var fail = true
        val docs = listOf(doc("x"))
        val gateway = SearchGateway { request -> if (fail) throw IllegalStateException("offline") else MockSearchGateway(docs).search(request) }
        val store = SearchStore(this, SearchActor(gateway, SearchFilterEngine()))
        try {
            advanceUntilIdle(); assertTrue(store.state.value.ui is SearchUiState.Error)
            fail = false; store.dispatch(SearchIntent.RetryRequested); advanceUntilIdle()
            assertTrue(store.state.value.ui is SearchUiState.Content)
            val emptyStore = newStore(this, emptyList()); advanceUntilIdle()
            assertTrue(emptyStore.state.value.ui is SearchUiState.Empty)
            emptyStore.close()
        } finally { store.close() }
    }

    @Test fun `new search cancels or rejects stale completion`() = runTest {
        val gate = CompletableDeferred<SearchResultPage>()
        var calls = 0
        val gateway = SearchGateway { request ->
            calls++
            if (request.criteria.text == "old") gate.await() else SearchResultPage(listOf(doc("new")), 0, 20, 1)
        }
        val store = SearchStore(this, SearchActor(gateway, SearchFilterEngine()), SearchBootstrapper { kotlinx.coroutines.flow.emptyFlow() })
        try {
            store.dispatch(SearchIntent.SearchTextChanged("old")); store.dispatch(SearchIntent.SearchSubmitted); runCurrent()
            store.dispatch(SearchIntent.SearchTextChanged("new")); store.dispatch(SearchIntent.SearchSubmitted); advanceUntilIdle()
            gate.complete(SearchResultPage(listOf(doc("old")), 0, 20, 1)); advanceUntilIdle()
            val content = store.state.value.ui as SearchUiState.Content
            assertEquals(listOf("new"), content.value.results.map { it.id })
            assertTrue(calls >= 2)
        } finally { store.close() }
    }

    private fun newStore(scope: kotlinx.coroutines.CoroutineScope, docs: List<SearchDocument>) =
        SearchStore(scope, SearchActor(MockSearchGateway(docs), SearchFilterEngine()))

    private fun doc(id: String, title: String = "Kotlin", salary: Long = 100L) = SearchDocument(
        id = id, title = title, salaryFrom = java.math.BigDecimal.valueOf(salary), currencyCode = "RUB")
}
