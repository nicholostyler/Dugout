package nicholos.tyler.dugout.model.ui

import androidx.compose.runtime.Immutable
import nicholos.tyler.dugout.ui.components.DivisionStandingUiModel
import nicholos.tyler.dugout.ui.components.TenDayStretchUiModel

@Immutable
data class HomeUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val todaysGame: GameSnapshotCardUiModel? = null,
    val tenDayStretch: TenDayStretchUiModel? = null,
    val teamMvps: TeamMVPsUiModel? = null,
    val divisionStandings: List<DivisionStandingUiModel> = emptyList(),
    val divisionTitle: String = "",
    val error: String? = null
)
