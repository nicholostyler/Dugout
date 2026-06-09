package nicholos.tyler.dugout.data.repository

import nicholos.tyler.dugout.data.api.MlbApiService
import nicholos.tyler.dugout.data.api.dto.PlayerApiDto
import nicholos.tyler.dugout.data.api.dto.PlayerStatValuesDto
import nicholos.tyler.dugout.data.api.dto.StatsResponseDto
import nicholos.tyler.dugout.data.api.dto.StatsSplitDto
import nicholos.tyler.dugout.model.domain.Boxscore
import nicholos.tyler.dugout.model.domain.Game
import nicholos.tyler.dugout.model.domain.GameDetails
import nicholos.tyler.dugout.model.domain.PlayerDetails
import nicholos.tyler.dugout.model.domain.PlayerQuickStat
import nicholos.tyler.dugout.model.domain.PlayerSplitSections
import nicholos.tyler.dugout.model.domain.PlayerSplitStatLine
import nicholos.tyler.dugout.model.domain.PlayerStatCategory
import nicholos.tyler.dugout.model.domain.PlayerStatItem
import nicholos.tyler.dugout.model.domain.PlayerStatLine
import nicholos.tyler.dugout.model.domain.PlayerStatSection
import nicholos.tyler.dugout.model.domain.TeamMVPs
import nicholos.tyler.dugout.model.domain.TeamRoster
import nicholos.tyler.dugout.model.mapper.toDomain
import nicholos.tyler.dugout.model.mapper.toGameDetails
import nicholos.tyler.dugout.model.mapper.toTeamMVPs
import nicholos.tyler.dugout.model.mapper.toTeamRoster
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

