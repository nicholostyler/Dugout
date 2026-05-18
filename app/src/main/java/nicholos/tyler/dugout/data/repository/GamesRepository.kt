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
        val basePlayer = api.getPlayer(playerId).people.first()

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

        val splitStatsResponse = api.getPlayerStats(
            stats = "season",
            group = primaryGroup,
            personId = playerId,
            season = season
        )

        val fieldingSplitStatsResponse = api.getPlayerStats(
            stats = "season",
            group = "fielding",
            personId = playerId,
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

        val splitSections = buildSplitSections(
            primaryCategory = primaryCategory,
            primaryResponse = splitStatsResponse,
            fieldingResponse = fieldingSplitStatsResponse,
            requestedSeason = season
        )

        return PlayerDetails(
            id = basePlayer.id,
            fullName = basePlayer.fullName,
            jerseyNumber = basePlayer.primaryNumber,
            position = basePlayer.primaryPosition?.abbreviation ?: "--",
            teamName = basePlayer.currentTeam?.name,
            age = basePlayer.currentAge,
            height = basePlayer.height,
            weight = basePlayer.weight,
            bats = basePlayer.batSide?.code,
            throwsHand = basePlayer.pitchHand?.code,
            quickStats = quickStats,
            statSections = statSections,
            splitSections = splitSections
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
                group.type?.displayName.equals(requestedType, ignoreCase = true)
    }?.splits
        ?.firstOrNull { split ->
            seasonString == null || split.season == seasonString
        }
        ?.stat
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

private fun buildSplitSections(
    primaryCategory: PlayerStatCategory,
    primaryResponse: StatsResponseDto,
    fieldingResponse: StatsResponseDto,
    requestedSeason: Int
): List<PlayerSplitStatLine> {
    val sections = mutableListOf<PlayerSplitStatLine>()

    sections += primaryResponse.stats
        .flatMap { it.splits }
        .filter { it.season == requestedSeason.toString() && it.stat != null }
        .map { split ->
            PlayerSplitStatLine(
                title = split.buildTitle(),
                stats = when (primaryCategory) {
                    PlayerStatCategory.PITCHING -> split.stat!!.toPitchingSplitItems()
                    PlayerStatCategory.BATTING -> split.stat!!.toBattingSplitItems()
                    PlayerStatCategory.FIELDING -> emptyList()
                }
            )
        }

    sections += fieldingResponse.stats
        .flatMap { it.splits }
        .filter { it.season == requestedSeason.toString() && it.stat != null }
        .map { split ->
            PlayerSplitStatLine(
                title = "${split.buildTitle()} • Fielding",
                stats = split.stat!!.toFieldingSplitItems()
            )
        }

    return sections
}

private fun StatsSplitDto.buildTitle(): String {
    return team?.name
        ?: league?.name
        ?: sport?.name
        ?: season
        ?: "Split"
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

data class PlayerSplitStatLine(
    val title: String,
    val stats: List<PlayerStatItem>
)

private fun Any?.display(): String = this?.toString() ?: "--"