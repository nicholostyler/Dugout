package nicholos.tyler.dugout.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneId
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import nicholos.tyler.dugout.data.repository.FirestoreGamesRepository
import nicholos.tyler.dugout.data.repository.GamesRepository
import nicholos.tyler.dugout.model.domain.CachedGame
import nicholos.tyler.dugout.model.mapper.toScoresSnapshotCardUiModel
import nicholos.tyler.dugout.model.ui.GameSnapshotCardUiModel
import nicholos.tyler.dugout.model.ui.ScoresUiState

class ScoresViewModel(
    _repository: GamesRepository,
    private val firestoreRepository: FirestoreGamesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScoresUiState())
    val uiState: StateFlow<ScoresUiState> = _uiState.asStateFlow()

    private var observationJob: Job? = null
    private var cachedTodayGames: List<CachedGame> = emptyList()
    private var hasResolvedInitialLoad = false

    init {
        loadScores(LocalDate.now())
    }

    fun onDateSelected(date: LocalDate) {
        loadScores(date)
    }

    fun loadScores(date: LocalDate, forceRefresh: Boolean = false) {
        _uiState.update { current ->
            current.copy(
                isLoading = !hasResolvedInitialLoad,
                isRefreshing = forceRefresh && hasResolvedInitialLoad && date == LocalDate.now(),
                games = if (hasResolvedInitialLoad) cachedTodayGames.toUiModelsFor(date) else current.games,
                selectedDate = date,
                error = null
            )
        }

        if (forceRefresh || observationJob == null) {
            startObservingTodayGames()
            return
        }

        if (hasResolvedInitialLoad) {
            publishGamesForDate(date)
        }
    }

    fun refresh() {
        loadScores(_uiState.value.selectedDate, forceRefresh = true)
    }

    fun refreshIfNeeded() {
        if (shouldRefresh()) {
            loadScores(_uiState.value.selectedDate, forceRefresh = true)
        }
    }

    private fun shouldRefresh(): Boolean {
        val selectedDate = _uiState.value.selectedDate
        val isToday = selectedDate == LocalDate.now()
        val hasLiveGames = _uiState.value.games.any { it.status.isLiveGameStatus() }
        return isToday || hasLiveGames
    }

    private fun startObservingTodayGames() {
        observationJob?.cancel()
        observationJob = viewModelScope.launch {
            try {
                firestoreRepository.observeTodayGames().collect { games ->
                    cachedTodayGames = games.sortedBy { it.gameDate }
                    hasResolvedInitialLoad = true
                    publishGamesForDate(_uiState.value.selectedDate)
                }
            } catch (throwable: Throwable) {
                hasResolvedInitialLoad = true
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        error = throwable.message ?: "Failed to load scores"
                    )
                }
            }
        }
    }

    private fun publishGamesForDate(date: LocalDate) {
        _uiState.update {
            it.copy(
                isLoading = false,
                isRefreshing = false,
                games = cachedTodayGames.toUiModelsFor(date),
                selectedDate = date,
                error = null
            )
        }
    }
}

private fun String?.isLiveGameStatus(): Boolean {
    if (this.isNullOrBlank()) return false
    val status = this.lowercase()
    return listOf("live", "in progress", "mid", "top", "bottom", "inning", "delayed", "warmup", "pregame")
        .any { it in status } && "final" !in status && "postponed" !in status
}

private fun List<CachedGame>.toUiModelsFor(date: LocalDate): List<GameSnapshotCardUiModel> {
    return this
        .filter { it.toLocalDate() == date }
        .sortedBy { it.gameDate }
        .map { it.toScoresSnapshotCardUiModel() }
}

private fun CachedGame.toLocalDate(): LocalDate? {
    return try {
        OffsetDateTime.parse(gameDate)
            .atZoneSameInstant(ZoneId.systemDefault())
            .toLocalDate()
    } catch (_: Exception) {
        null
    }
}
