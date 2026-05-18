package nicholos.tyler.dugout.model.mapper

import android.util.Log
import nicholos.tyler.dugout.data.api.dto.GameFeedResponseDto
import nicholos.tyler.dugout.data.api.dto.PlayDto
import nicholos.tyler.dugout.model.domain.GameDetails
import nicholos.tyler.dugout.model.domain.InningScore
import nicholos.tyler.dugout.model.domain.PlayItem

import nicholos.tyler.dugout.model.domain.PitchItem

fun GameFeedResponseDto.toGameDetails(gamePk: Int): GameDetails {
    val gameData = gameData
    val liveData = liveData
    val linescore = liveData?.linescore
    val plays = liveData?.plays?.allPlays.orEmpty()
    val lastPlay = plays.lastOrNull()

    val playList = plays
        .map { it.toPlayItem() }
        .reversed()

    val inningScores = linescore?.innings?.map {
        InningScore(
            number = it.num,
            awayRuns = it.away?.runs,
            homeRuns = it.home?.runs
        )
    }.orEmpty()

    Log.d("help me","inningState=${linescore?.inningState} currentInning=${linescore?.currentInning} detailedState=${gameData?.status?.detailedState}"
    )

    android.util.Log.d(
        "DUGOUT_FEED",
        "inningState=${linescore?.inningState} currentInning=${linescore?.currentInning} detailedState=${gameData?.status?.detailedState}"
    )

    return GameDetails(
        gamePk = gamePk,
        awayTeam = gameData?.teams?.away?.name,
        homeTeam = gameData?.teams?.home?.name,
        awayTeamId = gameData?.teams?.away?.id ?: 0,
        homeTeamId = gameData?.teams?.home?.id ?: 0,
        awayScore = linescore?.teams?.away?.runs ?: 0,
        homeScore = linescore?.teams?.home?.runs ?: 0,
        awayHits = linescore?.teams?.away?.hits ?: 0,
        homeHits = linescore?.teams?.home?.hits ?: 0,
        awayErrors = linescore?.teams?.away?.errors ?: 0,
        homeErrors = linescore?.teams?.home?.errors ?: 0,
        status = gameData?.status?.detailedState,
        venue = gameData?.venue?.name,
        currentInning = linescore?.currentInning ?: 0,
        inningState = linescore?.inningState,
        awayProbablePitcher = gameData?.probablePitchers?.away?.fullName,
        homeProbablePitcher = gameData?.probablePitchers?.home?.fullName,
        lastPlayDescription = lastPlay?.result?.description,
        lastPlayEvent = lastPlay?.result?.event,
        balls = lastPlay?.count?.balls ?: 0,
        strikes = lastPlay?.count?.strikes ?: 0,
        outs = lastPlay?.count?.outs ?: 0,
        currentBatter = lastPlay?.matchup?.batter?.fullName,
        currentPitcher = lastPlay?.matchup?.pitcher?.fullName,
        officialDate = gameData?.datetime?.officialDate,
        startDateTime = gameData?.datetime?.dateTime,
        onFirst = linescore?.offense?.first != null,
        onSecond = linescore?.offense?.second != null,
        onThird = linescore?.offense?.third != null,
        plays = playList,
        innings = inningScores
    )
}

fun PlayDto.toPlayItem(): PlayItem {
    return PlayItem(
        description = result?.description,
        event = result?.event,
        inning = about?.inning ?: 0,
        isTopInning = about?.isTopInning ?: false,
        batterName = matchup?.batter?.fullName,
        pitcherName = matchup?.pitcher?.fullName,
        batterId = matchup?.batter?.id,
        pitches = playEvents.filter { it.pitchData != null || it.details?.call != null }
            .map {
                val coords = it.pitchData?.coordinates ?: it.coordinates
                val px = coords?.pX ?: coords?.pxLower ?: coords?.plateX
                val pz = coords?.pZ ?: coords?.pzLower ?: coords?.plateZ

                PitchItem(
                    result = it.details?.description ?: it.details?.call?.description,
                    velocity = it.pitchData?.startSpeed,
                    px = px,
                    pz = pz,
                    pitchType = it.details?.type?.description,
                    balls = it.count?.balls,
                    strikes = it.count?.strikes
                )
            }
    )
}