package dev.betterhunter.search

import dev.betterhunter.search.model.SearchInputIssue
import dev.betterhunter.search.parser.SearchQueryParser
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SearchQueryParserTest {
    private val parser = SearchQueryParser()

    @Test
    fun duplicateKeysAndUnknownsKeepInsertionAndValueOrder() {
        val result = parser.parseQuery("area=1&custom=x&area=2&other=y")

        assertEquals(listOf("1", "2"), result.values.getValue("area"))
        assertEquals(listOf("custom", "other"), result.residualParameters.keys.toList())
        assertEquals(2, result.issues.count { it.reason == SearchInputIssue.Reason.UNKNOWN_PARAMETER })
    }

    @Test
    fun uriSourceRewritesLiteralPlusToSpaceButRawQueryRetainsIt() {
        assertEquals("a b", parser.parseUri("https://example.test/search?text=a+b").values.getValue("text").first())
        assertEquals("a+b", parser.parseQuery("text=a+b").values.getValue("text").first())
    }

    @Test
    fun percentEncodingAndEmptyScalarAreParsed() {
        val result = parser.parseQuery("text=hello%20world&salary=&area")

        assertEquals("hello world", result.values.getValue("text").first())
        assertEquals("", result.values.getValue("salary").first())
        assertEquals(listOf(""), result.values.getValue("area"))
    }

    @Test
    fun malformedEncodingIsReportedAndRawValueRetained() {
        val result = parser.parseQuery("text=%Q1")

        assertEquals("%Q1", result.values.getValue("text").first())
        assertTrue(result.issues.any { it.reason == SearchInputIssue.Reason.MALFORMED_ENCODING })
    }

    @Test
    fun queryConsumptionDoesNotRemoveParametersFromReturnedRawValues() {
        val result = parser.parseQuery("period=4&search_period=9&unknown=x")

        assertEquals("4", result.values.getValue("period").first())
        assertEquals(listOf("unknown"), result.residualParameters.keys.toList())
    }
}
