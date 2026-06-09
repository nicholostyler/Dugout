package nicholos.tyler.dugout.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface PlayerSearchDao {

    @Query(
        """
        SELECT *
        FROM cached_players
        WHERE season = :season
            AND (
                :query = ''
                OR fullName LIKE '%' || :query || '%' COLLATE NOCASE
                OR firstName LIKE '%' || :query || '%' COLLATE NOCASE
                OR lastName LIKE '%' || :query || '%' COLLATE NOCASE
                OR useName LIKE '%' || :query || '%' COLLATE NOCASE
            )
        ORDER BY fullName COLLATE NOCASE
        LIMIT :limit
        """
    )
    fun observePlayers(
        season: Int,
        query: String,
        limit: Int
    ): Flow<List<CachedPlayerEntity>>

    @Query("SELECT COUNT(*) FROM cached_players WHERE season = :season")
    suspend fun countPlayers(season: Int): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlayers(players: List<CachedPlayerEntity>)

    @Query("DELETE FROM cached_players WHERE season = :season")
    suspend fun deletePlayersForSeason(season: Int)

    @Transaction
    suspend fun replacePlayersForSeason(
        season: Int,
        players: List<CachedPlayerEntity>
    ) {
        deletePlayersForSeason(season)
        insertPlayers(players)
    }
}
