package nicholos.tyler.dugout.model.mapper

import nicholos.tyler.dugout.data.api.dto.BoxscoreResponseDto
import nicholos.tyler.dugout.data.api.dto.BoxscoreTeamDto
import nicholos.tyler.dugout.model.domain.BattingStats
import nicholos.tyler.dugout.model.domain.Boxscore
import nicholos.tyler.dugout.model.domain.BoxscorePlayer
import nicholos.tyler.dugout.model.domain.BoxscoreTeam
import nicholos.tyler.dugout.model.domain.PitchingStats

fun BoxscoreResponseDto.toDomain(): Boxscore {
    return Boxscore(
        away = teams?.away?.toDomain("Away") ?: BoxscoreTeam("Away", emptyList(), emptyList()),
        home = teams?.home?.toDomain("Home") ?: BoxscoreTeam("Home", emptyList(), emptyList())
    )
}

fun BoxscoreTeamDto.toDomain(fallbackName: String): BoxscoreTeam {
    val teamPlayers = players.values

    val batterList = batters.mapNotNull { id ->
        players["ID$id"]?.let { p ->
            BoxscorePlayer(
                id = id,
                fullName = p.person?.fullName ?: "Unknown",
                position = p.position?.abbreviation ?: "",
                battingStats = p.stats?.batting?.let {
                    BattingStats(
                        ab = it.atBats,
                        r = it.runs,
                        h = it.hits,
                        rbi = it.rbi,
                        bb = it.baseOnBalls,
                        k = it.strikeOuts
                    )
                }
            )
        }
    }

    val pitcherList = pitchers.mapNotNull { id ->
        players["ID$id"]?.let { p ->
            BoxscorePlayer(
                id = id,
                fullName = p.person?.fullName ?: "Unknown",
                position = p.position?.abbreviation ?: "",
                pitchingStats = p.stats?.pitching?.let {
                    PitchingStats(
                        ip = it.inningsPitched ?: "0.0",
                        h = it.hits,
                        r = it.runs,
                        er = it.earnedRuns,
                        bb = it.baseOnBalls,
                        k = it.strikeOuts
                    )
                }
            )
        }
    }

    return BoxscoreTeam(
        teamName = team?.name ?: fallbackName,
        batters = batterList,
        pitchers = pitcherList
    )
}
