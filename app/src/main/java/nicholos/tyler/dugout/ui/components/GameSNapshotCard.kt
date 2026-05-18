package nicholos.tyler.dugout.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import nicholos.tyler.dugout.model.domain.MlbTeams
import nicholos.tyler.dugout.model.domain.TeamDisplayInfo
import nicholos.tyler.dugout.model.ui.GameSnapshotCardUiModel
import nicholos.tyler.dugout.model.ui.InningScoreUiModel
import nicholos.tyler.dugout.model.ui.LinescoreUiModel
import nicholos.tyler.dugout.model.ui.TeamScoreUiModel
import nicholos.tyler.dugout.model.ui.TeamTotalUiModel
import nicholos.tyler.dugout.ui.theme.DugoutTheme

@Composable
fun GameSnapshotCard(
    model: GameSnapshotCardUiModel,
    modifier: Modifier = Modifier,
    showFooter: Boolean = true,
    onClick: (Int) -> Unit = {}
) {
    when {
        model.isLiveGame() -> LiveGameSnapshotCard(
            model = model,
            onClick = onClick,
            modifier = modifier
        )

        model.shouldUseLinescoreBoxscore() -> LinescoreBoxscoreSnapshotCard(
            model = model,
            showFooter = showFooter,
            onClick = onClick,
            modifier = modifier
        )

        else -> QuickViewSnapshotCard(
            model = model,
            showFooter = showFooter,
            onClick = onClick,
            modifier = modifier
        )
    }
}

private fun GameSnapshotCardUiModel.shouldUseLinescoreBoxscore(): Boolean =
    isFinalGame() && linescore != null

@Composable
private fun SnapshotCardContainer(
    model: GameSnapshotCardUiModel,
    onClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick(model.gameId) },
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        content()
    }
}

@Composable
private fun QuickViewSnapshotCard(
    model: GameSnapshotCardUiModel,
    showFooter: Boolean,
    onClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    SnapshotCardContainer(
        model = model,
        onClick = onClick,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            QuickViewHeader(model = model)
            QuickViewTeamsAndPitchers(model = model)
            SnapshotFooter(model = model, showFooter = showFooter)
        }
    }
}

