package nicholos.tyler.dugout.model.ui

import nicholos.tyler.dugout.model.domain.LeagueLeaderGroup

data class LeagueLeadersUiState(
    val isLoading: Boolean = false,
    val stats: List<LeagueLeaderGroup> = emptyList(),
    val error: String? = null
)