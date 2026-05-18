package nicholos.tyler.dugout.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import nicholos.tyler.dugout.model.domain.GameOutcome
import nicholos.tyler.dugout.model.ui.GameCardUiModel

@Composable
fun GameCard(
    game: GameCardUiModel,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    shape: Shape = MaterialTheme.shapes.large,
    border: BorderStroke? = null
) {
    val isDark = isSystemInDarkTheme()
    val containerColor = when {
        game.isSelected -> MaterialTheme.colorScheme.secondaryContainer
        isDark -> MaterialTheme.colorScheme.surfaceContainerHigh
        else -> MaterialTheme.colorScheme.surfaceContainer
    }

    val borderColor = border ?: BorderStroke(
        1.dp,
        if (game.isSelected) {
            MaterialTheme.colorScheme.primary.copy(alpha = if (isDark) 0.34f else 0.22f)
        } else {
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = if (isDark) 0.32f else 0.18f)
        }
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
            ),
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = borderColor,
    ) {
        GameRowContent(
            game = game,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp)
        )
    }
}

@Composable
fun GameRowContent(
    game: GameCardUiModel,
    modifier: Modifier = Modifier,
) {
    val resultColor = when (game.outcome) {
        GameOutcome.Win -> MaterialTheme.colorScheme.primary
        GameOutcome.Loss -> MaterialTheme.colorScheme.error
        GameOutcome.Pending -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column {
            Text(
                text = game.shortDate,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )

            if (game.year.isNotBlank()) {
                Text(
                    text = game.year,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = game.matchup,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            Text(
                text = game.ballpark,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = game.score,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )

            if (game.resultText.isNotBlank()) {
                Text(
                    text = game.resultText,
                    style = MaterialTheme.typography.labelLarge,
                    color = resultColor,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun GameCardPreview() {
    MaterialTheme {
        GameCard(
            game = GameCardUiModel(
                id = 1,
                shortDate = "Mar 26",
                year = "2026",
                date = "2026-03-26",
                matchup = "Phillies @ Braves",
                ballpark = "Truist Park",
                score = "5 - 3",
                resultText = "Win",
                outcome = GameOutcome.Win,
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun GameCardSelectedPreview() {
    MaterialTheme {
        GameCard(
            game = GameCardUiModel(
                id = 2,
                shortDate = "Mar 27",
                year = "2026",
                date = "2026-03-27",
                matchup = "Phillies @ Mets",
                ballpark = "Citi Field",
                score = "—",
                resultText = "",
                outcome = GameOutcome.Pending,
                isSelected = true,
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun GameCardLossPreview() {
    MaterialTheme {
        GameCard(
            game = GameCardUiModel(
                id = 3,
                shortDate = "Apr 02",
                year = "2026",
                matchup = "Dodgers @ Phillies",
                ballpark = "Citizens Bank Park",
                score = "2 - 6",
                resultText = "Loss",
                outcome = GameOutcome.Loss,
                date = "2026, 2, 10"
            )
        )
    }
}