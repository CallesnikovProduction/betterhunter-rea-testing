package dev.betterhunter.search.parser

/**
 * A mutable ordered multimap. Constructing a reader copies both the map and its lists,
 * so consuming values never mutates caller-owned input.
 */
class QueryParameterReader(parameters: Map<String, List<String>>) {
    private val remaining = LinkedHashMap<String, MutableList<String>>().apply {
        parameters.forEach { (key, values) -> put(key, values.toMutableList()) }
    }

    fun firstOrNull(key: String): String? = remove(key)?.firstOrNull()

    fun firstOrEmpty(key: String): String = firstOrNull(key).orEmpty()

    fun listOrEmpty(key: String): List<String> = remove(key)?.toList() ?: emptyList()

    fun contains(key: String): Boolean = remaining.containsKey(key)

    /** Returns an ordered defensive snapshot of unconsumed values. */
    fun remaining(): Map<String, List<String>> =
        LinkedHashMap<String, List<String>>().apply {
            remaining.forEach { (key, values) -> put(key, values.toList()) }
        }

    private fun remove(key: String): MutableList<String>? = remaining.remove(key)
}
