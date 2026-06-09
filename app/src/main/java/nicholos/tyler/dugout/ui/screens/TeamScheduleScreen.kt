package nicholos.tyler.dugout.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.svg.SvgDecoder
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import nicholos.tyler.dugout.model.domain.GameOutcome
import nicholos.tyler.dugout.model.domain.MlbTeams
import nicholos.tyler.dugout.model.ui.GameCardUiModel
import nicholos.tyler.dugout.ui.components.GameCard
import nicholos.tyler.dugout.ui.theme.DugoutTheme
import nicholos.tyler.dugout.viewmodel.ScheduleView
import nicholos.tyler.dugout.viewmodel.TeamScheduleUiState
import nicholos.tyler.dugout.viewmodel.TeamScheduleViewModel
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

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
        teamId = teamId,
        modifier = modifier,
        onGameClick = { gamePk ->
            viewModel.selectGame(gamePk)
            onGameClick(gamePk)
        },
        onViewSelected = { view ->
            viewModel.selectView(view)
        }
    )
}

@Composable
fun TeamScheduleContent(
    uiState: TeamScheduleUiState,
    teamId: Int,
    modifier: Modifier = Modifier,
    onGameClick: (Int) -> Unit = {},
    onViewSelected: (ScheduleView) -> Unit = {}
) {
    Column(modifier = modifier.fillMaxSize()) {
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
                ScheduleListTypeSelector(
                    selectedView = uiState.selectedView,
                    onViewSelected = onViewSelected
                )

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
                                teamId = teamId,
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
private fun ScheduleListTypeSelector(
    selectedView: ScheduleView,
    onViewSelected: (ScheduleView) -> Unit,
    modifier: Modifier = Modifier
) {
    if (selectedView == ScheduleView.Calendar) return

    ButtonGroup(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .height(48.dp),
        expandedRatio = 0f,
        horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
        overflowIndicator = {}
    ) {
        toggleableItem(
            checked = selectedView == ScheduleView.List,
            onCheckedChange = { if (it) onViewSelected(ScheduleView.List) },
            label = "Individual",
            weight = 1f
        )
        toggleableItem(
            checked = selectedView == ScheduleView.Series,
            onCheckedChange = { if (it) onViewSelected(ScheduleView.Series) },
            label = "Series",
            weight = 1f
        )
    }
}

@Composable
private fun ScheduleListView(
    uiState: TeamScheduleUiState,
    onGameClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val sections = uiState.gameRows.toMonthlySections()

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
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
                val shape = when {
                    section.games.size == 1 -> RoundedCornerShape(12.dp)
                    index == 0 -> RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp, bottomStart = 4.dp, bottomEnd = 4.dp)
                    index == section.games.size - 1 -> RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp, bottomStart = 12.dp, bottomEnd = 12.dp)
                    else -> RoundedCornerShape(4.dp)
                }
                item(key = game.id) {
                    GameCard(
                        game = game,
                        onClick = { onGameClick(game.id) },
                        shape = shape
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
private fun ScheduleCalendarView(
    uiState: TeamScheduleUiState,
    teamId: Int,
    onGameClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val today = LocalDate.now()
    val gamesByDate = remember(uiState.gameRows) {
        uiState.gameRows
            .mapNotNull { game -> game.localDate()?.let { date -> date to game } }
            .groupBy(keySelector = { it.first }, valueTransform = { it.second })
    }

    val initialDate = remember(uiState.gameRows, uiState.selectedGamePk) {
        uiState.gameRows
            .firstOrNull { it.id == uiState.selectedGamePk }
            ?.localDate()
            ?: gamesByDate[today]?.firstOrNull()?.localDate()
            ?: gamesByDate.keys.filter { !it.isBefore(today) }.minOrNull()
            ?: gamesByDate.keys.minOrNull()
            ?: today
    }

    val localDateSaver = Saver<LocalDate, String>(
        save = { it.toString() },
        restore = { LocalDate.parse(it) }
    )
    val yearMonthSaver = Saver<YearMonth, String>(
        save = { it.toString() },
        restore = { YearMonth.parse(it) }
    )

    var selectedDate by rememberSaveable(uiState.gameRows.size, stateSaver = localDateSaver) {
        mutableStateOf(initialDate)
    }
    var visibleMonth by rememberSaveable(uiState.gameRows.size, stateSaver = yearMonthSaver) {
        mutableStateOf(YearMonth.from(initialDate))
    }

    val scheduleColors = rememberScheduleColors()
    val selectedGames = gamesByDate[selectedDate].orEmpty()
    val selectedGame = selectedGames.firstOrNull()
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, top = 6.dp, end = 16.dp, bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            CalendarMonthHeader(
                visibleMonth = visibleMonth,
                subtitle = "12\u20137 \u00B7 2nd in NL East",
                onPreviousMonth = {
                    visibleMonth = visibleMonth.minusMonths(1)
                },
                onNextMonth = {
                    visibleMonth = visibleMonth.plusMonths(1)
                }
            )
        }

        item {
            WeekdayHeader()
        }

        item {
            ScheduleMonthGrid(
                visibleMonth = visibleMonth,
                selectedDate = selectedDate,
                today = today,
                gamesByDate = gamesByDate,
                scheduleColors = scheduleColors,
                onDateSelected = { date ->
                    selectedDate = date
                    visibleMonth = YearMonth.from(date)
                    gamesByDate[date]?.firstOrNull()?.let { onGameClick(it.id) }
                }
            )
        }

        item {
            CalendarLegend(scheduleColors = scheduleColors)
        }

        item {
            SelectedCalendarDayCard(
                selectedDate = selectedDate,
                today = today,
                teamId = teamId,
                game = selectedGame,
                scheduleColors = scheduleColors,
                onGameClick = onGameClick
            )
        }
    }
}

@Composable
private fun CalendarMonthHeader(
    visibleMonth: YearMonth,
    subtitle: String,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 2.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        MonthArrowButton(
            onClick = onPreviousMonth,
            direction = CalendarMonthDirection.Previous
        )

        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = visibleMonth.format(monthFormatter),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }

        MonthArrowButton(
            onClick = onNextMonth,
            direction = CalendarMonthDirection.Next
        )
    }
}

