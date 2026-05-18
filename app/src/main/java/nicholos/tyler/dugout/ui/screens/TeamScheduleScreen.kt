package nicholos.tyler.dugout.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import nicholos.tyler.dugout.model.domain.GameOutcome
import nicholos.tyler.dugout.model.ui.GameCardUiModel
import nicholos.tyler.dugout.ui.components.GameCard
import nicholos.tyler.dugout.ui.theme.DugoutTheme
import nicholos.tyler.dugout.viewmodel.ScheduleView
import nicholos.tyler.dugout.viewmodel.TeamScheduleUiState
import nicholos.tyler.dugout.viewmodel.TeamScheduleViewModel

private data class ScheduleSectionUiModel(
    val title: String,
    val games: List<GameCardUiModel>
)

@Composable
fun TeamScheduleScreen(
    viewModel: TeamScheduleViewModel,
    teamId: Int,
    season: Int,
    modifier: Modifier = Modifier,
    onGameClick: (Int) -> Unit = {}
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(teamId, season) {
        viewModel.loadSeasonGames(teamId = teamId, season = season)
    }

    TeamScheduleContent(
        uiState = uiState.value,
        modifier = modifier,
        onGameClick = { gamePk ->
            viewModel.selectGame(gamePk)
            onGameClick(gamePk)
        },
        onViewSelected = { viewModel.selectView(it) }
    )
}

