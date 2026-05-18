package nicholos.tyler.dugout.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.ListItemShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import nicholos.tyler.dugout.model.domain.MlbTeams
import nicholos.tyler.dugout.model.ui.GameSnapshotCardUiModel
import nicholos.tyler.dugout.model.ui.ScoresUiState
import nicholos.tyler.dugout.model.ui.TeamScoreUiModel
import nicholos.tyler.dugout.ui.components.DiamondRunners
import nicholos.tyler.dugout.ui.components.GameSnapshotCard
import nicholos.tyler.dugout.ui.theme.DugoutTheme
import nicholos.tyler.dugout.viewmodel.ScoresViewModel

@Composable
fun ScoresScreen(
    viewModel: ScoresViewModel,
    onGameClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ScoresScreenContent(
        uiState = uiState,
        onDateSelected = viewModel::onDateSelected,
        onGameClick = onGameClick,
        modifier = modifier
    )
}

@Composable
private fun ScoresScreenContent(
    uiState: ScoresUiState,
    onDateSelected: (LocalDate) -> Unit,
    onGameClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        DateSelector(
            selectedDate = uiState.selectedDate,
            onDateSelected = onDateSelected,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        when {
            uiState.isLoading -> LoadingState(Modifier.weight(1f))
            uiState.error != null -> ErrorState(message = uiState.error, modifier = Modifier.weight(1f))
            uiState.games.isEmpty() -> EmptyState(Modifier.weight(1f))
            else -> ScoresList(
                games = uiState.games,
                onGameClick = onGameClick,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun DateSelector(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    val daysPerPage = remember(configuration.screenWidthDp) {
        when {
            configuration.screenWidthDp < 380 -> 3
            configuration.screenWidthDp < 560 -> 4
            configuration.screenWidthDp < 720 -> 5
            else -> 7
        }
    }

    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = selectedDate
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
    )

    val today = remember { LocalDate.now() }
    val anchorDate = remember { today.minusDays(2) }
    val initialPage = 500
    val pagerState = rememberPagerState(
        initialPage = initialPage,
        pageCount = { 1000 }
    )

    LaunchedEffect(selectedDate, daysPerPage) {
        val dayOffset = selectedDate.toEpochDay() - anchorDate.toEpochDay()
        val targetPage = Math.floorDiv(dayOffset, daysPerPage).toInt() + initialPage
        if (pagerState.currentPage != targetPage) {
            pagerState.animateScrollToPage(targetPage)
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            val date = Instant
                                .ofEpochMilli(it)
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate()
                            onDateSelected(date)
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f),
            pageSpacing = 16.dp,
            verticalAlignment = Alignment.CenterVertically
        ) { page ->
            val pageAnchor = anchorDate.plusDays(((page - initialPage) * daysPerPage).toLong())
            val dates = (0 until daysPerPage).map { pageAnchor.plusDays(it.toLong()) }

            DateButtonGroup(
                dates = dates,
                selectedDate = selectedDate,
                onDateSelected = onDateSelected
            )
        }

        Surface(
            onClick = { showDatePicker = true },
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceContainer,
            modifier = Modifier.size(56.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = "Select Date",
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

private val monthDayFormatter = DateTimeFormatter.ofPattern("MMM d", Locale.ENGLISH)
private val dayOfWeekFormatter = DateTimeFormatter.ofPattern("EEE", Locale.ENGLISH)

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun DateButtonGroup(
    dates: List<LocalDate>,
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    ButtonGroup(
        modifier = modifier.fillMaxWidth(),
        expandedRatio = 0f,
        horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
        overflowIndicator = {}
    ) {
        dates.forEachIndexed { index, date ->
            val isSelected = date == selectedDate
            
            customItem(
                buttonGroupContent = {
                    val interactionSource = remember { MutableInteractionSource() }
                    ToggleButton(
                        checked = isSelected,
                        onCheckedChange = {
                            if (!isSelected) onDateSelected(date)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .animateWidth(interactionSource)
                            .semantics { role = Role.RadioButton },
                        interactionSource = interactionSource,
                        shapes = when (index) {
                            0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                            dates.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                            else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                        },
                        colors = ToggleButtonDefaults.toggleButtonColors(
                            checkedContainerColor = MaterialTheme.colorScheme.primary,
                            checkedContentColor = MaterialTheme.colorScheme.onPrimary,
                            containerColor = MaterialTheme.colorScheme.surfaceContainer,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = date.format(monthDayFormatter),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1
                            )
                            Text(
                                text = date.format(dayOfWeekFormatter),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                        }
                    }
                },
                menuContent = { }
            )
        }
    }
}

@Composable
private fun ScoresList(
    games: List<GameSnapshotCardUiModel>,
    onGameClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val liveGames = games.filter { it.isLiveGame() }
    val finalGames = games.filter { it.isFinalGame() }
    val upcomingGames = games.filter { it.isUpcomingGame() }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        if (liveGames.isNotEmpty()) {
            item {
                SectionHeader(
                    title = "Live",
                    count = liveGames.size,
                    modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                )
            }

            item(key = "hero_${liveGames.first().gameId}_${liveGames.first().shortDate}") {
                GameSnapshotCard(
                    model = liveGames.first(),
                    onClick = onGameClick,
                    modifier = Modifier.padding(bottom = 14.dp)
                )
            }

            if (liveGames.size > 1) {
                item {
                    LiveGamesGroup(
                        games = liveGames.drop(1),
                        onGameClick = onGameClick
                    )
                }
            }
        }

        if (finalGames.isNotEmpty()) {
            item {
                SectionHeader(
                    title = "Final",
                    count = finalGames.size,
                    modifier = Modifier.padding(
                        top = if (liveGames.isEmpty()) 4.dp else 18.dp,
                        bottom = 12.dp
                    )
                )
            }

            item {
                ScoreGamesGroup(
                    games = finalGames,
                    type = ScoreRowType.Final,
                    onGameClick = onGameClick
                )
            }
        }

        if (upcomingGames.isNotEmpty()) {
            item {
                SectionHeader(
                    title = "Upcoming",
                    count = upcomingGames.size,
                    modifier = Modifier.padding(top = 18.dp, bottom = 12.dp)
                )
            }

            item {
                ScoreGamesGroup(
                    games = upcomingGames,
                    type = ScoreRowType.Upcoming,
                    onGameClick = onGameClick
                )
            }
        }
    }
}

@Composable
fun SectionHeader(
    title: String,
    count: Int = 0,
    modifier: Modifier = Modifier,
    action: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (title.equals("Live", ignoreCase = true)) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }

        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        if (count > 0) {
            Spacer(modifier = Modifier.width(8.dp))

            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceContainerHighest
            ) {
                Text(
                    text = count.toString(),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (action != null) {
            Spacer(modifier = Modifier.weight(1f))
            action()
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun LiveGamesGroup(
    games: List<GameSnapshotCardUiModel>,
    onGameClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)
    ) {
        games.forEachIndexed { index, game ->
            LiveGameRow(
                model = game,
                onClick = onGameClick,
                shape = ListItemDefaults.segmentedShapes(
                    index = index,
                    count = games.size
                )
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun LiveGameRow(
    model: GameSnapshotCardUiModel,
    onClick: (Int) -> Unit,
    shape: ListItemShapes,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = { onClick(model.gameId) },
        modifier = modifier.fillMaxWidth(),
        shape = shape.shape,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Team and Score
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = model.leftTeam.name.shortName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = model.leftTeam.record,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = model.leftTeam.score.ifBlank { "0" },
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            // Center: Diamond and Inning
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(100.dp)
            ) {
                DiamondRunners(
                    onFirst = model.onFirst,
                    onSecond = model.onSecond,
                    onThird = model.onThird,
                    modifier = Modifier.size(32.dp),
                    baseSize = 11.dp,
                    unoccupiedColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.12f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = model.inningText.ifBlank { model.status },
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Right Score and Team
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = model.rightTeam.score.ifBlank { "0" },
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = model.rightTeam.name.shortName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.End,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = model.rightTeam.record,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.End
                    )
                }
            }
        }
    }
}

private enum class ScoreRowType {
    Final,
    Upcoming
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ScoreGamesGroup(
    games: List<GameSnapshotCardUiModel>,
    type: ScoreRowType,
    onGameClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)
    ) {
        games.forEachIndexed { index, game ->
            ScoreGameRow(
                model = game,
                type = type,
                onClick = onGameClick,
                shape = ListItemDefaults.segmentedShapes(
                    index = index,
                    count = games.size
                )
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ScoreGameRow(
    model: GameSnapshotCardUiModel,
    type: ScoreRowType,
    onClick: (Int) -> Unit,
    shape: ListItemShapes,
    modifier: Modifier = Modifier
) {
    val leftScore = model.leftTeam.score.toIntOrNull()
    val rightScore = model.rightTeam.score.toIntOrNull()
    val leftWinner = type == ScoreRowType.Final &&
            leftScore != null &&
            rightScore != null &&
            leftScore > rightScore
    val rightWinner = type == ScoreRowType.Final &&
            leftScore != null &&
            rightScore != null &&
            rightScore > leftScore

    Surface(
        onClick = { onClick(model.gameId) },
        modifier = modifier.fillMaxWidth(),
        shape = shape.shape,
        color = MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 14.dp,
                    vertical = if (type == ScoreRowType.Upcoming) 14.dp else 12.dp
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TeamInfo(
                team = model.leftTeam,
                emphasized = leftWinner,
                modifier = Modifier.weight(1f)
            )

            ScoreCenterContent(
                model = model,
                type = type,
                leftWinner = leftWinner,
                rightWinner = rightWinner,
                modifier = Modifier.width(
                    if (type == ScoreRowType.Final) 96.dp else 84.dp
                )
            )

            TeamInfo(
                team = model.rightTeam,
                emphasized = rightWinner,
                alignment = Alignment.End,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ScoreCenterContent(
    model: GameSnapshotCardUiModel,
    type: ScoreRowType,
    leftWinner: Boolean,
    rightWinner: Boolean,
    modifier: Modifier = Modifier
) {
    when (type) {
        ScoreRowType.Final -> FinalScoreContent(
            leftScore = model.leftTeam.score,
            rightScore = model.rightTeam.score,
            leftWinner = leftWinner,
            rightWinner = rightWinner,
            modifier = modifier
        )

        ScoreRowType.Upcoming -> UpcomingGameTimeContent(
            model = model,
            modifier = modifier
        )
    }
}

@Composable
private fun FinalScoreContent(
    leftScore: String,
    rightScore: String,
    leftWinner: Boolean,
    rightWinner: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        ScoreText(
            score = leftScore,
            isWinner = leftWinner
        )

        Text(
            text = "-",
            modifier = Modifier.width(24.dp),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        ScoreText(
            score = rightScore,
            isWinner = rightWinner
        )
    }
}

@Composable
private fun UpcomingGameTimeContent(
    model: GameSnapshotCardUiModel,
    modifier: Modifier = Modifier
) {
    Text(
        text = when {
            model.inningText.isNotBlank() -> model.inningText
            model.status.isNotBlank() -> model.status.uppercase()
            else -> "TBD"
        },
        modifier = modifier,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun ScoreText(
    score: String,
    isWinner: Boolean,
    modifier: Modifier = Modifier
) {
    Text(
        text = score.ifBlank { "-" },
        modifier = modifier.width(36.dp),
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
        color = if (isWinner) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.onSurface
        }
    )
}

@Composable
private fun TeamInfo(
    team: TeamScoreUiModel,
    emphasized: Boolean,
    modifier: Modifier = Modifier,
    showBadge: Boolean = false,
    isAwayBadge: Boolean = true,
    badgeSize: androidx.compose.ui.unit.Dp = 40.dp,
    alignment: Alignment.Horizontal = Alignment.Start
) {
    Column(
        modifier = modifier,
        horizontalAlignment = alignment
    ) {
        if (showBadge) {
            TeamBadge(
                abbreviation = team.name.abbreviation,
                isAway = isAwayBadge,
                size = badgeSize
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        Text(
            text = team.name.shortName,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = if (emphasized) FontWeight.Bold else FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = when (alignment) {
                Alignment.CenterHorizontally -> TextAlign.Center
                Alignment.End -> TextAlign.End
                else -> TextAlign.Start
            },
            color = MaterialTheme.colorScheme.onSurface
        )

        if (team.record.isNotBlank()) {
            Text(
                text = team.record,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = when (alignment) {
                    Alignment.CenterHorizontally -> TextAlign.Center
                    Alignment.End -> TextAlign.End
                    else -> TextAlign.Start
                }
            )
        }
    }
}

@Composable
private fun TeamBadge(
    abbreviation: String,
    isAway: Boolean,
    size: androidx.compose.ui.unit.Dp
) {
    Surface(
        modifier = Modifier.size(size),
        shape = CircleShape,
        color = if (isAway) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.surfaceContainerHighest
        }
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = abbreviation.ifBlank { "?" }.take(3),
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                fontWeight = FontWeight.ExtraBold,
                color = if (isAway) {
                    MaterialTheme.colorScheme.onPrimary
                } else {
                    MaterialTheme.colorScheme.primary
                },
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun LoadingState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorState(
    message: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(24.dp)
        )
    }
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "No games for this date",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ScoresScreenPreview() {
    DugoutTheme {
        ScoresScreenContent(
            uiState = ScoresUiState(
                isLoading = false,
                games = listOf(
                    GameSnapshotCardUiModel(
                        gameId = 1,
                        leftTeam = TeamScoreUiModel(MlbTeams.get(133), "3", "52-68"),
                        rightTeam = TeamScoreUiModel(MlbTeams.get(143), "2", "69-50"),
                        status = "In Progress",
                        startTime = "2026-05-07T19:10:00Z",
                        inningText = "Bottom 7th",
                        isTopInning = false,
                        countText = "1-2",
                        outsText = "2 Outs",
                        onFirst = true,
                        onSecond = false,
                        onThird = false,
                        shortDate = "2026-05-07",
                        linescore = null
                    ),
                    GameSnapshotCardUiModel(
                        gameId = 2,
                        leftTeam = TeamScoreUiModel(MlbTeams.get(146), "2", "15-21"),
                        rightTeam = TeamScoreUiModel(MlbTeams.get(110), "3", "19-16"),
                        status = "In Progress",
                        startTime = "",
                        inningText = "Bot 4th",
                        isTopInning = false,
                        countText = "",
                        outsText = "",
                        onFirst = false,
                        onSecond = false,
                        onThird = false,
                        shortDate = "2026-05-07",
                        linescore = null
                    ),
                    GameSnapshotCardUiModel(
                        gameId = 3,
                        leftTeam = TeamScoreUiModel(MlbTeams.get(140), "2", "16-21"),
                        rightTeam = TeamScoreUiModel(MlbTeams.get(147), "9", "26-12"),
                        status = "Final",
                        startTime = "",
                        inningText = "",
                        isTopInning = null,
                        countText = "",
                        outsText = "",
                        onFirst = false,
                        onSecond = false,
                        onThird = false,
                        shortDate = "2026-05-07",
                        linescore = null
                    ),
                    GameSnapshotCardUiModel(
                        gameId = 4,
                        leftTeam = TeamScoreUiModel(MlbTeams.get(142), "5", "18-19"),
                        rightTeam = TeamScoreUiModel(MlbTeams.get(120), "7", "18-20"),
                        status = "Final",
                        startTime = "",
                        inningText = "",
                        isTopInning = null,
                        countText = "",
                        outsText = "",
                        onFirst = false,
                        onSecond = false,
                        onThird = false,
                        shortDate = "2026-05-07",
                        linescore = null
                    ),
                    GameSnapshotCardUiModel(
                        gameId = 5,
                        leftTeam = TeamScoreUiModel(MlbTeams.get(114), "8", "20-19"),
                        rightTeam = TeamScoreUiModel(MlbTeams.get(118), "5", "15-23"),
                        status = "Final",
                        startTime = "",
                        inningText = "",
                        isTopInning = null,
                        countText = "",
                        outsText = "",
                        onFirst = false,
                        onSecond = false,
                        onThird = false,
                        shortDate = "2026-05-07",
                        linescore = null
                    ),
                    GameSnapshotCardUiModel(
                        gameId = 6,
                        leftTeam = TeamScoreUiModel(MlbTeams.get(139), "", "24-12"),
                        rightTeam = TeamScoreUiModel(MlbTeams.get(111), "", "16-21"),
                        status = "7:10 PM",
                        startTime = "",
                        inningText = "",
                        isTopInning = null,
                        countText = "",
                        outsText = "",
                        onFirst = false,
                        onSecond = false,
                        onThird = false,
                        shortDate = "2026-05-07",
                        linescore = null
                    ),
                    GameSnapshotCardUiModel(
                        gameId = 7,
                        leftTeam = TeamScoreUiModel(MlbTeams.get(139), "", "24-12"),
                        rightTeam = TeamScoreUiModel(MlbTeams.get(111), "", "16-21"),
                        status = "7:10 PM",
                        startTime = "",
                        inningText = "",
                        isTopInning = null,
                        countText = "",
                        outsText = "",
                        onFirst = false,
                        onSecond = false,
                        onThird = false,
                        shortDate = "2026-05-07",
                        linescore = null
                    ),
                    GameSnapshotCardUiModel(
                        gameId = 8,
                        leftTeam = TeamScoreUiModel(MlbTeams.get(139), "", "24-12"),
                        rightTeam = TeamScoreUiModel(MlbTeams.get(111), "", "16-21"),
                        status = "7:10 PM",
                        startTime = "",
                        inningText = "",
                        isTopInning = null,
                        countText = "",
                        outsText = "",
                        onFirst = false,
                        onSecond = false,
                        onThird = false,
                        shortDate = "2026-05-07",
                        linescore = null
                    ),

                    )
            ),
            onDateSelected = {},
            onGameClick = {}
        )
    }
}
