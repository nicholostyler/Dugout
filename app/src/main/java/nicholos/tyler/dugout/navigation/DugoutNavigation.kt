package nicholos.tyler.dugout.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
import nicholos.tyler.dugout.model.domain.TeamRoster

@Serializable
sealed interface DugoutRoute : NavKey

@Serializable
data object HomeRoute : DugoutRoute

@Serializable
data object GamesRoute : DugoutRoute

@Serializable
data object LeagueRoute : DugoutRoute

@Serializable
data object ScheduleRoute : DugoutRoute

@Serializable
data object ScoresRoute : DugoutRoute

@Serializable
data class GameDetailRoute(
    val gamePk: Int
) : DugoutRoute

@Serializable
data class TeamRosterRoute(
    val teamId: Int
) : DugoutRoute
enum class TopLevelDestination {
    HOME, SCHEDULE, SCORES, LEAGUE
}

@Stable
class DugoutNavigationState(
    val backStack: SnapshotStateList<DugoutRoute> = mutableStateListOf(HomeRoute)
) {
    fun currentTopLevel(): TopLevelDestination {
        return when (backStack.firstOrNull()) {
            is ScheduleRoute -> TopLevelDestination.SCHEDULE
            is ScoresRoute -> TopLevelDestination.SCORES
            is LeagueRoute -> TopLevelDestination.LEAGUE
            else -> TopLevelDestination.HOME
        }
    }

    fun navigateToTopLevel(destination: TopLevelDestination) {
        val root = when (destination) {
            TopLevelDestination.HOME -> HomeRoute
            TopLevelDestination.SCHEDULE -> ScheduleRoute
            TopLevelDestination.SCORES -> ScoresRoute
            TopLevelDestination.LEAGUE -> LeagueRoute
        }

        backStack.clear()
        backStack.add(root)
    }

    fun navigateToGameDetail(gamePk: Int) {
        backStack.add(GameDetailRoute(gamePk))
    }

    fun navigateTeamRoster(teamId: Int) {
        backStack.add(TeamRosterRoute(teamId))
    }

    fun canGoBack(): Boolean = backStack.size > 1

    fun goBack() {
        if (canGoBack()) {
            backStack.removeLast()
        }
    }

    fun title(): String {
        return when (backStack.last()) {
            HomeRoute -> "Home"
            ScheduleRoute -> "Schedule"
            ScoresRoute -> "Scores"
            LeagueRoute -> "League"
            is GameDetailRoute -> "Game Detail"
            is TeamRosterRoute -> "Team Roster"
            else -> "Home"
        }
    }
}

@Composable
fun rememberDugoutNavigationState(): DugoutNavigationState {
    return remember { DugoutNavigationState() }
}