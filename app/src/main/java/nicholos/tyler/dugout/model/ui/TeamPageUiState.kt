package nicholos.tyler.dugout.model.ui

import nicholos.tyler.dugout.ui.components.DivisionStandingUiModel
import nicholos.tyler.dugout.ui.components.TenDayStretchUiModel

data class TeamPageUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val todaysGame: GameSnapshotCardUiModel? = null,
    val tenDayStretch: TenDayStretchUiModel? = null,
    val teamMvps: TeamMVPsUiModel? = null,
    val divisionStandings: List<DivisionStandingUiModel> = emptyList(),
    val divisionTitle: String = ""
)