class GamesRepository(
    private val api: MlbApiService
) {

    suspend fun getTodaysGame(
        teamId: Int
    ): Game? {
        val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)

        val response = api.getScheduleByDate(
            teamId = teamId,
            date = today
        )

        return response.dates
            .flatMap { it.games }
            .firstOrNull()
            ?.toDomain()
    }

    suspend fun getSeasonGames(
        teamId: Int,
        season: Int
    ): List<Game> {
        val response = api.getSchedule(
            teamId = teamId,
            season = season
        )

        return response.dates
            .flatMap { it.games }
            .distinctBy { it.gamePk }
            .map { it.toDomain() }
            .sortedBy { it.gameDate }
    }

    suspend fun getGamesByDate(date: LocalDate): List<Game> {
        val dateString = date.format(DateTimeFormatter.ISO_LOCAL_DATE)

        val response = api.getScheduleByDate(
            date = dateString
        )

        return response.dates
            .flatMap { it.games }
            .distinctBy { it.gamePk }
            .map { it.toDomain() }
            .sortedBy { it.gameDate }
    }

    suspend fun getTodaysGames(): List<Game> {
        return getGamesByDate(LocalDate.now())
    }

    suspend fun getStretchGames(
        teamId: Int
    ): List<Game> {
        val formatter = DateTimeFormatter.ISO_LOCAL_DATE
        val today = LocalDate.now(ZoneOffset.UTC)

        val startDate = today.minusDays(5).format(formatter)
        val endDate = today.plusDays(5).format(formatter)

        val response = api.getScheduleRange(
            teamId = teamId,
            startDate = startDate,
            endDate = endDate
        )

        return response.dates
            .flatMap { it.games }
            .distinctBy { it.gamePk }
            .map { it.toDomain() }
            .sortedBy { it.gameDate }
    }

    suspend fun getGameDetails(
        gamePk: Int
    ): GameDetails {
        val response = api.getGameFeed(gamePk)
        return response.toGameDetails(gamePk)
    }

    suspend fun getBoxscore(
        gamePk: Int
    ): Boxscore {
        return api.getBoxscore(gamePk).toDomain()
    }

    suspend fun getTeamMVPs(
        teamId: Int
    ): TeamMVPs {
        return api.getRosterWithStats(teamId, season = LocalDate.now().year).toTeamMVPs()
    }

    suspend fun getTeamRoster(teamId: Int): TeamRoster {
        return api.getRosterWithStats(teamId, season = LocalDate.now().year).toTeamRoster()
    }

    suspend fun getPlayerDetails(
        playerId: Int,
        season: Int
    ): PlayerDetails {
        val basePlayer = api.getPlayerWithHydratedStats(
            personId = playerId,
            hydrate = "currentTeam,primaryPosition"
        ).people.first()

        val isPitcher = basePlayer.primaryPosition?.abbreviation?.uppercase() == "P"
        val primaryCategory = if (isPitcher) {
            PlayerStatCategory.PITCHING
        } else {
            PlayerStatCategory.BATTING
        }

        val primaryGroup = if (isPitcher) "pitching" else "hitting"

        val hydratedSeasonPrimaryPlayer = api.getPlayerWithHydratedStats(
            personId = playerId,
            hydrate = "stats(group=[$primaryGroup],type=[season],season=$season)"
        ).people.first()

        val hydratedCareerPrimaryPlayer = api.getPlayerWithHydratedStats(
            personId = playerId,
            hydrate = "stats(group=[$primaryGroup],type=[careerRegularSeason])"
        ).people.first()

        val hydratedSeasonFieldingPlayer = api.getPlayerWithHydratedStats(
            personId = playerId,
            hydrate = "stats(group=[fielding],type=[season],season=$season)"
        ).people.first()

        val hydratedCareerFieldingPlayer = api.getPlayerWithHydratedStats(
            personId = playerId,
            hydrate = "stats(group=[fielding],type=[careerRegularSeason])"
        ).people.first()

        val opponentGameLogResponse = api.getPersonStats(
            personId = playerId,
            stats = "gameLog",
            group = primaryGroup,
            season = season
        )

        val primarySeasonStats = hydratedSeasonPrimaryPlayer.findHydratedStatLine(
            requestedGroup = primaryGroup,
            requestedType = "season",
            requestedSeason = season
        )

        val primaryCareerStats = hydratedCareerPrimaryPlayer.findHydratedStatLine(
            requestedGroup = primaryGroup,
            requestedType = "careerRegularSeason",
            requestedSeason = null
        )

        val fieldingSeasonStats = hydratedSeasonFieldingPlayer.findHydratedStatLine(
            requestedGroup = "fielding",
            requestedType = "season",
            requestedSeason = season
        )

        val fieldingCareerStats = hydratedCareerFieldingPlayer.findHydratedStatLine(
            requestedGroup = "fielding",
            requestedType = "careerRegularSeason",
            requestedSeason = null
        )

        val quickStats = buildQuickStats(
            category = primaryCategory,
            seasonStats = primarySeasonStats
        )

        val statSections = buildStatSections(
            primaryCategory = primaryCategory,
            primarySeasonStats = primarySeasonStats,
            primaryCareerStats = primaryCareerStats,
            fieldingSeasonStats = fieldingSeasonStats,
            fieldingCareerStats = fieldingCareerStats
        )

        val splitSections = PlayerSplitSections(
            season = buildOpponentSplitSections(
                primaryCategory = primaryCategory,
                response = opponentGameLogResponse,
                requestedSeason = season
            ),
            career = emptyList()
        )

        return PlayerDetails(
            id = basePlayer.id,
            fullName = basePlayer.fullName,
            jerseyNumber = basePlayer.primaryNumber,
            position = basePlayer.primaryPosition?.abbreviation 
                ?: basePlayer.primaryPosition?.name 
                ?: "--",
            teamName = basePlayer.currentTeam?.fullName ?: basePlayer.currentTeam?.name,
            age = basePlayer.currentAge,
            height = basePlayer.height,
            weight = basePlayer.weight,
            bats = basePlayer.batSide?.code,
            throwsHand = basePlayer.pitchHand?.code,
            quickStats = quickStats,
            statSections = statSections,
            splitSections = splitSections,
            teamId = basePlayer.currentTeam?.id
        )
    }
}

private fun PlayerApiDto.findHydratedStatLine(
    requestedGroup: String,
    requestedType: String,
    requestedSeason: Int?
): PlayerStatValuesDto? {
    val seasonString = requestedSeason?.toString()

    return stats.firstOrNull { group ->
        group.group?.displayName.equals(requestedGroup, ignoreCase = true) &&
                group.type?.displayName.matchesStatType(requestedType)
    }?.splits
        ?.firstOrNull { split ->
            seasonString == null || split.season == seasonString
        }
        ?.stat
}

private fun String?.matchesStatType(requestedType: String): Boolean {
    val actual = this?.lowercase(Locale.US) ?: return false
    val requested = requestedType.lowercase(Locale.US)

    return actual == requested ||
            requested == "careerregularseason" && actual == "career"
}