@Composable
fun TeamScheduleContent(
    uiState: TeamScheduleUiState,
    modifier: Modifier = Modifier,
    onGameClick: (Int) -> Unit = {},
    onViewSelected: (ScheduleView) -> Unit = {}
) {
    Column(modifier = modifier.fillMaxSize()) {
        ScheduleViewSelector(
            selectedView = uiState.selectedView,
            onViewSelected = onViewSelected,
            modifier = Modifier.fillMaxWidth()
        )

        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            uiState.error != null -> {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = uiState.error,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            uiState.gameRows.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No games found.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            else -> {
                Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    when (uiState.selectedView) {
                        ScheduleView.List -> {
                            ScheduleListView(
                                uiState = uiState,
                                onGameClick = onGameClick
                            )
                        }

                        ScheduleView.Calendar -> {
                            ScheduleCalendarView(
                                uiState = uiState,
                                onGameClick = onGameClick
                            )
                        }

                        ScheduleView.Series -> {
                            ScheduleSeriesView(
                                uiState = uiState,
                                onGameClick = onGameClick
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ScheduleListView(
    uiState: TeamScheduleUiState,
    onGameClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val sections = uiState.gameRows.toMonthlySections()

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)
    ) {
        sections.forEach { section ->
            item {
                Text(
                    text = section.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            section.games.forEachIndexed { index, game ->
                item(key = game.id) {
                    GameCard(
                        game = game,
                        onClick = { onGameClick(game.id) },
                        shape = ListItemDefaults.segmentedShapes(
                            index = index,
                            count = section.games.size
                        ).shape
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun ScheduleListItem(
    game: GameCardUiModel,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = if (isSelected) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = game.shortDate,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
                Text(
                    text = game.matchup,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (game.score.isNotBlank() && game.score != "—") {
                    Text(
                        text = game.score,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    if (game.resultText.isNotBlank()) {
                        Spacer(modifier = Modifier.width(8.dp))
                        val resultColor = when (game.outcome) {
                            GameOutcome.Win -> MaterialTheme.colorScheme.primary
                            GameOutcome.Loss -> MaterialTheme.colorScheme.error
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                        Text(
                            text = game.resultText,
                            style = MaterialTheme.typography.labelLarge,
                            color = resultColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Text(
                        text = "Scheduled",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun ScheduleCalendarView(
    uiState: TeamScheduleUiState,
    onGameClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("Calendar View Coming Soon")
    }
}

@Composable
private fun ScheduleSeriesView(
    uiState: TeamScheduleUiState,
    onGameClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val series = uiState.gameRows.toSeriesSections()

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        series.forEachIndexed { index, section ->
            item(key = "series_${section.title}_${section.games.firstOrNull()?.id ?: index}") {
                SeriesExpandableCard(
                    section = section,
                    selectedGameId = uiState.selectedGamePk,
                    onGameClick = onGameClick
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun SeriesExpandableCard(
    section: ScheduleSectionUiModel,
    selectedGameId: Int?,
    onGameClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by rememberSaveable {
        mutableStateOf(selectedGameId != null && section.games.any { it.id == selectedGameId })
    }

    LaunchedEffect(selectedGameId) {
        if (selectedGameId != null && section.games.any { it.id == selectedGameId }) {
            expanded = true
        }
    }

    val firstGame = section.games.first()
    val lastGame = section.games.last()
    val dateRange = if (section.games.size > 1) {
        "${firstGame.shortDate} - ${lastGame.shortDate}"
    } else {
        firstGame.shortDate
    }

    val wins = section.games.count { it.outcome == GameOutcome.Win }
    val losses = section.games.count { it.outcome == GameOutcome.Loss }
    val isCompleted = section.games.none { it.outcome == GameOutcome.Pending }

    val seriesText = when {
        wins > losses -> if (isCompleted) "Won $wins-$losses" else "Leading $wins-$losses"
        losses > wins -> if (isCompleted) "Lost $wins-$losses" else "Trailing $wins-$losses"
        wins == losses && (wins > 0 || isCompleted) -> if (isCompleted) "Split $wins-$wins" else "Tied $wins-$wins"
        else -> null
    }

    val resultColor = when {
        wins > losses -> MaterialTheme.colorScheme.primary
        losses > wins -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .animateContentSize(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = section.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = firstGame.ballpark,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = dateRange,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }

                if (seriesText != null) {
                    AssistChip(
                        onClick = { expanded = !expanded },
                        label = {
                            Text(
                                text = seriesText,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            labelColor = resultColor,
                        ),
                        border = AssistChipDefaults.assistChipBorder(
                            enabled = true,
                            borderColor = resultColor.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }

                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            if (expanded) {
                Column(
                    modifier = Modifier
                        .padding(bottom = 12.dp, start = 12.dp, end = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)
                ) {
                    section.games.forEachIndexed { index, game ->
                        SeriesGameListItem(
                            game = game,
                            isSelected = game.id == selectedGameId,
                            onClick = { onGameClick(game.id) },
                            shape = ListItemDefaults.segmentedShapes(
                                index = index,
                                count = section.games.size
                            ).shape
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SeriesGameListItem(
    game: GameCardUiModel,
    isSelected: Boolean,
    onClick: () -> Unit,
    shape: Shape,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val containerColor = when {
        isSelected -> MaterialTheme.colorScheme.primaryContainer
        isDark -> MaterialTheme.colorScheme.surfaceContainerHigh
        else -> MaterialTheme.colorScheme.surfaceContainerHigh
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = containerColor,
        shape = shape
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = game.shortDate,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                modifier = Modifier.weight(1f)
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (game.score.isNotBlank() && game.score != "—") {
                    Text(
                        text = game.score,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    if (game.resultText.isNotBlank()) {
                        Spacer(modifier = Modifier.width(8.dp))
                        val resultColor = when (game.outcome) {
                            GameOutcome.Win -> MaterialTheme.colorScheme.primary
                            GameOutcome.Loss -> MaterialTheme.colorScheme.error
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                        Text(
                            text = game.resultText,
                            style = MaterialTheme.typography.labelLarge,
                            color = resultColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Text(
                        text = "Scheduled",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ScheduleViewSelector(
    selectedView: ScheduleView,
    onViewSelected: (ScheduleView) -> Unit,
    modifier: Modifier = Modifier
) {
    ButtonGroup(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
        overflowIndicator = {}
    ) {
        ScheduleView.entries.forEach { view ->
            val isSelected = view == selectedView
            toggleableItem(
                checked = isSelected,
                onCheckedChange = { checked -> if (checked) onViewSelected(view) },
                label = view.name,
                weight = 1f
            )
        }
    }
}

private fun List<GameCardUiModel>.toMonthlySections(): List<ScheduleSectionUiModel> {
    return groupBy { game ->
        val parts = game.shortDate.split(" ")
        if (parts.size >= 1) parts[0] else "Unknown"
    }.map { entry ->
        ScheduleSectionUiModel(title = entry.key, games = entry.value)
    }
}

private fun List<GameCardUiModel>.toSeriesSections(): List<ScheduleSectionUiModel> {
    if (isEmpty()) return emptyList<ScheduleSectionUiModel>()

    val sections = mutableListOf<ScheduleSectionUiModel>()
    var currentSeriesGames = mutableListOf<GameCardUiModel>()

    forEach { game ->
        if (currentSeriesGames.isEmpty()) {
            currentSeriesGames.add(game)
        } else {
            val lastGame = currentSeriesGames.last()
            val isSameSeries = game.opponentAbbreviation == lastGame.opponentAbbreviation &&
                    game.isHome == lastGame.isHome

            if (isSameSeries) {
                currentSeriesGames.add(game)
            } else {
                sections.add(
                    ScheduleSectionUiModel(
                        title = createSeriesTitle(currentSeriesGames.first()),
                        games = currentSeriesGames.toList()
                    )
                )
                currentSeriesGames = mutableListOf(game)
            }
        }
    }

    if (currentSeriesGames.isNotEmpty()) {
        sections.add(
            ScheduleSectionUiModel(
                title = createSeriesTitle(currentSeriesGames.first()),
                games = currentSeriesGames.toList()
            )
        )
    }

    return sections
}

private fun createSeriesTitle(game: GameCardUiModel): String {
    return if (game.isHome) {
        "vs ${game.opponentAbbreviation}"
    } else {
        "at ${game.opponentAbbreviation}"
    }
}

@Preview(name = "List View", showBackground = true)
@Composable
private fun TeamScheduleListPreview() {
    DugoutTheme {
        TeamScheduleContent(
            uiState = TeamScheduleUiState(
                isLoading = false,
                selectedView = ScheduleView.List,
                gameRows = sampleGameRows,
                selectedGamePk = 2,
                error = null
            )
        )
    }
}

@Preview(name = "Calendar View", showBackground = true)
@Composable
private fun TeamScheduleCalendarPreview() {
    DugoutTheme {
        TeamScheduleContent(
            uiState = TeamScheduleUiState(
                isLoading = false,
                selectedView = ScheduleView.Calendar,
                gameRows = sampleGameRows,
                error = null
            )
        )
    }
}

@Preview(name = "Series View - Phone", showBackground = true)
@Preview(name = "Tablet", device = "spec:width=1280dp,height=800dp,dpi=240", showBackground = true)
@Preview(name = "Landscape", device = "spec:width=640dp,height=360dp,orientation=landscape,dpi=240", showBackground = true)
@Composable
private fun TeamScheduleSeriesPreview() {
    DugoutTheme {
        TeamScheduleContent(
            uiState = TeamScheduleUiState(
                isLoading = false,
                selectedView = ScheduleView.Series,
                gameRows = sampleGameRows,
                selectedGamePk = 2,
                error = null
            )
        )
    }
}

private val sampleGameRows = listOf(
    GameCardUiModel(
        id = 1,
        shortDate = "Apr 4",
        year = "2026",
        date = "2026-04-04",
        matchup = "Phillies at Braves",
        ballpark = "Truist Park",
        score = "4 - 3",
        resultText = "W",
        outcome = GameOutcome.Win,
        isSelected = false,
        opponentAbbreviation = "ATL",
        isHome = false
    ),
    GameCardUiModel(
        id = 2,
        shortDate = "Apr 5",
        year = "2026",
        date = "2026-04-05",
        matchup = "Phillies at Braves",
        ballpark = "Truist Park",
        score = "2 - 6",
        resultText = "L",
        outcome = GameOutcome.Loss,
        isSelected = true,
        opponentAbbreviation = "ATL",
        isHome = false
    ),
    GameCardUiModel(
        id = 3,
        shortDate = "Apr 6",
        year = "2026",
        date = "2026-04-06",
        matchup = "Phillies at Braves",
        ballpark = "Truist Park",
        score = "—",
        resultText = "",
        outcome = GameOutcome.Pending,
        isSelected = false,
        opponentAbbreviation = "ATL",
        isHome = false
    ),
    GameCardUiModel(
        id = 4,
        shortDate = "May 2",
        year = "2026",
        date = "2026-05-02",
        matchup = "Phillies at Mets",
        ballpark = "Citi Field",
        score = "—",
        resultText = "",
        outcome = GameOutcome.Pending,
        isSelected = false,
        opponentAbbreviation = "NYM",
        isHome = false
    )
)
