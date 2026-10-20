package dev.betterhunter.app

import dev.betterhunter.search.model.*
import dev.betterhunter.search.presentation.SearchUiState
import dev.betterhunter.search.store.SearchReducer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SearchReducerTest {
    @Test fun searchBeginsLoadingThenStoresEmptyResult() {
        val loading = SearchReducer.reduce(SearchState.Initial, SearchResult.SearchStarted)
        assertTrue(loading.ui is SearchUiState.Loading)
        val empty = SearchReducer.reduce(loading, SearchResult.SearchSucceeded(SearchUiState.Empty(loading.criteria), loading.requestId))
        assertTrue(empty.ui is SearchUiState.Empty)
    }

    @Test fun returningFromDetailsRetainsTheSearchSnapshot() {
        val ui = SearchUiState.Empty(VacancySearchCriteria(text = "Kotlin"))
        val selected = SearchReducer.reduce(SearchState(queryText = "Kotlin", ui = ui), SearchResult.VacancyChosen("id/1"))
        val restored = SearchReducer.reduce(selected, SearchResult.ReturnedToSearch)
        assertEquals(ui, restored.ui)
        assertEquals("Kotlin", restored.queryText)
        assertEquals(null, restored.selectedVacancyId)
    }
}
