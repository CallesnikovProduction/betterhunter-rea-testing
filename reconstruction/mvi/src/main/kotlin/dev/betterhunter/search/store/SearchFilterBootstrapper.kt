package dev.betterhunter.search.store

import dev.betterhunter.search.model.SearchFilterIntent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/** Emits the explicit first event, matching the observed initial-command role. */
fun interface SearchFilterBootstrapper {
    fun intents(): Flow<SearchFilterIntent>

    companion object {
        val Default = SearchFilterBootstrapper { flowOf(SearchFilterIntent.Initialize) }
    }
}

