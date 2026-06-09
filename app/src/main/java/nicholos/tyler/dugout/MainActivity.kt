package nicholos.tyler.dugout

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.svg.SvgDecoder
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import nicholos.tyler.dugout.data.api.NetworkModule
import nicholos.tyler.dugout.data.local.DugoutDatabase
import nicholos.tyler.dugout.data.repository.DugoutFirebaseRepository
import nicholos.tyler.dugout.data.repository.FirestoreGamesRepository
import nicholos.tyler.dugout.data.repository.GamesRepository
import nicholos.tyler.dugout.data.repository.LeagueRepository
import nicholos.tyler.dugout.data.repository.PlayerSearchRepository
import nicholos.tyler.dugout.model.domain.MlbTeams
import nicholos.tyler.dugout.navigation.DugoutRoute
import nicholos.tyler.dugout.navigation.GameDetailRoute
import nicholos.tyler.dugout.navigation.GamesRoute
import nicholos.tyler.dugout.navigation.HomeRoute
import nicholos.tyler.dugout.navigation.LeagueRoute
import nicholos.tyler.dugout.navigation.PlayerRoute
import nicholos.tyler.dugout.navigation.ScheduleRoute
import nicholos.tyler.dugout.navigation.SearchRoute
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
import nicholos.tyler.dugout.ui.screens.PlayerSearchScreen
import nicholos.tyler.dugout.ui.screens.RosterScreen
import nicholos.tyler.dugout.ui.screens.ScoresScreen
import nicholos.tyler.dugout.ui.screens.TeamPageScreen
import nicholos.tyler.dugout.ui.screens.TeamScheduleScreen
import nicholos.tyler.dugout.ui.theme.DugoutTheme
import nicholos.tyler.dugout.viewmodel.DugoutViewModelFactory
import nicholos.tyler.dugout.viewmodel.GameDetailViewModel
import nicholos.tyler.dugout.viewmodel.HomeViewModel
import nicholos.tyler.dugout.viewmodel.LeagueLeadersViewModel
import nicholos.tyler.dugout.viewmodel.LeagueViewModel
import nicholos.tyler.dugout.viewmodel.PlayerSearchViewModel
import nicholos.tyler.dugout.viewmodel.PlayerViewModel
import nicholos.tyler.dugout.viewmodel.RosterViewModel
import nicholos.tyler.dugout.viewmodel.ScheduleView
import nicholos.tyler.dugout.viewmodel.ScoresViewModel
import nicholos.tyler.dugout.viewmodel.TeamPageViewModel
import nicholos.tyler.dugout.viewmodel.TeamScheduleViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        createNotificationChannel()
        DugoutFirebaseRepository(this).initializeUser()


        val appContext = applicationContext

        setContent {
            DugoutTheme {
                val context = LocalContext.current
                val permissionLauncher = rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestPermission()
                ) { isGranted ->
                    // Handle result
                }

                LaunchedEffect(Unit) {
                    if (ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.POST_NOTIFICATIONS
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }

                val repository = remember { GamesRepository(NetworkModule.mlbApi) }
                val firestoreGamesRepository = remember { FirestoreGamesRepository() }
                val leagueRepository = remember { LeagueRepository(NetworkModule.mlbApi) }
                val database = remember { DugoutDatabase.getInstance(appContext) }
                val playerSearchRepository = remember {
                    PlayerSearchRepository(
                        api = NetworkModule.mlbApi,
                        playerSearchDao = database.playerSearchDao()
                    )
                }
                val factory = remember {
                    DugoutViewModelFactory(
                        repository = repository,
                        firestoreGamesRepository = firestoreGamesRepository,
                        leagueRepository = leagueRepository,
                        playerSearchRepository = playerSearchRepository
                    )
                }

                val homeViewModel: HomeViewModel = viewModel(factory = factory)
                val scheduleViewModel: TeamScheduleViewModel = viewModel(factory = factory)
                val detailViewModel: GameDetailViewModel = viewModel(factory = factory)
                val rosterViewModel: RosterViewModel = viewModel(factory = factory)
                val leagueViewModel: LeagueViewModel = viewModel(factory = factory)
                val scoresViewModel: ScoresViewModel = viewModel(factory = factory)
                val teamPageViewModel: TeamPageViewModel = viewModel(factory = factory)
                val playerViewModel: PlayerViewModel = viewModel(factory = factory)
                val playerSearchViewModel: PlayerSearchViewModel = viewModel(factory = factory)
                val statsViewModel: LeagueLeadersViewModel = viewModel(factory = factory)

                DugoutApp(
                    homeViewModel = homeViewModel,
                    scheduleViewModel = scheduleViewModel,
                    detailViewModel = detailViewModel,
                    rosterViewModel = rosterViewModel,
                    leagueViewModel = leagueViewModel,
                    scoresViewModel = scoresViewModel,
                    teamPageViewModel = teamPageViewModel,
                    playerViewModel = playerViewModel,
                    playerSearchViewModel = playerSearchViewModel,
                    statsViewModel = statsViewModel
                )
            }
        }
    }

    private fun createNotificationChannel() {
        val name = "Dugout Notifications"
        val descriptionText = "Game alerts and updates"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel("dugout_channel", name, importance).apply {
            description = descriptionText
        }
        val notificationManager: NotificationManager =
            getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}

