package dev.betterhunter.app

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import dev.betterhunter.app.di.AppGraph
import dev.betterhunter.app.ui.HunterGlyph
import dev.betterhunter.app.ui.HunterIcon
import dev.betterhunter.search.engine.MockSearchScenario
import dev.betterhunter.search.model.*
import dev.betterhunter.search.presentation.SearchUiState
import dev.betterhunter.search.store.*
import dev.betterhunter.vacancy.domain.model.Vacancy
import dev.betterhunter.vacancy.presentation.compose.VacancyCard
import dev.betterhunter.vacancy.presentation.compose.VacancyDetailsScreen
import dev.betterhunter.vacancy.presentation.model.VacancyDetailsUiState
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

private const val SEARCH = "search"
private const val FILTERS = "filters"
private const val DETAILS = "details/{vacancyId}"

@Composable
fun BetterHunterApp() {
    val graph = remember { AppGraph() }
    val scope = rememberCoroutineScope()
    val store = remember(graph) { SearchStore(scope, SearchActor(graph.gateway, graph.filterEngine, pageSize = 6)) }
    val state by store.state.collectAsStateWithLifecycle()
    val nav = rememberNavController()
    val scenario by graph.scenario.collectAsStateWithLifecycle()
    LaunchedEffect(store, nav) {
        store.effects.collect { effect ->
            when (effect) {
                SearchEffect.OpenFilters -> nav.navigate(FILTERS)
                is SearchEffect.OpenVacancy -> nav.navigate("details/${android.net.Uri.encode(effect.id)}")
                SearchEffect.NavigateBack -> if (nav.previousBackStackEntry != null) nav.popBackStack()
            }
        }
    }
    DisposableEffect(store) { onDispose { store.close() } }
    NavHost(navController = nav, startDestination = SEARCH, modifier = Modifier.fillMaxSize().safeDrawingPadding().imePadding()) {
        composable(SEARCH) {
            SearchScreen(state, scenario, graph) { intent -> scope.launch { store.dispatch(intent) } }
        }
        composable(FILTERS) {
            val back: () -> Unit = { scope.launch { store.dispatch(SearchIntent.BackNavigation) } }
            BackHandler(onBack = back)
            FilterScreen(
                state.filterSelection, graph,
                onSelection = { selection -> scope.launch { store.dispatch(SearchIntent.FiltersApplied(selection)); store.dispatch(SearchIntent.BackNavigation) } },
                onReset = { scope.launch { store.dispatch(SearchIntent.FiltersReset) } },
                onBack = back,
            )
        }
        composable(DETAILS, arguments = listOf(navArgument("vacancyId") { type = NavType.StringType })) { entry ->
            val id = entry.arguments?.getString("vacancyId").orEmpty()
            val back: () -> Unit = { scope.launch { store.dispatch(SearchIntent.BackNavigation) } }
            BackHandler(onBack = back)
            Column(Modifier.fillMaxSize()) {
                ScreenBar("О вакансии", onBack = back)
                VacancyDetailsRoute(id, graph, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun SearchScreen(state: SearchState, scenario: MockSearchScenario, graph: AppGraph, onIntent: (SearchIntent) -> Unit) {
    val colors = MaterialTheme.colorScheme
    val focus = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current
    fun submit() {
        focus.clearFocus()
        keyboard?.hide()
        onIntent(SearchIntent.SearchSubmitted)
    }
    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = RoundedCornerShape(13.dp), color = colors.primary, modifier = Modifier.size(39.dp)) {
                Box(contentAlignment = Alignment.Center) { Text("bh", fontWeight = FontWeight.Black, color = colors.onPrimary, style = MaterialTheme.typography.titleMedium) }
            }
            Spacer(Modifier.width(10.dp))
            Text("BetterHunter", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
            DemoMenu(scenario, graph) { onIntent(SearchIntent.RetryRequested) }
        }
        Column(Modifier.padding(horizontal = 20.dp)) {
            Spacer(Modifier.height(12.dp))
            Text("Найдите своё место.", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(5.dp))
            Text("Работа под ваш ритм жизни", style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
            Spacer(Modifier.height(20.dp))
            OutlinedTextField(
                value = state.queryText,
                onValueChange = { onIntent(SearchIntent.SearchTextChanged(it)) },
                placeholder = { Text("Профессия или навык", style = MaterialTheme.typography.bodyMedium) },
                leadingIcon = { HunterIcon(HunterGlyph.Search, colors.onSurfaceVariant, Modifier.size(21.dp)) },
                trailingIcon = {
                    FilledIconButton(onClick = ::submit, shape = RoundedCornerShape(13.dp), modifier = Modifier.padding(end = 5.dp).semantics { contentDescription = "Найти вакансии" }) {
                        HunterIcon(HunterGlyph.Chevron, colors.onPrimary, Modifier.size(20.dp))
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(19.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = colors.surface,
                    focusedContainerColor = colors.surface,
                    unfocusedBorderColor = colors.outlineVariant,
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { submit() }),
                modifier = Modifier.fillMaxWidth().semantics { contentDescription = "Поисковый запрос" },
            )
            Spacer(Modifier.height(12.dp))
            SearchTools(state, onIntent)
            Spacer(Modifier.height(20.dp))
        }
        when (val ui = state.ui) {
            SearchUiState.Initial, is SearchUiState.Loading -> StatusPanel("Подбираем вакансии", "Ещё немного — и можно выбирать.", loading = true, modifier = Modifier.weight(1f))
            is SearchUiState.Empty -> StatusPanel("Пока ничего не нашлось", "Попробуйте другой запрос или измените фильтры.", action = "Изменить фильтры", onAction = { onIntent(SearchIntent.FiltersOpened) }, modifier = Modifier.weight(1f))
            is SearchUiState.Error -> StatusPanel("Не удалось загрузить", ui.message, action = "Попробовать ещё раз", onAction = { onIntent(SearchIntent.RetryRequested) }, modifier = Modifier.weight(1f))
            is SearchUiState.Content -> SearchResults(ui.value.results.mapNotNull { graph.vacancies.find(it.id) }, ui.value.totalCount, graph, state.isLoadingNextPage, ui.value.hasNextPage, onIntent, Modifier.weight(1f))
            is SearchUiState.LoadingNextPage -> SearchResults(ui.content.results.mapNotNull { graph.vacancies.find(it.id) }, ui.content.totalCount, graph, true, ui.content.hasNextPage, onIntent, Modifier.weight(1f))
        }
    }
}

@Composable
private fun DemoMenu(scenario: MockSearchScenario, graph: AppGraph, onChanged: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        TextButton(onClick = { expanded = true }, contentPadding = PaddingValues(horizontal = 10.dp)) {
            Text("Демо", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelMedium)
            Text(" · ", color = MaterialTheme.colorScheme.primary)
            Text(when (scenario) { MockSearchScenario.SUCCESS -> "пример"; MockSearchScenario.EMPTY -> "пусто"; MockSearchScenario.ERROR -> "ошибка" }, style = MaterialTheme.typography.labelMedium)
        }
        DropdownMenu(expanded, onDismissRequest = { expanded = false }) {
            listOf(MockSearchScenario.SUCCESS to "Пример вакансий", MockSearchScenario.EMPTY to "Нет результатов", MockSearchScenario.ERROR to "Ошибка и повтор").forEach { (option, label) ->
                DropdownMenuItem(text = { Text(label) }, onClick = { expanded = false; graph.setScenario(option); onChanged() })
            }
        }
    }
}

@Composable
private fun SearchTools(state: SearchState, onIntent: (SearchIntent) -> Unit) {
    val colors = MaterialTheme.colorScheme
    val count = state.filterSelection.values.values.sumOf { it.size } + (if (state.filterSelection.salaryMinimum != null) 1 else 0) + (if (state.filterSelection.currencyCode != null) 1 else 0)
    val options = listOf("relevance" to "По релевантности", "publication_time" to "Сначала новые", "salary_desc" to "Зарплата ↓", "salary_asc" to "Зарплата ↑")
    val sortId = (state.criteria.sort as? SearchSort.Known)?.id ?: "relevance"
    var menu by remember { mutableStateOf(false) }
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedButton(
            onClick = { onIntent(SearchIntent.FiltersOpened) }, modifier = Modifier.weight(1f),
            border = BorderStroke(1.dp, if (count > 0) colors.primary else colors.outlineVariant),
            shape = RoundedCornerShape(14.dp), contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp),
        ) {
            HunterIcon(HunterGlyph.Filters, colors.primary, Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(if (count > 0) "Фильтры · $count" else "Фильтры", color = colors.onSurface, maxLines = 1)
        }
        Box(Modifier.weight(1.2f)) {
            OutlinedButton(onClick = { menu = true }, modifier = Modifier.fillMaxWidth(), border = BorderStroke(1.dp, colors.outlineVariant), shape = RoundedCornerShape(14.dp), contentPadding = PaddingValues(horizontal = 10.dp, vertical = 12.dp)) {
                HunterIcon(HunterGlyph.Sort, colors.onSurfaceVariant, Modifier.size(17.dp))
                Spacer(Modifier.width(7.dp))
                Text(options.first { it.first == sortId }.second, style = MaterialTheme.typography.labelMedium, color = colors.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            DropdownMenu(menu, onDismissRequest = { menu = false }) {
                options.forEach { (id, label) ->
                    DropdownMenuItem(text = { Text(label, color = if (id == sortId) colors.primary else colors.onSurface) }, onClick = {
                        menu = false
                        onIntent(SearchIntent.SortChanged(if (id == "relevance") SearchSort.Unspecified else SearchSort.Known(id)))
                    })
                }
            }
        }
    }
}

@Composable
private fun SearchResults(vacancies: List<Vacancy>, totalCount: Int, graph: AppGraph, loadingNext: Boolean, hasNext: Boolean, onIntent: (SearchIntent) -> Unit, modifier: Modifier = Modifier) {
    LazyColumn(modifier.fillMaxWidth(), contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Row(Modifier.fillMaxWidth().padding(bottom = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("Найдено: $totalCount", style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                Text("ДЕМО-ВАКАНСИИ", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        items(vacancies, key = { it.id }) { vacancy ->
            VacancyCard(graph.vacancyMapper.toCard(vacancy), onClick = { onIntent(SearchIntent.VacancySelected(it)) })
        }
        if (hasNext || loadingNext) item {
            OutlinedButton(onClick = { onIntent(SearchIntent.NextPageRequested) }, enabled = !loadingNext, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), contentPadding = PaddingValues(15.dp)) {
                if (loadingNext) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp) else Text("Показать ещё")
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FilterScreen(selection: SearchFilterSelection, graph: AppGraph, onSelection: (SearchFilterSelection) -> Unit, onReset: () -> Unit, onBack: () -> Unit) {
    var draft by remember(selection) { mutableStateOf(selection) }
    var salaryText by rememberSaveable(selection.salaryMinimum) { mutableStateOf(selection.salaryMinimum?.toPlainString().orEmpty()) }
    val salaryInvalid = salaryText.isNotBlank() && (salaryText.toBigDecimalOrNull()?.signum()?.let { it < 0 } != false)
    val colors = MaterialTheme.colorScheme
    val definitions = remember(graph) { graph.filterEngine.definitions(graph.filterEngine.facets(graph.documents)) }
    val scroll = rememberScrollState()
    Column(Modifier.fillMaxSize()) {
        ScreenBar("Фильтры", onBack, trailing = {
            TextButton(onClick = { salaryText = ""; draft = graph.filterEngine.reset(); onReset() }) { Text("Сбросить") }
        })
        Column(Modifier.weight(1f).verticalScroll(scroll).padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
            Text("Оставьте только то, что подходит вам.", color = colors.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
            Surface(shape = RoundedCornerShape(22.dp), color = colors.surface, border = BorderStroke(1.dp, colors.outlineVariant)) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text("Зарплата", style = MaterialTheme.typography.titleMedium)
                    OutlinedTextField(
                        value = salaryText, onValueChange = { salaryText = it; draft = draft.copy(salaryMinimum = it.toBigDecimalOrNull()) },
                        label = { Text("От какой суммы") }, singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done),
                        isError = salaryInvalid,
                        supportingText = if (salaryInvalid) ({ Text("Введите неотрицательное число") }) else null,
                        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp),
                    )
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("RUB" to "₽ RUB", "USD" to "$ USD", "EUR" to "€ EUR", "KZT" to "₸ KZT").forEach { (id, text) ->
                            FilterChip(selected = draft.currencyCode == id, onClick = { draft = draft.copy(currencyCode = id.takeUnless { draft.currencyCode == id }) }, label = { Text(text) })
                        }
                    }
                    HorizontalDivider(color = colors.outlineVariant)
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("Только с зарплатой", style = MaterialTheme.typography.titleSmall)
                            Text("Без вакансий с пустой суммой", style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                        }
                        Switch(checked = "true" in draft.selected("with_salary"), onCheckedChange = { enabled -> draft = draft.with("with_salary", if (enabled) setOf("true") else emptySet()) }, modifier = Modifier.semantics { contentDescription = "Только с зарплатой" })
                    }
                }
            }
            definitions.filter { it.id !in setOf("salary", "with_salary") }.forEach { definition ->
                Column {
                    Text(definition.title, style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(6.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        definition.options.forEach { option ->
                            val selected = option.id in draft.selected(definition.id)
                            FilterChip(selected, onClick = {
                                val values = draft.selected(definition.id).toMutableSet()
                                if (selected) values.remove(option.id) else values.add(option.id)
                                draft = draft.with(definition.id, values)
                            }, label = { Text(optionLabel(option.id)) })
                        }
                    }
                }
            }
            Spacer(Modifier.height(4.dp))
        }
        Surface(color = colors.background, shadowElevation = 10.dp) {
            Button(onClick = { onSelection(draft) }, enabled = !salaryInvalid, shape = RoundedCornerShape(17.dp), modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp).heightIn(min = 54.dp)) {
                Text("Показать вакансии")
            }
        }
    }
}

private fun optionLabel(id: String): String = when (id) {
    "NO_EXPERIENCE" -> "Без опыта"; "ONE_TO_THREE_YEARS" -> "1–3 года"; "THREE_TO_SIX_YEARS" -> "3–6 лет"; "MORE_THAN_SIX_YEARS" -> "Более 6 лет"
    "FULL_TIME" -> "Полная занятость"; "PART_TIME" -> "Подработка"; "PROJECT" -> "Проектная работа"; "INTERNSHIP" -> "Стажировка"; "VOLUNTEER" -> "Волонтёрство"
    "ON_SITE" -> "В офисе"; "HYBRID" -> "Гибрид"; "REMOTE" -> "Удалённо"
    "FIVE_TWO" -> "5 / 2"; "SIX_ONE" -> "6 / 1"; "FLEXIBLE" -> "Гибкий"; "SHIFT" -> "Сменный"; "ROTATIONAL" -> "Вахта"; "UNKNOWN" -> "Не указан"
    else -> id
}

@Composable
private fun ScreenBar(title: String, onBack: () -> Unit, trailing: @Composable () -> Unit = {}) {
    Row(Modifier.fillMaxWidth().padding(start = 8.dp, end = 12.dp, top = 8.dp, bottom = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onBack, modifier = Modifier.semantics { contentDescription = "Назад" }) {
            HunterIcon(HunterGlyph.Back, MaterialTheme.colorScheme.onSurface, Modifier.size(22.dp))
        }
        Text(title, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f).padding(start = 6.dp))
        trailing()
    }
}

@Composable
private fun VacancyDetailsRoute(id: String, graph: AppGraph, modifier: Modifier) {
    val vacancy = remember(id, graph) { graph.vacancies.find(id) }
    val state = vacancy?.let { VacancyDetailsUiState.Content(graph.vacancyMapper.toDetails(it)) } ?: VacancyDetailsUiState.Empty
    VacancyDetailsScreen(state, dev.betterhunter.app.di.AppVacancyLabels, onRetry = {}, modifier = modifier)
}

@Composable
private fun StatusPanel(title: String, message: String, modifier: Modifier = Modifier, loading: Boolean = false, action: String? = null, onAction: () -> Unit = {}) {
    Column(modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        if (loading) CircularProgressIndicator(Modifier.size(34.dp), strokeWidth = 2.dp)
        else Surface(shape = RoundedCornerShape(22.dp), color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.size(72.dp)) {
            Box(contentAlignment = Alignment.Center) { HunterIcon(HunterGlyph.Search, MaterialTheme.colorScheme.primary, Modifier.size(28.dp)) }
        }
        Spacer(Modifier.height(20.dp))
        Text(title, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        Text(message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        if (action != null) {
            Spacer(Modifier.height(16.dp))
            OutlinedButton(onClick = onAction) { Text(action) }
        }
    }
}