@Composable
private fun MonthArrowButton(
    onClick: () -> Unit,
    direction: CalendarMonthDirection,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
    ) {
        Icon(
            imageVector = when (direction) {
                CalendarMonthDirection.Previous -> Icons.Default.ChevronLeft
                CalendarMonthDirection.Next -> Icons.Default.ChevronRight
            },
            contentDescription = when (direction) {
                CalendarMonthDirection.Previous -> "Previous month"
                CalendarMonthDirection.Next -> "Next month"
            },
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
private fun WeekdayHeader(modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth()) {
        weekdayLabels.forEach { label ->
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.78f),
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ScheduleMonthGrid(
    visibleMonth: YearMonth,
    selectedDate: LocalDate,
    today: LocalDate,
    gamesByDate: Map<LocalDate, List<GameCardUiModel>>,
    scheduleColors: ScheduleColors,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val weeks = remember(visibleMonth) {
        calendarDatesFor(visibleMonth).chunked(7)
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.72f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.18f))
    ) {
        Column(modifier = Modifier.clip(RoundedCornerShape(20.dp))) {
            weeks.forEachIndexed { weekIndex, week ->
                Row(modifier = Modifier.fillMaxWidth()) {
                    week.forEachIndexed { dayIndex, date ->
                        CalendarGameDayCell(
                            date = date,
                            visibleMonth = visibleMonth,
                            selected = date == selectedDate,
                            isToday = date == today,
                            games = gamesByDate[date].orEmpty(),
                            scheduleColors = scheduleColors,
                            onClick = { onDateSelected(date) },
                            modifier = Modifier.weight(1f)
                        )

                        if (dayIndex < 6) {
                            VerticalDivider(
                                modifier = Modifier
                                    .height(CalendarDayCellHeight)
                                    .width(1.dp),
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.16f)
                            )
                        }
                    }
                }

                if (weekIndex < weeks.lastIndex) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.16f))
                }
            }
        }
    }
}