private fun buildQuickStats(
    category: PlayerStatCategory,
    seasonStats: PlayerStatValuesDto?
): List<PlayerQuickStat> {
    if (seasonStats == null) return emptyList()

    return when (category) {
        PlayerStatCategory.PITCHING -> listOf(
            PlayerQuickStat("ERA", seasonStats.era.display()),
            PlayerQuickStat("WHIP", seasonStats.whip.display()),
            PlayerQuickStat("W", seasonStats.wins.display()),
            PlayerQuickStat("L", seasonStats.losses.display())
        )

        PlayerStatCategory.BATTING -> listOf(
            PlayerQuickStat("AVG", seasonStats.avg.display()),
            PlayerQuickStat("HR", seasonStats.homeRuns.display()),
            PlayerQuickStat("RBI", seasonStats.rbi.display()),
            PlayerQuickStat("OPS", seasonStats.ops.display())
        )

        PlayerStatCategory.FIELDING -> emptyList()
    }
}

private fun buildStatSections(
    primaryCategory: PlayerStatCategory,
    primarySeasonStats: PlayerStatValuesDto?,
    primaryCareerStats: PlayerStatValuesDto?,
    fieldingSeasonStats: PlayerStatValuesDto?,
    fieldingCareerStats: PlayerStatValuesDto?
): List<PlayerStatSection> {
    val sections = mutableListOf<PlayerStatSection>()

    when (primaryCategory) {
        PlayerStatCategory.PITCHING -> {
            if (primarySeasonStats != null || primaryCareerStats != null) {
                sections.add(
                    PlayerStatSection(
                        category = PlayerStatCategory.PITCHING,
                        season = primarySeasonStats?.toPitchingLine(),
                        career = primaryCareerStats?.toPitchingLine()
                    )
                )
            }
        }

        PlayerStatCategory.BATTING -> {
            if (primarySeasonStats != null || primaryCareerStats != null) {
                sections.add(
                    PlayerStatSection(
                        category = PlayerStatCategory.BATTING,
                        season = primarySeasonStats?.toBattingLine(),
                        career = primaryCareerStats?.toBattingLine()
                    )
                )
            }
        }

        PlayerStatCategory.FIELDING -> {
            // not used as primary in current flow
        }
    }

    if (fieldingSeasonStats != null || fieldingCareerStats != null) {
        sections.add(
            PlayerStatSection(
                category = PlayerStatCategory.FIELDING,
                season = fieldingSeasonStats?.toFieldingLine(),
                career = fieldingCareerStats?.toFieldingLine()
            )
        )
    }

    return sections
}

private fun buildOpponentSplitSections(
    primaryCategory: PlayerStatCategory,
    response: StatsResponseDto,
    requestedSeason: Int
): List<PlayerSplitStatLine> {
    val seasonString = requestedSeason.toString()

    return response.stats
        .flatMap { it.splits }
        .filter { split ->
            split.stat != null &&
                    split.opponent?.id != null &&
                    split.opponent.name != null &&
                    split.season == seasonString
        }
        .groupBy { split -> split.opponent!!.id to split.opponent.name!! }
        .map { (opponent, splits) ->
            val aggregate = splits.mapNotNull { it.stat }.toStatAggregate()
            PlayerSplitStatLine(
                title = "vs ${opponent.second}",
                subtitle = "${aggregate.games} G",
                teamId = opponent.first,
                stats = when (primaryCategory) {
                    PlayerStatCategory.PITCHING -> aggregate.toPitchingOpponentSplitItems()
                    PlayerStatCategory.BATTING -> aggregate.toBattingOpponentSplitItems()
                    PlayerStatCategory.FIELDING -> emptyList()
                }
            )
        }
        .sortedWith(
            compareByDescending<PlayerSplitStatLine> { split ->
                split.stats.firstOrNull { it.label == "G" }?.value?.toIntOrNull() ?: 0
            }.thenBy { it.title }
        )
}

