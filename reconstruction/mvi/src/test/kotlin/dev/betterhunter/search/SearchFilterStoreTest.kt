package dev.betterhunter.search

import dev.betterhunter.search.model.SearchFilterCriteria
import dev.betterhunter.search.model.SearchFilterEffect
import dev.betterhunter.search.model.SearchFilterIntent
import dev.betterhunter.search.store.SearchFilterActor
import dev.betterhunter.search.store.SearchFilterAnalytics
import dev.betterhunter.search.store.SearchFilterGateway
import dev.betterhunter.search.store.SearchFilterStore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class SearchFilterStoreTest {
    @Test
    fun `bootstrap salary toggles and apply are processed in order`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val gateway = FakeGateway()
        val analyticsActions = mutableListOf<SearchFilterEffect.SalaryAction>()
        val actor = SearchFilterActor(
            gateway = gateway,
            analytics = SearchFilterAnalytics { analyticsActions += it },
            ioDispatcher = dispatcher,
        )
        val store = SearchFilterStore(this, actor)
        val emittedEffects = mutableListOf<SearchFilterEffect>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { store.effects.collect { emittedEffects += it } }

        try {
            advanceUntilIdle()
            assertTrue(store.state.value.isInitialized)

            store.dispatch(SearchFilterIntent.ToggleWithSalary)
            store.dispatch(SearchFilterIntent.ToggleWithSalary)
            store.dispatch(SearchFilterIntent.Apply)
            advanceUntilIdle()

            assertFalse(store.state.value.criteria.withSalary)
            assertFalse(store.state.value.isApplying)
            assertEquals(
                listOf(SearchFilterEffect.SalaryAction.APPLY, SearchFilterEffect.SalaryAction.RESET),
                analyticsActions,
            )
            assertEquals(listOf(SearchFilterCriteria()), gateway.appliedCriteria)
            assertTrue(emittedEffects.contains(SearchFilterEffect.FiltersApplied(SearchFilterCriteria())))
        } finally {
            store.close()
        }
    }

    @Test
    fun `failed apply reports failure and does not publish applied effect`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val gateway = FakeGateway().apply { applyFailure = IllegalStateException("service unavailable") }
        val store = SearchFilterStore(
            parentScope = this,
            actor = SearchFilterActor(gateway, SearchFilterAnalytics {}, dispatcher),
        )
        val emittedEffects = mutableListOf<SearchFilterEffect>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { store.effects.collect { emittedEffects += it } }

        try {
            advanceUntilIdle()
            store.dispatch(SearchFilterIntent.ToggleWithSalary)
            store.dispatch(SearchFilterIntent.Apply)
            advanceUntilIdle()

            assertTrue(store.state.value.criteria.withSalary)
            assertFalse(store.state.value.isApplying)
            assertEquals("service unavailable", store.state.value.errorMessage)
            assertFalse(emittedEffects.any { it is SearchFilterEffect.FiltersApplied })
            assertTrue(emittedEffects.any { it == SearchFilterEffect.ShowError("service unavailable") })
        } finally {
            store.close()
        }
    }

    private class FakeGateway : SearchFilterGateway {
        val appliedCriteria = mutableListOf<SearchFilterCriteria>()
        var applyFailure: Exception? = null

        override suspend fun load(): SearchFilterCriteria = SearchFilterCriteria()

        override suspend fun apply(criteria: SearchFilterCriteria) {
            applyFailure?.let { throw it }
            appliedCriteria += criteria
        }
    }
}

