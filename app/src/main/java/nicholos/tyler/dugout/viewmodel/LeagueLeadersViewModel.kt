package nicholos.tyler.dugout.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import nicholos.tyler.dugout.data.repository.LeagueRepository
import nicholos.tyler.dugout.model.ui.LeagueLeadersUiState
import java.time.LocalDate

class LeagueLeadersViewModel(
    private val leagueRepository: LeagueRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LeagueLeadersUiState())
    val uiState: StateFlow<LeagueLeadersUiState> = _uiState.asStateFlow()

    private var currentJob: Job? = null
    private var lastRefreshDate: LocalDate? = null
    
    private var lastCategories: List<String>? = null
    private var lastGroup: String? = null
    private var lastSeason: Int? = null
    private var lastLeagueId: String? = null
    private var lastStatType: String? = null

    init {
        loadLeaders()
    }

    fun loadLeaders(
        categories: List<String> = listOf("homeRuns"),
        group: String = "hitting",
        season: Int = LocalDate.now().year,
        leagueId: String? = null,
        statType: String? = "season",
        forceRefresh: Boolean = false
    ) {
        if (!forceRefresh && 
            lastCategories == categories && 
            lastGroup == group && 
            lastSeason == season && 
            lastLeagueId == leagueId && 
            lastStatType == statType &&
            _uiState.value.stats.isNotEmpty()) return

        lastCategories = categories
        lastGroup = group
        lastSeason = season
        lastLeagueId = leagueId
        lastStatType = statType

        currentJob?.cancel()
        currentJob = viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            runCatching {
                leagueRepository.getLeagueLeaders(
                    categories = categories,
                    group = group,
                    season = season,
                    leagueId = leagueId,
                    statType = statType
                )
            }.onSuccess { stats ->
                _uiState.value = LeagueLeadersUiState(
                    isLoading = false,
                    stats = stats
                )
                lastRefreshDate = LocalDate.now()
            }.onFailure { throwable ->
                _uiState.value = LeagueLeadersUiState(
                    isLoading = false,
                    error = throwable.message ?: "Failed to load leaders"
                )
            }
        }
    }

    fun refreshIfNeeded() {
        if (lastRefreshDate?.isBefore(LocalDate.now()) ?: true) {
            loadLeaders(
                categories = lastCategories ?: listOf("homeRuns"),
                group = lastGroup ?: "hitting",
                season = lastSeason ?: LocalDate.now().year,
                leagueId = lastLeagueId,
                statType = lastStatType ?: "season",
                forceRefresh = true
            )
        }
    }
}
