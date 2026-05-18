package nicholos.tyler.dugout.model.mapper

import nicholos.tyler.dugout.data.api.dto.StandingsResponseDto
import nicholos.tyler.dugout.data.api.dto.StatsResponseDto
import nicholos.tyler.dugout.data.api.dto.StatsSplitDto
import nicholos.tyler.dugout.model.domain.DivisionStandings
import nicholos.tyler.dugout.model.domain.DivisionTeamStanding
import nicholos.tyler.dugout.model.domain.LeagueLeader
import nicholos.tyler.dugout.model.domain.LeagueLeaderGroup
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
            else -> record.division.name ?: "Division $divisionId"
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

fun StatsResponseDto.toDomain(): List<LeagueLeaderGroup> {
    val allStats = stats + leagueLeaders
    return allStats.mapNotNull { group ->
        val categoryId = group.leaderCategory ?: group.type?.displayName ?: return@mapNotNull null
        val categoryName = categoryId.replace(Regex("([a-z])([A-Z])"), "$1 $2").uppercase()
        
        val items = if (group.leaders.isNotEmpty()) group.leaders else group.splits
        
        val leaders = items.mapIndexed { index, split ->
            val player = split.player ?: split.person
            val teamId = split.team?.id ?: 0
            val teamInfo = MlbTeams.byId[teamId]
            
            LeagueLeader(
                rank = split.rank ?: (index + 1),
                id = player?.id ?: teamId,
                name = player?.fullName ?: player?.name ?: teamInfo?.shortName ?: split.team?.name ?: "Unknown",
                teamName = teamInfo?.shortName ?: split.team?.name,
                statValue = split.value ?: getStatValue(split, categoryId),
                isPlayer = player != null
            )
        }
        
        LeagueLeaderGroup(
            categoryId = categoryId,
            categoryName = categoryName,
            leaders = leaders
        )
    }
}

private fun getStatValue(split: StatsSplitDto, category: String?): String {
    val stat = split.stat ?: return "-"
    return when (category) {
        "homeRuns" -> stat.homeRuns?.toString()
        "runsBattedIn" -> stat.rbi?.toString()
        "battingAverage" -> stat.avg?.let { if (it.startsWith("0")) it.substring(1) else it }
        "onBasePlusSlugging" -> stat.ops?.let { if (it.startsWith("0")) it.substring(1) else it }
        "onBasePercentage" -> stat.obp?.let { if (it.startsWith("0")) it.substring(1) else it }
        "sluggingPercentage" -> stat.slg?.let { if (it.startsWith("0")) it.substring(1) else it }
        "earnedRunAverage" -> stat.era
        "wins" -> stat.wins?.toString()
        "losses" -> stat.losses?.toString()
        "strikeOuts" -> stat.strikeOuts?.toString()
        "saves" -> stat.saves?.toString()
        "walksAndHitsPerInningPitched" -> stat.whip
        "inningsPitched" -> stat.inningsPitched
        "hits" -> stat.hits?.toString()
        "runs" -> stat.runs?.toString()
        "stolenBases" -> stat.stolenBases?.toString()
        else -> {
            stat.homeRuns?.toString()
                ?: stat.rbi?.toString()
                ?: stat.avg
                ?: stat.ops
                ?: stat.era
                ?: "-"
        }
    } ?: "-"
}