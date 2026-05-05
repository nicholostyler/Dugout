package nicholos.tyler.dugout.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import nicholos.tyler.dugout.model.domain.PlayerStatCategory
import nicholos.tyler.dugout.model.domain.PlayerStatItem
import nicholos.tyler.dugout.model.ui.PlayerCategoryStatsUiModel
import nicholos.tyler.dugout.model.ui.PlayerProfileUiModel
import nicholos.tyler.dugout.model.ui.PlayerSplitStatsUiModel
import nicholos.tyler.dugout.model.ui.PlayerStatRange
import nicholos.tyler.dugout.model.ui.PlayerUiState
import nicholos.tyler.dugout.viewmodel.PlayerViewModel

@Composable
fun PlayerScreen(
    viewModel: PlayerViewModel,
    playerId: Int,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(playerId) {
        viewModel.loadPlayer(playerId)
    }

    PlayerScreenContent(
        uiState = uiState,
        modifier = modifier,
        onCategorySelected = viewModel::onCategorySelected,
        onRangeSelected = viewModel::onRangeSelected
    )
}

@Composable
fun PlayerScreenContent(
    uiState: PlayerUiState,
    modifier: Modifier = Modifier,
    onCategorySelected: (PlayerStatCategory) -> Unit = {},
    onRangeSelected: (PlayerStatRange) -> Unit = {}
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

        uiState.player != null -> {
            val player = uiState.player
            val categories = player.categories

            val selectedCategory = remember(
                categories,
                uiState.selectedCategory,
                player.position
            ) {
                categories.firstOrNull { it.category == uiState.selectedCategory }
                    ?: categories.firstOrNull { it.category == player.defaultStatCategory() }
                    ?: categories.firstOrNull()
            }

            LazyColumn(
                modifier = modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    PlayerHeroCard(player = player)
                }

                if (player.quickStats.isNotEmpty()) {
                    item {
                        QuickStatsCard(player = player)
                    }
                }

                if (categories.isNotEmpty() && selectedCategory != null) {
                    item {
                        CategoryButtonGroup(
                            categories = categories,
                            selectedCategory = selectedCategory.category,
                            onCategorySelected = onCategorySelected
                        )
                    }

                    item {
                        RangeChips(
                            selectedRange = uiState.selectedRange,
                            onRangeSelected = onRangeSelected
                        )
                    }

                    item {
                        PlayerStatsCard(
                            category = selectedCategory,
                            range = uiState.selectedRange
                        )
                    }
                } else {
                    item {
                        NoStatsCard()
                    }
                }

                if (player.splits.isNotEmpty()) {
                    item {
                        SplitsCard(splits = player.splits)
                    }
                }
            }
        }
    }
}