@Composable
private fun QuickViewHeader(model: GameSnapshotCardUiModel) {
    val headerText = when {
        model.inningText.isNotBlank() -> model.inningText
        model.isLiveGame() -> "Live"
        model.isFinalGame() -> "Final"
        model.isUpcomingGame() -> model.startTime.ifBlank { "Pre Game" }
        else -> model.status
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (model.inningText.isNotBlank() && model.isTopInning != null) {
            Icon(
                imageVector = if (model.isTopInning) {
                    Icons.Default.ArrowUpward
                } else {
                    Icons.Default.ArrowDownward
                },
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
        }

        Text(
            text = headerText,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
private fun QuickViewTeamsAndPitchers(model: GameSnapshotCardUiModel) {
    val isUpcoming = model.isUpcomingGame()
    val isFinal = model.isFinalGame()

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SnapshotTeamColumn(
            team = model.leftTeam.name,
            score = model.leftTeam.score,
            record = model.leftTeam.record,
            emphasize = false,
            isCompact = isFinal,
            showScore = !isUpcoming,
            probablePitcher = model.leftTeam.probablePitcher,
            modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp)
        )

        Box(
            modifier = Modifier.width(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "@",
                style = if (isFinal) {
                    MaterialTheme.typography.labelMedium
                } else {
                    MaterialTheme.typography.labelLarge
                },
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

        SnapshotTeamColumn(
            team = model.rightTeam.name,
            score = model.rightTeam.score,
            record = model.rightTeam.record,
            emphasize = true,
            isCompact = isFinal,
            showScore = !isUpcoming,
            probablePitcher = model.rightTeam.probablePitcher,
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp)
        )
    }
}

@Composable
private fun LinescoreBoxscoreSnapshotCard(
    model: GameSnapshotCardUiModel,
    showFooter: Boolean,
    onClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val linescore = model.linescore ?: return

    SnapshotCardContainer(
        model = model,
        onClick = onClick,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            LinescoreBoxscoreHeader(model = model)
            LinescoreTable(
                leftTeamName = model.leftTeam.name.shortName,
                rightTeamName = model.rightTeam.name.shortName,
                linescore = linescore,
                modifier = Modifier.padding(vertical = 4.dp)
            )
            SnapshotFooter(model = model, showFooter = showFooter)
        }
    }
}

@Composable
private fun LinescoreBoxscoreHeader(model: GameSnapshotCardUiModel) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        TeamFinalSummary(
            abbreviation = model.leftTeam.name.abbreviation,
            score = model.leftTeam.score,
            modifier = Modifier.weight(1f)
        )

        Text(
            text = "FINAL",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        TeamFinalSummary(
            abbreviation = model.rightTeam.name.abbreviation,
            score = model.rightTeam.score,
            isHomeTeam = true,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun TeamFinalSummary(
    abbreviation: String,
    score: String,
    modifier: Modifier = Modifier,
    isHomeTeam: Boolean = false
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = if (isHomeTeam) Arrangement.End else Arrangement.Start,
        modifier = modifier
    ) {
        if (isHomeTeam) {
            FinalScoreText(score = score)
            Spacer(modifier = Modifier.width(12.dp))
            FinalTeamAbbreviationText(abbreviation = abbreviation)
        } else {
            FinalTeamAbbreviationText(abbreviation = abbreviation)
            Spacer(modifier = Modifier.width(12.dp))
            FinalScoreText(score = score)
        }
    }
}

@Composable
private fun FinalTeamAbbreviationText(abbreviation: String) {
    Text(
        text = abbreviation,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onPrimaryContainer
    )
}

@Composable
private fun FinalScoreText(score: String) {
    Text(
        text = score,
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.ExtraBold,
        color = MaterialTheme.colorScheme.onPrimaryContainer
    )
}

@Composable
fun LiveGameSnapshotCard(
    model: GameSnapshotCardUiModel,
    onClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        onClick = { onClick(model.gameId) },
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        LiveGameSnapshotContent(model = model)
    }
}



@Composable
private fun LiveGameSnapshotContent(model: GameSnapshotCardUiModel) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp, bottom = 32.dp, start = 16.dp, end = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LiveStatusPill(model)

        Spacer(modifier = Modifier.height(28.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            LiveTeamScoreColumn(
                team = model.leftTeam,
                modifier = Modifier.weight(1f)
            )

            LiveGameCenterColumn(model = model)

            LiveTeamScoreColumn(
                team = model.rightTeam,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun LiveTeamScoreColumn(
    team: TeamScoreUiModel,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Text(
            text = team.name.shortName,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Text(
            text = team.score.ifBlank { "0" },
            style = MaterialTheme.typography.displayLarge.copy(
                fontSize = 72.sp,
                lineHeight = 72.sp
            ),
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Text(
            text = team.record,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.72f)
        )
    }
}

@Composable
private fun LiveGameCenterColumn(model: GameSnapshotCardUiModel) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(80.dp)
            .padding(top = 4.dp)
    ) {
        Text(
            text = "@",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.4f)
        )
        Spacer(modifier = Modifier.height(12.dp))
        DiamondRunners(
            onFirst = model.onFirst,
            onSecond = model.onSecond,
            onThird = model.onThird,
            modifier = Modifier.size(32.dp),
            unoccupiedColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.28f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = model.outsText.ifBlank { "0 Outs" },
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
private fun SnapshotFooter(
    model: GameSnapshotCardUiModel,
    showFooter: Boolean
) {
    val footer = listOfNotNull(
        model.outsText.takeIf { it.isNotBlank() },
        model.countText.takeIf { it.isNotBlank() }
    ).joinToString(" • ")

    if (!showFooter || footer.isBlank()) return

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = if (model.isFinalGame()) 8.dp else 15.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = footer,
            style = if (model.isFinalGame()) {
                MaterialTheme.typography.bodyMedium
            } else {
                MaterialTheme.typography.bodyLarge
            },
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun DiamondRunners(
    onFirst: Boolean,
    onSecond: Boolean,
    onThird: Boolean,
    modifier: Modifier = Modifier,
    unoccupiedColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
    baseSize: Dp = 9.dp
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        BaseDiamond(
            occupied = onSecond,
            unoccupiedColor = unoccupiedColor,
            size = baseSize,
            modifier = Modifier.align(Alignment.TopCenter)
        )
        BaseDiamond(
            occupied = onThird,
            unoccupiedColor = unoccupiedColor,
            size = baseSize,
            modifier = Modifier.align(Alignment.CenterStart)
        )
        BaseDiamond(
            occupied = onFirst,
            unoccupiedColor = unoccupiedColor,
            size = baseSize,
            modifier = Modifier.align(Alignment.CenterEnd)
        )
    }
}

@Composable
private fun BaseDiamond(
    occupied: Boolean,
    unoccupiedColor: Color,
    size: Dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(size)
            .graphicsLayer { rotationZ = 45f }
            .background(
                if (occupied) MaterialTheme.colorScheme.tertiary else Color.Transparent
            )
            .border(
                width = 1.5.dp,
                color = if (occupied) MaterialTheme.colorScheme.tertiary else unoccupiedColor
            )
    )
}

@Composable
private fun LiveStatusPill(model: GameSnapshotCardUiModel) {
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.secondaryContainer,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.error)
            )
            Spacer(modifier = Modifier.width(10.dp))
            val text = "LIVE - ${model.inningText.ifBlank { model.status }}"
            Text(
                text = text.uppercase(),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                letterSpacing = 0.8.sp
            )
        }
    }
}

