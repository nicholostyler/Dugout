package nicholos.tyler.dugout.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.util.Locale
import nicholos.tyler.dugout.model.domain.DivisionStandings
import nicholos.tyler.dugout.model.domain.DivisionTeamStanding
import nicholos.tyler.dugout.model.domain.LeagueStandings
import nicholos.tyler.dugout.model.ui.LeagueUiState
import nicholos.tyler.dugout.ui.theme.DugoutTheme
import nicholos.tyler.dugout.viewmodel.LeagueViewModel

enum class LeagueFilter {
    ALL,
    AMERICAN,
    NATIONAL
}

enum class StandingViewType {
    DIVISIONAL,
    WILD_CARD
}

@Composable
fun LeagueScreen(
    modifier: Modifier = Modifier,
    viewModel: LeagueViewModel,
    onTeamClick: (Int) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LeagueScreen(
        modifier = modifier,
        uiState = uiState,
        onTeamClick = onTeamClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeagueScreen(
    modifier: Modifier = Modifier,
    uiState: LeagueUiState,
    onTeamClick: (Int) -> Unit
) {
    var selectedLeague by rememberSaveable { mutableStateOf(LeagueFilter.ALL) }
    var viewType by rememberSaveable { mutableStateOf(StandingViewType.DIVISIONAL) }

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
            val standings = uiState.standings ?: return

            val visibleDivisions = remember(selectedLeague, viewType, standings) {
                if (viewType == StandingViewType.DIVISIONAL) {
                    standings.divisions.filter { division ->
                        when (selectedLeague) {
                            LeagueFilter.ALL -> true
                            LeagueFilter.AMERICAN -> division.divisionName.contains("American", ignoreCase = true)
                            LeagueFilter.NATIONAL -> division.divisionName.contains("National", ignoreCase = true)
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
                                teams = alTeams.mapIndexed { index, team -> team.copy(rank = index + 1) }
                            )
                        )
                    }
                    if (selectedLeague == LeagueFilter.ALL || selectedLeague == LeagueFilter.NATIONAL) {
                        wildCardDivisions.add(
                            DivisionStandings(
                                divisionId = -2,
                                divisionName = "National League Wild Card",
                                teams = nlTeams.mapIndexed { index, team -> team.copy(rank = index + 1) }
                            )
                        )
                    }
                    wildCardDivisions
                }
            }

            Column(
                modifier = modifier.fillMaxSize()
            ) {
                LeagueFilterSection(
                    selectedLeague = selectedLeague,
                    onLeagueSelected = { selectedLeague = it },
                    selectedViewType = viewType,
                    onViewTypeSelected = { viewType = it }
                )

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 12.dp),
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
                                DivisionStandingsSection(
                                    division = division,
                                    onTeamClick = onTeamClick,
                                    shortenName = true
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
                                DivisionStandingsSection(
                                    division = division,
                                    onTeamClick = onTeamClick,
                                    shortenName = true
                                )
                            }
                        }
                    } else {
                        items(
                            items = visibleDivisions,
                            key = { it.divisionName }
                        ) { division ->
                            DivisionStandingsSection(
                                division = division,
                                onTeamClick = onTeamClick,
                                shortenName = false
                            )
                        }
                    }
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
            .padding(horizontal = 16.dp, vertical = 8.dp)
    )
}



@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun LeagueFilterSection(
    selectedLeague: LeagueFilter,
    onLeagueSelected: (LeagueFilter) -> Unit,
    selectedViewType: StandingViewType,
    onViewTypeSelected: (StandingViewType) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(
                ButtonGroupDefaults.ConnectedSpaceBetween
            )
        ) {
            ToggleButton(
                checked = selectedViewType == StandingViewType.DIVISIONAL,
                onCheckedChange = { onViewTypeSelected(StandingViewType.DIVISIONAL) },
                modifier = Modifier
                    .weight(1f)
                    .semantics { role = Role.RadioButton },
                shapes = ButtonGroupDefaults.connectedLeadingButtonShapes()
            ) {
                Text("Divisional")
            }

            ToggleButton(
                checked = selectedViewType == StandingViewType.WILD_CARD,
                onCheckedChange = { onViewTypeSelected(StandingViewType.WILD_CARD) },
                modifier = Modifier
                    .weight(1f)
                    .semantics { role = Role.RadioButton },
                shapes = ButtonGroupDefaults.connectedTrailingButtonShapes()
            ) {
                Text("Wild Card")
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            LeagueFilter.entries.forEach { league ->
                FilterChip(
                    selected = selectedLeague == league,
                    onClick = { onLeagueSelected(league) },
                    label = {
                        Text(
                            when (league) {
                                LeagueFilter.ALL -> "All"
                                LeagueFilter.AMERICAN -> "American"
                                LeagueFilter.NATIONAL -> "National"
                            }
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun DivisionStandingsSection(
    division: DivisionStandings,
    onTeamClick: (Int) -> Unit,
    shortenName: Boolean = false
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = if (shortenName) shortDivisionName(division.divisionName) else division.divisionName,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        LeagueTableHeader()

        division.teams.forEachIndexed { index, team ->
            TeamStandingRow(
                team = team,
                onClick = { onTeamClick(team.teamId) }
            )

            if (index < division.teams.lastIndex) {
                HorizontalDivider()
            }
        }
    }
}

@Composable
private fun LeagueTableHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("RK", modifier = Modifier.weight(0.8f), fontWeight = FontWeight.SemiBold)
        Text("TEAM", modifier = Modifier.weight(3f), fontWeight = FontWeight.SemiBold)
        Text("W", modifier = Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
        Text("L", modifier = Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
        Text("PCT", modifier = Modifier.weight(1.4f), fontWeight = FontWeight.SemiBold)
        Text("GB", modifier = Modifier.weight(1.2f), fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun TeamStandingRow(
    team: DivisionTeamStanding,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("${team.rank}", modifier = Modifier.weight(0.8f))
        Text(team.teamName, modifier = Modifier.weight(3f))
        Text("${team.wins}", modifier = Modifier.weight(1f))
        Text("${team.losses}", modifier = Modifier.weight(1f))
        Text(
            text = String.format(Locale.US, "%.3f", team.winPct).removePrefix("0"),
            modifier = Modifier.weight(1.4f)
        )
        Text(team.gamesBack, modifier = Modifier.weight(1.2f))
    }
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
            onTeamClick = {}
        )
    }
}

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
