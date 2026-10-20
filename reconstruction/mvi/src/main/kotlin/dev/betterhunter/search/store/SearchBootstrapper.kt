package dev.betterhunter.search.store

import dev.betterhunter.search.model.SearchIntent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

fun interface SearchBootstrapper {
    fun intents(): Flow<SearchIntent>
    companion object { val Default = SearchBootstrapper { flowOf(SearchIntent.Initialize) } }
}
