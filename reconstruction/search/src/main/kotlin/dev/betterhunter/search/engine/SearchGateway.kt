package dev.betterhunter.search.engine

import dev.betterhunter.search.model.*
import java.math.BigDecimal

fun interface SearchGateway { suspend fun search(request: SearchRequest): SearchResultPage }

fun interface SearchDocumentAdapter<T> { fun adapt(value: T): SearchDocument }

enum class MockSearchScenario { SUCCESS, EMPTY, ERROR }

class MockSearchGateway(
    documents: List<SearchDocument>,
    private val scenario: MockSearchScenario = MockSearchScenario.SUCCESS,
    private val failureMessage: String = "Локальный поиск временно недоступен",
    private val nowEpochMillis: () -> Long = System::currentTimeMillis,
) : SearchGateway {
    private val source = documents.toList()

    override suspend fun search(request: SearchRequest): SearchResultPage {
        if (scenario == MockSearchScenario.ERROR) throw SearchGatewayException(failureMessage)
        if (scenario == MockSearchScenario.EMPTY) return SearchResultPage(emptyList(), request.pageIndex, request.pageSize, 0)
        val criteria = request.criteria
        val query = criteria.text.trim().lowercase().split(Regex("\\s+")).filter(String::isNotBlank)
        val matches = source.withIndex().filter { (_, doc) -> matches(doc, criteria, query) }
        val sorted = when ((criteria.sort as? SearchSort.Known)?.id) {
            "publication_time" -> matches.sortedWith(compareByDescending<IndexedValue<SearchDocument>> { it.value.publishedAtEpochMillis ?: Long.MIN_VALUE })
            "salary_desc" -> matches.sortedWith(compareByDescending<IndexedValue<SearchDocument>> { salaryKey(it.value) })
            "salary_asc" -> matches.sortedWith(compareBy<IndexedValue<SearchDocument>> { salaryKey(it.value) })
            else -> matches.sortedWith(compareByDescending<IndexedValue<SearchDocument>> { relevance(it.value, query) })
        }
        val start = (request.pageIndex.toLong() * request.pageSize).coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
        return SearchResultPage(sorted.drop(start).take(request.pageSize).map { it.value }, request.pageIndex, request.pageSize, sorted.size)
    }

    private fun matches(d: SearchDocument, c: VacancySearchCriteria, query: List<String>): Boolean {
        val haystack = listOfNotNull(d.title, d.employer, d.description).joinToString(" ").lowercase()
        if (query.any { token -> !haystack.contains(token) }) return false
        if (c.areas.isNotEmpty() && c.areas.none { it.equals(d.location, true) }) return false
        if (c.salary != null) {
            val minimum = c.salary.amount ?: return false
            if (minimum < BigDecimal.ZERO) return false
            if (c.currencyCode != null && !c.currencyCode.equals(d.currencyCode, true)) return false
            // With no requested currency, compare displayed numeric values as-is; no FX conversion is implied.
            val maximum = listOfNotNull(d.salaryFrom, d.salaryTo).maxOrNull() ?: return false
            if (maximum < minimum) return false
        }
        if (c.withSalaryOnly && d.salaryFrom == null && d.salaryTo == null) return false
        if (c.experience.isNotEmpty() && c.experience.none { it.equals(d.experience, true) }) return false
        if (c.employmentForms.isNotEmpty() && c.employmentForms.none { it in d.employmentForms }) return false
        if (c.workFormats.isNotEmpty() && c.workFormats.none { it in d.workFormats }) return false
        if (c.workScheduleByDays.isNotEmpty() && c.workScheduleByDays.none { it in d.workSchedules }) return false
        if (c.periodDays != null) {
            if (c.periodDays < 0) return false
            val published = d.publishedAtEpochMillis ?: return false
            if (published < nowEpochMillis() - c.periodDays * DAY_MILLIS) return false
        }
        if (c.labels.any { it !in d.labels }) return false
        return true
    }

    private fun relevance(d: SearchDocument, terms: List<String>): Int {
        if (terms.isEmpty()) return 0
        val title = d.title.lowercase(); val employer = d.employer.orEmpty().lowercase(); val description = d.description.orEmpty().lowercase()
        return terms.sumOf { term -> (if (title.contains(term)) 8 else 0) + (if (employer.contains(term)) 4 else 0) + (if (description.contains(term)) 1 else 0) }
    }

    private fun salaryKey(d: SearchDocument): BigDecimal? = listOfNotNull(d.salaryFrom, d.salaryTo).maxOrNull()
    private companion object { const val DAY_MILLIS = 86_400_000L }
}

class SearchGatewayException(message: String) : RuntimeException(message)
