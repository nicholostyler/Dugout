package nicholos.tyler.dugout.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.time.LocalDate
import java.util.Locale
import nicholos.tyler.dugout.model.domain.DivisionStandings
import nicholos.tyler.dugout.model.domain.DivisionTeamStanding
import nicholos.tyler.dugout.model.domain.LeagueLeader
import nicholos.tyler.dugout.model.domain.LeagueLeaderGroup
import nicholos.tyler.dugout.model.domain.LeagueStandings
import nicholos.tyler.dugout.model.ui.LeagueLeadersUiState
import nicholos.tyler.dugout.model.ui.LeagueUiState
import nicholos.tyler.dugout.ui.components.DivisionStandingUiModel
import nicholos.tyler.dugout.ui.components.DivisionStandingsCard
import nicholos.tyler.dugout.ui.theme.DugoutTheme
import nicholos.tyler.dugout.viewmodel.LeagueLeadersViewModel
import nicholos.tyler.dugout.viewmodel.LeagueViewModel

enum class LeagueTab {
    STANDINGS,
    STATS
}

enum class LeagueFilter {
    ALL,
    AMERICAN,
    NATIONAL
}

enum class StandingViewType {
    DIVISIONAL,
    WILD_CARD
}

enum class StatsScope {
    PLAYER,
    TEAM
}

enum class StatsGroup {
    HITTING,
    PITCHING
}

