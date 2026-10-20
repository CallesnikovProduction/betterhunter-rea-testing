package dev.betterhunter.search

import dev.betterhunter.search.model.*
import dev.betterhunter.search.presentation.SearchUiState
import dev.betterhunter.search.store.SearchReducer
import kotlin.test.*

class SearchReducerTest {
    @Test fun queryAndNavigationKeepResults() {
        val content = SearchUiState.Empty(VacancySearchCriteria())
        val initial = SearchState(ui = content, queryText = "Kotlin")
        val selected = SearchReducer.reduce(initial, SearchResult.VacancyChosen("vac-1"))
        val back = SearchReducer.reduce(selected, SearchResult.ReturnedToSearch)
        assertEquals(content, back.ui)
        assertEquals("Kotlin", back.queryText)
        assertNull(back.selectedVacancyId)
    }

    @Test fun staleResponseCannotReplaceCurrentRequest() {
        val state = SearchState(requestId = 3)
        val result = SearchReducer.reduce(state, SearchResult.SearchSucceeded(SearchUiState.Empty(state.criteria), 2))
        assertEquals(state, result)
    }

    @Test fun loadingAndEmptyTransitionsAreExplicit() {
        val loading = SearchReducer.reduce(SearchState.Initial, SearchResult.SearchStarted)
        assertTrue(loading.ui is SearchUiState.Loading)
        val empty = SearchReducer.reduce(loading, SearchResult.SearchSucceeded(SearchUiState.Empty(loading.criteria), loading.requestId))
        assertTrue(empty.ui is SearchUiState.Empty)
    }
}
