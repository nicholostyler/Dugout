package nicholos.tyler.dugout.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Height
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SportsBaseball
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import nicholos.tyler.dugout.ui.components.TeamLogo
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.graphics.shapes.CornerRounding
import androidx.graphics.shapes.RoundedPolygon
import androidx.graphics.shapes.star
import androidx.graphics.shapes.toPath
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import java.util.Locale
import nicholos.tyler.dugout.model.domain.PlayerStatCategory
import nicholos.tyler.dugout.model.domain.PlayerStatItem
import nicholos.tyler.dugout.model.ui.PlayerCategoryStatsUiModel
import nicholos.tyler.dugout.model.ui.PlayerProfileUiModel
import nicholos.tyler.dugout.model.ui.PlayerQuickStatUiModel
import nicholos.tyler.dugout.model.ui.PlayerSplitStatsUiModel
import nicholos.tyler.dugout.model.ui.PlayerStatRange
import nicholos.tyler.dugout.model.ui.PlayerUiState
import nicholos.tyler.dugout.ui.components.MetaChip
import nicholos.tyler.dugout.ui.components.SnapshotTile
import nicholos.tyler.dugout.ui.components.TitleActionRow
import nicholos.tyler.dugout.ui.theme.DugoutTheme
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreenContent(
    uiState: PlayerUiState,
    modifier: Modifier = Modifier,
    onCategorySelected: (PlayerStatCategory) -> Unit = {},
    onRangeSelected: (PlayerStatRange) -> Unit = {}
) {
    var showStatsSheet by remember { mutableStateOf(false) }
    var showSplitsSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }

            uiState.error != null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = uiState.error,
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
                val selectedSplits = when (uiState.selectedRange) {
                    PlayerStatRange.SEASON -> player.seasonSplits
                    PlayerStatRange.CAREER -> player.careerSplits
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        top = 14.dp,
                        end = 16.dp,
                        bottom = 96.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        PlayerHeroCard(player = player)
                    }

                    if (player.quickStats.isNotEmpty()) {
                        item {
                            TitleActionRow(
                                title = "2026 Season Snapshot",
                                actionText = ""
                            )
                        }
                        item {
                            SeasonSnapshotRow(
                                player = player,
                                selectedCategory = selectedCategory
                            )
                        }
                    }

                    if (categories.isNotEmpty() && selectedCategory != null) {
                        item {
                            CategorySegmentedControl(
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
                            TitleActionRow(
                                title = "${uiState.selectedRange.label()} ${selectedCategory.category.label()}",
                                actionText = "View full stats",
                                onActionClick = { showStatsSheet = true }
                            )
                        }
                        item {
                            PlayerStatsCard(
                                category = selectedCategory,
                                range = uiState.selectedRange,
                                maxItems = 4
                            )
                        }
                    } else {
                        item {
                            NoStatsCard()
                        }
                    }

                    if (selectedSplits.isNotEmpty()) {
                        item {
                            TitleActionRow(
                                title = if (uiState.selectedRange == PlayerStatRange.SEASON) {
                                    "Opponent Splits"
                                } else {
                                    "Career Splits"
                                },
                                actionText = "View all",
                                onActionClick = { showSplitsSheet = true }
                            )
                        }
                        item {
                            SplitsCard(
                                splits = selectedSplits,
                                maxItems = 3
                            )
                        }
                    }
                }

                if (showStatsSheet && selectedCategory != null) {
                    ModalBottomSheet(
                        onDismissRequest = { showStatsSheet = false },
                        sheetState = sheetState,
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .padding(bottom = 48.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "${uiState.selectedRange.label()} ${selectedCategory.category.label()}",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            StatGrid(stats = selectedCategory.displayStats(uiState.selectedRange))
                        }
                    }
                }

                if (showSplitsSheet) {
                    ModalBottomSheet(
                        onDismissRequest = { showSplitsSheet = false },
                        sheetState = sheetState,
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .padding(bottom = 48.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = if (uiState.selectedRange == PlayerStatRange.SEASON) {
                                    "Opponent Splits"
                                } else {
                                    "Career Splits"
                                },
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(selectedSplits) { split ->
                                    SplitRow(split = split)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PlayerHeroCard(
    player: PlayerProfileUiModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val imageUrl =
        "https://img.mlbstatic.com/mlb-photos/image/upload/" +
                "w_426,q_100,f_jpg/v1/people/${player.id}/headshot/67/current"

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val cloverPolygon = remember {
                    RoundedPolygon.star(
                        numVerticesPerRadius = 12,
                        innerRadius = 0.92f,
                        rounding = CornerRounding(0.15f)
                    )
                }
                val cloverShape = remember(cloverPolygon) {
                    CloverShape(cloverPolygon)
                }

                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.72f),
                            cloverShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(imageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = "${player.fullName} headshot",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(54.dp)
                            .clip(cloverShape)
                            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                    )
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        player.jerseyNumber?.let {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.72f)
                            ) {
                                Text(
                                    text = "#$it",
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }

                        player.teamId?.let {
                            TeamLogo(
                                teamId = it,
                                teamName = player.teamName ?: "",
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.72f)
                        ) {
                            Text(
                                text = player.position,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Text(
                        text = player.fullName,
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.ExtraBold,
                        lineHeight = 28.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    player.teamName?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                player.age?.let {
                    MetaChip(
                        label = "Age $it",
                        icon = Icons.Default.CalendarMonth
                    )
                }
                player.height?.let {
                    MetaChip(
                        label = it,
                        icon = Icons.Default.Height
                    )
                }
                player.weight?.let {
                    MetaChip(
                        label = "$it lb",
                        icon = Icons.Default.MonitorWeight
                    )
                }
            }
        }
    }
}



@Composable
private fun SeasonSnapshotRow(
    player: PlayerProfileUiModel,
    selectedCategory: PlayerCategoryStatsUiModel?,
    modifier: Modifier = Modifier
) {
    val stats = remember(player.quickStats, selectedCategory) {
        buildSnapshotStats(player.quickStats, selectedCategory)
    }

    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 0.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(stats) { stat ->
            SnapshotTile(stat = stat)
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun CategorySegmentedControl(
    categories: List<PlayerCategoryStatsUiModel>,
    selectedCategory: PlayerStatCategory,
    onCategorySelected: (PlayerStatCategory) -> Unit,
    modifier: Modifier = Modifier
) {
    val orderedCategories = remember(categories) {
        val available = categories.map { it.category }.distinct()
        PlayerStatCategory.entries.filter { it in available }
    }

    ButtonGroup(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp),
        horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
        expandedRatio = 0f,
        overflowIndicator = {}
    ) {
        orderedCategories.forEach { category ->
            toggleableItem(
                checked = selectedCategory == category,
                onCheckedChange = { if (it) onCategorySelected(category) },
                icon = {
                    Icon(
                        imageVector = category.icon(),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                },
                label = category.label(),
                weight = 1f
            )
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
                leadingIcon = if (selectedRange == range) {
                    {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(FilterChipDefaults.IconSize)
                        )
                    }
                } else {
                    null
                }
            )
        }
    }
}

@Composable
private fun PlayerStatsCard(
    category: PlayerCategoryStatsUiModel,
    range: PlayerStatRange,
    modifier: Modifier = Modifier,
    maxItems: Int? = null
) {
    val stats = remember(category, range) {
        val all = category.displayStats(range)
        if (maxItems != null) all.take(maxItems) else all
    }

    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            if (stats.isEmpty()) {
                Text(
                    text = "No stats available",
                    modifier = Modifier.padding(vertical = 4.dp),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                return@Column
            }

            StatGrid(stats = stats)
        }
    }
}

@Composable
private fun StatGrid(
    stats: List<PlayerStatItem>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        stats.chunked(4).forEachIndexed { rowIndex, rowStats ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                rowStats.forEachIndexed { index, stat ->
                    StatGridCell(
                        stat = stat,
                        modifier = Modifier.weight(1f),
                        isPrimary = rowIndex == 0
                    )
                    if (index < rowStats.lastIndex) {
                        Box(
                            modifier = Modifier
                                .height(50.dp)
                                .width(1.dp)
                                .background(MaterialTheme.colorScheme.outlineVariant)
                        )
                    }
                }

                repeat(4 - rowStats.size) {
                    Box(modifier = Modifier.weight(1f))
                }
            }

            if (rowIndex < stats.chunked(4).lastIndex) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            }
        }
    }
}

