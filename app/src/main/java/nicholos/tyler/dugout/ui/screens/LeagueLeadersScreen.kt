package nicholos.tyler.dugout.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.ListItemShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import nicholos.tyler.dugout.model.domain.LeagueLeader
import nicholos.tyler.dugout.model.domain.LeagueLeaderGroup
import nicholos.tyler.dugout.model.ui.LeagueLeadersUiState
import nicholos.tyler.dugout.ui.theme.DugoutTheme

@Composable
fun LeagueLeadersScreenContent(
    uiState: LeagueLeadersUiState,
    modifier: Modifier = Modifier,
    onLeaderClick: (Int, Boolean) -> Unit = { _, _ -> }
) {
    Box(modifier = modifier.fillMaxSize()) {
        when {
            uiState.isLoading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }

            uiState.error != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = uiState.error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                }
            }

            uiState.stats.isEmpty() || uiState.stats.all { it.leaders.isEmpty() } -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "No leaders found for the selected filters.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    items(uiState.stats) { leaderGroup ->
                        if (leaderGroup.leaders.isNotEmpty()) {
                            LeaderCategorySection(
                                leaderGroup = leaderGroup,
                                onLeaderClick = onLeaderClick
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
private fun LeaderCategorySection(
    leaderGroup: LeagueLeaderGroup,
    onLeaderClick: (Int, Boolean) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = leaderGroup.categoryName,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 10.dp)
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)
        ) {
            leaderGroup.leaders.forEachIndexed { index, leader ->
                LeaderRow(
                    leader = leader,
                    shape = ListItemDefaults.segmentedShapes(
                        index = index,
                        count = leaderGroup.leaders.size
                    ),
                    onClick = { onLeaderClick(leader.id, leader.isPlayer) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun LeaderRow(
    leader: LeagueLeader,
    shape: ListItemShapes,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val selected = leader.teamName?.contains("Phillies", ignoreCase = true) == true

    val containerColor = when {
        selected -> MaterialTheme.colorScheme.secondaryContainer
        isDark -> MaterialTheme.colorScheme.surfaceContainerHigh
        else -> MaterialTheme.colorScheme.surfaceContainer
    }

    val contentColor = if (selected) {
        MaterialTheme.colorScheme.onSecondaryContainer
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    val secondaryContentColor = if (selected) {
        MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.72f)
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

//    val borderColor = if (selected) {
//        MaterialTheme.colorScheme.primary.copy(alpha = if (isDark) 0.34f else 0.22f)
//    } else {
//        MaterialTheme.colorScheme.outlineVariant.copy(alpha = if (isDark) 0.32f else 0.18f)
//    }

    val context = LocalContext.current
    val imageUrl = if (leader.isPlayer) {
        "https://img.mlbstatic.com/mlb-photos/image/upload/d_people:generic:headshot:67:current.png/w_426,q_auto:best/v1/people/${leader.id}/headshot/67/current"
    } else {
        "https://www.mlbstatic.com/team-logos/${leader.id}.svg"
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = shape.shape,
        color = containerColor,
        //border = BorderStroke(1.dp, borderColor),
        tonalElevation = if (selected) 1.dp else 0.dp,
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.width(40.dp),
                contentAlignment = Alignment.Center
            ) {
                RankBadge(
                    rank = "${leader.rank}",
                    selected = selected
                )
            }

            Box(
                modifier = Modifier.width(52.dp),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(if (selected) 42.dp else 38.dp)
                        .background(
                            color = if (selected) {
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
                            } else {
                                MaterialTheme.colorScheme.surfaceContainerHighest
                            },
                            shape = CircleShape
                        )
                        .clip(CircleShape)
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp, end = 8.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = leader.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.SemiBold,
                    color = contentColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                leader.teamName?.let { teamName ->
                    Text(
                        text = teamName,
                        style = MaterialTheme.typography.bodySmall,
                        color = secondaryContentColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Box(
                modifier = Modifier.width(56.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Text(
                    text = leader.statValue,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = contentColor,
                    textAlign = TextAlign.End,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun RankBadge(
    rank: String,
    selected: Boolean,
    modifier: Modifier = Modifier
) {
    val shape: Shape = if (selected) {
        RoundedCornerShape(
            topStart = 18.dp,
            topEnd = 10.dp,
            bottomEnd = 18.dp,
            bottomStart = 10.dp
        )
    } else {
        CircleShape
    }

    val containerColor = if (selected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.secondaryContainer
    }

    val contentColor = if (selected) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSecondaryContainer
    }

    Box(
        modifier = modifier
            .size(34.dp)
            .clip(shape)
            .background(containerColor),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = rank,
            style = MaterialTheme.typography.labelLarge,
            color = contentColor,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}

@Preview(showBackground = true)
@Composable
fun LeagueLeadersScreenPreview() {
    DugoutTheme {
        LeagueLeadersScreenContent(
            uiState = LeagueLeadersUiState(
                stats = listOf(
                    LeagueLeaderGroup(
                        categoryId = "homeRuns",
                        categoryName = "HOME RUNS",
                        leaders = listOf(
                            LeagueLeader(1, 1, "Aaron Judge", "New York Yankees", "16", true),
                            LeagueLeader(2, 1, "Kyle Schwarber", "Philadelphia Phillies", "16", true),
                            LeagueLeader(3, 3, "Munetaka Murakami", "Chicago White Sox", "15", true),
                            LeagueLeader(4, 4, "Matt Olson", "Atlanta Braves", "14", true),
                            LeagueLeader(5, 5, "Yordan Alvarez", "Houston Astros", "13", true),
                            LeagueLeader(6, 5, "Byron Buxton", "Minnesota Twins", "13", true),
                            LeagueLeader(7, 5, "Ben Rice", "New York Yankees", "13", true),
                            LeagueLeader(8, 8, "Junior Caminero", "Tampa Bay Rays", "11", true)
                        )
                    )
                )
            )
        )
    }
}
