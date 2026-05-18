package nicholos.tyler.dugout.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import nicholos.tyler.dugout.model.domain.GameDetails
import nicholos.tyler.dugout.model.domain.MlbTeams
import nicholos.tyler.dugout.model.domain.PlayItem
import nicholos.tyler.dugout.model.domain.PitchItem
import nicholos.tyler.dugout.model.ui.GameSnapshotCardUiModel
import nicholos.tyler.dugout.model.ui.InningScoreUiModel
import nicholos.tyler.dugout.model.ui.LinescoreUiModel
import nicholos.tyler.dugout.model.ui.TeamTotalUiModel
import nicholos.tyler.dugout.model.ui.TeamScoreUiModel
import nicholos.tyler.dugout.ui.components.GameSnapshotCard
import nicholos.tyler.dugout.ui.theme.DugoutTheme
import nicholos.tyler.dugout.viewmodel.GameDetailUiState
import nicholos.tyler.dugout.viewmodel.GameDetailViewModel
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.FilterChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import nicholos.tyler.dugout.model.domain.Boxscore
import nicholos.tyler.dugout.model.domain.BoxscorePlayer
import nicholos.tyler.dugout.model.domain.BoxscoreTeam
import androidx.compose.foundation.Canvas
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlinx.coroutines.launch
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

