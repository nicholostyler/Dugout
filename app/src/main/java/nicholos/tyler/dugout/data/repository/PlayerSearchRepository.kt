package nicholos.tyler.dugout.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import nicholos.tyler.dugout.data.api.MlbApiService
import nicholos.tyler.dugout.data.api.dto.PlayerApiDto
import nicholos.tyler.dugout.data.local.CachedPlayerEntity
import nicholos.tyler.dugout.data.local.PlayerSearchDao
import nicholos.tyler.dugout.model.domain.PlayerSearchResult

class PlayerSearchRepository(
    private val api: MlbApiService,
    private val playerSearchDao: PlayerSearchDao
) {

    fun searchPlayers(
        season: Int,
        query: String,
        limit: Int = 80
    ): Flow<List<PlayerSearchResult>> {
        return playerSearchDao.observePlayers(
            season = season,
            query = query.trim(),
            limit = limit
        ).map { players ->
            players.map { it.toDomain() }
        }
    }

    suspend fun syncPlayers(
        season: Int,
        forceRefresh: Boolean = false
    ) {
        if (!forceRefresh && playerSearchDao.countPlayers(season) > 0) return

        val players = api.getSportPlayers(season = season)
            .people
            .filter { it.fullName.isNotBlank() }
            .distinctBy { it.id }
            .map { it.toEntity(season) }

        playerSearchDao.replacePlayersForSeason(season, players)
    }
}

private fun PlayerApiDto.toEntity(season: Int): CachedPlayerEntity {
    return CachedPlayerEntity(
        id = id,
        season = season,
        fullName = fullName,
        firstName = firstName,
        lastName = lastName,
        useName = useName,
        jerseyNumber = primaryNumber,
        position = primaryPosition?.abbreviation,
        teamId = currentTeam?.id,
        teamName = currentTeam?.name,
        bats = batSide?.code,
        throwsHand = pitchHand?.code,
        active = active ?: true
    )
}

private fun CachedPlayerEntity.toDomain(): PlayerSearchResult {
    return PlayerSearchResult(
        id = id,
        fullName = fullName,
        jerseyNumber = jerseyNumber,
        position = position,
        teamId = teamId,
        teamName = teamName,
        bats = bats,
        throwsHand = throwsHand,
        active = active
    )
}