@Composable
private fun SplitsCard(
    splits: List<PlayerSplitStatsUiModel>,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Splits",
                modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 20.dp),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )

            splits.forEachIndexed { index, split ->
                Column(
                    modifier = Modifier.padding(
                        start = 20.dp,
                        end = 20.dp,
                        bottom = if (index == splits.lastIndex) 20.dp else 0.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = split.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    split.stats.forEach { stat ->
                        StatRow(stat = stat)
                    }
                }

                if (index != splits.lastIndex) {
                    HorizontalDivider()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun PlayerHeroCard(
    player: PlayerProfileUiModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val imageUrl =
        "https://img.mlbstatic.com/mlb-photos/image/upload/" +
                "w_426,q_100,f_jpg/v1/people/${player.id}/headshot/67/current"

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = "${player.fullName} headshot",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(96.dp)
                    .background(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = MaterialShapes.Cookie12Sided.toShape()
                    )
                    .clip(MaterialShapes.Cookie12Sided.toShape())
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    player.jerseyNumber?.let {
                        Text(
                            text = "#$it",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Text(
                        text = player.fullName,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Text(
                    text = player.position,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            player.bats?.let { MetaChip(text = "Bats $it") }
            player.throwsHand?.let { MetaChip(text = "Throws $it") }
            player.age?.let { MetaChip(text = "Age $it") }
            player.height?.let { MetaChip(text = it) }
            player.weight?.let { MetaChip(text = "${it} lb") }
        }
    }
}

@Composable
private fun MetaChip(
    text: String,
    modifier: Modifier = Modifier
) {
    AssistChip(
        onClick = {},
        enabled = false,
        label = {
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge
            )
        },
        modifier = modifier
    )
}

@Composable
private fun QuickStatsCard(
    player: PlayerProfileUiModel,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 18.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            player.quickStats.take(4).forEach { stat ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = stat.label,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = stat.value,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun CategoryButtonGroup(
    categories: List<PlayerCategoryStatsUiModel>,
    selectedCategory: PlayerStatCategory,
    onCategorySelected: (PlayerStatCategory) -> Unit,
    modifier: Modifier = Modifier
) {
    val orderedCategories = remember(categories) {
        val available = categories.map { it.category }.distinct()
        PlayerStatCategory.entries.filter { it in available }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .widthIn(max = 560.dp),
        horizontalArrangement = Arrangement.spacedBy(
            ButtonGroupDefaults.ConnectedSpaceBetween
        )
    ) {
        orderedCategories.forEachIndexed { index, category ->
            val shapes = when (index) {
                0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                orderedCategories.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
            }

            ToggleButton(
                checked = selectedCategory == category,
                onCheckedChange = { onCategorySelected(category) },
                modifier = Modifier
                    .weight(1f)
                    .semantics { role = Role.RadioButton },
                shapes = shapes
            ) {
                Text(category.label())
            }
        }
    }
}

@Composable
private fun RangeChips(
    selectedRange: PlayerStatRange,
    onRangeSelected: (PlayerStatRange) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        PlayerStatRange.entries.forEach { range ->
            FilterChip(
                selected = selectedRange == range,
                onClick = { onRangeSelected(range) },
                label = { Text(range.label()) },
                colors = FilterChipDefaults.filterChipColors()
            )
        }
    }
}

@Composable
private fun PlayerStatsCard(
    category: PlayerCategoryStatsUiModel,
    range: PlayerStatRange,
    modifier: Modifier = Modifier
) {
    val primary = when (range) {
        PlayerStatRange.SEASON -> category.seasonPrimary
        PlayerStatRange.CAREER -> category.careerPrimary
    }

    val secondary = when (range) {
        PlayerStatRange.SEASON -> category.seasonSecondary
        PlayerStatRange.CAREER -> category.careerSecondary
    }

    ElevatedCard(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "${category.category.label()} • ${range.label()}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )

            if (primary.isEmpty() && secondary.isEmpty()) {
                Text(
                    text = "No stats available",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                return@Column
            }

            if (primary.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    primary.forEach { stat ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = stat.label,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = stat.value,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            if (secondary.isNotEmpty()) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    secondary.forEach { stat ->
                        StatRow(stat = stat)
                    }
                }
            }
        }
    }
}

@Composable
private fun NoStatsCard(
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Stats",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "No stats available",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun StatRow(
    stat: PlayerStatItem,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = stat.label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = stat.value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium
        )
    }
}

private fun PlayerProfileUiModel.defaultStatCategory(): PlayerStatCategory {
    val normalized = position.trim().lowercase()

    return when {
        normalized == "p" -> PlayerStatCategory.PITCHING
        normalized.contains("pitcher") -> PlayerStatCategory.PITCHING
        else -> PlayerStatCategory.BATTING
    }
}

private fun PlayerStatCategory.label(): String {
    return when (this) {
        PlayerStatCategory.BATTING -> "Batting"
        PlayerStatCategory.PITCHING -> "Pitching"
        PlayerStatCategory.FIELDING -> "Fielding"
    }
}

private fun PlayerStatRange.label(): String {
    return when (this) {
        PlayerStatRange.SEASON -> "Season"
        PlayerStatRange.CAREER -> "Career"
    }
}