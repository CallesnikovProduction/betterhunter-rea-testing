package dev.betterhunter.search

import dev.betterhunter.search.parser.QueryParameterReader
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotSame
import kotlin.test.assertNull
import kotlin.test.assertNotSame

class QueryParameterReaderTest {
    @Test
    fun missingEmptyAndRepeatedReadsFollowRemovalContract() {
        val input = linkedMapOf("empty" to emptyList(), "many" to listOf("first", "second"))
        val reader = QueryParameterReader(input)

        assertNull(reader.firstOrNull("missing"))
        assertEquals("", reader.firstOrEmpty("empty"))
        assertEquals("first", reader.firstOrNull("many"))
        assertNull(reader.firstOrNull("many"))
        assertEquals(emptyMap(), reader.remaining())
        assertEquals(listOf("first", "second"), input["many"])
    }

    @Test
    fun listReadPreservesOrderAndRemovesKey() {
        val values = listOf("a", "b", "a")
        val reader = QueryParameterReader(linkedMapOf("x" to values, "y" to listOf("z")))

        val result = reader.listOrEmpty("x")

        assertEquals(values, result)
        assertNotSame(values, result)
        assertEquals(linkedMapOf("y" to listOf("z")), reader.remaining())
    }

    @Test
    fun residualOrderIsStableAndSnapshotCannotMutateReader() {
        val reader = QueryParameterReader(linkedMapOf("first" to listOf("1"), "second" to listOf("2")))
        val snapshot = reader.remaining()

        assertEquals(listOf("first", "second"), snapshot.keys.toList())
        assertEquals(listOf("1"), snapshot.getValue("first"))
        assertEquals(listOf("1"), reader.remaining().getValue("first"))
    }
}


