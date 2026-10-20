package dev.betterhunter.search.converter

/** Conservative BetterHunter text policy; exact source-client normalization is not claimed. */
fun interface SearchTextNormalizer {
    fun normalize(text: String): String
}

object DefaultSearchTextNormalizer : SearchTextNormalizer {
    override fun normalize(text: String): String = text.trim().replace(WHITESPACE, " ")

    private val WHITESPACE = Regex("\\s+")
}