private fun List<PlayerStatValuesDto>.toStatAggregate(): PlayerStatAggregate {
    return fold(PlayerStatAggregate()) { aggregate, stat ->
        aggregate.copy(
            games = aggregate.games + (stat.gamesPitched ?: stat.gamesPlayed ?: stat.games ?: 1),
            outs = aggregate.outs + (stat.outs ?: stat.inningsPitched.toOuts()),
            earnedRuns = aggregate.earnedRuns + stat.earnedRuns.orZero(),
            hits = aggregate.hits + stat.hits.orZero(),
            walks = aggregate.walks + stat.baseOnBalls.orZero(),
            strikeOuts = aggregate.strikeOuts + stat.strikeOuts.orZero(),
            wins = aggregate.wins + stat.wins.orZero(),
            losses = aggregate.losses + stat.losses.orZero(),
            saves = aggregate.saves + stat.saves.orZero(),
            atBats = aggregate.atBats + stat.atBats.orZero(),
            totalBases = aggregate.totalBases + stat.totalBases.orZero(),
            hitByPitch = aggregate.hitByPitch + stat.hitByPitch.orZero(),
            sacFlies = aggregate.sacFlies + stat.sacFlies.orZero(),
            homeRuns = aggregate.homeRuns + stat.homeRuns.orZero(),
            rbi = aggregate.rbi + stat.rbi.orZero()
        )
    }
}

private data class PlayerStatAggregate(
    val games: Int = 0,
    val outs: Int = 0,
    val earnedRuns: Int = 0,
    val hits: Int = 0,
    val walks: Int = 0,
    val strikeOuts: Int = 0,
    val wins: Int = 0,
    val losses: Int = 0,
    val saves: Int = 0,
    val atBats: Int = 0,
    val totalBases: Int = 0,
    val hitByPitch: Int = 0,
    val sacFlies: Int = 0,
    val homeRuns: Int = 0,
    val rbi: Int = 0
)

private fun PlayerStatAggregate.toPitchingOpponentSplitItems(): List<PlayerStatItem> {
    return listOf(
        PlayerStatItem("G", games.display()),
        PlayerStatItem("IP", outs.toInningsPitched()),
        PlayerStatItem("ERA", formatRate(numerator = earnedRuns * 27.0, denominator = outs.toDouble())),
        PlayerStatItem("WHIP", formatRate(numerator = (hits + walks) * 3.0, denominator = outs.toDouble())),
        PlayerStatItem("SO", strikeOuts.display()),
        PlayerStatItem("BB", walks.display()),
        PlayerStatItem("W", wins.display()),
        PlayerStatItem("L", losses.display()),
        PlayerStatItem("SV", saves.display())
    )
}

private fun PlayerStatAggregate.toBattingOpponentSplitItems(): List<PlayerStatItem> {
    val obpDenominator = atBats + walks + hitByPitch + sacFlies
    val obp = formatAverage(hits + walks + hitByPitch, obpDenominator)
    val slg = formatAverage(totalBases, atBats)
    val ops = if (obpDenominator == 0 || atBats == 0) "--" else {
        val obpValue = (hits + walks + hitByPitch).toDouble() / obpDenominator
        val slgValue = totalBases.toDouble() / atBats
        formatDecimal(obpValue + slgValue, digits = 3, leadingZero = false)
    }

    return listOf(
        PlayerStatItem("G", games.display()),
        PlayerStatItem("AVG", formatAverage(hits, atBats)),
        PlayerStatItem("OPS", ops),
        PlayerStatItem("H", hits.display()),
        PlayerStatItem("HR", homeRuns.display()),
        PlayerStatItem("RBI", rbi.display()),
        PlayerStatItem("BB", walks.display()),
        PlayerStatItem("SO", strikeOuts.display())
    )
}

private fun PlayerStatValuesDto.toBattingLine(): PlayerStatLine {
    return PlayerStatLine(
        primaryStats = listOf(
            PlayerStatItem("AVG", avg.display()),
            PlayerStatItem("OPS", ops.display()),
            PlayerStatItem("HR", homeRuns.display()),
            PlayerStatItem("RBI", rbi.display())
        ),
        secondaryStats = listOf(
            PlayerStatItem("G", gamesPlayed.display()),
            PlayerStatItem("R", runs.display()),
            PlayerStatItem("H", hits.display()),
            PlayerStatItem("TB", totalBases.display()),
            PlayerStatItem("2B", doubles.display()),
            PlayerStatItem("3B", triples.display()),
            PlayerStatItem("BB", baseOnBalls.display()),
            PlayerStatItem("SO", strikeOuts.display()),
            PlayerStatItem("SB", stolenBases.display())
        )
    )
}

