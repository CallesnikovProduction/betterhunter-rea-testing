package dev.betterhunter.vacancy.presentation.compose

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.betterhunter.vacancy.presentation.model.VacancyCardUiModel

@Composable
fun VacancyCard(model: VacancyCardUiModel, onClick: (String) -> Unit, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    Card(
        onClick = { onClick(model.id) },
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(23.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        border = BorderStroke(1.dp, colors.outlineVariant),
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = RoundedCornerShape(11.dp), color = colors.secondaryContainer, modifier = Modifier.size(34.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(model.employer?.trim()?.firstOrNull()?.uppercaseChar()?.toString() ?: "В", style = MaterialTheme.typography.titleSmall, color = colors.onSecondaryContainer)
                    }
                }
                Spacer(Modifier.width(10.dp))
                Text(model.employer ?: "Работодатель не указан", style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                Spacer(Modifier.width(10.dp))
                Text("↗", style = MaterialTheme.typography.titleLarge, color = colors.onSurfaceVariant)
            }
            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                VacancyHeader(model.title)
                model.salary?.let { VacancySalary(it) }
            }
            model.location?.let { VacancyLocation(it) }
            VacancyTags(listOfNotNull(model.experience, model.workFormat))
        }
    }
}

@Composable
fun VacancyHeader(title: String) {
    Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
}

@Composable
fun VacancySalary(text: String) {
    Text(text, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
}

@Composable
fun VacancyEmployer(name: String) {
    Text(name, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
}

@Composable
fun VacancyLocation(address: String) {
    Text(address, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun VacancyTags(labels: List<String>) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(7.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
        labels.filter(String::isNotBlank).distinct().forEach { text ->
            Surface(shape = RoundedCornerShape(9.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
                Text(text, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
