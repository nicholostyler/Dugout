package nicholos.tyler.dugout.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import nicholos.tyler.dugout.model.domain.GameOutcome
import nicholos.tyler.dugout.model.ui.GameCardUiModel

@Immutable
data class TenDayStretchUiModel(
    val title: String = "10-Day Stretch",
    val actionText: String = "Season Schedule",
    val games: List<GameCardUiModel>,
)

@Composable
fun TenDayStretchSection(
    model: TenDayStretchUiModel,
    modifier: Modifier = Modifier,
    onActionClick: () -> Unit = {},
    onGameClick: (GameCardUiModel) -> Unit = {},
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        TitleActionRow(
            title = model.title,
            actionText = model.actionText,
            onActionClick = onActionClick,
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(vertical = 4.dp),
        ) {
            items(
                items = model.games,
                key = { it.id }
            ) { game ->
                GameCard(
                    game = game,
                    modifier = Modifier.width(320.dp),
                    onClick = { onGameClick(game) }
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 420)
@Composable
private fun TenDayStretchSectionPreview() {
    MaterialTheme {
        TenDayStretchSection(
            model = TenDayStretchUiModel(
                games = listOf(
                    GameCardUiModel(
                        id = 1,
                        shortDate = "Mar 26",
                        year = "2026",
                        matchup = "Phillies @ Braves",
                        ballpark = "Truist Park",
                        score = "5 - 3",
                        resultText = "Win",
                        outcome = GameOutcome.Win,
                    ),
                    GameCardUiModel(
                        id = 2,
                        shortDate = "Mar 27",
                        year = "2026",
                        matchup = "Phillies @ Mets",
                        ballpark = "Citi Field",
                        score = "—",
                        resultText = "",
                        outcome = GameOutcome.Pending,
                    ),
                    GameCardUiModel(
                        id = 3,
                        shortDate = "Mar 29",
                        year = "2026",
                        matchup = "Dodgers @ Phillies",
                        ballpark = "Citizens Bank Park",
                        score = "2 - 6",
                        resultText = "Loss",
                        outcome = GameOutcome.Loss,
                    ),
                )
            )
        )
    }
}