@Composable
private fun StatGridCell(
    stat: PlayerStatItem,
    isPrimary: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = stat.label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1
        )
        Text(
            text = stat.value,
            style = MaterialTheme.typography.headlineSmall,
            color = if (isPrimary && stat.label in listOf("ERA", "AVG")) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurface
            },
            fontWeight = FontWeight.ExtraBold,
            maxLines = 1
        )
    }
}

@Composable
private fun SplitsCard(
    splits: List<PlayerSplitStatsUiModel>,
    modifier: Modifier = Modifier,
    maxItems: Int? = null
) {
    val displaySplits = remember(splits, maxItems) {
        if (maxItems != null) splits.take(maxItems) else splits
    }

    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            displaySplits.forEach { split ->
                SplitRow(split = split)
            }
        }
    }
}

@Composable
private fun SplitRow(
    split: PlayerSplitStatsUiModel,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.44f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TeamLogo(
                teamId = split.teamId,
                teamName = split.title.replace("vs ", ""),
                modifier = Modifier.size(36.dp)
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = split.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                split.subtitle?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                split.stats.take(3).forEach { stat ->
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = stat.label,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = stat.value,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.ExtraBold
                        )
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
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Stats",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = "No stats available",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun buildSnapshotStats(
    quickStats: List<PlayerQuickStatUiModel>,
    selectedCategory: PlayerCategoryStatsUiModel?
): List<PlayerStatItem> {
    if (selectedCategory?.category == PlayerStatCategory.PITCHING) {
        val seasonPrimary = selectedCategory.seasonPrimary
        val seasonSecondary = selectedCategory.seasonSecondary
        val wins = seasonPrimary.valueFor("W")
        val losses = seasonPrimary.valueFor("L")

        return listOfNotNull(
            seasonPrimary.itemFor("ERA") ?: quickStats.quickItemFor("ERA"),
            seasonPrimary.itemFor("WHIP") ?: quickStats.quickItemFor("WHIP"),
            if (wins != null || losses != null) {
                PlayerStatItem("W-L", "${wins ?: "-"}-${losses ?: "-"}")
            } else {
                null
            },
            seasonSecondary.itemFor("SO"),
            seasonSecondary.itemFor("IP")
        )
    }

    val categoryStats = selectedCategory?.seasonPrimary.orEmpty()
    return listOfNotNull(
        categoryStats.itemFor("AVG") ?: quickStats.quickItemFor("AVG"),
        categoryStats.itemFor("OPS") ?: quickStats.quickItemFor("OPS"),
        categoryStats.itemFor("HR") ?: quickStats.quickItemFor("HR"),
        categoryStats.itemFor("RBI") ?: quickStats.quickItemFor("RBI")
    ).ifEmpty {
        quickStats.map { PlayerStatItem(it.label, it.value) }
    }
}

private fun PlayerCategoryStatsUiModel.displayStats(range: PlayerStatRange): List<PlayerStatItem> {
    val primary = when (range) {
        PlayerStatRange.SEASON -> seasonPrimary
        PlayerStatRange.CAREER -> careerPrimary
    }
    val secondary = when (range) {
        PlayerStatRange.SEASON -> seasonSecondary
        PlayerStatRange.CAREER -> careerSecondary
    }

    if (primary.isEmpty() && secondary.isEmpty()) return emptyList()

    return when (category) {
        PlayerStatCategory.PITCHING -> {
            val wins = primary.valueFor("W")
            val losses = primary.valueFor("L")
            val strikeouts = secondary.valueFor("SO")
            val innings = secondary.valueFor("IP")

            listOfNotNull(
                primary.itemFor("ERA"),
                primary.itemFor("WHIP"),
                if (wins != null || losses != null) {
                    PlayerStatItem("W-L", "${wins ?: "-"}-${losses ?: "-"}")
                } else {
                    null
                },
                secondary.itemFor("IP"),
                secondary.itemFor("SO"),
                secondary.itemFor("BB"),
                secondary.itemFor("SV"),
                calculateKPerNine(strikeouts, innings)?.let { PlayerStatItem("K/9", it) }
            )
        }

        PlayerStatCategory.BATTING,
        PlayerStatCategory.FIELDING -> primary + secondary
    }
}

private fun List<PlayerStatItem>.itemFor(label: String): PlayerStatItem? {
    return firstOrNull { it.label.equals(label, ignoreCase = true) }
}

private fun List<PlayerQuickStatUiModel>.quickItemFor(label: String): PlayerStatItem? {
    return firstOrNull { it.label.equals(label, ignoreCase = true) }
        ?.let { PlayerStatItem(it.label, it.value) }
}

private fun List<PlayerStatItem>.valueFor(label: String): String? {
    return itemFor(label)?.value?.takeUnless { it == "--" }
}

private fun calculateKPerNine(strikeouts: String?, inningsPitched: String?): String? {
    val strikeoutCount = strikeouts?.toDoubleOrNull() ?: return null
    val outs = inningsPitched.toOuts()
    if (outs <= 0) return null
    return String.format(Locale.US, "%.1f", strikeoutCount * 27.0 / outs)
}

private fun String?.toOuts(): Int {
    if (this == null) return 0
    val parts = split(".")
    val innings = parts.getOrNull(0)?.toIntOrNull() ?: 0
    val partialOuts = parts.getOrNull(1)?.toIntOrNull()?.coerceIn(0, 2) ?: 0
    return innings * 3 + partialOuts
}

private fun PlayerProfileUiModel.defaultStatCategory(): PlayerStatCategory {
    val normalized = position.trim().lowercase(Locale.US)

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

private fun PlayerStatCategory.icon(): ImageVector {
    return when (this) {
        PlayerStatCategory.BATTING -> Icons.Default.SportsBaseball
        PlayerStatCategory.PITCHING -> Icons.Default.SportsBaseball
        PlayerStatCategory.FIELDING -> Icons.Default.Person
    }
}

private fun PlayerStatRange.label(): String {
    return when (this) {
        PlayerStatRange.SEASON -> "Season"
        PlayerStatRange.CAREER -> "Career"
    }
}

@Preview(showBackground = true)
@Composable
fun PlayerScreenContentPreview() {
    DugoutTheme {
        PlayerScreenContent(
            uiState = samplePlayerUiState
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PlayerScreenContentLoadingPreview() {
    DugoutTheme {
        PlayerScreenContent(
            uiState = PlayerUiState(isLoading = true)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PlayerScreenContentErrorPreview() {
    DugoutTheme {
        PlayerScreenContent(
            uiState = PlayerUiState(error = "Unable to load player details")
        )
    }
}

private class CloverShape(private val polygon: RoundedPolygon) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val path = polygon.toPath().asComposePath()
        val matrix = Matrix()
        matrix.scale(size.width / 2f, size.height / 2f)
        matrix.translate(1f, 1f)
        path.transform(matrix)
        return Outline.Generic(path)
    }
}

private val samplePlayer = PlayerProfileUiModel(
    id = 605400,
    fullName = "Aaron Nola",
    jerseyNumber = "27",
    position = "P",
    teamId = 143,
    teamName = "Philadelphia Phillies",
    age = 32,
    height = "6' 2\"",
    weight = 200,
    bats = "R",
    throwsHand = "R",
    quickStats = listOf(
        PlayerQuickStatUiModel("ERA", "5.72"),
        PlayerQuickStatUiModel("WHIP", "1.45"),
        PlayerQuickStatUiModel("W", "3"),
        PlayerQuickStatUiModel("L", "4")
    ),
    categories = listOf(
        PlayerCategoryStatsUiModel(
            category = PlayerStatCategory.PITCHING,
            seasonPrimary = listOf(
                PlayerStatItem("ERA", "5.72"),
                PlayerStatItem("WHIP", "1.45"),
                PlayerStatItem("W", "3"),
                PlayerStatItem("L", "4")
            ),
            seasonSecondary = listOf(
                PlayerStatItem("IP", "60.1"),
                PlayerStatItem("SO", "52"),
                PlayerStatItem("BB", "18"),
                PlayerStatItem("SV", "0")
            ),
            careerPrimary = listOf(
                PlayerStatItem("ERA", "3.89"),
                PlayerStatItem("WHIP", "1.16"),
                PlayerStatItem("W", "112"),
                PlayerStatItem("L", "93")
            ),
            careerSecondary = listOf(
                PlayerStatItem("IP", "1772.1"),
                PlayerStatItem("SO", "1932"),
                PlayerStatItem("BB", "467"),
                PlayerStatItem("SV", "0")
            )
        ),
        PlayerCategoryStatsUiModel(
            category = PlayerStatCategory.FIELDING,
            seasonPrimary = listOf(
                PlayerStatItem("FLD%", ".957"),
                PlayerStatItem("E", "1"),
                PlayerStatItem("A", "10"),
                PlayerStatItem("PO", "12")
            ),
            seasonSecondary = listOf(
                PlayerStatItem("G", "12"),
                PlayerStatItem("GS", "12"),
                PlayerStatItem("TC", "23"),
                PlayerStatItem("DP", "1")
            ),
            careerPrimary = listOf(
                PlayerStatItem("FLD%", ".962"),
                PlayerStatItem("E", "17"),
                PlayerStatItem("A", "243"),
                PlayerStatItem("PO", "163")
            ),
            careerSecondary = listOf(
                PlayerStatItem("G", "296"),
                PlayerStatItem("GS", "296"),
                PlayerStatItem("TC", "423"),
                PlayerStatItem("DP", "21")
            )
        )
    ),
    seasonSplits = listOf(
        PlayerSplitStatsUiModel(
            title = "vs Atlanta Braves",
            subtitle = "3 G",
            teamId = 144,
            stats = listOf(
                PlayerStatItem("ERA", "2.45"),
                PlayerStatItem("IP", "18.1"),
                PlayerStatItem("SO", "21")
            )
        ),
        PlayerSplitStatsUiModel(
            title = "vs New York Mets",
            subtitle = "2 G",
            teamId = 121,
            stats = listOf(
                PlayerStatItem("ERA", "4.22"),
                PlayerStatItem("IP", "10.2"),
                PlayerStatItem("SO", "9")
            )
        )
    ),
    careerSplits = emptyList()
)

private val samplePlayerUiState = PlayerUiState(
    player = samplePlayer,
    isLoading = false,
    selectedCategory = PlayerStatCategory.PITCHING,
    selectedRange = PlayerStatRange.CAREER
)
