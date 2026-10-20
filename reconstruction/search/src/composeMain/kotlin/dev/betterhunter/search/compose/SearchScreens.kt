package dev.betterhunter.search.compose

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.betterhunter.search.model.*
import dev.betterhunter.search.presentation.*

@Composable
fun SearchScreen(
    state: SearchUiState,
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onOpenFilters: () -> Unit,
    onSort: (SearchSort) -> Unit,
    onNextPage: () -> Unit,
    onRetry: () -> Unit,
    onOpenVacancy: (SearchResultItemUiModel) -> Unit,
    modifier: Modifier = Modifier,
    itemContent: @Composable (SearchResultItemUiModel, () -> Unit) -> Unit = { item, open ->
        TextButtonRow(title = item.title, subtitle = item.employerAndLocation, onClick = open)
    },
) {
    Column(modifier.fillMaxSize().padding(16.dp)) {
        SearchBar(query, onQueryChange, onSearch)
        Spacer(Modifier.height(8.dp))
        SearchFilterChips(state, onOpenFilters, onSort)
        Spacer(Modifier.height(8.dp))
        when (state) {
            SearchUiState.Initial -> Text("Введите запрос и начните поиск")
            is SearchUiState.Loading -> CircularProgressIndicator()
            is SearchUiState.Content -> SearchResultList(state.value, onOpenVacancy, itemContent, onNextPage)
            is SearchUiState.LoadingNextPage -> SearchResultList(state.content, onOpenVacancy, itemContent, onNextPage, loading = true)
            is SearchUiState.Empty -> SearchEmptyState()
            is SearchUiState.Error -> SearchErrorState(state.message, onRetry)
        }
    }
}

@Composable
fun SearchBar(query: String, onQueryChange: (String) -> Unit, onSearch: () -> Unit, modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(query, onQueryChange, Modifier.weight(1f), label = { Text("Вакансия или компания") }, singleLine = true)
        Button(onClick = onSearch, modifier = Modifier.alignByBaseline()) { Text("Найти") }
    }
}

@Composable
fun SearchFilterChips(state: SearchUiState, onOpenFilters: () -> Unit, onSort: (SearchSort) -> Unit) {
    val active = when (state) {
        is SearchUiState.Content -> state.value.criteria
        is SearchUiState.LoadingNextPage -> state.content.criteria
        is SearchUiState.Loading -> state.criteria
        is SearchUiState.Empty -> state.criteria
        is SearchUiState.Error -> state.criteria
        SearchUiState.Initial -> VacancySearchCriteria()
    }
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        AssistChip(onClick = onOpenFilters, label = { Text("Фильтры${if (active.areas.isNotEmpty() || active.experience.isNotEmpty()) " •" else ""}") })
        AssistChip(onClick = { onSort(SearchSort.Known("publication_time")) }, label = { Text("Сортировка") })
    }
}

@Composable
fun SearchFiltersScreen(
    definitions: List<SearchFilterDefinition>,
    selection: SearchFilterSelection,
    onSelectionChange: (SearchFilterSelection) -> Unit,
    onApply: () -> Unit,
    onReset: () -> Unit,
) {
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        definitions.forEach { definition ->
            Text(definition.title, style = MaterialTheme.typography.titleMedium)
            definition.options.forEach { option ->
                val selected = option.id in selection.selected(definition.id)
                FilterChip(selected, onClick = {
                    val old = selection.selected(definition.id)
                    val next = if (selected) old - option.id else if (definition.allowsMultiple) old + option.id else setOf(option.id)
                    onSelectionChange(selection.with(definition.id, next))
                }, label = { Text(option.title) })
            }
        }
        Text("Зарплата от", style = MaterialTheme.typography.titleMedium)
        OutlinedTextField(
            value = selection.salaryMinimum?.toPlainString().orEmpty(),
            onValueChange = { raw -> onSelectionChange(selection.copy(salaryMinimum = raw.toBigDecimalOrNull())) },
            label = { Text("Сумма") }, singleLine = true,
        )
        OutlinedTextField(
            value = selection.currencyCode.orEmpty(),
            onValueChange = { raw -> onSelectionChange(selection.copy(currencyCode = raw.trim().takeIf(String::isNotEmpty)) },
            label = { Text("Код валюты, например RUB") }, singleLine = true,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextButton(onClick = onReset) { Text("Сбросить") }
            Button(onClick = onApply) { Text("Применить") }
        }
    }
}

@Composable
fun SortSelector(selected: SearchSort, onSelect: (SearchSort) -> Unit) {
    Column {
        listOf("relevance" to "По релевантности", "publication_time" to "По дате", "salary_desc" to "Сначала высокая зарплата", "salary_asc" to "Сначала низкая зарплата").forEach { (id, title) ->
            val checked = (selected as? SearchSort.Known)?.id == id
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(title); RadioButton(checked, onClick = { onSelect(SearchSort.Known(id)) })
            }
        }
    }
}

@Composable
fun SearchResultList(
    content: SearchContentUiModel,
    onOpenVacancy: (SearchResultItemUiModel) -> Unit,
    itemContent: @Composable (SearchResultItemUiModel, () -> Unit) -> Unit,
    onNextPage: () -> Unit,
    loading: Boolean = false,
) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(content.results, key = { it.id }) { item -> itemContent(item) { onOpenVacancy(item) } }
        item { SearchPagingFooter(content.hasNextPage, loading, onNextPage) }
    }
}

@Composable
fun SearchPagingFooter(hasNextPage: Boolean, loading: Boolean, onNextPage: () -> Unit) {
    if (loading) CircularProgressIndicator()
    else if (hasNextPage) Button(onClick = onNextPage, modifier = Modifier.fillMaxWidth()) { Text("Показать ещё") }
}

@Composable fun SearchEmptyState() { Text("Ничего не найдено. Измените запрос или фильтры.") }

@Composable
fun SearchErrorState(message: String, onRetry: () -> Unit) {
    Column { Text(message); Button(onClick = onRetry) { Text("Повторить") } }
}

/** Default renderer; an app can pass VacancyCard through itemContent instead. */
@Composable
private fun TextButtonRow(title: String, subtitle: String, onClick: () -> Unit) {
    TextButton(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.fillMaxWidth()) { Text(title, style = MaterialTheme.typography.titleMedium); Text(subtitle) }
    }
}

@Composable
fun SearchPreviewContent(onOpenVacancy: (SearchResultItemUiModel) -> Unit = {}) {
    val docs = listOf(
        SearchDocument("preview-1", "Android-разработчик", "Северный ветер", location = "Москва", salaryFrom = java.math.BigDecimal("260000"), currencyCode = "RUB"),
        SearchDocument("preview-2", "Kotlin Backend Engineer", "Лаборатория данных", location = "Удалённо"),
    )
    val state = SearchUiState.Content(SearchContentUiModel(VacancySearchCriteria(text = "Kotlin"), docs.map(SearchResultUiMapper()::map), 2, 0, 20, false))
    SearchScreen(state, "Kotlin", {}, {}, {}, {}, {}, {}, onOpenVacancy)
}