private fun PlayerStatValuesDto.toPitchingLine(): PlayerStatLine {
    return PlayerStatLine(
        primaryStats = listOf(
            PlayerStatItem("ERA", era.display()),
            PlayerStatItem("WHIP", whip.display()),
            PlayerStatItem("W", wins.display()),
            PlayerStatItem("L", losses.display())
        ),
        secondaryStats = listOf(
            PlayerStatItem("IP", inningsPitched.display()),
            PlayerStatItem("SO", strikeOuts.display()),
            PlayerStatItem("BB", baseOnBalls.display()),
            PlayerStatItem("SV", saves.display())
        )
    )
}

private fun PlayerStatValuesDto.toFieldingLine(): PlayerStatLine {
    return PlayerStatLine(
        primaryStats = listOf(
            PlayerStatItem("FLD%", fielding.display()),
            PlayerStatItem("E", errors.display()),
            PlayerStatItem("A", assists.display()),
            PlayerStatItem("PO", putOuts.display())
        ),
        secondaryStats = listOf(
            PlayerStatItem("G", gamesPlayed.display()),
            PlayerStatItem("GS", gamesStarted.display()),
            PlayerStatItem("TC", chances.display()),
            PlayerStatItem("DP", doublePlays.display())
        )
    )
}

private fun PlayerStatValuesDto.toPitchingSplitItems(): List<PlayerStatItem> {
    return listOf(
        PlayerStatItem("ERA", era.display()),
        PlayerStatItem("WHIP", whip.display()),
        PlayerStatItem("IP", inningsPitched.display()),
        PlayerStatItem("SO", strikeOuts.display()),
        PlayerStatItem("BB", baseOnBalls.display()),
        PlayerStatItem("W", wins.display()),
        PlayerStatItem("L", losses.display()),
        PlayerStatItem("SV", saves.display())
    )
}

private fun PlayerStatValuesDto.toBattingSplitItems(): List<PlayerStatItem> {
    return listOf(
        PlayerStatItem("AVG", avg.display()),
        PlayerStatItem("OPS", ops.display()),
        PlayerStatItem("G", gamesPlayed.display()),
        PlayerStatItem("H", hits.display()),
        PlayerStatItem("HR", homeRuns.display()),
        PlayerStatItem("RBI", rbi.display()),
        PlayerStatItem("BB", baseOnBalls.display()),
        PlayerStatItem("SO", strikeOuts.display())
    )
}

private fun PlayerStatValuesDto.toFieldingSplitItems(): List<PlayerStatItem> {
    return listOf(
        PlayerStatItem("FLD%", fielding.display()),
        PlayerStatItem("G", gamesPlayed.display()),
        PlayerStatItem("GS", gamesStarted.display()),
        PlayerStatItem("TC", chances.display()),
        PlayerStatItem("PO", putOuts.display()),
        PlayerStatItem("A", assists.display()),
        PlayerStatItem("E", errors.display()),
        PlayerStatItem("DP", doublePlays.display())
    )
}

private fun Int?.orZero(): Int = this ?: 0

private fun String?.toOuts(): Int {
    if (this == null) return 0
    val parts = split(".")
    val innings = parts.getOrNull(0)?.toIntOrNull() ?: 0
    val partialOuts = parts.getOrNull(1)?.toIntOrNull()?.coerceIn(0, 2) ?: 0
    return innings * 3 + partialOuts
}

private fun Int.toInningsPitched(): String {
    val innings = this / 3
    val remainingOuts = this % 3
    return "$innings.$remainingOuts"
}

private fun formatRate(numerator: Double, denominator: Double): String {
    if (denominator <= 0.0) return "--"
    return formatDecimal(numerator / denominator, digits = 2, leadingZero = true)
}

private fun formatAverage(numerator: Int, denominator: Int): String {
    if (denominator <= 0) return "--"
    return formatDecimal(numerator.toDouble() / denominator, digits = 3, leadingZero = false)
}

private fun formatDecimal(value: Double, digits: Int, leadingZero: Boolean): String {
    val formatted = String.format(Locale.US, "%.${digits}f", value)
    return if (!leadingZero && formatted.startsWith("0")) {
        formatted.drop(1)
    } else {
        formatted
    }
}

private fun Any?.display(): String = this?.toString() ?: "--"
