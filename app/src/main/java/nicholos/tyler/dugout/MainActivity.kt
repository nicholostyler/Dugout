package nicholos.tyler.dugout

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SportsBaseball
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import nicholos.tyler.dugout.data.api.NetworkModule
import nicholos.tyler.dugout.data.repository.GamesRepository
import nicholos.tyler.dugout.data.repository.LeagueRepository
import nicholos.tyler.dugout.navigation.DugoutRoute
import nicholos.tyler.dugout.navigation.GameDetailRoute
import nicholos.tyler.dugout.navigation.HomeRoute
import nicholos.tyler.dugout.navigation.LeagueRoute
import nicholos.tyler.dugout.navigation.PlayerRoute
import nicholos.tyler.dugout.navigation.ScheduleRoute
import nicholos.tyler.dugout.navigation.ScoresRoute
import nicholos.tyler.dugout.navigation.TeamPageRoute
import nicholos.tyler.dugout.navigation.TeamRosterRoute
import nicholos.tyler.dugout.navigation.TeamScheduleRoute
import nicholos.tyler.dugout.navigation.TopLevelDestination
import nicholos.tyler.dugout.navigation.rememberDugoutNavigationState
import nicholos.tyler.dugout.ui.screens.GameDetailScreen
import nicholos.tyler.dugout.ui.screens.HomeScreen
import nicholos.tyler.dugout.ui.screens.LeagueScreen
import nicholos.tyler.dugout.ui.screens.PlayerScreen
import nicholos.tyler.dugout.ui.screens.RosterScreen
import nicholos.tyler.dugout.ui.screens.ScoresScreen
import nicholos.tyler.dugout.ui.screens.TeamPageScreen
import nicholos.tyler.dugout.ui.screens.TeamScheduleScreen
import nicholos.tyler.dugout.ui.screens.TodayGamesScreen
import nicholos.tyler.dugout.ui.theme.DugoutTheme
import nicholos.tyler.dugout.viewmodel.DugoutViewModelFactory
import nicholos.tyler.dugout.viewmodel.GameDetailViewModel
import nicholos.tyler.dugout.viewmodel.HomeViewModel
import nicholos.tyler.dugout.viewmodel.LeagueViewModel
import nicholos.tyler.dugout.viewmodel.PlayerViewModel
import nicholos.tyler.dugout.viewmodel.RosterViewModel
import nicholos.tyler.dugout.viewmodel.ScoresViewModel
import nicholos.tyler.dugout.viewmodel.TeamPageViewModel
import nicholos.tyler.dugout.viewmodel.TeamScheduleViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            DugoutTheme {
                val repository = remember { GamesRepository(NetworkModule.mlbApi) }
                val leagueRepository = remember { LeagueRepository(NetworkModule.mlbApi) }
                val factory = remember { DugoutViewModelFactory(repository, leagueRepository) }

                val homeViewModel: HomeViewModel = viewModel(factory = factory)
                val scheduleViewModel: TeamScheduleViewModel = viewModel(factory = factory)
                val detailViewModel: GameDetailViewModel = viewModel(factory = factory)
                val rosterViewModel: RosterViewModel = viewModel(factory = factory)
                val leagueViewModel: LeagueViewModel = viewModel(factory = factory)
                val scoresViewModel: ScoresViewModel = viewModel(factory = factory)
                val teamPageViewModel: TeamPageViewModel = viewModel(factory = factory)
                val playerViewModel: PlayerViewModel = viewModel(factory = factory)

                DugoutApp(
                    homeViewModel = homeViewModel,
                    scheduleViewModel = scheduleViewModel,
                    detailViewModel = detailViewModel,
                    rosterViewModel = rosterViewModel,
                    leagueViewModel = leagueViewModel,
                    scoresViewModel = scoresViewModel,
                    teamPageViewModel = teamPageViewModel,
                    playerViewModel = playerViewModel
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DugoutApp(
    homeViewModel: HomeViewModel,
    scheduleViewModel: TeamScheduleViewModel,
    detailViewModel: GameDetailViewModel,
    rosterViewModel: RosterViewModel,
    leagueViewModel: LeagueViewModel,
    scoresViewModel: ScoresViewModel,
    teamPageViewModel: TeamPageViewModel,
    playerViewModel: PlayerViewModel

) {
    val navigationState = rememberDugoutNavigationState()
    val currentRoute = navigationState.backStack.lastOrNull()

    BackHandler(enabled = navigationState.canGoBack()) {
        navigationState.goBack()
    }

    Scaffold(
        //containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                ),
                title = { Text(navigationState.title()) },
                navigationIcon = {
                    if (navigationState.canGoBack()) {
                        IconButton(onClick = { navigationState.goBack() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    }
                },
                actions = {
                    when (val route = currentRoute) {
                        HomeRoute -> {
                            IconButton(
                                onClick = { homeViewModel.refreshIfNeeded(teamId = 143) }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Refresh"
                                )
                            }
                        }

                        is TeamPageRoute -> {
                            IconButton(
                                onClick = { teamPageViewModel.refreshIfNeeded(teamId = route.teamId) }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Refresh"
                                )
                            }
                        }

                        else -> Unit
                    }

                    IconButton(onClick = { }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings"
                        )
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = navigationState.currentTopLevel() == TopLevelDestination.HOME,
                    onClick = { navigationState.navigateToTopLevel(TopLevelDestination.HOME) },
                    icon = { Icon(Icons.Default.Home, contentDescription = null) },
                    label = { Text("Home") }
                )

                NavigationBarItem(
                    selected = navigationState.currentTopLevel() == TopLevelDestination.SCHEDULE,
                    onClick = { navigationState.navigateToTopLevel(TopLevelDestination.SCHEDULE) },
                    icon = { Icon(Icons.Default.CalendarMonth, contentDescription = null) },
                    label = { Text("Schedule") }
                )

                NavigationBarItem(
                    selected = navigationState.currentTopLevel() == TopLevelDestination.SCORES,
                    onClick = { navigationState.navigateToTopLevel(TopLevelDestination.SCORES) },
                    icon = { Icon(Icons.Default.SportsBaseball, contentDescription = null) },
                    label = { Text("Scores") }
                )

                NavigationBarItem(
                    selected = navigationState.currentTopLevel() == TopLevelDestination.LEAGUE,
                    onClick = { navigationState.navigateToTopLevel(TopLevelDestination.LEAGUE) },
                    icon = { Icon(Icons.Default.Leaderboard, contentDescription = null) },
                    label = { Text("League") }
                )
            }
        }
    ) { innerPadding ->
        NavDisplay(
            backStack = navigationState.backStack,
            onBack = { navigationState.goBack() },
            entryDecorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator()
            ),
            transitionSpec = {
                val from = initialState.key
                val to = targetState.key

                val isPushToDetail =
                    from !is GameDetailRoute && to is GameDetailRoute

                if (isPushToDetail) {
                    (slideInHorizontally { fullWidth -> fullWidth / 8 } + fadeIn()) togetherWith
                            (slideOutHorizontally { fullWidth -> -fullWidth / 16 } + fadeOut())
                } else {
                    fadeIn() togetherWith fadeOut()
                }
            },
            popTransitionSpec = {
                val from = initialState.key
                val to = targetState.key

                val isPopFromDetail =
                    from is GameDetailRoute && to !is GameDetailRoute

                if (isPopFromDetail) {
                    (slideInHorizontally { fullWidth -> -fullWidth / 16 } + fadeIn()) togetherWith
                            (slideOutHorizontally { fullWidth -> fullWidth / 8 } + fadeOut())
                } else {
                    fadeIn() togetherWith fadeOut()
                }
            },
            predictivePopTransitionSpec = {
                fadeIn() togetherWith fadeOut()
            },
            entryProvider = entryProvider<DugoutRoute> {
                entry<HomeRoute> {
                    HomeScreen(
                        viewModel = homeViewModel,
                        teamId = 143,
                        modifier = Modifier.padding(innerPadding),
                        onTodaysGameClick = { gamePk ->
                            detailViewModel.loadGame(gamePk)
                            navigationState.navigateToGameDetail(gamePk)
                        },
                        onStretchGameClick = { gamePk ->
                            detailViewModel.loadGame(gamePk)
                            navigationState.navigateToGameDetail(gamePk)
                        },
                        onSeasonScheduleClick = {
                            navigationState.navigateToTeamSchedule(teamId=143,season=2026)
                        },
                        onTeamRosterClick = {
                            navigationState.navigateTeamRoster(143)
                        },
                        onViewLeagueClick = {
                            navigationState.navigateToTopLevel(TopLevelDestination.LEAGUE)
                        }
                    )
                }

                entry<ScheduleRoute> {
                    TeamScheduleScreen(
                        viewModel = scheduleViewModel,
                        teamId = 143,
                        season = 2026,
                        modifier = Modifier.padding(innerPadding),
                        onGameClick = { gamePk ->
                            detailViewModel.loadGame(gamePk)
                            navigationState.navigateToGameDetail(gamePk)
                        }
                    )
                }

                entry<ScoresRoute> {
                    ScoresScreen(
                        modifier = Modifier.padding(innerPadding),
                        viewModel = scoresViewModel,
                        onGameClick = { gamePk ->
                            detailViewModel.loadGame(gamePk)
                            navigationState.navigateToGameDetail(gamePk)
                        }
                    )
                }

                entry<LeagueRoute> {
                    LeagueScreen(
                        modifier = Modifier.padding(innerPadding),
                        viewModel = leagueViewModel,
                        onTeamClick = { teamId ->
                            navigationState.navigateToTeamPage(teamId)
                        }
                    )
                }



                entry<GameDetailRoute> { route ->
                    GameDetailScreen(
                        viewModel = detailViewModel,
                        gamePk = route.gamePk,
                        modifier = Modifier.padding(innerPadding)
                    )
                }

                entry<TeamRosterRoute> { route ->
                    RosterScreen(
                        viewModel = rosterViewModel,
                        teamId = route.teamId,
                        modifier = Modifier.padding(innerPadding),
                        onPlayerClick = { playerId ->
                            navigationState.navigateToPlayer(
                                teamId = route.teamId,
                                playerId = playerId
                            )
                        }
                    )
                }

                entry<PlayerRoute> { route ->
                    PlayerScreen(
                        viewModel = playerViewModel,
                        playerId = route.playerId,
                        modifier = Modifier.padding(innerPadding)
                    )
                }

                entry<TeamPageRoute> { route ->
                    TeamPageScreen(
                        viewModel = teamPageViewModel,
                        teamId = route.teamId,
                        modifier = Modifier.padding(innerPadding),
                        onTodaysGameClick = { gamePk ->
                            detailViewModel.loadGame(gamePk)
                            navigationState.navigateToGameDetail(gamePk)
                        },
                        onStretchGameClick = { gamePk ->
                            detailViewModel.loadGame(gamePk)
                            navigationState.navigateToGameDetail(gamePk)
                        },
                        onSeasonScheduleClick = {
                            navigationState.navigateToTeamSchedule(teamId = route.teamId, season = 2026)
                        },
                        onTeamRosterClick = {
                            navigationState.navigateTeamRoster(route.teamId)
                        },
                        onViewLeagueClick = {
                            navigationState.navigateToTopLevel(TopLevelDestination.LEAGUE)
                        }
                    )
                }
                entry<TeamScheduleRoute> { route ->
                    TeamScheduleScreen(
                        viewModel = scheduleViewModel,
                        teamId = route.teamId,
                        season = route.season,
                        modifier = Modifier.padding(innerPadding),
                        onGameClick = { gamePk ->
                            detailViewModel.loadGame(gamePk)
                            navigationState.navigateToGameDetail(gamePk)
                        }
                    )
                }
            }
        )
    }
}