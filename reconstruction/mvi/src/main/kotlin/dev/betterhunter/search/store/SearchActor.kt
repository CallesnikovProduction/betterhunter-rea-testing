package dev.betterhunter.search.store

import dev.betterhunter.search.engine.SearchFilterEngine
import dev.betterhunter.search.engine.SearchGateway
import dev.betterhunter.search.model.*
import dev.betterhunter.search.presentation.SearchResultUiMapper
import dev.betterhunter.search.presentation.SearchUiState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

data class SearchActorOutput(val result: SearchResult, val effects: List<SearchEffect> = emptyList())

class SearchActor(
    private val gateway: SearchGateway,
    private val filterEngine: SearchFilterEngine,
    private val mapper: SearchResultUiMapper = SearchResultUiMapper(),
    private val pageSize: Int = SearchRequest.DEFAULT_PAGE_SIZE,
) {
    init { require(pageSize > 0) }
    fun handle(intent: SearchIntent, state: SearchState, requestId: Long): Flow<SearchActorOutput> = flow {
        when (intent) {
            SearchIntent.Initialize, SearchIntent.SearchSubmitted, SearchIntent.RetryRequested -> search(state, requestId, 0, null)
            is SearchIntent.SearchTextChanged -> emit(SearchActorOutput(SearchResult.QueryChanged(intent.value)))
            SearchIntent.FiltersOpened -> emit(SearchActorOutput(SearchResult.FiltersOpened, listOf(SearchEffect.OpenFilters)))
            is SearchIntent.FiltersApplied -> {
                val criteria = filterEngine.apply(state.criteria.copy(text = state.queryText), intent.selection)
                emit(SearchActorOutput(SearchResult.FiltersDraftChanged(intent.selection)))
                emit(SearchActorOutput(SearchResult.CriteriaChanged(criteria)))
                val current = state.copy(criteria = criteria, filterSelection = intent.selection)
                search(current, requestId, 0, criteria)
            }
            SearchIntent.FiltersReset -> {
                val empty = filterEngine.reset()
                val criteria = filterEngine.apply(state.criteria.copy(text = state.queryText), empty)
                emit(SearchActorOutput(SearchResult.FiltersDraftChanged(empty)))
                emit(SearchActorOutput(SearchResult.CriteriaChanged(criteria)))
                search(state.copy(criteria = criteria, filterSelection = empty), requestId, 0, criteria)
            }
            is SearchIntent.SortChanged -> {
                emit(SearchActorOutput(SearchResult.SortUpdated(intent.sort)))
                val criteria = state.criteria.copy(sort = intent.sort)
                emit(SearchActorOutput(SearchResult.CriteriaChanged(criteria)))
                search(state.copy(criteria = criteria), requestId, 0, criteria)
            }
            SearchIntent.NextPageRequested -> {
                val content = when (val ui = state.ui) {
                    is SearchUiState.Content -> ui.value
                    is SearchUiState.LoadingNextPage -> ui.content
                    is SearchUiState.Error -> ui.previous
                    else -> null
                }
                val next = content?.takeIf { it.hasNextPage && !state.isLoadingNextPage }?.let { it.pageIndex + 1 } ?: return@flow
                search(state, requestId, next, state.criteria, append = content)
            }
            is SearchIntent.VacancySelected -> emit(SearchActorOutput(SearchResult.VacancyChosen(intent.id), listOf(SearchEffect.OpenVacancy(intent.id))))
            SearchIntent.BackNavigation -> emit(SearchActorOutput(SearchResult.ReturnedToSearch, listOf(SearchEffect.NavigateBack)))
        }
    }

    private suspend fun kotlinx.coroutines.flow.FlowCollector<SearchActorOutput>.search(
        state: SearchState, requestId: Long, page: Int, overrideCriteria: VacancySearchCriteria?, append: dev.betterhunter.search.presentation.SearchContentUiModel? = null,
    ) {
        val criteria = overrideCriteria ?: state.criteria.copy(text = state.queryText)
        try {
            val result = gateway.search(SearchRequest(criteria, page, pageSize))
            val ui = if (append == null) mapper.map(result, criteria) else SearchUiState.Content(append.append(result))
            emit(if (page == 0) SearchActorOutput(SearchResult.SearchSucceeded(ui, requestId)) else SearchActorOutput(SearchResult.NextPageSucceeded(ui, requestId)))
        } catch (cancelled: CancellationException) { throw cancelled }
        catch (failure: Exception) {
            val message = failure.message?.takeIf(String::isNotBlank) ?: "Поиск не выполнен"
            emit(if (page == 0) SearchActorOutput(SearchResult.SearchFailed(message, requestId)) else SearchActorOutput(SearchResult.NextPageFailed(message, requestId)))
        }
    }
}
