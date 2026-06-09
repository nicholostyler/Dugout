package nicholos.tyler.dugout.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import kotlinx.coroutines.delay
import nicholos.tyler.dugout.model.ui.PlayerSearchResultUiModel
import nicholos.tyler.dugout.model.ui.PlayerSearchUiState
import nicholos.tyler.dugout.ui.theme.DugoutTheme
import nicholos.tyler.dugout.viewmodel.PlayerSearchViewModel

@Composable
fun PlayerSearchScreen(
    viewModel: PlayerSearchViewModel,
    onPlayerClick: (Int) -> Unit,
    onBack: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    PlayerSearchScreenContent(
        uiState = uiState,
        onQueryChanged = viewModel::onQueryChanged,
        onBack = onBack,
        onPlayerClick = onPlayerClick,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun PlayerSearchScreenContent(
    uiState: PlayerSearchUiState,
    onQueryChanged: (String) -> Unit,
    onBack: () -> Unit,
    onPlayerClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .imePadding()
    ) {
        SearchBarDefaults.InputField(
            query = uiState.query,
            onQueryChange = onQueryChanged,
            onSearch = {
                keyboardController?.hide()
                focusManager.clearFocus()
            },
            expanded = true,
            onExpandedChange = { expanded ->
                if (!expanded) {
                    keyboardController?.hide()
                    onBack()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(
                    WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top)
                )
                .focusRequester(focusRequester),
            placeholder = { Text("Search players") },
            leadingIcon = {
                IconButton(
                    onClick = {
                        keyboardController?.hide()
                        onBack()
                    }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            },
            trailingIcon = {
                if (uiState.query.isNotBlank()) {
                    IconButton(onClick = { onQueryChanged("") }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear search"
                        )
                    }
                }
            },
            colors = SearchBarDefaults.inputFieldColors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent
            )
        )

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

        val bottomPadding = WindowInsets.safeDrawing.asPaddingValues().calculateBottomPadding()

        SearchBody(
            uiState = uiState,
            onPlayerClick = { playerId ->
                keyboardController?.hide()
                onBack()
                onPlayerClick(playerId)
            },
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(
                start = 16.dp,
                top = 0.dp,
                end = 16.dp,
                bottom = 16.dp + bottomPadding
            )
        )
    }

    LaunchedEffect(Unit) {
        delay(180)
        focusRequester.requestFocus()
        keyboardController?.show()
    }
}

@Composable
private fun SearchBody(
    uiState: PlayerSearchUiState,
    onPlayerClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues()
) {
    when {
        uiState.isSyncing && uiState.players.isEmpty() -> {
            LoadingState(modifier)
        }

        uiState.players.isEmpty() -> {
            EmptyState(
                query = uiState.query,
                error = uiState.error,
                modifier = modifier
            )
        }

        else -> {
            PlayerSearchResults(
                players = uiState.players,
                query = uiState.query,
                season = uiState.season,
                isSyncing = uiState.isSyncing,
                error = uiState.error,
                onPlayerClick = onPlayerClick,
                modifier = modifier,
                contentPadding = contentPadding
            )
        }
    }
}

@Composable
private fun PlayerSearchResults(
    players: List<PlayerSearchResultUiModel>,
    query: String,
    season: Int,
    isSyncing: Boolean,
    error: String?,
    onPlayerClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(start = 16.dp, top = 4.dp, end = 16.dp, bottom = 16.dp)
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = contentPadding
    ) {
        item {
            SearchStatusRow(
                count = players.size,
                query = query,
                season = season,
                isSyncing = isSyncing,
                error = error
            )
        }

        item {
            PlayerSearchSegment(
                players = players,
                onPlayerClick = onPlayerClick
            )
        }
    }
}

