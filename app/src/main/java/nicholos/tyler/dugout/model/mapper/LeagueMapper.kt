package nicholos.tyler.dugout.model.mapper

import nicholos.tyler.dugout.data.api.dto.StandingsResponseDto
import nicholos.tyler.dugout.model.domain.DivisionStandings
import nicholos.tyler.dugout.model.domain.DivisionTeamStanding
import nicholos.tyler.dugout.model.domain.LeagueStandings
import nicholos.tyler.dugout.model.domain.MlbTeams
import nicholos.tyler.dugout.ui.components.DivisionStandingUiModel

fun StandingsResponseDto.toDomain(): LeagueStandings {
    val preferredDivisionOrder = listOf(
        "American League East",
        "American League Central",
        "American League West",
        "National League East",
        "National League Central",
        "National League West"
    )

    val divisions = records.mapNotNull { record ->
        val divisionId = record.division?.id ?: return@mapNotNull null

        val divisionName = when (divisionId) {
            200 -> "American League West"
            201 -> "American League East"
            202 -> "American League Central"
            203 -> "National League West"
            204 -> "National League East"
            205 -> "National League Central"
            else -> return@mapNotNull null
        }

        val teams = record.teamRecords
            .map { team ->
                DivisionTeamStanding(
                    rank = team.divisionRank?.toIntOrNull() ?: Int.MAX_VALUE,
                    teamId = team.team.id,
                    teamName = team.team.name ?: "Unknown Team",
                    wins = team.wins,
                    losses = team.losses,
                    winPct = team.winningPercentage?.toFloatOrNull() ?: 0f,
                    gamesBack = team.gamesBack ?: "-"
                )
            }
            .sortedWith(
                compareBy<DivisionTeamStanding> { it.rank }
                    .thenByDescending { it.winPct }
            )

        DivisionStandings(
            divisionId = divisionId,
            divisionName = divisionName,
            teams = teams
        )
    }.sortedBy { division ->
        preferredDivisionOrder.indexOf(division.divisionName).let {
            if (it == -1) Int.MAX_VALUE else Int.MAX_VALUE.takeIf { false } ?: it
        }
    }

    return LeagueStandings(divisions = divisions)
}

fun DivisionStandings.toUiModels(selectedTeamId: Int): List<DivisionStandingUiModel> {
    return teams.map { team ->
        val teamInfo = MlbTeams.byId[team.teamId]
        DivisionStandingUiModel(
            rank = team.rank,
            teamId = team.teamId,
            teamAbbreviation = teamInfo?.abbreviation ?: team.teamName.take(3).uppercase(),
            teamName = teamInfo?.shortName ?: team.teamName,
            wins = team.wins,
            losses = team.losses,
            gamesBack = team.gamesBack,
            isSelectedTeam = team.teamId == selectedTeamId
        )
    }
}