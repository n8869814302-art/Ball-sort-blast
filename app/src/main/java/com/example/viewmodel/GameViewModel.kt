package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.CrazySoundEngine
import com.example.data.GamePreferences
import com.example.logic.BallSortLogic
import com.example.model.BallSkin
import com.example.model.GameMode
import com.example.model.HintMove
import com.example.model.TubeSnapshot
import com.example.model.TubeTheme
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class Screen {
    GAME,
    SHOP,
    SOUNDBOARD,
    LEVEL_SELECT
}

data class GameUiState(
    val screen: Screen = Screen.GAME,
    val gameMode: GameMode = GameMode.CLASSIC,
    val tubes: List<List<Int>> = emptyList(),
    val selectedTube: Int? = null,
    val shakingTube: Int? = null,
    val hint: HintMove? = null,
    val level: Int = 1,
    val colorsCount: Int = 3,
    val moves: Int = 0,
    val bestMoves: Int = -1,
    val extraTubesCount: Int = 0,
    val coins: Int = 250,
    val selectedBallSkin: BallSkin = BallSkin.NEON,
    val selectedTubeTheme: TubeTheme = TubeTheme.CYBER,
    val isWinModalVisible: Boolean = false,
    val isGameOver: Boolean = false,
    val showConfetti: Boolean = false,
    val comboStreak: Int = 0,
    val comboCalloutText: String? = null,
    val rushTimeRemaining: Int = 90,
    val rushScore: Int = 0,
    val maxUnlockedLevel: Int = 1,
    val volumeScale: Float = 1.25f,
    val isCrazyMode: Boolean = true,
    val isHapticsEnabled: Boolean = true,
    val canUndo: Boolean = false,
    val canAddTube: Boolean = true
)

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = GamePreferences(application)
    val soundEngine = CrazySoundEngine(application)

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private val history = mutableListOf<TubeSnapshot>()
    private var rushTimerJob: Job? = null

    init {
        // Sync preferences to sound engine
        soundEngine.volumeScale = prefs.volumeScale
        soundEngine.isCrazyModeEnabled = prefs.isCrazyModeEnabled
        soundEngine.isHapticsEnabled = prefs.isHapticsEnabled

        _uiState.update {
            it.copy(
                level = prefs.currentLevel,
                maxUnlockedLevel = prefs.maxUnlockedLevel,
                coins = prefs.coins,
                selectedBallSkin = prefs.selectedBallSkin,
                selectedTubeTheme = prefs.selectedTubeTheme,
                volumeScale = prefs.volumeScale,
                isCrazyMode = prefs.isCrazyModeEnabled,
                isHapticsEnabled = prefs.isHapticsEnabled
            )
        }

        loadLevel(prefs.currentLevel)
    }

    fun navigateTo(screen: Screen) {
        soundEngine.playClick()
        _uiState.update { it.copy(screen = screen) }
    }

    fun setGameMode(mode: GameMode) {
        soundEngine.playClick()
        _uiState.update { it.copy(gameMode = mode) }
        when (mode) {
            GameMode.CLASSIC -> loadLevel(_uiState.value.level)
            GameMode.RUSH -> startRushMode()
            GameMode.DAILY -> loadLevel(999) // Special curated seed
        }
    }

    fun loadLevel(levelNumber: Int) {
        val (colors, _) = BallSortLogic.getLevelTubeSetup(levelNumber)
        val newTubes = BallSortLogic.generateLevel(levelNumber)
        history.clear()

        _uiState.update {
            it.copy(
                level = levelNumber,
                colorsCount = colors,
                tubes = newTubes,
                selectedTube = null,
                shakingTube = null,
                hint = null,
                moves = 0,
                bestMoves = prefs.getBestMoves(levelNumber),
                extraTubesCount = 0,
                isWinModalVisible = false,
                isGameOver = false,
                showConfetti = false,
                comboStreak = 0,
                comboCalloutText = null,
                canUndo = false,
                canAddTube = true
            )
        }
        prefs.currentLevel = levelNumber
    }

    private fun startRushMode() {
        rushTimerJob?.cancel()
        val (colors, _) = BallSortLogic.getLevelTubeSetup(1)
        val newTubes = BallSortLogic.generateLevel(1)
        history.clear()

        _uiState.update {
            it.copy(
                tubes = newTubes,
                colorsCount = colors,
                selectedTube = null,
                moves = 0,
                rushTimeRemaining = 90,
                rushScore = 0,
                isGameOver = false,
                isWinModalVisible = false,
                canUndo = false,
                canAddTube = true
            )
        }

        rushTimerJob = viewModelScope.launch {
            while (_uiState.value.rushTimeRemaining > 0 && !_uiState.value.isGameOver) {
                delay(1000)
                _uiState.update { current ->
                    val newTime = current.rushTimeRemaining - 1
                    if (newTime <= 0) {
                        soundEngine.playError()
                        current.copy(rushTimeRemaining = 0, isGameOver = true)
                    } else {
                        current.copy(rushTimeRemaining = newTime)
                    }
                }
            }
        }
    }

    fun onTubeClicked(tubeIndex: Int) {
        val state = _uiState.value
        if (state.isWinModalVisible || state.isGameOver) return
        if (tubeIndex !in state.tubes.indices) return

        val currentSelected = state.selectedTube

        if (currentSelected == null) {
            // First tap: Select source tube if it has balls
            if (state.tubes[tubeIndex].isNotEmpty()) {
                soundEngine.playBallSelect()
                _uiState.update { it.copy(selectedTube = tubeIndex, hint = null) }
            }
            return
        }

        if (currentSelected == tubeIndex) {
            // Tapped same tube: deselect
            soundEngine.playClick()
            _uiState.update { it.copy(selectedTube = null) }
            return
        }

        if (currentSelected !in state.tubes.indices) {
            _uiState.update { it.copy(selectedTube = null) }
            return
        }

        // Attempting to move from currentSelected to tubeIndex
        val mutableTubes = state.tubes.map { it.toMutableList() }.toMutableList()
        val sourceTube = mutableTubes[currentSelected]
        val destTube = mutableTubes[tubeIndex]

        if (!BallSortLogic.canMove(sourceTube, destTube)) {
            // Invalid move: shake target tube and play error boing
            soundEngine.playError()
            triggerShake(tubeIndex)
            // Keep current tube selected so user can tap an empty or matching tube to complete the move
            return
        }

        // Valid move! Save history snapshot for undo (deep-copied to prevent mutation)
        history.add(TubeSnapshot(state.tubes.map { it.toList() }, state.moves, state.extraTubesCount))

        // Execute multi-ball pour
        val ballsMoved = BallSortLogic.executePour(mutableTubes, currentSelected, tubeIndex)
        val newMoves = state.moves + 1

        // Play cascade pops for each moved ball
        for (i in 0 until ballsMoved) {
            soundEngine.playCascade(i)
        }
        soundEngine.playBallDrop(ballsMoved)

        // Check if destination tube was completed by this move!
        var newCombo = state.comboStreak
        var newRushScore = state.rushScore
        var newRushTime = state.rushTimeRemaining

        if (BallSortLogic.isTubeComplete(mutableTubes[tubeIndex])) {
            newCombo += 1
            soundEngine.playTubeComplete()

            if (state.gameMode == GameMode.RUSH) {
                newRushTime += 15
                newRushScore += 250 * newCombo
            }

            // Crazy combo callout
            val callout = when (newCombo) {
                1 -> "TUBE CLEARED! 🌟"
                2 -> "COMBO x2! 🔥"
                3 -> "SAVAGE! ⚡"
                4 -> "UNSTOPPABLE! 💥"
                else -> "GODLIKE x$newCombo! 🚀"
            }
            showComboCallout(callout)
            soundEngine.playComboStreak(newCombo)
        }

        val isSolved = BallSortLogic.isLevelSolved(mutableTubes)

        _uiState.update {
            it.copy(
                tubes = mutableTubes,
                selectedTube = null,
                moves = newMoves,
                canUndo = true,
                comboStreak = newCombo,
                rushScore = newRushScore,
                rushTimeRemaining = newRushTime
            )
        }

        if (isSolved) {
            handleLevelWin(newMoves)
        }
    }

    private fun handleLevelWin(movesCount: Int) {
        rushTimerJob?.cancel()
        soundEngine.playLevelWin()

        val currentLvl = _uiState.value.level
        prefs.saveBestMoves(currentLvl, movesCount)

        val nextLevel = currentLvl + 1
        if (nextLevel > prefs.maxUnlockedLevel) {
            prefs.maxUnlockedLevel = nextLevel
        }

        val earnedCoins = 60 + (_uiState.value.comboStreak * 15)
        val totalCoins = prefs.coins + earnedCoins
        prefs.coins = totalCoins

        _uiState.update {
            it.copy(
                isWinModalVisible = true,
                showConfetti = true,
                coins = totalCoins,
                bestMoves = prefs.getBestMoves(currentLvl),
                maxUnlockedLevel = prefs.maxUnlockedLevel
            )
        }
    }

    fun nextLevel() {
        soundEngine.playClick()
        val next = _uiState.value.level + 1
        loadLevel(next)
    }

    fun undoMove() {
        if (history.isEmpty()) return
        soundEngine.playUndo()
        val last = history.removeAt(history.size - 1)

        _uiState.update {
            it.copy(
                tubes = last.tubes,
                moves = last.moves,
                extraTubesCount = last.extraTubes,
                selectedTube = null,
                hint = null,
                canUndo = history.isNotEmpty(),
                canAddTube = last.extraTubes < 2
            )
        }
    }

    fun restartLevel() {
        soundEngine.playClick()
        loadLevel(_uiState.value.level)
    }

    fun addExtraTube() {
        val state = _uiState.value
        if (state.extraTubesCount >= 2) return

        soundEngine.playAddTube()
        history.add(TubeSnapshot(state.tubes.map { it.toList() }, state.moves, state.extraTubesCount))

        val updatedTubes = state.tubes.map { it.toList() }.toMutableList()
        updatedTubes.add(emptyList())

        val newExtraCount = state.extraTubesCount + 1

        _uiState.update {
            it.copy(
                tubes = updatedTubes,
                extraTubesCount = newExtraCount,
                canUndo = true,
                canAddTube = newExtraCount < 2
            )
        }
    }

    fun requestHint() {
        soundEngine.playClick()
        val hintMove = BallSortLogic.findHint(_uiState.value.tubes)
        if (hintMove != null) {
            _uiState.update { it.copy(hint = hintMove, selectedTube = null) }
        } else {
            soundEngine.playError()
            showComboCallout("NO MOVES! ADD TUBE 💡")
        }
    }

    private fun triggerShake(tubeIdx: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(shakingTube = tubeIdx) }
            delay(280)
            _uiState.update { it.copy(shakingTube = null) }
        }
    }

    private fun showComboCallout(text: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(comboCalloutText = text) }
            delay(1500)
            _uiState.update { it.copy(comboCalloutText = null) }
        }
    }

    fun clearConfetti() {
        _uiState.update { it.copy(showConfetti = false) }
    }

    // --- Audio & Settings ---

    fun setVolumeScale(volume: Float) {
        prefs.volumeScale = volume
        soundEngine.volumeScale = volume
        _uiState.update { it.copy(volumeScale = volume) }
    }

    fun toggleCrazyMode(enabled: Boolean) {
        prefs.isCrazyModeEnabled = enabled
        soundEngine.isCrazyModeEnabled = enabled
        _uiState.update { it.copy(isCrazyMode = enabled) }
        if (enabled) {
            soundEngine.playAirhorn()
        }
    }

    fun toggleHaptics(enabled: Boolean) {
        prefs.isHapticsEnabled = enabled
        soundEngine.isHapticsEnabled = enabled
        _uiState.update { it.copy(isHapticsEnabled = enabled) }
        if (enabled) {
            soundEngine.vibrateSuccess()
        }
    }

    fun selectBallSkin(skin: BallSkin) {
        if (!prefs.isSkinUnlocked(skin)) {
            if (_uiState.value.coins >= skin.price) {
                val newCoins = _uiState.value.coins - skin.price
                prefs.coins = newCoins
                prefs.unlockSkin(skin)
                prefs.selectedBallSkin = skin
                soundEngine.playTubeComplete()
                _uiState.update { it.copy(coins = newCoins, selectedBallSkin = skin) }
            } else {
                soundEngine.playError()
            }
        } else {
            prefs.selectedBallSkin = skin
            soundEngine.playClick()
            _uiState.update { it.copy(selectedBallSkin = skin) }
        }
    }

    fun selectTubeTheme(theme: TubeTheme) {
        if (!prefs.isTubeUnlocked(theme)) {
            if (_uiState.value.coins >= theme.price) {
                val newCoins = _uiState.value.coins - theme.price
                prefs.coins = newCoins
                prefs.unlockTube(theme)
                prefs.selectedTubeTheme = theme
                soundEngine.playTubeComplete()
                _uiState.update { it.copy(coins = newCoins, selectedTubeTheme = theme) }
            } else {
                soundEngine.playError()
            }
        } else {
            prefs.selectedTubeTheme = theme
            soundEngine.playClick()
            _uiState.update { it.copy(selectedTubeTheme = theme) }
        }
    }

    fun isSkinUnlocked(skin: BallSkin): Boolean = prefs.isSkinUnlocked(skin)
    fun isTubeUnlocked(theme: TubeTheme): Boolean = prefs.isTubeUnlocked(theme)

    override fun onCleared() {
        super.onCleared()
        rushTimerJob?.cancel()
        soundEngine.release()
    }
}
