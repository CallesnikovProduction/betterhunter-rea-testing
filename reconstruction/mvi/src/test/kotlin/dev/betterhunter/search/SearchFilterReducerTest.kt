package dev.betterhunter.search

import dev.betterhunter.search.model.SearchFilterCriteria
import dev.betterhunter.search.model.SearchFilterResult
import dev.betterhunter.search.model.SearchFilterState
import dev.betterhunter.search.store.SearchFilterReducer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SearchFilterReducerTest {
    @Test
    fun `salary change apply and reset form a deterministic transition sequence`() {
        val initial = SearchFilterState.Initial
        val enabled = SearchFilterReducer.reduce(initial, SearchFilterResult.SalaryToggled(true))
        val applying = SearchFilterReducer.reduce(enabled, SearchFilterResult.ApplyStarted)
        val applied = SearchFilterReducer.reduce(
            applying,
            SearchFilterResult.ApplySucceeded(enabled.criteria),
        )
        val reset = SearchFilterReducer.reduce(applied, SearchFilterResult.FiltersReset)

        assertFalse(initial.criteria.withSalary)
        assertTrue(enabled.criteria.withSalary)
        assertTrue(applying.isApplying)
        assertTrue(applied.isInitialized)
        assertFalse(applied.isApplying)
        assertEquals(SearchFilterCriteria(), reset.criteria)
        assertNull(reset.errorMessage)
    }

    @Test
    fun `failure clears applying status and keeps the current criteria`() {
        val beforeFailure = SearchFilterState(
            criteria = SearchFilterCriteria(withSalary = true),
            isInitialized = true,
            isApplying = true,
        )

        val failed = SearchFilterReducer.reduce(
            beforeFailure,
            SearchFilterResult.Failed(SearchFilterResult.Operation.APPLY, "offline"),
        )

        assertEquals(beforeFailure.criteria, failed.criteria)
        assertTrue(failed.isInitialized)
        assertFalse(failed.isApplying)
        assertEquals("offline", failed.errorMessage)
    }
}

