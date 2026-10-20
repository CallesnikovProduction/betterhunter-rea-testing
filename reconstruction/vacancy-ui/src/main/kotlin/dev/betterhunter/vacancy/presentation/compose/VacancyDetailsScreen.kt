package dev.betterhunter.vacancy.presentation.compose

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.betterhunter.vacancy.presentation.model.*

@Composable
fun VacancyDetailsScreen(state: VacancyDetailsUiState, labels: VacancyScreenLabels, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    when (state) {
        VacancyDetailsUiState.Loading -> Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        VacancyDetailsUiState.Empty -> Box(modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) { Text(labels.notFound(), style = MaterialTheme.typography.titleMedium) }
        is VacancyDetailsUiState.Error -> Column(modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Text(state.message)
            Spacer(Modifier.height(16.dp))
            Button(onClick = onRetry) { Text(labels.retry()) }
        }
        is VacancyDetailsUiState.Content -> VacancyDetailsContent(state.vacancy, labels, modifier)
    }
}

@Composable
private fun VacancyDetailsContent(vacancy: VacancyDetailsUiModel, labels: VacancyScreenLabels, modifier: Modifier) {
    LazyColumn(modifier.fillMaxSize(), contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 28.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            Surface(shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.surface, border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
                Column(Modifier.padding(21.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(vacancy.card.title, style = MaterialTheme.typography.headlineSmall)
                    vacancy.card.salary?.let { VacancySalary(it) }
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        vacancy.card.employer?.let { VacancyEmployer(it) }
                        vacancy.card.location?.let { VacancyLocation(it) }
                    }
                    VacancyTags(listOfNotNull(vacancy.card.experience, vacancy.card.employment, vacancy.card.workFormat, vacancy.card.schedule))
                }
            }
        }
        vacancy.description?.takeIf(String::isNotBlank)?.let { text -> item { VacancyDescriptionSection(labels.description(), listOf(text)) } }
        if (vacancy.requirements.isNotEmpty()) item { VacancyDescriptionSection(labels.requirements(), vacancy.requirements) }
        if (vacancy.skills.isNotEmpty()) item { VacancySkillsSection(labels.skills(), vacancy.skills) }
        if (vacancy.additionalConditions.isNotEmpty()) item { VacancyDescriptionSection(labels.conditions(), vacancy.additionalConditions) }
    }
}

@Composable
fun VacancyDescriptionSection(title: String, lines: List<String>) {
    SectionCard(title) {
        lines.forEach { Text(if (lines.size > 1) "•  $it" else it, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant) }
    }
}

@Composable
fun VacancySkillsSection(title: String, skills: List<String>) = SectionCard(title) { VacancyTags(skills) }

@Composable
private fun SectionCard(title: String, content: @Composable () -> Unit) {
    Column(Modifier.fillMaxWidth().padding(horizontal = 2.dp, vertical = 5.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        content()
    }
}