@Composable
fun GameDetailScreen(
    viewModel: GameDetailViewModel,
    gamePk: Int,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(gamePk) {
        viewModel.loadGame(gamePk)
    }

    GameDetailContent(
        uiState = uiState,
        modifier = modifier.fillMaxSize()
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameDetailContent(
    uiState: GameDetailUiState,
    modifier: Modifier = Modifier,
) {
    when {
        uiState.isLoading && uiState.gameDetails == null -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        uiState.error != null -> {
            Box(
                modifier = modifier
                    .fillMaxSize()
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

        uiState.gameDetails == null -> {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No game details available.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        else -> {
            val game = uiState.gameDetails
            var selectedTab by rememberSaveable { mutableStateOf(GameDetailTab.PLAYS) }
            var selectedPlayFilter by rememberSaveable { mutableStateOf(PlayFilter.ALL) }
            var selectedBoxTeam by rememberSaveable { mutableStateOf(BoxTeamFilter.AWAY) }

            var selectedPlayForSheet by remember { mutableStateOf<PlayItem?>(null) }
            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            val scope = rememberCoroutineScope()

            if (selectedPlayForSheet != null) {
                PlayDetailBottomSheet(
                    play = selectedPlayForSheet!!,
                    sheetState = sheetState,
                    onDismissRequest = {
                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                            selectedPlayForSheet = null
                        }
                    }
                )
            }

            LazyColumn(
                modifier = modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    GameSnapshotCard(
                        model = game.toSnapshotUiModel()
                    )
                }

                item {
                    MainTabToggle(
                        selectedTab = selectedTab,
                        onTabSelected = { selectedTab = it }
                    )
                }

                when (selectedTab) {
                    GameDetailTab.PLAYS -> {
                        playsSection(
                            plays = game.plays,
                            selectedFilter = selectedPlayFilter,
                            onFilterSelected = { selectedPlayFilter = it },
                            onPlayClicked = { selectedPlayForSheet = it }
                        )
                    }
                    GameDetailTab.BOX -> {
                        boxscoreSection(
                            boxscore = uiState.boxscore,
                            awayTeamId = game.awayTeamId,
                            awayTeamName = game.awayTeam,
                            homeTeamId = game.homeTeamId,
                            homeTeamName = game.homeTeam,
                            selectedTeam = selectedBoxTeam,
                            onTeamSelected = { selectedBoxTeam = it }
                        )
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3ExpressiveApi::class)
fun LazyListScope.playsSection(
    plays: List<PlayItem>,
    selectedFilter: PlayFilter,
    onFilterSelected: (PlayFilter) -> Unit,
    onPlayClicked: (PlayItem) -> Unit
) {
    item {
        PlayFilterChips(
            selectedFilter = selectedFilter,
            onFilterSelected = onFilterSelected,
            modifier = Modifier.padding(bottom = 8.dp)
        )
    }

    val filteredPlays = when (selectedFilter) {
        PlayFilter.ALL -> plays
        PlayFilter.SCORING -> plays.filter { it.isLikelyScoringPlay() }
    }

    if (filteredPlays.isEmpty()) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            ) {
                Text(
                    text = if (selectedFilter == PlayFilter.SCORING) {
                        "No scoring plays available yet."
                    } else {
                        "No plays available yet."
                    },
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    } else {
        val playGroups = groupPlaysByInningHalf(filteredPlays)

        playGroups.forEach { group ->
            item(key = "${group.inning}-${group.isTopInning}") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Text(
                        text = group.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)
                    ) {
                        group.plays.forEachIndexed { index, play ->
                            PlayRow(
                                play = play,
                                shape = ListItemDefaults.segmentedShapes(
                                    index = index,
                                    count = group.plays.size
                                ).shape,
                                onClick = { onPlayClicked(play) }
                            )
                        }
                    }
                }
            }
        }
    }
}

fun LazyListScope.boxscoreSection(
    boxscore: Boxscore?,
    awayTeamId: Int,
    awayTeamName: String?,
    homeTeamId: Int,
    homeTeamName: String?,
    selectedTeam: BoxTeamFilter,
    onTeamSelected: (BoxTeamFilter) -> Unit
) {
    if (boxscore == null) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    } else {
        item {
            BoxTeamToggle(
                awayTeamName = MlbTeams.get(awayTeamId, awayTeamName).shortName,
                homeTeamName = MlbTeams.get(homeTeamId, homeTeamName).shortName,
                selectedTeam = selectedTeam,
                onTeamSelected = onTeamSelected
            )
        }
        item {
            BoxscoreContent(boxscore, selectedTeam)
        }
    }
}

fun GameDetails.toSnapshotUiModel(): GameSnapshotCardUiModel {
    val isFinal = status.equals("Final", ignoreCase = true)

    return GameSnapshotCardUiModel(
        gameId = gamePk,
        leftTeam = TeamScoreUiModel(
            name = MlbTeams.get(teamId = awayTeamId, fallbackName = awayTeam),
            score = awayScore.toString(),
            probablePitcher = awayProbablePitcher
        ),
        rightTeam = TeamScoreUiModel(
            name = MlbTeams.get(teamId = homeTeamId, fallbackName = homeTeam),
            score = homeScore.toString(),
            probablePitcher = homeProbablePitcher
        ),
        status = status.orEmpty(),
        startTime = try {
            startDateTime?.let {
                val parsed = java.time.OffsetDateTime.parse(it)
                parsed.atZoneSameInstant(java.time.ZoneId.systemDefault())
                    .format(java.time.format.DateTimeFormatter.ofPattern("h:mm a", java.util.Locale.US))
            } ?: ""
        } catch (_: Exception) { "" },
        countText = if (isFinal) "" else countText(),
        outsText = if (isFinal) "" else outsText(),
        inningText = if (isFinal) "" else inningDisplay,
        isTopInning = if (isFinal) {
            null
        } else {
            when (inningState?.lowercase()) {
                "top" -> true
                "bottom", "bot" -> false
                else -> null
            }
        },
        shortDate = try {
            startDateTime?.let {
                val parsed = java.time.OffsetDateTime.parse(it)
                parsed.atZoneSameInstant(java.time.ZoneId.systemDefault())
                    .format(java.time.format.DateTimeFormatter.ofPattern("MMM dd", java.util.Locale.US))
            } ?: ""
        } catch (_: Exception) { "" },
        onFirst = onFirst,
        onSecond = onSecond,
        onThird = onThird,
        linescore = LinescoreUiModel(
            innings = innings.map {
                InningScoreUiModel(
                    number = it.number,
                    leftRuns = it.awayRuns?.toString() ?: "-",
                    rightRuns = it.homeRuns?.toString() ?: "-"
                )
            },
            leftTotal = TeamTotalUiModel(runs = awayScore, hits = awayHits, errors = awayErrors),
            rightTotal = TeamTotalUiModel(runs = homeScore, hits = homeHits, errors = homeErrors)
        )
    )
}

private fun GameDetails.countText(): String {
    val hasCount = balls > 0 || strikes > 0
    return if (hasCount) "$balls-$strikes" else ""
}

private fun GameDetails.outsText(): String {
    return when (outs) {
        1 -> "1 Out"
        2, 3 -> "$outs Outs"
        else -> ""
    }
}

// Play-by-play components

enum class GameDetailTab {
    PLAYS,
    BOX
}

enum class BoxTeamFilter {
    AWAY,
    HOME
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MainTabToggle(
    selectedTab: GameDetailTab,
    onTabSelected: (GameDetailTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween)
    ) {
        ToggleButton(
            checked = selectedTab == GameDetailTab.PLAYS,
            onCheckedChange = { onTabSelected(GameDetailTab.PLAYS) },
            modifier = Modifier
                .weight(1f)
                .semantics { role = Role.Tab },
            shapes = ButtonGroupDefaults.connectedLeadingButtonShapes()
        ) {
            Text("Plays")
        }

        ToggleButton(
            checked = selectedTab == GameDetailTab.BOX,
            onCheckedChange = { onTabSelected(GameDetailTab.BOX) },
            modifier = Modifier
                .weight(1f)
                .semantics { role = Role.Tab },
            shapes = ButtonGroupDefaults.connectedTrailingButtonShapes()
        ) {
            Text("Box")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BoxTeamToggle(
    awayTeamName: String,
    homeTeamName: String,
    selectedTeam: BoxTeamFilter,
    onTeamSelected: (BoxTeamFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = selectedTeam == BoxTeamFilter.AWAY,
            onClick = { onTeamSelected(BoxTeamFilter.AWAY) },
            label = { Text(awayTeamName) }
        )

        FilterChip(
            selected = selectedTeam == BoxTeamFilter.HOME,
            onClick = { onTeamSelected(BoxTeamFilter.HOME) },
            label = { Text(homeTeamName) }
        )
    }
}


@Composable
fun BoxscoreContent(
    boxscore: Boxscore,
    selectedTeam: BoxTeamFilter,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        when (selectedTeam) {
            BoxTeamFilter.AWAY -> TeamBoxscoreSection(boxscore.away)
            BoxTeamFilter.HOME -> TeamBoxscoreSection(boxscore.home)
        }
    }
}

@Composable
fun TeamBoxscoreSection(
    team: BoxscoreTeam,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Batting Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Batting",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                BattingHeader()
                team.batters.forEach { batter ->
                    BattingRow(batter)
                }
            }
        }

        // Pitching Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Pitching",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                PitchingHeader()
                team.pitchers.forEach { pitcher ->
                    PitchingRow(pitcher)
                }
            }
        }
    }
}