@Composable
private fun ScheduleAppBarTitle(
    teamId: Int,
    modifier: Modifier = Modifier
) {
    val team = MlbTeams.get(teamId)

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        AppBarTeamLogo(
            teamId = teamId,
            teamName = team.shortName,
            modifier = Modifier.size(38.dp)
        )

        Spacer(Modifier.width(10.dp))

        Text(
            text = team.shortName.ifBlank { team.fullName },
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            maxLines = 1
        )

        Icon(
            imageVector = Icons.Default.KeyboardArrowDown,
            contentDescription = "Change team",
            modifier = Modifier.size(22.dp),
            tint = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun AppBarTeamLogo(
    teamId: Int,
    teamName: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    AsyncImage(
        model = ImageRequest.Builder(context)
            .data("https://www.mlbstatic.com/team-logos/$teamId.svg")
            .decoderFactory(SvgDecoder.Factory())
            .crossfade(true)
            .build(),
        contentDescription = "$teamName logo",
        contentScale = ContentScale.Fit,
        modifier = modifier.padding(4.dp)
    )
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
    playerViewModel: PlayerViewModel,
    playerSearchViewModel: PlayerSearchViewModel,
    statsViewModel: LeagueLeadersViewModel
) {
    val navigationState = rememberDugoutNavigationState()
    val currentRoute = navigationState.backStack.lastOrNull()
    val isSearchOpen = currentRoute == SearchRoute
    val visibleBackStack =
        if (isSearchOpen && navigationState.backStack.size > 1) {
            navigationState.backStack.dropLast(1)
        } else {
            navigationState.backStack
        }
    val visibleRoute = visibleBackStack.lastOrNull() ?: HomeRoute
    val scheduleUiState by scheduleViewModel.uiState.collectAsStateWithLifecycle()
    val openPlayerPage: (Int) -> Unit = { playerId ->
        navigationState.navigateToPlayer(teamId = 0, playerId = playerId)
    }

    BackHandler(enabled = isSearchOpen) {
        navigationState.goBack()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                ),
                title = {
                    val route = visibleRoute
                    if (route == ScheduleRoute || route is TeamScheduleRoute) {
                        ScheduleAppBarTitle(
                            teamId = if (route is TeamScheduleRoute) route.teamId else 143
                        )
                    } else {
                        Text(titleForRoute(route))
                    }
                },
                navigationIcon = {
                    if (visibleBackStack.size > 1) {
                        IconButton(onClick = { navigationState.goBack() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    }
                },
                actions = {
                    when (val route = visibleRoute) {
                        is PlayerRoute -> {
                            IconButton(
                                onClick = { playerViewModel.refreshIfNeeded() }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Refresh"
                                )
                            }
                        }

                        is TeamRosterRoute -> {
                            IconButton(
                                onClick = { rosterViewModel.refreshIfNeeded(teamId = route.teamId) }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Refresh"
                                )
                            }
                        }

                        ScheduleRoute, is TeamScheduleRoute -> {
                            IconButton(
                                onClick = { navigationState.navigateToSearch() }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search players"
                                )
                            }

                            IconButton(
                                onClick = {
                                    val nextView = if (scheduleUiState.selectedView == ScheduleView.Calendar) {
                                        ScheduleView.List
                                    } else {
                                        ScheduleView.Calendar
                                    }
                                    scheduleViewModel.selectView(nextView)
                                }
                            ) {
                                val icon = if (scheduleUiState.selectedView == ScheduleView.Calendar) {
                                    Icons.AutoMirrored.Filled.List
                                } else {
                                    Icons.Default.CalendarMonth
                                }
                                Icon(
                                    imageVector = icon,
                                    contentDescription = if (scheduleUiState.selectedView == ScheduleView.Calendar) {
                                        "Show list view"
                                    } else {
                                        "Show calendar view"
                                    }
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

                        is LeagueRoute -> {
                            IconButton(
                                onClick = { 
                                    leagueViewModel.refreshIfNeeded()
                                    statsViewModel.refreshIfNeeded()
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Refresh"
                                )
                            }
                        }

                        is GameDetailRoute -> {
                            IconButton(
                                onClick = { detailViewModel.refresh() }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Refresh"
                                )
                            }
                        }

                        else -> Unit
                    }

                    if (
                        visibleRoute != ScheduleRoute &&
                        visibleRoute !is TeamScheduleRoute
                    ) {
                        IconButton(onClick = { navigationState.navigateToSearch() }) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search players"
                            )
                        }
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
                backStack = visibleBackStack,
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
                        },
                        onPlayerClick = openPlayerPage
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
                        statsViewModel = statsViewModel,
                        onTeamClick = { teamId ->
                            navigationState.navigateToTeamPage(teamId)
                        },
                        onPlayerClick = openPlayerPage
                    )
                }

                entry<GameDetailRoute> { route ->
                    GameDetailScreen(
                        viewModel = detailViewModel,
                        playerViewModel = playerViewModel,
                        gamePk = route.gamePk,
                        modifier = Modifier.padding(innerPadding),
                        onPlayerClick = openPlayerPage
                    )
                }

                entry<TeamRosterRoute> { route ->
                    RosterScreen(
                        viewModel = rosterViewModel,
                        teamId = route.teamId,
                        modifier = Modifier.padding(innerPadding),
                        onPlayerClick = openPlayerPage
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
                        },
                        onPlayerClick = openPlayerPage
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

        if (isSearchOpen) {
            PlayerSearchScreen(
                modifier = Modifier.fillMaxSize(),
                viewModel = playerSearchViewModel,
                onBack = { navigationState.goBack() },
                onPlayerClick = openPlayerPage
            )
        }
    }

}

private fun titleForRoute(route: DugoutRoute): String {
    return when (route) {
        HomeRoute -> "Home"
        GamesRoute -> "Games"
        ScheduleRoute -> "Schedule"
        ScoresRoute -> "Scores"
        SearchRoute -> "Player Search"
        LeagueRoute -> "League"
        is GameDetailRoute -> "Game Detail"
        is TeamRosterRoute -> "Team Roster"
        is TeamPageRoute -> "Team"
        is TeamScheduleRoute -> "Team Schedule"
        is PlayerRoute -> "Player"
    }
}
