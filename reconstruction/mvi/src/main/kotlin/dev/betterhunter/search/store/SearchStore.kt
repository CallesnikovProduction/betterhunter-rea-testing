package dev.betterhunter.search.store

import dev.betterhunter.search.model.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import dev.betterhunter.search.presentation.SearchUiState

/** Serial intent mailbox; searches are latest-wins, paging is single-flight. */
class SearchStore(parentScope: CoroutineScope, private val actor: SearchActor, bootstrapper: SearchBootstrapper = SearchBootstrapper.Default) : AutoCloseable {
    private val job = SupervisorJob(parentScope.coroutineContext[Job])
    private val scope = CoroutineScope(parentScope.coroutineContext + job)
    private val inbox = Channel<SearchIntent>(Channel.UNLIMITED)
    private val effectChannel = Channel<SearchEffect>(Channel.BUFFERED)
    private val mutableState = MutableStateFlow(SearchState.Initial)
    private var searchOperation: Job? = null
    private var generation = 0L
    private val worker = scope.launch {
        bootstrapper.intents().collect { process(it) }
        for (intent in inbox) process(intent)
    }
    val state: StateFlow<SearchState> = mutableState.asStateFlow()
    val effects: Flow<SearchEffect> = effectChannel.receiveAsFlow()

    suspend fun dispatch(intent: SearchIntent) { inbox.send(intent) }

    private suspend fun process(intent: SearchIntent) {
        val paging = intent == SearchIntent.NextPageRequested
        val newSearch = intent == SearchIntent.Initialize || intent == SearchIntent.SearchSubmitted || intent == SearchIntent.RetryRequested ||
            intent is SearchIntent.FiltersApplied || intent == SearchIntent.FiltersReset || intent is SearchIntent.SortChanged
        // Actor must see the state before NextPageStarted marks the request in flight.
        val before = mutableState.value
        if (paging) {
            val content = when (val ui = before.ui) {
                is SearchUiState.Content -> ui.value
                is SearchUiState.Error -> ui.previous
                else -> null
            }
            if (before.isLoadingNextPage || content?.hasNextPage != true) return
        }
        if (newSearch) { searchOperation?.cancel(); generation++ }
        val id = generation
        if (newSearch) mutableState.value = SearchReducer.reduce(mutableState.value, SearchResult.SearchStarted)
        if (paging) mutableState.value = SearchReducer.reduce(mutableState.value, SearchResult.NextPageStarted)
        val actorState = if (paging) before else mutableState.value
        val operation = scope.launch {
            actor.handle(intent, actorState, id).collect { output ->
                if (id != generation && intent !is SearchIntent.VacancySelected && intent != SearchIntent.BackNavigation) return@collect
                mutableState.value = SearchReducer.reduce(mutableState.value, output.result)
                output.effects.forEach { effectChannel.send(it) }
            }
        }
        if (newSearch || paging) searchOperation = operation else operation.join()
    }

    override fun close() { inbox.close(); effectChannel.close(); searchOperation?.cancel(); worker.cancel(); scope.cancel() }
}