@Composable
private fun SearchStatusRow(
    count: Int,
    query: String,
    season: Int,
    isSyncing: Boolean,
    error: String?,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 12.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = if (query.isBlank()) "$season Players" else "Players",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (query.isNotBlank()) {
                Text(
                    text = "$count result${if (count == 1) "" else "s"}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        if (isSyncing) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                strokeWidth = 2.dp
            )
        }

        error?.let {
            Text(
                text = it,
                modifier = Modifier.padding(start = 12.dp),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.error,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun PlayerSearchSegment(
    players: List<PlayerSearchResultUiModel>,
    onPlayerClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)
    ) {
        players.forEachIndexed { index, player ->
            PlayerSearchResultRow(
                player = player,
                rank = index + 1,
                shape = ListItemDefaults.segmentedShapes(
                    index = index,
                    count = players.size
                ).shape,
                onClick = { onPlayerClick(player.id) }
            )
        }
    }
}

@Composable
private fun PlayerSearchResultRow(
    player: PlayerSearchResultUiModel,
    rank: Int,
    shape: Shape,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val containerColor = if (isDark) {
        MaterialTheme.colorScheme.surfaceContainerHigh
    } else {
        MaterialTheme.colorScheme.surfaceContainer
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = shape,
        color = containerColor,
        tonalElevation = 0.dp,
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
                PlayerRankBadge(rank = rank)
            }

            Box(
                modifier = Modifier.width(52.dp),
                contentAlignment = Alignment.Center
            ) {
                PlayerHeadshot(
                    playerId = player.id,
                    playerName = player.fullName
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp, end = 8.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = player.fullName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = player.teamName ?: "MLB player",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Column(
                modifier = Modifier.width(72.dp),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = player.jerseyNumber?.let { "#$it" } ?: "-",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.End,
                    maxLines = 1
                )

                Text(
                    text = player.position.takeUnless { it == "--" } ?: "POS",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.End,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun PlayerHeadshot(
    playerId: Int,
    playerName: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val imageUrl =
        "https://img.mlbstatic.com/mlb-photos/image/upload/" +
                "d_people:generic:headshot:67:current.png/" +
                "w_213,q_auto:best,f_auto/v1/people/$playerId/headshot/67/current"

    AsyncImage(
        model = ImageRequest.Builder(context)
            .data(imageUrl)
            .crossfade(true)
            .build(),
        contentDescription = "$playerName headshot",
        contentScale = ContentScale.Crop,
        modifier = modifier
            .size(42.dp)
            .background(MaterialTheme.colorScheme.surfaceContainerHighest, CircleShape)
            .clip(CircleShape)
    )
}

@Composable
private fun PlayerRankBadge(
    rank: Int,
    modifier: Modifier = Modifier
) {
    val rankText = rank.takeIf { it < 100 }?.toString() ?: "99+"

    Box(
        modifier = modifier
            .size(34.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.secondaryContainer),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = rankText,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
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
private fun EmptyState(
    query: String,
    error: String?,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = error ?: if (query.isBlank()) {
                "Search cached MLB players"
            } else {
                "No players found"
            },
            modifier = Modifier.padding(24.dp),
            style = MaterialTheme.typography.titleMedium,
            color = if (error == null) {
                MaterialTheme.colorScheme.onSurfaceVariant
            } else {
                MaterialTheme.colorScheme.error
            },
            textAlign = TextAlign.Center
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PlayerSearchScreenPreview() {
    DugoutTheme {
        PlayerSearchScreenContent(
            uiState = PlayerSearchUiState(
                query = "nola",
                season = 2026,
                players = listOf(
                    PlayerSearchResultUiModel(
                        id = 605400,
                        fullName = "Aaron Nola",
                        jerseyNumber = "27",
                        position = "P",
                        teamId = 143,
                        teamName = "Philadelphia Phillies",
                        handedness = "Bats R / Throws R",
                        active = true
                    )
                )
            ),
            onQueryChanged = {},
            onBack = {},
            onPlayerClick = {}
        )
    }
}