@Composable
private fun CalendarGameDayCell(
    date: LocalDate,
    visibleMonth: YearMonth,
    selected: Boolean,
    isToday: Boolean,
    games: List<GameCardUiModel>,
    scheduleColors: ScheduleColors,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val inVisibleMonth = YearMonth.from(date) == visibleMonth
    val primaryGame = games.firstOrNull()
    val dateColor = when {
        selected -> scheduleColors.todayOutline
        inVisibleMonth -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.78f)
        else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.36f)
    }

    Box(
        modifier = modifier
            .height(CalendarDayCellHeight)
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 5.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = date.dayOfMonth.toString(),
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.Medium,
                color = dateColor,
                textAlign = TextAlign.Center,
                maxLines = 1
            )

            if (isToday && !selected) {
                Box(
                    modifier = Modifier
                        .padding(top = 2.dp)
                        .size(4.dp)
                        .background(scheduleColors.todayOutline, CircleShape)
                )
            } else {
                Spacer(Modifier.height(6.dp))
            }
        }

        if (primaryGame != null) {
            CalendarGameChip(
                games = games,
                selected = selected,
                scheduleColors = scheduleColors,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
            )
        }
    }
}

@Composable
private fun CalendarGameChip(
    games: List<GameCardUiModel>,
    selected: Boolean,
    scheduleColors: ScheduleColors,
    modifier: Modifier = Modifier
) {
    val game = games.first()
    val isDoubleheader = games.size > 1
    val isCompleted = game.outcome != GameOutcome.Pending && game.score.isNotBlank() && game.score != "—"
    val containerColor = when {
        selected -> scheduleColors.todayContainer
        game.isHome -> scheduleColors.homeContainer
        else -> scheduleColors.awayContainer
    }
    val contentColor = when {
        selected -> scheduleColors.onTodayContainer
        game.isHome -> scheduleColors.onHomeContainer
        else -> scheduleColors.onAwayContainer
    }
    val secondaryContentColor = contentColor.copy(alpha = 0.78f)
    val lineOne = game.opponentAbbreviation
    val lineTwo = when {
        isDoubleheader -> "${games.size} Games"
        isCompleted -> game.completedResultLabel()
        else -> game.gameTimeOrScore().let { if (it == "Scheduled") "TBD" else it }
    }

    Surface(
        modifier = modifier.heightIn(min = 44.dp),
        shape = RoundedCornerShape(12.dp),
        color = containerColor,
        tonalElevation = if (selected) 2.dp else 0.dp,
        border = if (selected) BorderStroke(1.dp, scheduleColors.todayOutline.copy(alpha = 0.55f)) else null
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 3.dp, vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = lineOne,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 13.sp),
                fontWeight = FontWeight.ExtraBold,
                color = contentColor,
                maxLines = 1,
                textAlign = TextAlign.Center,
                overflow = TextOverflow.Clip
            )

            Text(
                text = lineTwo,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                fontWeight = FontWeight.Medium,
                color = secondaryContentColor,
                maxLines = 1,
                textAlign = TextAlign.Center,
                overflow = TextOverflow.Clip
            )
        }
    }
}

@Composable
private fun CalendarLegend(
    scheduleColors: ScheduleColors,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        LegendItem(label = "Home", containerColor = scheduleColors.homeContainer)
        LegendItem(label = "Away", containerColor = scheduleColors.awayContainer)
        LegendItem(
            label = "Today",
            containerColor = Color.Transparent,
            indicatorColor = scheduleColors.todayOutline,
            isToday = true
        )
        LegendItem(
            label = "Selected",
            containerColor = scheduleColors.todayContainer,
            borderColor = scheduleColors.todayOutline
        )
    }
}

@Composable
private fun LegendItem(
    label: String,
    containerColor: Color,
    borderColor: Color? = null,
    indicatorColor: Color? = null,
    isToday: Boolean = false,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        if (isToday) {
            Box(
                modifier = Modifier
                    .size(10.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(indicatorColor ?: Color.Transparent, CircleShape)
                )
            }
        } else {
            Surface(
                modifier = Modifier.size(width = 18.dp, height = 11.dp),
                shape = RoundedCornerShape(3.dp),
                color = containerColor,
                border = borderColor?.let { BorderStroke(1.dp, it.copy(alpha = 0.55f)) },
                tonalElevation = if (borderColor != null) 2.dp else 0.dp
            ) {}
        }

        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1
        )
    }
}

