package nicholos.tyler.dugout.data.repository

import nicholos.tyler.dugout.data.api.MlbApiService
import nicholos.tyler.dugout.model.domain.Game
import nicholos.tyler.dugout.model.domain.GameDetails
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
            .map { it.toDomain() }
            .sortedBy { it.gameDate }
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
            .map { it.toDomain() }
            .sortedBy { it.gameDate }
    }

    suspend fun getGameDetails(
        gamePk: Int
    ): GameDetails {
        val response = api.getGameFeed(gamePk)
        return response.toGameDetails(gamePk)
    }

    suspend fun getTeamMVPs(
        teamId: Int
    ): TeamMVPs {
        return api.getRosterWithStats(teamId).toTeamMVPs()
    }

    suspend fun getTeamRoster(teamId: Int): TeamRoster {
        return api.getRosterWithStats(teamId).toTeamRoster()
    }
}