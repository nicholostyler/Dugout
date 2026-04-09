package nicholos.tyler.dugout.model.ui

import androidx.compose.runtime.Immutable
import nicholos.tyler.dugout.model.domain.TeamDisplayInfo

@Immutable
data class TeamScoreUiModel(
    val name: TeamDisplayInfo,
    val score: String,
)