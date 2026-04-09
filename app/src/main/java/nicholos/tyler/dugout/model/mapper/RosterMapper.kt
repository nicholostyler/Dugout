package nicholos.tyler.dugout.model.mapper

import nicholos.tyler.dugout.data.api.dto.PersonStatWrapperDto
import nicholos.tyler.dugout.data.api.dto.RosterResponseDto
import nicholos.tyler.dugout.data.api.dto.RosterPlayerDto
import nicholos.tyler.dugout.model.domain.StatLeader
import nicholos.tyler.dugout.model.domain.TeamMVPs

fun RosterResponseDto.toTeamMVPs(): TeamMVPs {
    val players = roster

    return TeamMVPs(
        battingAverageLeader = players
            .mapNotNull { player ->
                val value = player.hittingStats()?.avg?.toDoubleOrNull() ?: return@mapNotNull null
                LeaderCandidate(
                    playerId = player.person.id,
                    playerName = player.person.fullName,
                    numericValue = value,
                    displayValue = formatAverage(value)
                )
            }
            .maxByOrNull { it.numericValue }
            ?.toStatLeader("AVG"),

        homeRunLeader = players
            .mapNotNull { player ->
                val value = player.hittingStats()?.homeRuns ?: return@mapNotNull null
                LeaderCandidate(
                    playerId = player.person.id,
                    playerName = player.person.fullName,
                    numericValue = value.toDouble(),
                    displayValue = value.toString()
                )
            }
            .maxByOrNull { it.numericValue }
            ?.toStatLeader("HR"),

        rbiLeader = players
            .mapNotNull { player ->
                val value = player.hittingStats()?.rbi ?: return@mapNotNull null
                LeaderCandidate(
                    playerId = player.person.id,
                    playerName = player.person.fullName,
                    numericValue = value.toDouble(),
                    displayValue = value.toString()
                )
            }
            .maxByOrNull { it.numericValue }
            ?.toStatLeader("RBI"),

        eraLeader = players
            .mapNotNull { player ->
                val value = player.pitchingStats()?.era?.toDoubleOrNull() ?: return@mapNotNull null
                if (value <= 0.0) return@mapNotNull null

                LeaderCandidate(
                    playerId = player.person.id,
                    playerName = player.person.fullName,
                    numericValue = value,
                    displayValue = formatEra(value)
                )
            }
            .minByOrNull { it.numericValue }
            ?.toStatLeader("ERA"),

        strikeoutLeader = players
            .mapNotNull { player ->
                val value = player.pitchingStats()?.strikeOuts ?: return@mapNotNull null
                LeaderCandidate(
                    playerId = player.person.id,
                    playerName = player.person.fullName,
                    numericValue = value.toDouble(),
                    displayValue = value.toString()
                )
            }
            .maxByOrNull { it.numericValue }
            ?.toStatLeader("SO")
    )
}

private data class LeaderCandidate(
    val playerId: Int,
    val playerName: String,
    val numericValue: Double,
    val displayValue: String
)

private fun LeaderCandidate.toStatLeader(label: String): StatLeader {
    return StatLeader(
        playerId = playerId,
        playerName = playerName,
        statLabel = label,
        statValue = displayValue
    )
}

private fun RosterPlayerDto.hittingStats() =
    person.stats.firstStat(group = "hitting")

private fun RosterPlayerDto.pitchingStats() =
    person.stats.firstStat(group = "pitching")

private fun List<PersonStatWrapperDto>.firstStat(group: String) =
    firstOrNull {
        it.group?.displayName.equals(group, ignoreCase = true)
    }?.splits?.firstOrNull()?.stat

private fun formatAverage(value: Double): String = String.format("%.3f", value)
private fun formatEra(value: Double): String = String.format("%.2f", value)