@Composable
private fun LinescoreTable(
    leftTeamName: String,
    rightTeamName: String,
    linescore: LinescoreUiModel,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        LinescoreHeaderRow(
            linescore = linescore,
            scrollState = scrollState
        )
        TeamLinescoreRow(
            teamName = leftTeamName,
            inningRuns = linescore.innings.map { it.leftRuns },
            totals = linescore.leftTotal,
            scrollState = scrollState
        )
        TeamLinescoreRow(
            teamName = rightTeamName,
            inningRuns = linescore.innings.map { it.rightRuns },
            totals = linescore.rightTotal,
            scrollState = scrollState
        )
    }
}

@Composable
private fun LinescoreHeaderRow(
    linescore: LinescoreUiModel,
    scrollState: ScrollState
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "",
            modifier = Modifier.width(60.dp),
            style = MaterialTheme.typography.labelSmall
        )

        Row(
            modifier = Modifier
                .weight(1f)
                .horizontalScroll(scrollState),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            linescore.innings.forEach { inning ->
                Text(
                    text = inning.number.toString(),
                    modifier = Modifier.width(18.dp),
                    style = MaterialTheme.typography.labelSmall,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        LinescoreTotalsDivider()
        LinescoreTotalsHeader()
    }
}

@Composable
private fun LinescoreTotalsDivider() {
    Spacer(modifier = Modifier.width(8.dp))
    VerticalDivider(
        modifier = Modifier.height(16.dp),
        color = MaterialTheme.colorScheme.outlineVariant
    )
    Spacer(modifier = Modifier.width(8.dp))
}

@Composable
private fun LinescoreTotalsHeader() {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        listOf("R", "H", "E").forEach { label ->
            Text(
                text = label,
                modifier = Modifier.width(20.dp),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun TeamLinescoreRow(
    teamName: String,
    inningRuns: List<String>,
    totals: TeamTotalUiModel,
    scrollState: ScrollState
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = teamName,
            modifier = Modifier.width(60.dp),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Row(
            modifier = Modifier
                .weight(1f)
                .horizontalScroll(scrollState),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            inningRuns.forEach { runs ->
                Text(
                    text = runs,
                    modifier = Modifier.width(18.dp),
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center
                )
            }
        }

        LinescoreTotalsDivider()
        LinescoreTotals(totals = totals)
    }
}

@Composable
private fun LinescoreTotals(totals: TeamTotalUiModel) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = totals.runs.toString(),
            modifier = Modifier.width(20.dp),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Text(
            text = totals.hits.toString(),
            modifier = Modifier.width(20.dp),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            textAlign = TextAlign.Center
        )
        Text(
            text = totals.errors.toString(),
            modifier = Modifier.width(20.dp),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun SnapshotTeamColumn(
    team: TeamDisplayInfo,
    score: String,
    record: String = "",
    emphasize: Boolean,
    isCompact: Boolean = false,
    showScore: Boolean = true,
    probablePitcher: String? = null,
    modifier: Modifier = Modifier
) {
    val color = if (emphasize) {
        MaterialTheme.colorScheme.onSecondaryContainer
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = team.shortName,
            modifier = Modifier.fillMaxWidth(),
            style = if (isCompact) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleLarge,
            fontWeight = if (emphasize) FontWeight.SemiBold else FontWeight.Normal,
            color = color,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        if (emphasize) PitcherText(probablePitcher = probablePitcher, MaterialTheme.colorScheme.onPrimaryContainer) else PitcherText(probablePitcher = probablePitcher)

        if (record.isNotBlank() && !showScore) {
            Text(
                text = "($record)",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                textAlign = TextAlign.Center
            )
        }

        if (showScore) {
            Spacer(modifier = Modifier.height(if (isCompact) 4.dp else 10.dp))

            Text(
                text = score,
                modifier = Modifier.fillMaxWidth(),
                style = if (isCompact) MaterialTheme.typography.displayMedium else MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Bold,
                color = color,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun PitcherText(probablePitcher: String?, color: Color = MaterialTheme.colorScheme.onPrimaryContainer) {
    Text(
        text = probablePitcher.takeUnless { it.isNullOrBlank() } ?: "TBD",
        modifier = Modifier.fillMaxWidth(),
        style = MaterialTheme.typography.labelSmall,
        color = color,
        textAlign = TextAlign.Center,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}

@Preview(showBackground = true, widthDp = 420)
@Composable
private fun ScheduledGameSnapshotCardPreview() {
    DugoutTheme {
        GameSnapshotCard(
            model = GameSnapshotCardUiModel(
                gameId = 2,
                leftTeam = TeamScoreUiModel(
                    name = MlbTeams.get(120),
                    score = "—",
                    record = "65-80",
                    probablePitcher = "Patrick Corbin"
                ),
                rightTeam = TeamScoreUiModel(
                    name = MlbTeams.get(143),
                    score = "—",
                    record = "82-64",
                    probablePitcher = "Zack Wheeler"
                ),
                status = "Scheduled",
                startTime = "7:10 PM"
            ),
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true, widthDp = 420)
@Composable
private fun FinalExtraInningsGameSnapshotCardPreview() {
    val innings = (1..12).map {
        InningScoreUiModel(it, if (it == 12) "1" else "0", "0")
    }
    DugoutTheme {
        GameSnapshotCard(
            model = GameSnapshotCardUiModel(
                gameId = 3,
                leftTeam = TeamScoreUiModel(
                    name = MlbTeams.get(120),
                    score = "1"
                ),
                rightTeam = TeamScoreUiModel(
                    name = MlbTeams.get(143),
                    score = "0"
                ),
                status = "Final",
                linescore = LinescoreUiModel(
                    innings = innings,
                    leftTotal = TeamTotalUiModel(1, 8, 0),
                    rightTotal = TeamTotalUiModel(0, 4, 1)
                )
            ),
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true, widthDp = 420)
@Composable
private fun LiveGameSnapshotCardPreview() {
    DugoutTheme {
        LiveGameSnapshotCard(
            model = GameSnapshotCardUiModel(
                gameId = 2,
                leftTeam = TeamScoreUiModel(
                    name = MlbTeams.get(120),
                    score = "4"
                ),
                rightTeam = TeamScoreUiModel(
                    name = MlbTeams.get(143),
                    score = "9"
                ),
                status = "Bottom 8th",
            ),
            onClick = { },
            modifier = Modifier.padding(16.dp),
        )
    }
}
