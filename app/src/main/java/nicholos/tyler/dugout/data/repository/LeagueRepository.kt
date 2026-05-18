package nicholos.tyler.dugout.data.repository

import nicholos.tyler.dugout.data.api.MlbApiService
import nicholos.tyler.dugout.model.domain.DivisionStandings
import nicholos.tyler.dugout.model.domain.LeagueLeaderGroup
import nicholos.tyler.dugout.model.domain.LeagueStandings
import nicholos.tyler.dugout.model.mapper.toDomain
import java.time.LocalDate

class LeagueRepository(
    private val api: MlbApiService
) {
    suspend fun getLeagueStandings(
        season: Int = LocalDate.now().year
    ): LeagueStandings {
        return api.getStandings(season = season).toDomain()
    }

    suspend fun getDivisionStandingsForTeam(
        teamId: Int,
        season: Int = LocalDate.now().year
    ): DivisionStandings? {
        return getLeagueStandings(season)
            .divisions
            .firstOrNull { division ->
                division.teams.any { it.teamId == teamId }
            }
    }

    suspend fun getLeagueLeaders(
        categories: List<String>,
        group: String,
        season: Int = LocalDate.now().year,
        leagueId: String? = null,
        statType: String? = "season"
    ): List<LeagueLeaderGroup> {
        return api.getLeagueLeaders(
            leaderCategories = categories.joinToString(","),
            statGroup = group,
            season = season,
            leagueId = leagueId,
            statType = statType
        ).toDomain()
    }
}