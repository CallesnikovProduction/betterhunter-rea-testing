package dev.betterhunter.search.presentation

import dev.betterhunter.search.model.*

data class SearchResultItemUiModel(
    val id: String,
    val title: String,
    val employerAndLocation: String,
    val salaryText: String?,
    val source: SearchDocument,
)

data class SearchContentUiModel(
    val criteria: VacancySearchCriteria,
    val results: List<SearchResultItemUiModel>,
    val totalCount: Int,
    val pageIndex: Int,
    val pageSize: Int,
    val hasNextPage: Boolean,
) {
    fun append(page: SearchResultPage, mapper: SearchResultUiMapper = SearchResultUiMapper()): SearchContentUiModel = copy(
        results = (results + page.items.map(mapper::map)).distinctBy { it.id },
        totalCount = page.totalCount, pageIndex = page.pageIndex, pageSize = page.pageSize, hasNextPage = page.hasNextPage,
    )
}

sealed interface SearchUiState {
    data object Initial : SearchUiState
    data class Loading(val criteria: VacancySearchCriteria) : SearchUiState
    data class Content(val value: SearchContentUiModel) : SearchUiState
    data class Empty(val criteria: VacancySearchCriteria) : SearchUiState
    data class Error(val criteria: VacancySearchCriteria, val message: String, val previous: SearchContentUiModel? = null) : SearchUiState
    data class LoadingNextPage(val content: SearchContentUiModel) : SearchUiState
}

class SearchResultUiMapper {
    fun map(document: SearchDocument) = SearchResultItemUiModel(
        id = document.id,
        title = document.title,
        employerAndLocation = listOfNotNull(document.employer, document.location).joinToString(" · "),
        salaryText = when {
            document.salaryFrom == null && document.salaryTo == null -> null
            document.salaryFrom != null && document.salaryTo != null -> "${document.salaryFrom}–${document.salaryTo} ${document.currencyCode.orEmpty()}".trim()
            else -> "${document.salaryFrom ?: document.salaryTo} ${document.currencyCode.orEmpty()}".trim()
        },
        source = document,
    )

    fun map(page: SearchResultPage, criteria: VacancySearchCriteria): SearchUiState = if (page.items.isEmpty()) {
        SearchUiState.Empty(criteria)
    } else {
        SearchUiState.Content(SearchContentUiModel(criteria, page.items.map(::map), page.totalCount, page.pageIndex, page.pageSize, page.hasNextPage))
    }

    fun mapFailure(criteria: VacancySearchCriteria, cause: Throwable, previous: SearchContentUiModel? = null): SearchUiState.Error =
        SearchUiState.Error(criteria, cause.message?.takeIf(String::isNotBlank) ?: "Поиск не выполнен", previous)
}
