package nicholos.tyler.dugout.model.ui

import androidx.compose.runtime.Immutable
import nicholos.tyler.dugout.ui.components.TenDayStretchUiModel

@Immutable
data class HomeUiState(
    val isLoading: Boolean = false,
    val todaysGame: GameSnapshotCardUiModel? = null,
    val tenDayStretch: TenDayStretchUiModel? = null,
    val teamMvps: TeamMVPsUiModel? = null,
    val error: String? = null
)
