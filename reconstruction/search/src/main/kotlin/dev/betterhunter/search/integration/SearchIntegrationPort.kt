package dev.betterhunter.search.integration

import dev.betterhunter.search.model.*
import dev.betterhunter.search.presentation.SearchUiState

/** Boundary for wiring Search into the existing MVI owner; Search does not create another store. */
interface SearchIntegrationPort {
    fun observe(observer: (SearchUiState) -> Unit): AutoCloseable
    fun updateCriteria(criteria: VacancySearchCriteria)
    suspend fun search()
    fun applyFilters(selection: SearchFilterSelection)
    fun updateSort(sort: SearchSort)
    suspend fun requestNextPage()
    fun retry()
}