@Composable
fun LeagueScreen(
    modifier: Modifier = Modifier,
    viewModel: LeagueViewModel,
    statsViewModel: LeagueLeadersViewModel,
    onTeamClick: (Int) -> Unit,
    onPlayerClick: (Int) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val statsUiState by statsViewModel.uiState.collectAsStateWithLifecycle()

    LeagueScreen(
        modifier = modifier,
        uiState = uiState,
        statsUiState = statsUiState,
        onTeamClick = onTeamClick,
        onPlayerClick = onPlayerClick,
        onLoadStats = { categories, group, season, leagueId, statType ->
            statsViewModel.loadLeaders(categories, group, season, leagueId, statType)
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeagueScreen(
    modifier: Modifier = Modifier,
    uiState: LeagueUiState,
    statsUiState: LeagueLeadersUiState,
    onTeamClick: (Int) -> Unit,
    onPlayerClick: (Int) -> Unit = {},
    onLoadStats: (categories: List<String>, group: String, season: Int, leagueId: String?, statType: String?) -> Unit = { _, _, _, _, _ -> },
    initialTab: LeagueTab = LeagueTab.STANDINGS
) {
    var selectedTab by rememberSaveable { mutableStateOf(initialTab) }
    var selectedLeague by rememberSaveable { mutableStateOf(LeagueFilter.ALL) }
    var viewType by rememberSaveable { mutableStateOf(StandingViewType.DIVISIONAL) }

    // Stats filters
    var statsScope by rememberSaveable { mutableStateOf(StatsScope.PLAYER) }
    var statsGroup by rememberSaveable { mutableStateOf(StatsGroup.HITTING) }
    var statsLeague by rememberSaveable { mutableStateOf(LeagueFilter.ALL) }
    var statsYear by rememberSaveable { mutableIntStateOf(LocalDate.now().year) }
    var statsCategory by rememberSaveable { mutableStateOf("homeRuns") }

    LaunchedEffect(selectedTab, statsScope, statsGroup, statsLeague, statsYear, statsCategory) {
        if (selectedTab == LeagueTab.STATS) {
            val leagueId = when (statsLeague) {
                LeagueFilter.ALL -> null
                LeagueFilter.AMERICAN -> "103"
                LeagueFilter.NATIONAL -> "104"
            }
            val statType = when (statsScope) {
                StatsScope.PLAYER -> "season"
                StatsScope.TEAM -> "seasonTeam"
            }
            onLoadStats(
                listOf(statsCategory),
                statsGroup.name.lowercase(),
                statsYear,
                leagueId,
                statType
            )
        }
    }

    when {
        uiState.isLoading -> {
            Column(
                modifier = modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
            }
        }

        uiState.error != null -> {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = uiState.error,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        else -> {
            Column(
                modifier = modifier.fillMaxSize()
            ) {
                LeagueFilterSection(
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it },
                    selectedLeague = selectedLeague,
                    onLeagueSelected = { selectedLeague = it },
                    selectedViewType = viewType,
                    onViewTypeSelected = { viewType = it },
                    statsScope = statsScope,
                    onStatsScopeSelected = { statsScope = it },
                    statsGroup = statsGroup,
                    onStatsGroupSelected = { statsGroup = it },
                    statsLeague = statsLeague,
                    onStatsLeagueSelected = { statsLeague = it },
                    statsYear = statsYear,
                    onStatsYearSelected = { statsYear = it },
                    statsCategory = statsCategory,
                    onStatsCategorySelected = { statsCategory = it }
                )

                if (selectedTab == LeagueTab.STANDINGS) {
                    val standings = uiState.standings ?: return@Column

                    val visibleDivisions = remember(selectedLeague, viewType, standings) {
                        if (viewType == StandingViewType.DIVISIONAL) {
                            standings.divisions.filter { division ->
                                when (selectedLeague) {
                                    LeagueFilter.ALL -> true
                                    LeagueFilter.AMERICAN -> division.divisionName.contains(
                                        "American",
                                        ignoreCase = true
                                    )

                                    LeagueFilter.NATIONAL -> division.divisionName.contains(
                                        "National",
                                        ignoreCase = true
                                    )
                                }
                            }
                        } else {
                            // Wild Card view
                            val alTeams = standings.divisions
                                .filter { it.divisionName.contains("American", ignoreCase = true) }
                                .flatMap { it.teams }
                                .sortedByDescending { it.winPct }

                            val nlTeams = standings.divisions
                                .filter { it.divisionName.contains("National", ignoreCase = true) }
                                .flatMap { it.teams }
                                .sortedByDescending { it.winPct }

                            val wildCardDivisions = mutableListOf<DivisionStandings>()

                            if (selectedLeague == LeagueFilter.ALL || selectedLeague == LeagueFilter.AMERICAN) {
                                wildCardDivisions.add(
                                    DivisionStandings(
                                        divisionId = -1,
                                        divisionName = "American League Wild Card",
                                        teams = alTeams.mapIndexed { index, team ->
                                            team.copy(rank = index + 1)
                                        }
                                    )
                                )
                            }
                            if (selectedLeague == LeagueFilter.ALL || selectedLeague == LeagueFilter.NATIONAL) {
                                wildCardDivisions.add(
                                    DivisionStandings(
                                        divisionId = -2,
                                        divisionName = "National League Wild Card",
                                        teams = nlTeams.mapIndexed { index, team ->
                                            team.copy(rank = index + 1)
                                        }
                                    )
                                )
                            }
                            wildCardDivisions
                        }
                    }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 12.dp, start = 12.dp, end = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (selectedLeague == LeagueFilter.ALL) {
                            val alSections = visibleDivisions.filter {
                                it.divisionName.contains("American", ignoreCase = true)
                            }
                            if (alSections.isNotEmpty()) {
                                item(key = "AL_HEADER_ALL") {
                                    LeagueHeader(text = "American League")
                                }
                                items(
                                    items = alSections,
                                    key = { "AL_${it.divisionName}" }
                                ) { division ->
                                    DivisionStandingsCard(
                                        title = shortDivisionName(division.divisionName),
                                        standings = division.teams.map { it.toDivisionStandingUiModel() },
                                        onTeamClick = { team -> onTeamClick(team.teamId) }
                                    )
                                }
                            }

                            val nlSections = visibleDivisions.filter {
                                it.divisionName.contains("National", ignoreCase = true)
                            }
                            if (nlSections.isNotEmpty()) {
                                item(key = "NL_HEADER_ALL") {
                                    LeagueHeader(text = "National League")
                                }
                                items(
                                    items = nlSections,
                                    key = { "NL_${it.divisionName}" }
                                ) { division ->
                                    DivisionStandingsCard(
                                        title = shortDivisionName(division.divisionName),
                                        standings = division.teams.map { it.toDivisionStandingUiModel() },
                                        onTeamClick = { team -> onTeamClick(team.teamId) }
                                    )
                                }
                            }
                        } else {
                            items(
                                items = visibleDivisions,
                                key = { it.divisionName }
                            ) { division ->
                                DivisionStandingsCard(
                                    title = division.divisionName,
                                    standings = division.teams.map { it.toDivisionStandingUiModel() },
                                    onTeamClick = { team -> onTeamClick(team.teamId) }
                                )
                            }
                        }
                    }
                } else {
                    LeagueLeadersScreenContent(
                        uiState = statsUiState,
                        modifier = Modifier.fillMaxSize(),
                        onLeaderClick = { id, isPlayer ->
                            if (isPlayer) {
                                onPlayerClick(id)
                            } else {
                                onTeamClick(id)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun LeagueHeader(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp)
    )
}



@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun LeagueFilterSection(
    selectedTab: LeagueTab,
    onTabSelected: (LeagueTab) -> Unit,
    selectedLeague: LeagueFilter,
    onLeagueSelected: (LeagueFilter) -> Unit,
    selectedViewType: StandingViewType,
    onViewTypeSelected: (StandingViewType) -> Unit,
    statsScope: StatsScope,
    onStatsScopeSelected: (StatsScope) -> Unit,
    statsGroup: StatsGroup,
    onStatsGroupSelected: (StatsGroup) -> Unit,
    statsLeague: LeagueFilter,
    onStatsLeagueSelected: (LeagueFilter) -> Unit,
    statsYear: Int,
    onStatsYearSelected: (Int) -> Unit,
    statsCategory: String,
    onStatsCategorySelected: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        ButtonGroup(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            overflowIndicator = {},
            expandedRatio = 0f,
            horizontalArrangement = Arrangement.spacedBy(
                ButtonGroupDefaults.ConnectedSpaceBetween
            )
        ) {
            toggleableItem(
                checked = selectedTab == LeagueTab.STANDINGS,
                label = "Standings",
                onCheckedChange = { checked ->
                    if (checked) onTabSelected(LeagueTab.STANDINGS)
                },
                weight = 1f
            )

            toggleableItem(
                checked = selectedTab == LeagueTab.STATS,
                label = "Stats",
                onCheckedChange = { checked ->
                    if (checked) onTabSelected(LeagueTab.STATS)
                },
                weight = 1f
            )
        }

        if (selectedTab == LeagueTab.STANDINGS) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                var showViewTypeMenu by remember { mutableStateOf(false) }
                Box {
                    FilterChip(
                        selected = true,
                        onClick = { showViewTypeMenu = true },
                        label = {
                            Text(
                                when (selectedViewType) {
                                    StandingViewType.DIVISIONAL -> "Divisional"
                                    StandingViewType.WILD_CARD -> "Wild Card"
                                }
                            )
                        },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null
                            )
                        }
                    )
                    DropdownMenu(
                        expanded = showViewTypeMenu,
                        onDismissRequest = { showViewTypeMenu = false }
                    ) {
                        StandingViewType.entries.forEach { type ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        when (type) {
                                            StandingViewType.DIVISIONAL -> "Divisional"
                                            StandingViewType.WILD_CARD -> "Wild Card"
                                        }
                                    )
                                },
                                onClick = {
                                    onViewTypeSelected(type)
                                    showViewTypeMenu = false
                                }
                            )
                        }
                    }
                }

                var showLeagueMenu by remember { mutableStateOf(false) }
                Box {
                    FilterChip(
                        selected = true,
                        onClick = { showLeagueMenu = true },
                        label = {
                            Text(
                                when (selectedLeague) {
                                    LeagueFilter.ALL -> "All"
                                    LeagueFilter.AMERICAN -> "American"
                                    LeagueFilter.NATIONAL -> "National"
                                }
                            )
                        },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null
                            )
                        }
                    )
                    DropdownMenu(
                        expanded = showLeagueMenu,
                        onDismissRequest = { showLeagueMenu = false }
                    ) {
                        LeagueFilter.entries.forEach { league ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        when (league) {
                                            LeagueFilter.ALL -> "All"
                                            LeagueFilter.AMERICAN -> "American"
                                            LeagueFilter.NATIONAL -> "National"
                                        }
                                    )
                                },
                                onClick = {
                                    onLeagueSelected(league)
                                    showLeagueMenu = false
                                }
                            )
                        }
                    }
                }
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Group Chip (Hitting/Pitching)
                var showGroupMenu by remember { mutableStateOf(false) }
                Box {
                    FilterChip(
                        selected = true,
                        onClick = { showGroupMenu = true },
                        label = {
                            Text(
                                when (statsGroup) {
                                    StatsGroup.HITTING -> "Hitting"
                                    StatsGroup.PITCHING -> "Pitching"
                                }
                            )
                        },
                        trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) }
                    )
                    DropdownMenu(expanded = showGroupMenu, onDismissRequest = { showGroupMenu = false }) {
                        StatsGroup.entries.forEach { group ->
                            DropdownMenuItem(
                                text = { Text(group.name.lowercase().replaceFirstChar { it.uppercase() }) },
                                onClick = { onStatsGroupSelected(group); showGroupMenu = false }
                            )
                        }
                    }
                }
                // Category Chip
                var showCategoryMenu by remember { mutableStateOf(false) }
                val categories = if (statsGroup == StatsGroup.HITTING) {
                    listOf(
                        "homeRuns" to "HR",
                        "runsBattedIn" to "RBI",
                        "battingAverage" to "AVG",
                        "onBasePlusSlugging" to "OPS",
                        "onBasePercentage" to "OBP",
                        "sluggingPercentage" to "SLG",
                        "hits" to "H",
                        "runs" to "R",
                        "stolenBases" to "SB"
                    )
                } else {
                    listOf(
                        "earnedRunAverage" to "ERA",
                        "wins" to "W",
                        "losses" to "L",
                        "strikeOuts" to "SO",
                        "saves" to "SV",
                        "walksAndHitsPerInningPitched" to "WHIP",
                        "inningsPitched" to "IP"
                    )
                }
                Box {
                    FilterChip(
                        selected = true,
                        onClick = { showCategoryMenu = true },
                        label = { Text(categories.find { it.first == statsCategory }?.second ?: statsCategory) },
                        trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) }
                    )
                    DropdownMenu(expanded = showCategoryMenu, onDismissRequest = { showCategoryMenu = false }) {
                        categories.forEach { (id, label) ->
                            DropdownMenuItem(
                                text = { Text(label) },
                                onClick = { onStatsCategorySelected(id); showCategoryMenu = false }
                            )
                        }
                    }
                }
                // Year Chip
                var showYearMenu by remember { mutableStateOf(false) }
                Box {
                    FilterChip(
                        selected = true,
                        onClick = { showYearMenu = true },
                        label = { Text("$statsYear") },
                        trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) }
                    )
                    DropdownMenu(expanded = showYearMenu, onDismissRequest = { showYearMenu = false }) {
                        val currentYear = LocalDate.now().year
                        (currentYear downTo currentYear - 3).forEach { year ->
                            DropdownMenuItem(
                                text = { Text("$year") },
                                onClick = { onStatsYearSelected(year); showYearMenu = false }
                            )
                        }
                    }
                }
                // Scope Chip (Player/Team)
                var showScopeMenu by remember { mutableStateOf(false) }
                Box {
                    FilterChip(
                        selected = true,
                        onClick = { showScopeMenu = true },
                        label = {
                            Text(
                                when (statsScope) {
                                    StatsScope.PLAYER -> "Player"
                                    StatsScope.TEAM -> "Team"
                                }
                            )
                        },
                        trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) }
                    )
                    DropdownMenu(expanded = showScopeMenu, onDismissRequest = { showScopeMenu = false }) {
                        StatsScope.entries.forEach { scope ->
                            DropdownMenuItem(
                                text = { Text(scope.name.lowercase().replaceFirstChar { it.uppercase() }) },
                                onClick = { onStatsScopeSelected(scope); showScopeMenu = false }
                            )
                        }
                    }
                }



                // League Chip (All/American/National)
                var showLeagueMenu by remember { mutableStateOf(false) }
                Box {
                    FilterChip(
                        selected = true,
                        onClick = { showLeagueMenu = true },
                        label = {
                            Text(
                                when (statsLeague) {
                                    LeagueFilter.ALL -> "All"
                                    LeagueFilter.AMERICAN -> "American"
                                    LeagueFilter.NATIONAL -> "National"
                                }
                            )
                        },
                        trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) }
                    )
                    DropdownMenu(expanded = showLeagueMenu, onDismissRequest = { showLeagueMenu = false }) {
                        LeagueFilter.entries.forEach { league ->
                            DropdownMenuItem(
                                text = { Text(league.name.lowercase().replaceFirstChar { it.uppercase() }) },
                                onClick = { onStatsLeagueSelected(league); showLeagueMenu = false }
                            )
                        }
                    }
                }




            }
        }
    }
}

