package dev.betterhunter.search.parser

import dev.betterhunter.search.model.SearchInputIssue
import java.net.URI
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

data class SearchQueryParameters(
    val values: Map<String, List<String>>,
    val residualParameters: Map<String, List<String>>,
    val issues: List<SearchInputIssue>,
)

/** Parses an URI query into an ordered multimap without making network requests. */
class SearchQueryParser(
    private val registry: SearchParameterRegistry = SearchParameterRegistry(),
) {
    fun parseUri(uri: String): SearchQueryParameters {
        val sourceCompatible = uri.replace("+", "%20")
        val query = try {
            URI(sourceCompatible).rawQuery.orEmpty()
        } catch (_: Exception) {
            sourceCompatible.substringAfter('?', "").substringBefore('#')
        }
        return parseQuery(query)
    }

    /** Parses a raw query component (without the leading '?'); literal plus is retained. */
    fun parseQuery(rawQuery: String): SearchQueryParameters {
        val parsed = LinkedHashMap<String, MutableList<String>>()
        val issues = mutableListOf<SearchInputIssue>()
        if (rawQuery.isNotEmpty()) {
            rawQuery.split('&').forEach { pair ->
                if (pair.isEmpty()) return@forEach
                val separator = pair.indexOf('=')
                val rawKey = if (separator < 0) pair else pair.substring(0, separator)
                val rawValue = if (separator < 0) "" else pair.substring(separator + 1)
                val key = decode(rawKey, "query-key", issues)
                val value = decode(rawValue, key, issues)
                parsed.getOrPut(key) { mutableListOf() }.add(value)
            }
        }

        val reader = QueryParameterReader(parsed)
        registry.consumedKeys.forEach(reader::listOrEmpty)
        val residual = reader.remaining().filterNot { (key, _) -> key in registry.omittedFromResidual }
        residual.keys
            .filterNot { it in registry.recognizedButUnmapped }
            .forEach { issues += SearchInputIssue(it, SearchInputIssue.Reason.UNKNOWN_PARAMETER) }

        return SearchQueryParameters(
            values = parsed.mapValues { (_, list) -> list.toList() },
            residualParameters = LinkedHashMap(residual),
            issues = issues,
        )
    }

    private fun decode(value: String, parameter: String, issues: MutableList<SearchInputIssue>): String =
        try {
            // URLDecoder normally treats '+' as a space; Uri.decode does not.
            URLDecoder.decode(value.replace("+", "%2B"), StandardCharsets.UTF_8)
        } catch (_: Exception) {
            issues += SearchInputIssue(parameter, SearchInputIssue.Reason.MALFORMED_ENCODING, value)
            value
        }
}

