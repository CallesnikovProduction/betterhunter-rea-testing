package dev.betterhunter.search.store

import dev.betterhunter.search.model.SearchFilterEffect
import dev.betterhunter.search.model.SearchFilterIntent
import dev.betterhunter.search.model.SearchFilterState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

/** Single-consumer store: intents are handled in order, state updates are atomic, effects are one-shot. */
class SearchFilterStore(
    parentScope: CoroutineScope,
    private val actor: SearchFilterActor,
    bootstrapper: SearchFilterBootstrapper = SearchFilterBootstrapper.Default,
) : AutoCloseable {
    private val storeJob: Job = SupervisorJob(parentScope.coroutineContext[Job])
    private val storeScope = CoroutineScope(parentScope.coroutineContext + storeJob)
    private val inbox = Channel<SearchFilterIntent>(Channel.UNLIMITED)
    private val effectChannel = Channel<SearchFilterEffect>(Channel.BUFFERED)
    private val mutableState = MutableStateFlow(SearchFilterState.Initial)
    private val worker: Job = storeScope.launch {
        bootstrapper.intents().collect { process(it) }
        inbox.consumeAsFlow().collect { process(it) }
    }

    val state: StateFlow<SearchFilterState> = mutableState.asStateFlow()
    val effects: Flow<SearchFilterEffect> = effectChannel.receiveAsFlow()

    suspend fun dispatch(intent: SearchFilterIntent) {
        inbox.send(intent)
    }

    private suspend fun process(intent: SearchFilterIntent) {
        actor.handle(intent, mutableState.value).collect { output ->
            mutableState.value = SearchFilterReducer.reduce(mutableState.value, output.result)
            output.effects.forEach { effectChannel.send(it) }
        }
    }

    override fun close() {
        inbox.close()
        effectChannel.close()
        worker.cancel()
        storeScope.cancel()
    }
}

