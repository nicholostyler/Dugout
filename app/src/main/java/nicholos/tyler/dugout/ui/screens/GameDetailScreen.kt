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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import nicholos.tyler.dugout.model.domain.GameDetails
import nicholos.tyler.dugout.model.domain.MlbTeams
import nicholos.tyler.dugout.model.domain.PlayItem
import nicholos.tyler.dugout.model.ui.GameSnapshotCardUiModel
import nicholos.tyler.dugout.model.ui.TeamScoreUiModel
import nicholos.tyler.dugout.ui.components.GameSnapshotCard
import nicholos.tyler.dugout.ui.theme.DugoutTheme
import nicholos.tyler.dugout.viewmodel.GameDetailUiState
import nicholos.tyler.dugout.viewmodel.GameDetailViewModel
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi

private enum class PlayFilter {
    ALL,
    SCORING
}

private data class PlayGroup(
    val inning: Int,
    val isTopInning: Boolean,
    val plays: List<PlayItem>
) {
    val title: String
        get() = "${if (isTopInning) "Top" else "Bottom"} $inning"
}

private fun groupPlaysByInningHalf(plays: List<PlayItem>): List<PlayGroup> {
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

private fun PlayItem.isLikelyScoringPlay(): Boolean {
    val text = description.orEmpty().lowercase()

    return text.contains(" scores") ||
            text.contains("score.") ||
            text.contains("score,") ||
            text.contains("homers") ||
            text.contains("home run") ||
            text.contains("grand slam") ||
            text.contains("sacrifice fly") ||
            text.contains("sac fly") ||
            text.contains("steals home")
}

@Composable
fun GameDetailScreen(
    viewModel: GameDetailViewModel,
    gamePk: Int,
    modifier: Modifier = Modifier,
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(gamePk) {
        viewModel.loadGame(gamePk)
    }

    GameDetailContent(
        uiState = uiState.value,
        modifier = modifier
    )
}

@Composable
fun GameDetailContent(
    uiState: GameDetailUiState,
    modifier: Modifier = Modifier,
) {
    when {
        uiState.isLoading -> {
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
            var selectedFilter by rememberSaveable { mutableStateOf(PlayFilter.ALL) }

            val filteredPlays = when (selectedFilter) {
                PlayFilter.ALL -> game.plays
                PlayFilter.SCORING -> game.plays.filter { it.isLikelyScoringPlay() }
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
                    Text(
                        text = "Plays",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                item {
                    PlayFilterToggle(
                        selectedFilter = selectedFilter,
                        onFilterSelected = { selectedFilter = it }
                    )
                }

                if (filteredPlays.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.large,
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
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
                        itemsIndexed(
                            items = group.plays,
                            key = { index, play ->
                                "${group.inning}-${group.isTopInning}-$index-${play.event}-${play.description}"
                            }
                        ) { _, play ->
                            PlayRow(
                                play = play,
                                inningLabel = group.title
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
private fun PlayFilterToggle(
    selectedFilter: PlayFilter,
    onFilterSelected: (PlayFilter) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween)
    ) {
        ToggleButton(
            checked = selectedFilter == PlayFilter.ALL,
            onCheckedChange = { onFilterSelected(PlayFilter.ALL) },
            modifier = Modifier
                .weight(1f)
                .semantics { role = Role.RadioButton },
            shapes = ButtonGroupDefaults.connectedLeadingButtonShapes()
        ) {
            Text("All Plays")
        }

        ToggleButton(
            checked = selectedFilter == PlayFilter.SCORING,
            onCheckedChange = { onFilterSelected(PlayFilter.SCORING) },
            modifier = Modifier
                .weight(1f)
                .semantics { role = Role.RadioButton },
            shapes = ButtonGroupDefaults.connectedTrailingButtonShapes()
        ) {
            Text("Scoring Plays")
        }
    }
}

fun GameDetails.toSnapshotUiModel(): GameSnapshotCardUiModel {
    val isFinal = status.equals("Final", ignoreCase = true)

    return GameSnapshotCardUiModel(
        gameId = gamePk,
        leftTeam = TeamScoreUiModel(
            name = MlbTeams.get(teamId = awayTeamId, fallbackName = awayTeam),
            score = awayScore.toString()
        ),
        rightTeam = TeamScoreUiModel(
            name = MlbTeams.get(teamId = homeTeamId, fallbackName = homeTeam),
            score = homeScore.toString()
        ),
        status = status.orEmpty(),
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
        }
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

@Composable
private fun PlayRow(
    play: PlayItem,
    inningLabel: String? = null,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            PlayerHeadshot(
                playerId = play.batterId,
                contentDescription = play.batterName?.let { "$it headshot" } ?: "Player headshot"
            )

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!inningLabel.isNullOrBlank()) {
                        Text(
                            text = inningLabel,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    if (!play.event.isNullOrBlank()) {
                        Surface(
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(999.dp)
                        ) {
                            Text(
                                text = play.event.orEmpty(),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (!play.description.isNullOrBlank()) {
                    Text(
                        text = play.description.orEmpty(),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                val matchupLine = buildMatchupLine(
                    batter = play.batterName,
                    pitcher = play.pitcherName
                )

                if (matchupLine.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = matchupLine,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun PlayerHeadshot(
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
            .size(48.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
    )
}

private fun buildMatchupLine(
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

@Preview(showBackground = true)
@Composable
private fun GameDetailContentPreview() {
    DugoutTheme {
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
                            batterId = 547180
                        ),
                        PlayItem(
                            description = "Trea Turner doubles on a line drive to left fielder. Bryce Harper scores.",
                            event = "Double",
                            inning = 8,
                            isTopInning = true,
                            batterName = "Trea Turner",
                            pitcherName = "David Bednar",
                            batterId = 592785
                        )
                    )
                )
            )
        )
    }
}