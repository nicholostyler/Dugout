package nicholos.tyler.dugout.model.mapper

import nicholos.tyler.dugout.data.api.dto.GameDto
import nicholos.tyler.dugout.data.api.dto.GameStatusDto
import nicholos.tyler.dugout.data.api.dto.GameTeamsDto
import nicholos.tyler.dugout.data.api.dto.LeagueRecordDto
import nicholos.tyler.dugout.data.api.dto.LinescoreDto
import nicholos.tyler.dugout.data.api.dto.TeamDto
import nicholos.tyler.dugout.data.api.dto.TeamSideDto
import nicholos.tyler.dugout.data.api.dto.VenueDto
import nicholos.tyler.dugout.data.api.dto.ProbablePitcherDto
import nicholos.tyler.dugout.model.domain.ProbablePitcher
import nicholos.tyler.dugout.model.domain.Game
import nicholos.tyler.dugout.model.domain.GameStatus
import nicholos.tyler.dugout.model.domain.GameTeams
import nicholos.tyler.dugout.model.domain.LeagueRecord
import nicholos.tyler.dugout.model.domain.Team
import nicholos.tyler.dugout.model.domain.TeamSide
import nicholos.tyler.dugout.model.domain.Venue

fun GameDto.toDomain(): Game {
    return Game(
        gamePk = gamePk,
        gameGuid = gameGuid,
        gameDate = gameDate,
        gameType = gameType,
        status = status?.toDomain(),
        teams = teams?.toDomain(),
        venue = venue?.toDomain(),
        scheduledInnings = scheduledInnings,
        seriesDescription = seriesDescription,
        linescore = linescore?.toDomain()
    )
}

fun LinescoreDto.toDomain(): nicholos.tyler.dugout.model.domain.Linescore {
    return nicholos.tyler.dugout.model.domain.Linescore(
        currentInning = currentInning,
        inningState = inningState,
        inningHalf = inningHalf,
        isTopInning = isTopInning,
        scheduledInnings = scheduledInnings,
        balls = balls,
        strikes = strikes,
        outs = outs,
        onFirst = offense?.first != null,
        onSecond = offense?.second != null,
        onThird = offense?.third != null
    )
}

fun GameStatusDto.toDomain(): GameStatus {
    return GameStatus(
        abstractGameState = abstractGameState,
        detailedState = detailedState,
        statusCode = statusCode
    )
}

fun GameTeamsDto.toDomain(): GameTeams {
    return GameTeams(
        home = home?.toDomain(),
        away = away?.toDomain()
    )
}

fun TeamSideDto.toDomain(): TeamSide {
    return TeamSide(
        team = team?.toDomain(),
        leagueRecord = leagueRecord?.toDomain(),
        score = score,
        probablePitcher = probablePitcher?.toDomain()
    )
}

fun ProbablePitcherDto.toDomain(): ProbablePitcher {
    return ProbablePitcher(
        id = id,
        fullName = fullName
    )
}

fun TeamDto.toDomain(): Team {
    return Team(
        id = id,
        name = name
    )
}

fun LeagueRecordDto.toDomain(): LeagueRecord {
    return LeagueRecord(
        wins = wins,
        losses = losses,
        pct = pct
    )
}

fun VenueDto.toDomain(): Venue {
    return Venue(
        id = id,
        name = name
    )
}