@Composable
fun BattingHeader() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(text = "Player", modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelSmall)
        BoxscoreStatHeader("AB")
        BoxscoreStatHeader("R")
        BoxscoreStatHeader("H")
        BoxscoreStatHeader("RBI")
        BoxscoreStatHeader("BB")
        BoxscoreStatHeader("K")
    }
}

@Composable
fun BattingRow(player: BoxscorePlayer) {
    val stats = player.battingStats ?: return
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = player.fullName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(text = player.position, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        BoxscoreStatValue(stats.ab.toString())
        BoxscoreStatValue(stats.r.toString())
        BoxscoreStatValue(stats.h.toString())
        BoxscoreStatValue(stats.rbi.toString())
        BoxscoreStatValue(stats.bb.toString())
        BoxscoreStatValue(stats.k.toString())
    }
}

@Composable
fun PitchingHeader() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(text = "Player", modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelSmall)
        BoxscoreStatHeader("IP")
        BoxscoreStatHeader("H")
        BoxscoreStatHeader("R")
        BoxscoreStatHeader("ER")
        BoxscoreStatHeader("BB")
        BoxscoreStatHeader("K")
    }
}

@Composable
fun PitchingRow(player: BoxscorePlayer) {
    val stats = player.pitchingStats ?: return
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = player.fullName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        BoxscoreStatValue(stats.ip)
        BoxscoreStatValue(stats.h.toString())
        BoxscoreStatValue(stats.r.toString())
        BoxscoreStatValue(stats.er.toString())
        BoxscoreStatValue(stats.bb.toString())
        BoxscoreStatValue(stats.k.toString())
    }
}

