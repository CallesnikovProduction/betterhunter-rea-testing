package dev.betterhunter.search.store

import dev.betterhunter.search.model.SearchFilterCriteria
import dev.betterhunter.search.model.SearchFilterEffect
import dev.betterhunter.search.model.SearchFilterIntent
import dev.betterhunter.search.model.SearchFilterResult
import dev.betterhunter.search.model.SearchFilterState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext

data class SearchFilterActorOutput(
    val result: SearchFilterResult,
    val effects: List<SearchFilterEffect> = emptyList(),
)

/** Persistence/search boundary. Implementations belong to the integrating Search Domain. */
interface SearchFilterGateway {
    suspend fun load(): SearchFilterCriteria

    /** Returns only after the search domain has accepted/applied these criteria. */
    suspend fun apply(criteria: SearchFilterCriteria)
}

fun interface SearchFilterAnalytics {
    suspend fun recordWithSalaryChange(action: SearchFilterEffect.SalaryAction)
}

/** Executes asynchronous work and returns explicit results; cancellation is never converted to failure. */
class SearchFilterActor(
    private val gateway: SearchFilterGateway,
    private val analytics: SearchFilterAnalytics,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) {
    fun handle(
        intent: SearchFilterIntent,
        state: SearchFilterState,
    ): Flow<SearchFilterActorOutput> = flow {
        when (intent) {
            SearchFilterIntent.Initialize -> {
                emit(SearchFilterActorOutput(SearchFilterResult.InitializationStarted))
                try {
                    val criteria = withContext(ioDispatcher) { gateway.load() }
                    emit(SearchFilterActorOutput(SearchFilterResult.Initialized(criteria)))
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (failure: Exception) {
                    val message = failure.message ?: "Unable to load search filters"
                    emit(
                        SearchFilterActorOutput(
                            result = SearchFilterResult.Failed(
                                SearchFilterResult.Operation.INITIALIZE,
                                message,
                            ),
                            effects = listOf(SearchFilterEffect.ShowError(message)),
                        ),
                    )
                }
            }

            SearchFilterIntent.ToggleWithSalary -> {
                val wasEnabled = state.criteria.withSalary
                val enabled = !wasEnabled
                val action = if (wasEnabled) {
                    SearchFilterEffect.SalaryAction.RESET
                } else {
                    SearchFilterEffect.SalaryAction.APPLY
                }
                val effect = recordSalaryChange(action)
                emit(
                    SearchFilterActorOutput(
                        result = SearchFilterResult.SalaryToggled(enabled),
                        effects = listOfNotNull(effect),
                    ),
                )
            }

            SearchFilterIntent.Reset -> {
                val effects = if (state.criteria.withSalary) {
                    listOfNotNull(recordSalaryChange(SearchFilterEffect.SalaryAction.RESET))
                } else {
                    emptyList()
                }
                emit(SearchFilterActorOutput(SearchFilterResult.FiltersReset, effects))
            }

            SearchFilterIntent.Apply -> {
                val criteria = state.criteria
                emit(SearchFilterActorOutput(SearchFilterResult.ApplyStarted))
                try {
                    withContext(ioDispatcher) { gateway.apply(criteria) }
                    emit(
                        SearchFilterActorOutput(
                            result = SearchFilterResult.ApplySucceeded(criteria),
                            effects = listOf(SearchFilterEffect.FiltersApplied(criteria)),
                        ),
                    )
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (failure: Exception) {
                    val message = failure.message ?: "Unable to apply search filters"
                    emit(
                        SearchFilterActorOutput(
                            result = SearchFilterResult.Failed(
                                SearchFilterResult.Operation.APPLY,
                                message,
                            ),
                            effects = listOf(SearchFilterEffect.ShowError(message)),
                        ),
                    )
                }
            }
        }
    }

    private suspend fun recordSalaryChange(action: SearchFilterEffect.SalaryAction): SearchFilterEffect? =
        try {
            withContext(ioDispatcher) { analytics.recordWithSalaryChange(action) }
            SearchFilterEffect.TrackWithSalaryChange(action)
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (failure: Exception) {
            SearchFilterEffect.NonCriticalFailure(
                source = "analytics",
                message = failure.message ?: "Unable to record filter analytics",
            )
        }
}

