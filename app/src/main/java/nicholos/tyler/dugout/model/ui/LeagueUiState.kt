package nicholos.tyler.dugout.model.ui

import nicholos.tyler.dugout.model.domain.LeagueStandings

data class LeagueUiState(
    val isLoading: Boolean = true,
    val standings: LeagueStandings? = null,
    val error: String? = null
)