private fun DivisionTeamStanding.toDivisionStandingUiModel(): DivisionStandingUiModel {
    return DivisionStandingUiModel(
        rank = rank,
        teamId = teamId,
        teamAbbreviation = teamName.take(3).uppercase(Locale.US),
        teamName = teamName,
        wins = wins,
        losses = losses,
        gamesBack = gamesBack,
        winPct = winPct,
        isSelectedTeam = teamId == 143 || teamName.contains("Phillies", ignoreCase = true)
    )
}

private fun shortDivisionName(fullName: String): String {
    return fullName
        .replace("American League", "", ignoreCase = true)
        .replace("National League", "", ignoreCase = true)
        .replace("American", "", ignoreCase = true)
        .replace("National", "", ignoreCase = true)
        .trim()
}

@Preview(showBackground = true)
@Composable
fun LeagueScreenPreview() {
    DugoutTheme {
        LeagueScreen(
            uiState = LeagueUiState(
                isLoading = false,
                standings = sampleLeagueStandings
            ),
            statsUiState = LeagueLeadersUiState(),
            onTeamClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun LeagueScreenLoadingPreview() {
    DugoutTheme {
        LeagueScreen(
            uiState = LeagueUiState(isLoading = true),
            statsUiState = LeagueLeadersUiState(),
            onTeamClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun LeagueScreenErrorPreview() {
    DugoutTheme {
        LeagueScreen(
            uiState = LeagueUiState(
                isLoading = false,
                error = "Unable to load standings. Please try again later."
            ),
            statsUiState = LeagueLeadersUiState(),
            onTeamClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun LeagueScreenStatsPreview() {
    DugoutTheme {
        LeagueScreen(
            uiState = LeagueUiState(
                isLoading = false,
                standings = sampleLeagueStandings
            ),
            statsUiState = sampleStatsUiState,
            onTeamClick = {},
            initialTab = LeagueTab.STATS
        )
    }
}

private val sampleStatsUiState = LeagueLeadersUiState(
    isLoading = false,
    stats = listOf(
        LeagueLeaderGroup(
            categoryId = "homeRuns",
            categoryName = "HOME RUNS",
            leaders = listOf(
                LeagueLeader(
                    rank = 1,
                    id = 1,
                    name = "Aaron Judge",
                    teamName = "New York Yankees",
                    statValue = "58",
                    isPlayer = true
                ),
                LeagueLeader(
                    rank = 2,
                    id = 2,
                    name = "Shohei Ohtani",
                    teamName = "Los Angeles Dodgers",
                    statValue = "54",
                    isPlayer = true
                ),
                LeagueLeader(
                    rank = 3,
                    id = 3,
                    name = "Juan Soto",
                    teamName = "New York Yankees",
                    statValue = "41",
                    isPlayer = true
                )
            )
        )
    )
)

private val sampleLeagueStandings = LeagueStandings(
    divisions = listOf(
        DivisionStandings(
            divisionName = "American League East",
            teams = listOf(
                DivisionTeamStanding(
                    rank = 1,
                    teamId = 1,
                    teamName = "New York Yankees",
                    wins = 94,
                    losses = 68,
                    winPct = 0.580f,
                    gamesBack = "-"
                ),
                DivisionTeamStanding(
                    rank = 2,
                    teamId = 2,
                    teamName = "Baltimore Orioles",
                    wins = 91,
                    losses = 71,
                    winPct = 0.562f,
                    gamesBack = "3.0"
                ),
                DivisionTeamStanding(
                    rank = 3,
                    teamId = 3,
                    teamName = "Boston Red Sox",
                    wins = 81,
                    losses = 81,
                    winPct = 0.500f,
                    gamesBack = "13.0"
                ),
                DivisionTeamStanding(
                    rank = 4,
                    teamId = 4,
                    teamName = "Tampa Bay Rays",
                    wins = 80,
                    losses = 82,
                    winPct = 0.494f,
                    gamesBack = "14.0"
                ),
                DivisionTeamStanding(
                    rank = 5,
                    teamId = 5,
                    teamName = "Toronto Blue Jays",
                    wins = 74,
                    losses = 88,
                    winPct = 0.457f,
                    gamesBack = "20.0"
                )
            ),
            divisionId = 103
        ),
        DivisionStandings(
            divisionName = "National League West",
            teams = listOf(
                DivisionTeamStanding(
                    rank = 1,
                    teamId = 6,
                    teamName = "Los Angeles Dodgers",
                    wins = 98,
                    losses = 64,
                    winPct = 0.605f,
                    gamesBack = "-"
                ),
                DivisionTeamStanding(
                    rank = 2,
                    teamId = 7,
                    teamName = "San Diego Padres",
                    wins = 93,
                    losses = 69,
                    winPct = 0.574f,
                    gamesBack = "5.0"
                )
            ),
            divisionId = 104
        )
    )
)
