package dev.betterhunter.search

import dev.betterhunter.search.converter.SearchCriteriaConverter
import dev.betterhunter.search.currency.DefaultCurrencyResolver
import dev.betterhunter.search.model.GeoBounds
import dev.betterhunter.search.model.SearchInputIssue
import dev.betterhunter.search.model.SearchSort
import dev.betterhunter.search.parser.SearchQueryParser
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SearchCriteriaConverterTest {
    private val parser = SearchQueryParser()
    private val converter = SearchCriteriaConverter(DefaultCurrencyResolver())

    private fun convert(query: String) = converter.convert(parser.parseQuery(query))

    @Test
    fun absentParametersUseExplicitBetterHunterDefaults() {
        val criteria = convert("")

        assertEquals("", criteria.text)
        assertTrue(criteria.areas.isEmpty())
        assertNull(criteria.salary)
        assertNull(criteria.periodDays)
        assertEquals(SearchSort.Unspecified, criteria.sort)
        assertTrue(criteria.residualParameters.isEmpty())
    }

    @Test
    fun periodUsesFirstPresentKeyAndInvalidValueDoesNotFallThrough() {
        assertEquals(7, convert("period=7&period=30&search_period=9").periodDays)
        val invalid = convert("period=bad&search_period=14")

        assertNull(invalid.periodDays)
        assertTrue(invalid.issues.any { it.reason == SearchInputIssue.Reason.INVALID_INTEGER })
        assertEquals(14, convert("search_period=14").periodDays)
    }

    @Test
    fun booleanParsingIsCaseInsensitiveAndStrict() {
        assertTrue(convert("accept_temporary=TRUE").acceptsTemporary)
        assertFalse(convert("accept_temporary=yes").acceptsTemporary)
        assertFalse(convert("").acceptsTemporary)
    }

    @Test
    fun salaryExperienceLabelsAndCurrencyAreConverted() {
        val criteria = convert(
            "salary=1250&currency=usd&experience=doesNotMatter&experience=between3And6" +
                "&label=with_salary&label=night_shifts&label=custom",
        )

        assertEquals("1250", criteria.salary?.rawAmount)
        assertEquals("USD", criteria.currencyCode)
        assertEquals(listOf("between3And6"), criteria.experience)
        assertEquals(listOf("custom"), criteria.labels)
        assertTrue(criteria.withSalaryOnly)
        assertTrue(criteria.nightShifts)
    }

    @Test
    fun explicitEmployersWinAndHeadEmployerIsFallback() {
        assertEquals(listOf("1", "2"), convert("employer_id=1&employer_id=2&head_employer_id=3").employers)
        assertEquals(listOf("3"), convert("head_employer_id=3").employers)
    }

    @Test
    fun malformedSalaryIsPreservedAndReported() {
        val criteria = convert("salary=not-a-number")

        assertEquals("not-a-number", criteria.salary?.rawAmount)
        assertNull(criteria.salary?.amount)
        assertTrue(criteria.issues.any { it.reason == SearchInputIssue.Reason.INVALID_DECIMAL })
    }

    @Test
    fun incompleteAndInvalidGeoBoundsAreRejectedWithIssue() {
        val partial = convert("top_lat=50&left_lng=30")

        assertNull(partial.geoBounds)
        assertTrue(partial.issues.any { it.reason == SearchInputIssue.Reason.INVALID_COORDINATE })

        val invalid = convert("top_lat=95&left_lng=30&bottom_lat=40&right_lng=40")
        assertNull(invalid.geoBounds)
        assertTrue(invalid.issues.any { it.reason == SearchInputIssue.Reason.INVALID_GEOGRAPHIC_RANGE })
    }

    @Test
    fun completeBoundsAndSortPointAreTyped() {
        val criteria = convert(
            "top_lat=55&left_lng=30&bottom_lat=50&right_lng=40" +
                "&top_right_lat=56&top_right_lng=41&bottom_left_lat=49&bottom_left_lng=29" +
                "&sort_point_lat=52.5&sort_point_lng=37.6",
        )

        assertEquals(GeoBounds(56.0, 29.0, 49.0, 41.0), criteria.geoBounds)
        assertEquals(52.5, criteria.sortPoint?.latitude)
        assertEquals(37.6, criteria.sortPoint?.longitude)
    }

    @Test
    fun unknownSortIsPreservedAndResidualParamsKeepOrder() {
        val criteria = convert("custom_first=a&order_by=vendor-sort&custom_second=b")

        assertEquals("vendor-sort", assertIs<SearchSort.Unknown>(criteria.sort).rawValue)
        assertEquals(listOf("custom_first", "custom_second"), criteria.residualParameters.keys.toList())
    }

    @Test
    fun currencyPolicyIsExplicitAndIndependent() {
        val resolver = DefaultCurrencyResolver()

        assertEquals("EUR", resolver.resolve("eur", "unknown"))
        assertEquals("KZT", resolver.resolve(null, "KZ"))
        assertNull(resolver.resolve(null, "unknown"))
        assertNull(convert("currency=eur").currencyCode)
    }
}