@Composable
private fun SelectedCalendarDayCard(
    selectedDate: LocalDate,
    today: LocalDate,
    teamId: Int,
    game: GameCardUiModel?,
    scheduleColors: ScheduleColors,
    onGameClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        if (game == null) {
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "No game scheduled",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(22.dp)
                )
            }
        } else {
            Surface(
                onClick = { onGameClick(game.id) },
                shape = RoundedCornerShape(22.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                tonalElevation = 2.dp,
                shadowElevation = 1.dp,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.18f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1.05f)) {
                        Surface(
                            shape = RoundedCornerShape(7.dp),
                            color = scheduleColors.todayContainer,
                        ) {
                            Text(
                                text = if (selectedDate == today) "TODAY" else selectedDate.format(shortDayFormatter).uppercase(Locale.US),
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                fontWeight = FontWeight.ExtraBold,
                                color = scheduleColors.onTodayContainer,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                                maxLines = 1
                            )
                        }

                        Spacer(Modifier.height(4.dp))

                        Text(
                            text = game.gameTimeOrScore(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1
                        )

                        Text(
                            text = game.ballpark,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Row(
                        modifier = Modifier.weight(2.15f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        TeamLogoMark(
                            teamId = teamId,
                            teamName = MlbTeams.get(teamId).shortName,
                            modifier = Modifier.size(32.dp)
                        )

                        Spacer(Modifier.width(6.dp))

                        Text(
                            text = MlbTeams.get(teamId).abbreviation,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1
                        )

                        Text(
                            text = "\u2022",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )

                        Text(
                            text = game.opponentAbbreviation,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1
                        )

                        Spacer(Modifier.width(6.dp))

                        TeamLogoMark(
                            teamId = teamIdForAbbreviation(game.opponentAbbreviation),
                            teamName = game.opponentAbbreviation,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Open game",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun TeamLogoMark(
    teamId: Int,
    teamName: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Box(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surfaceContainerHighest, CircleShape)
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        if (teamId > 0) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data("https://www.mlbstatic.com/team-logos/$teamId.svg")
                    .decoderFactory(SvgDecoder.Factory())
                    .crossfade(true)
                    .build(),
                contentDescription = "$teamName logo",
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Text(
                text = teamName.take(3),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )
        }
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
        contentPadding = PaddingValues(start = 0.dp, top = 8.dp, end = 0.dp, bottom = 8.dp)
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
        wins > 0 || isCompleted -> if (isCompleted) "Split $wins-$wins" else "Tied $wins-$wins"
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
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    section.games.forEachIndexed { index, game ->
                        val shape = when {
                            section.games.size == 1 -> RoundedCornerShape(12.dp)
                            index == 0 -> RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp, bottomStart = 4.dp, bottomEnd = 4.dp)
                            index == section.games.size - 1 -> RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp, bottomStart = 12.dp, bottomEnd = 12.dp)
                            else -> RoundedCornerShape(4.dp)
                        }
                        SeriesGameListItem(
                            game = game,
                            isSelected = game.id == selectedGameId,
                            onClick = { onGameClick(game.id) },
                            shape = shape
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

private enum class CalendarMonthDirection {
    Previous,
    Next
}

private fun calendarDatesFor(month: YearMonth): List<LocalDate> {
    val firstDay = month.atDay(1)
    val leadingDays = firstDay.dayOfWeek.value % 7
    val firstVisibleDate = firstDay.minusDays(leadingDays.toLong())
    val lastDay = month.atEndOfMonth()
    val trailingDays = 6 - (lastDay.dayOfWeek.value % 7)
    val lastVisibleDate = lastDay.plusDays(trailingDays.toLong())
    val totalDays = ChronoUnit.DAYS.between(firstVisibleDate, lastVisibleDate).toInt() + 1

    return List(totalDays) { offset -> firstVisibleDate.plusDays(offset.toLong()) }
}

private fun GameCardUiModel.localDate(): LocalDate? {
    return runCatching {
        OffsetDateTime.parse(date).atZoneSameInstant(ZoneId.systemDefault()).toLocalDate()
    }.getOrNull() ?: runCatching {
        LocalDate.parse(date)
    }.getOrNull()
}

private fun GameCardUiModel.gameTimeOrScore(): String {
    // Fixed encoding issue where em-dash was represented as "â€”"
    if (score.isNotBlank() && score != "—") return score

    return runCatching {
        OffsetDateTime.parse(date)
            .atZoneSameInstant(ZoneId.systemDefault())
            .format(timeFormatter)
    }.getOrElse {
        // Fallback for dates without time/offset (e.g., "2026-04-04")
        "Scheduled"
    }
}

private fun String.toLocalDateOr(fallback: LocalDate): LocalDate {
    return runCatching { LocalDate.parse(this) }.getOrDefault(fallback)
}

private fun GameCardUiModel.completedResultLabel(): String {
    val prefix = when (resultText.lowercase(Locale.US)) {
        "win", "w" -> "W"
        "loss", "l" -> "L"
        else -> resultText.take(1).uppercase(Locale.US)
    }
    val normalizedScore = score.replace(" ", "")
    return listOf(prefix, normalizedScore)
        .filter { it.isNotBlank() && it != "\u2014" && it != "-" }
        .joinToString(" ")
}

private fun String.toYearMonthOr(fallback: YearMonth): YearMonth {
    return runCatching { YearMonth.parse(this) }.getOrDefault(fallback)
}

private fun LocalDate.daySectionTitle(today: LocalDate): String {
    val prefix = when (this) {
        today -> "Today"
        today.plusDays(1) -> "Tomorrow"
        today.minusDays(1) -> "Yesterday"
        else -> null
    }
    val dateLabel = format(dayFormatter)
    return if (prefix == null) dateLabel else "$prefix \u00B7 $dateLabel"
}

private fun teamIdForAbbreviation(abbreviation: String): Int {
    return MlbTeams.byId.values.firstOrNull {
        it.abbreviation.equals(abbreviation, ignoreCase = true)
    }?.id ?: 0
}

private val weekdayLabels = listOf("SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT")
private val monthFormatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.US)
private val dayFormatter = DateTimeFormatter.ofPattern("EEE, MMM d", Locale.US)
private val shortDayFormatter = DateTimeFormatter.ofPattern("MMM d", Locale.US)
private val timeFormatter = DateTimeFormatter.ofPattern("h:mm a", Locale.US)
private val CalendarDayCellHeight = 86.dp

private data class ScheduleColors(
    val accent: Color,
    val homeContainer: Color,
    val onHomeContainer: Color,
    val awayContainer: Color,
    val onAwayContainer: Color,
    val todayContainer: Color,
    val onTodayContainer: Color,
    val todayOutline: Color,
    val springTrainingContainer: Color,
    val springTrainingOutline: Color
)

@Composable
private fun rememberScheduleColors(): ScheduleColors {
    val colorScheme = MaterialTheme.colorScheme

    return ScheduleColors(
        // Primary = team/app accent for branding moments.
        accent = colorScheme.primary,

        // Secondary container = home games.
        homeContainer = colorScheme.secondaryContainer,
        onHomeContainer = colorScheme.onSecondaryContainer,

        // Neutral surface = away games.
        awayContainer = colorScheme.surfaceContainerHighest,
        onAwayContainer = colorScheme.onSurface,

        // Tertiary = today / selected game state.
        todayContainer = colorScheme.tertiaryContainer,
        onTodayContainer = colorScheme.onTertiaryContainer,
        todayOutline = colorScheme.tertiary,

        // Spring training = outlined accent style.
        springTrainingContainer = colorScheme.primary.copy(alpha = 0.12f),
        springTrainingOutline = colorScheme.primary
    )
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
            ),
            teamId = 143,
            onViewSelected = {}
        )
    }
}

@Preview(name = "Calendar View", showBackground = true, device = "id:pixel_5")
@Composable
private fun TeamScheduleCalendarPreview() {
    DugoutTheme {
        TeamScheduleContent(
            uiState = TeamScheduleUiState(
                isLoading = false,
                selectedView = ScheduleView.Calendar,
                gameRows = sampleGameRows,
                error = null
            ),
            teamId = 143,
            onViewSelected = {}
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
            ),
            teamId = 143,
            onViewSelected = {}
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
