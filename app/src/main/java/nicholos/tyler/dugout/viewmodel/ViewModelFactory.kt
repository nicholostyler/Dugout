package nicholos.tyler.dugout.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import nicholos.tyler.dugout.data.repository.GamesRepository
import nicholos.tyler.dugout.data.repository.LeagueRepository

class DugoutViewModelFactory(
    private val repository: GamesRepository,
    private val leagueRepository: LeagueRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(TeamScheduleViewModel::class.java) -> {
                TeamScheduleViewModel(repository) as T
            }

            modelClass.isAssignableFrom(GameDetailViewModel::class.java) -> {
                GameDetailViewModel(repository) as T
            }
            modelClass.isAssignableFrom(PlayerViewModel::class.java) -> {
                PlayerViewModel(repository) as T
            }

            modelClass.isAssignableFrom(HomeViewModel::class.java) -> {
                HomeViewModel(repository, leagueRepository) as T
            }
            modelClass.isAssignableFrom(ScoresViewModel::class.java) -> {
                ScoresViewModel(repository) as T
            }

            modelClass.isAssignableFrom(RosterViewModel::class.java) -> {
                RosterViewModel(repository) as T
            }
            modelClass.isAssignableFrom(TeamPageViewModel::class.java) -> {
                TeamPageViewModel(repository, leagueRepository) as T
            }
            modelClass.isAssignableFrom(LeagueViewModel::class.java) -> {
                LeagueViewModel(leagueRepository) as T
            }

            else -> {
                throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
            }
        }
    }
}