@Composable
fun BoxscoreStatHeader(text: String) {
    Text(
        text = text,
        modifier = Modifier.size(width = 28.dp, height = 16.dp),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = androidx.compose.ui.text.style.TextAlign.Center
    )
}

@Composable
fun BoxscoreStatValue(text: String) {
    Text(
        text = text,
        modifier = Modifier.size(width = 28.dp, height = 20.dp),
        style = MaterialTheme.typography.bodyMedium,
        textAlign = androidx.compose.ui.text.style.TextAlign.Center
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayDetailBottomSheet(
    play: PlayItem,
    sheetState: androidx.compose.material3.SheetState,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Header Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                PlayerHeadshot(
                    playerId = play.batterId,
                    contentDescription = play.batterName?.let { "$it headshot" } ?: "Player headshot",
                    modifier = Modifier.size(72.dp)
                )

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = play.event ?: "Play Detail",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = buildMatchupLine(play.batterName, play.pitcherName),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Text(
                text = play.description ?: "",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 0.dp)
            )

            // Strike Zone Visual
            StrikeZone(
                pitches = play.pitches,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .padding(horizontal = 32.dp)
            )

            // Pitch List
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Pitch Sequence",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                PitchSequenceList(pitches = play.pitches)
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
fun StrikeZone(
    pitches: List<PitchItem>,
    modifier: Modifier = Modifier
) {
    val strikeZoneColor = MaterialTheme.colorScheme.outline
    val ballColor = MaterialTheme.colorScheme.error
    val strikeColor = MaterialTheme.colorScheme.primary

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        // Draw Strike Zone Box (roughly centered)
        // MLB Strike Zone is typically 17 inches wide.
        // pX is feet from center of plate.
        // pZ is feet from ground. Strike zone is usually ~1.5 to 3.5 ft.
        
        val zoneWidth = width * 0.6f
        val zoneHeight = height * 0.6f
        val zoneLeft = (width - zoneWidth) / 2
        val zoneTop = (height - zoneHeight) / 2

        drawRect(
            color = strikeZoneColor,
            topLeft = androidx.compose.ui.geometry.Offset(zoneLeft, zoneTop),
            size = androidx.compose.ui.geometry.Size(zoneWidth, zoneHeight),
            style = Stroke(width = 2.dp.toPx())
        )

        // Plot Pitches
        pitches.forEach { pitch ->
            val px = pitch.px ?: 0f
            val pz = pitch.pz ?: 0f

            // Map pX (-1.5 to 1.5 range typically) to zone coordinates
            // 0 is center. -0.708 to 0.708 is strike zone width in feet (17 inches)
            val centerX = width / 2
            val xPos = centerX + (px * (zoneWidth / 1.416f))

            // Map pZ (typical 1.5 to 3.5) to zone coordinates
            // Center of zone in Z is ~2.5
            val centerY = zoneTop + (zoneHeight / 2)
            val yPos = centerY - ((pz - 2.5f) * (zoneHeight / 2.0f))

            val isStrike = pitch.result?.lowercase()?.let { result ->
                result.contains("strike") || 
                result.contains("swinging") || 
                result.contains("called") ||
                result.contains("foul") ||
                result.contains("in play")
            } == true

            drawCircle(
                color = if (isStrike) strikeColor else ballColor,
                radius = 8.dp.toPx(),
                center = androidx.compose.ui.geometry.Offset(xPos, yPos)
            )
            
            // Draw pitch number
            // (Skipping text on canvas for simplicity, usually needs native canvas or specialized approach)
        }
    }
}

enum class PlayFilter {
    ALL,
    SCORING
}

data class PlayGroup(
    val inning: Int,
    val isTopInning: Boolean,
    val plays: List<PlayItem>
) {
    val title: String
        get() = "${if (isTopInning) "Top" else "Bottom"} $inning"
}

fun groupPlaysByInningHalf(plays: List<PlayItem>): List<PlayGroup> {
    return plays
        .groupBy { it.inning to it.isTopInning }
        .map { (key, playsInGroup) ->
            PlayGroup(
                inning = key.first,
                isTopInning = key.second,
                plays = playsInGroup
            )
        }
        .sortedWith(
            compareByDescending<PlayGroup> { it.inning }
                .thenByDescending { it.isTopInning }
        )
}

fun PlayItem.isLikelyScoringPlay(): Boolean {
    val text = description.orEmpty().lowercase()
    val eventText = event.orEmpty().lowercase()

    return text.contains(" scores") ||
            text.contains("score.") ||
            text.contains("score,") ||
            text.contains("homers") ||
            text.contains("home run") ||
            text.contains("grand slam") ||
            text.contains("sacrifice fly") ||
            text.contains("sac fly") ||
            text.contains("steals home") ||
            eventText.contains("home run") ||
            eventText.contains("grand slam")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayFilterChips(
    selectedFilter: PlayFilter,
    onFilterSelected: (PlayFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = selectedFilter == PlayFilter.ALL,
            onClick = { onFilterSelected(PlayFilter.ALL) },
            label = { Text("All Plays") },
            leadingIcon = null
        )

        FilterChip(
            selected = selectedFilter == PlayFilter.SCORING,
            onClick = { onFilterSelected(PlayFilter.SCORING) },
            label = { Text("Scoring Plays") },
            leadingIcon = null
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PlayRow(
    play: PlayItem,
    shape: Shape,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val containerColor = if (isDark) MaterialTheme.colorScheme.surfaceContainerHigh else MaterialTheme.colorScheme.surfaceContainer

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = shape,
        color = containerColor,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                PlayerHeadshot(
                    playerId = play.batterId,
                    contentDescription = play.batterName?.let { "$it headshot" } ?: "Player headshot",
                    modifier = Modifier.size(56.dp)
                )

                Spacer(Modifier.width(16.dp))

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    if (!play.event.isNullOrBlank()) {
                        Text(
                            text = play.event,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    val matchupLine = buildMatchupLine(
                        batter = play.batterName,
                        pitcher = play.pitcherName
                    )

                    if (matchupLine.isNotBlank()) {
                        Text(
                            text = matchupLine,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            if (!play.description.isNullOrBlank()) {
                Text(
                    text = play.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(top = 12.dp),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PitchSequenceList(
    pitches: List<PitchItem>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)
    ) {
        pitches.forEachIndexed { index, pitch ->
            PitchItemRow(
                pitch = pitch,
                index = index,
                shape = ListItemDefaults.segmentedShapes(
                    index = index,
                    count = pitches.size
                ).shape
            )
        }
    }
}

@Composable
fun PitchItemRow(
    pitch: PitchItem,
    index: Int,
    shape: Shape,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val containerColor = if (isDark) {
        MaterialTheme.colorScheme.surfaceContainerHigh
    } else {
        MaterialTheme.colorScheme.surfaceContainer
    }

    val isStrike = pitch.result?.lowercase()?.let { result ->
        result.contains("strike") ||
                result.contains("swinging") ||
                result.contains("called") ||
                result.contains("foul") ||
                result.contains("in play")
    } == true

    val indicatorColor = if (isStrike) {
        Color(0xFF4A6572) // Same blue as strike zone
    } else {
        Color(0xFFD32F2F) // Same red as strike zone
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = shape,
        color = containerColor
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(indicatorColor.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${index + 1}",
                        style = MaterialTheme.typography.labelSmall,
                        color = indicatorColor,
                        fontWeight = FontWeight.Bold
                    )
                }

                Column {
                    Text(
                        text = pitch.result ?: "Unknown",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                    if (pitch.pitchType != null) {
                        Text(
                            text = pitch.pitchType,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                pitch.velocity?.let { velo ->
                    Text(
                        text = "${velo.toInt()} mph",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )
                }
                if (pitch.balls != null && pitch.strikes != null) {
                    Text(
                        text = "${pitch.balls}-${pitch.strikes}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
fun PlayerHeadshot(
    playerId: Int?,
    contentDescription: String,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    val imageUrl = playerId?.let {
        "https://img.mlbstatic.com/mlb-photos/image/upload/" +
                "w_213,q_100,f_jpg/v1/people/$it/headshot/67/current"
    }

    val fallbackUrl =
        "https://img.mlbstatic.com/mlb-photos/image/upload/" +
                "w_213,d_people:generic:headshot:silo:current.png," +
                "q_auto:best,f_auto/v1/people/0/headshot/67/current"

    AsyncImage(
        model = ImageRequest.Builder(context)
            .data(imageUrl ?: fallbackUrl)
            .crossfade(true)
            .build(),
        contentDescription = contentDescription,
        contentScale = ContentScale.Crop,
        modifier = modifier
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
    )
}

fun buildMatchupLine(
    batter: String?,
    pitcher: String?
): String {
    return when {
        !batter.isNullOrBlank() && !pitcher.isNullOrBlank() -> "$batter vs $pitcher"
        !batter.isNullOrBlank() -> batter
        !pitcher.isNullOrBlank() -> pitcher
        else -> ""
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
private fun GameDetailContentPreview() {
    DugoutTheme {
        var selectedPlayForSheet by remember { mutableStateOf<PlayItem?>(null) }
        
        Box {
            GameDetailContent(
                uiState = GameDetailUiState(
                    isLoading = false,
                    gameDetails = GameDetails(
                        gamePk = 1,
                        awayTeam = "Pirates",
                        homeTeam = "Phillies",
                        awayScore = 4,
                        homeScore = 3,
                        status = "In Progress",
                        currentInning = 8,
                        inningState = "Top",
                        balls = 2,
                        strikes = 1,
                        outs = 1,
                        currentBatter = "Bryce Harper",
                        currentPitcher = "David Bednar",
                        plays = listOf(
                            PlayItem(
                                description = "Bryce Harper singled to right field.",
                                event = "Single",
                                inning = 8,
                                isTopInning = true,
                                batterName = "Bryce Harper",
                                pitcherName = "David Bednar",
                                batterId = 547180,
                                pitches = listOf(
                                    PitchItem(result = "Ball", pitchType = "Sinker", velocity = 94.2, px = -0.8f, pz = 1.2f),
                                    PitchItem(result = "Called Strike", pitchType = "Slider", velocity = 85.5, px = 0.2f, pz = 2.5f),
                                    PitchItem(result = "In Play, No Out", pitchType = "Sinker", velocity = 95.0, px = -0.3f, pz = 2.0f)
                                )
                            )
                        )
                    )
                )
            )

            if (selectedPlayForSheet != null) {
                PlayDetailBottomSheet(
                    play = selectedPlayForSheet!!,
                    sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                    onDismissRequest = { selectedPlayForSheet = null }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Preview(showBackground = true)
@Composable
private fun PlayDetailSheetContentPreview() {
    DugoutTheme {
        Surface {
            val play = PlayItem(
                description = "Trea Turner doubles on a line drive to left fielder. Bryce Harper scores.",
                event = "Double",
                inning = 8,
                isTopInning = true,
                batterName = "Trea Turner",
                pitcherName = "David Bednar",
                batterId = 592785,
                pitches = listOf(
                    PitchItem(result = "Ball", pitchType = "Four-Seam Fastball", velocity = 98.4, px = -0.9f, pz = 1.5f),
                    PitchItem(result = "Called Strike", pitchType = "Curveball", velocity = 82.1, px = 0.4f, pz = 2.8f),
                    PitchItem(result = "Foul", pitchType = "Slider", velocity = 88.5, px = 1.1f, pz = 2.0f),
                    PitchItem(result = "In Play, Out(s)", pitchType = "Four-Seam Fastball", velocity = 99.2, px = 0.1f, pz = 2.2f)
                )
            )
            
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    PlayerHeadshot(
                        playerId = play.batterId,
                        contentDescription = play.batterName?.let { "$it headshot" } ?: "Player headshot",
                        modifier = Modifier.size(72.dp)
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = play.event ?: "Play Detail",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = buildMatchupLine(play.batterName, play.pitcherName),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Text(
                    text = play.description ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 0.dp)
                )

                StrikeZone(
                    pitches = play.pitches,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .padding(horizontal = 32.dp)
                )

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Pitch Sequence",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    PitchSequenceList(pitches = play.pitches)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PlayRowExpandedPreview() {
    DugoutTheme {
        val play = PlayItem(
            description = "Trea Turner doubles on a line drive to left fielder. Bryce Harper scores.",
            event = "Double",
            inning = 8,
            isTopInning = true,
            batterName = "Trea Turner",
            pitcherName = "David Bednar",
            batterId = 592785,
            pitches = listOf(
                PitchItem(result = "Ball", pitchType = "Four-Seam Fastball", velocity = 98.4, px = -0.9f, pz = 1.5f),
                PitchItem(result = "Called Strike", pitchType = "Curveball", velocity = 82.1, px = 0.4f, pz = 2.8f),
                PitchItem(result = "In Play, Out(s)", pitchType = "Four-Seam Fastball", velocity = 99.2, px = 0.1f, pz = 2.2f)
            )
        )

        Column(modifier = Modifier.padding(16.dp)) {
            Text("Play Item", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 12.dp))
            PlayRow(
                play = play,
                shape = MaterialTheme.shapes.large,
                onClick = {}
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun StrikeZonePreview() {
    DugoutTheme {
        StrikeZone(
            pitches = listOf(
                PitchItem(result = "Ball", px = -0.9f, pz = 1.5f),
                PitchItem(result = "Called Strike", px = 0.4f, pz = 2.8f),
                PitchItem(result = "Foul", px = 1.1f, pz = 2.0f),
                PitchItem(result = "In Play, Out(s)", px = 0.1f, pz = 2.2f)
            ),
            modifier = Modifier
                .size(300.dp)
                .padding(16.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PitchSequencePreview() {
    DugoutTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            PitchSequenceList(
                pitches = listOf(
                    PitchItem(result = "Ball", pitchType = "Four-Seam Fastball", velocity = 98.4, px = -0.9f, pz = 1.5f),
                    PitchItem(result = "Called Strike", pitchType = "Curveball", velocity = 82.1, px = 0.4f, pz = 2.8f),
                    PitchItem(result = "Foul", pitchType = "Slider", velocity = 88.5, px = 1.1f, pz = 2.0f),
                    PitchItem(result = "In Play, Out(s)", pitchType = "Four-Seam Fastball", velocity = 99.2, px = 0.1f, pz = 2.2f)
                )
            )
        }
    }
}
