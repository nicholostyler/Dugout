package nicholos.tyler.dugout.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.ListItemShapes
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.graphics.shapes.RoundedPolygon
import androidx.graphics.shapes.toPath
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import nicholos.tyler.dugout.model.ui.RosterPlayerUiModel
import nicholos.tyler.dugout.model.ui.RosterSectionUiModel
import nicholos.tyler.dugout.model.ui.RosterSummaryUiModel
import nicholos.tyler.dugout.model.ui.RosterUiState
import nicholos.tyler.dugout.ui.theme.DugoutTheme
import nicholos.tyler.dugout.viewmodel.RosterViewModel
import kotlin.math.max

@Composable
fun RosterScreen(
    viewModel: RosterViewModel,
    teamId: Int,
    modifier: Modifier = Modifier,
    onPlayerClick: (Int) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(teamId) {
        viewModel.loadRoster(teamId)
    }

    RosterScreenContent(
        uiState = uiState,
        modifier = modifier,
        onPlayerClick = onPlayerClick
    )
}

@Composable
fun RosterScreenContent(
    uiState: RosterUiState,
    modifier: Modifier = Modifier,
    onPlayerClick: (Int) -> Unit = {}
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
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = uiState.error.orEmpty(),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        else -> {
            LazyColumn(
                modifier = modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                uiState.sections.forEach { section ->
                    stickyHeader {
                        RosterSectionHeader(title = section.title)
                    }

                    item {
                        RosterSegment(
                            players = section.players,
                            onPlayerClick = onPlayerClick
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RosterSectionHeader(
    title: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun RosterSegment(
    players: List<RosterPlayerUiModel>,
    onPlayerClick: (Int) -> Unit
) {
    Column(
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)
    ) {
        players.forEachIndexed { index, player ->
            RosterSegmentedItem(
                player = player,
                shape = ListItemDefaults.segmentedShapes(
                    index = index,
                    count = players.size
                ),
                onClick = { onPlayerClick(player.id) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun RosterSegmentedItem(
    player: RosterPlayerUiModel,
    shape: ListItemShapes,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = shape.shape,
        color = MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        ListItem(
            colors = ListItemDefaults.colors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            ),
            leadingContent = {
                RosterPlayerHeadshot(
                    playerId = player.id,
                    contentDescription = "${player.name} headshot",
                    isMvp = player.isMvp
                )
            },
            overlineContent = {
                player.jerseyNumber?.let { jersey ->
                    Text(
                        text = "#$jersey",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            headlineContent = {
                Text(
                    text = player.name,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            supportingContent = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = player.primaryStatLine,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    player.status?.let { status ->
                        Text(
                            text = status,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            },
            trailingContent = {
                Text(
                    text = player.position,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun rememberRosterHeadshotShape(isMvp: Boolean): Shape {
    return if (isMvp) {
        MaterialShapes.Cookie12Sided.toShape()
    } else {
        CircleShape
    }
}

@Composable
private fun RosterPlayerHeadshot(
    playerId: Int,
    contentDescription: String,
    isMvp: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val shape = rememberRosterHeadshotShape(isMvp)
    val headshotSize = if (isMvp) 60.dp else 56.dp

    val imageUrl =
        "https://img.mlbstatic.com/mlb-photos/image/upload/" +
                "w_213,q_100,f_jpg/v1/people/$playerId/headshot/67/current"

    AsyncImage(
        model = ImageRequest.Builder(context)
            .data(imageUrl)
            .crossfade(true)
            .build(),
        contentDescription = contentDescription,
        contentScale = ContentScale.Crop,
        modifier = modifier
            .size(headshotSize)
            .background(
                color = if (isMvp) {
                    MaterialTheme.colorScheme.secondaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                },
                shape = shape
            )
            .clip(shape)
    )
}

@Preview(showBackground = true)
@Composable
private fun RosterScreenPreview() {
    DugoutTheme {
        RosterScreenContent(
            uiState = RosterUiState(
                summary = RosterSummaryUiModel(
                    totalPlayers = 26,
                    pitchers = 13,
                    positionPlayers = 13
                ),
                sections = listOf(
                    RosterSectionUiModel(
                        title = "Pitchers",
                        players = listOf(
                            RosterPlayerUiModel(
                                id = 1,
                                name = "Zack Wheeler",
                                jerseyNumber = "45",
                                position = "P",
                                primaryStatLine = "ERA 2.84 • SO 187 • W 12 • WHIP 1.03",
                                isMvp = true
                            ),
                            RosterPlayerUiModel(
                                id = 2,
                                name = "Cristopher Sanchez",
                                jerseyNumber = "61",
                                position = "P",
                                primaryStatLine = "ERA 3.12 • SO 91 • W 7 • WHIP 1.18"
                            )
                        )
                    ),
                    RosterSectionUiModel(
                        title = "Infielders",
                        players = listOf(
                            RosterPlayerUiModel(
                                id = 3,
                                name = "Alec Bohm",
                                jerseyNumber = "28",
                                position = "3B",
                                primaryStatLine = "AVG .167 • HR 1 • RBI 7 • OPS .553"
                            ),
                            RosterPlayerUiModel(
                                id = 4,
                                name = "Trea Turner",
                                jerseyNumber = "7",
                                position = "SS",
                                primaryStatLine = "AVG .311 • HR 9 • RBI 28 • OPS .847",
                                isMvp = true
                            )
                        )
                    )
                )
            )
        )
    }
}