package nicholos.tyler.dugout.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import java.time.LocalDate
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import nicholos.tyler.dugout.model.domain.MlbTeams
import nicholos.tyler.dugout.data.repository.PlayerSearchRepository
import nicholos.tyler.dugout.model.domain.PlayerSearchResult
import nicholos.tyler.dugout.model.ui.PlayerSearchResultUiModel
import nicholos.tyler.dugout.model.ui.PlayerSearchUiState

@OptIn(ExperimentalCoroutinesApi::class)
class PlayerSearchViewModel(
    private val repository: PlayerSearchRepository
) : ViewModel() {

    private val season = LocalDate.now().year
    private val query = MutableStateFlow("")
    private val syncState = MutableStateFlow(PlayerSearchSyncState())

    private val players = query
        .flatMapLatest { searchText ->
            repository.searchPlayers(
                season = season,
                query = searchText
            )
        }
        .map { results -> results.map { it.toUiModel() } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    val uiState: StateFlow<PlayerSearchUiState> = combine(
        query,
        syncState.asStateFlow(),
        players
    ) { searchText, sync, playerResults ->
        PlayerSearchUiState(
            query = searchText,
            season = season,
            players = playerResults,
            isSyncing = sync.isSyncing,
            hasLoadedCache = sync.hasLoadedCache,
            error = sync.error
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = PlayerSearchUiState(season = season)
    )

    init {
        syncPlayers()
    }

    fun onQueryChanged(value: String) {
        query.value = value
    }

    fun syncPlayers(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            syncState.update {
                it.copy(isSyncing = true, error = null)
            }

            runCatching {
                repository.syncPlayers(
                    season = season,
                    forceRefresh = forceRefresh
                )
            }.onSuccess {
                syncState.update {
                    it.copy(
                        isSyncing = false,
                        hasLoadedCache = true,
                        error = null
                    )
                }
            }.onFailure { throwable ->
                syncState.update {
                    it.copy(
                        isSyncing = false,
                        hasLoadedCache = true,
                        error = throwable.message ?: "Unable to refresh players"
                    )
                }
            }
        }
    }
}

private data class PlayerSearchSyncState(
    val isSyncing: Boolean = false,
    val hasLoadedCache: Boolean = false,
    val error: String? = null
)

private fun PlayerSearchResult.toUiModel(): PlayerSearchResultUiModel {
    val handedness = listOfNotNull(
        bats?.let { "Bats $it" },
        throwsHand?.let { "Throws $it" }
    ).joinToString(" / ").ifBlank { null }

    return PlayerSearchResultUiModel(
        id = id,
        fullName = fullName,
        jerseyNumber = jerseyNumber,
        position = position ?: "--",
        teamId = teamId,
        teamName = teamId?.let { MlbTeams.get(teamId = it, fallbackName = teamName).fullName }
            ?: teamName,
        handedness = handedness,
        active = active
    )
}
