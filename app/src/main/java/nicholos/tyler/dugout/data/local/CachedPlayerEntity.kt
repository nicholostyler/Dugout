package nicholos.tyler.dugout.data.local

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "cached_players",
    primaryKeys = ["id", "season"],
    indices = [
        Index(value = ["season"]),
        Index(value = ["fullName"]),
        Index(value = ["lastName"])
    ]
)
data class CachedPlayerEntity(
    val id: Int,
    val season: Int,
    val fullName: String,
    val firstName: String?,
    val lastName: String?,
    val useName: String?,
    val jerseyNumber: String?,
    val position: String?,
    val teamId: Int?,
    val teamName: String?,
    val bats: String?,
    val throwsHand: String?,
    val active: Boolean
)
