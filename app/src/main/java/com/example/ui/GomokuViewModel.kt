package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.GameStatsRepository
import com.example.game.GomokuEngine
import com.example.model.Difficulty
import com.example.model.GameMode
import com.example.model.GameStats
import com.example.model.GameStatus
import com.example.model.Move
import com.example.model.Piece
import com.example.model.Point
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class GomokuUiState(
  val board: List<List<Piece>> = List(GomokuEngine.BOARD_SIZE) { List(GomokuEngine.BOARD_SIZE) { Piece.NONE } },
  val currentTurn: Piece = Piece.BLACK,
  val gameMode: GameMode = GameMode.PVE,
  val difficulty: Difficulty = Difficulty.MEDIUM,
  val humanPiece: Piece = Piece.BLACK,
  val gameStatus: GameStatus = GameStatus.Playing,
  val moveHistory: List<Move> = emptyList(),
  val lastMove: Move? = null,
  val isAiThinking: Boolean = false,
  val hint: Pair<Point, String>? = null,
  val showMoveNumbers: Boolean = false,
  val isReplayMode: Boolean = false,
  val replayStep: Int = 0,
  val isSettingsOpen: Boolean = false,
  val isStatsOpen: Boolean = false,
  val isRulesOpen: Boolean = false,
  val showGameOverDialog: Boolean = false
)

class GomokuViewModel(application: Application) : AndroidViewModel(application) {
  private val repository = GameStatsRepository(application)
  val stats: StateFlow<GameStats> = repository.stats

  private val _uiState = MutableStateFlow(GomokuUiState())
  val uiState: StateFlow<GomokuUiState> = _uiState.asStateFlow()

  // Internal mutable board
  private var boardState: Array<Array<Piece>> = GomokuEngine.createEmptyBoard()

  init {
    val (savedMode, savedDiff, savedPiece) = repository.getSavedGameSettings()
    _uiState.update {
      it.copy(
        gameMode = savedMode,
        difficulty = savedDiff,
        humanPiece = savedPiece
      )
    }
    startNewGame()
  }

  fun startNewGame() {
    boardState = GomokuEngine.createEmptyBoard()
    _uiState.update {
      it.copy(
        board = boardStateToList(),
        currentTurn = Piece.BLACK,
        gameStatus = GameStatus.Playing,
        moveHistory = emptyList(),
        lastMove = null,
        isAiThinking = false,
        hint = null,
        isReplayMode = false,
        replayStep = 0,
        showGameOverDialog = false
      )
    }

    // If PVE and human chosen is WHITE, AI moves first as BLACK
    val state = _uiState.value
    if (state.gameMode == GameMode.PVE && state.humanPiece == Piece.WHITE) {
      triggerAiMove()
    }
  }

  fun onCellClicked(row: Int, col: Int) {
    val state = _uiState.value
    if (state.isReplayMode) return
    if (state.gameStatus !is GameStatus.Playing) return
    if (state.isAiThinking) return
    if (boardState[row][col] != Piece.NONE) return

    // In PVE mode, human can only play on their turn
    if (state.gameMode == GameMode.PVE && state.currentTurn != state.humanPiece) return

    makeMove(Point(row, col), state.currentTurn)
  }

  private fun makeMove(point: Point, piece: Piece) {
    boardState[point.row][point.col] = piece
    val moveNumber = _uiState.value.moveHistory.size + 1
    val move = Move(point, piece, moveNumber)
    val newHistory = _uiState.value.moveHistory + move

    val winningLine = GomokuEngine.checkWin(boardState, point)
    val isDraw = winningLine == null && GomokuEngine.isBoardFull(boardState)

    val nextStatus = when {
      winningLine != null -> GameStatus.Won(piece, winningLine)
      isDraw -> GameStatus.Draw
      else -> GameStatus.Playing
    }

    val nextTurn = piece.opponent()

    _uiState.update {
      it.copy(
        board = boardStateToList(),
        currentTurn = nextTurn,
        gameStatus = nextStatus,
        moveHistory = newHistory,
        lastMove = move,
        hint = null, // clear hint when move is made
        showGameOverDialog = nextStatus !is GameStatus.Playing
      )
    }

    if (nextStatus is GameStatus.Won) {
      repository.recordResult(piece, _uiState.value.humanPiece, _uiState.value.gameMode)
    } else if (nextStatus is GameStatus.Draw) {
      repository.recordResult(Piece.NONE, _uiState.value.humanPiece, _uiState.value.gameMode)
    } else {
      // Continue game: If PVE and next turn is AI
      if (_uiState.value.gameMode == GameMode.PVE && nextTurn != _uiState.value.humanPiece) {
        triggerAiMove()
      }
    }
  }

  private fun triggerAiMove() {
    _uiState.update { it.copy(isAiThinking = true) }
    viewModelScope.launch {
      // Natural thinking delay for realistic game pace
      delay(380)

      val state = _uiState.value
      val aiPiece = state.humanPiece.opponent()
      val bestMove = withContext(Dispatchers.Default) {
        GomokuEngine.calculateBestMove(boardState, aiPiece, state.difficulty)
      }

      _uiState.update { it.copy(isAiThinking = false) }
      makeMove(bestMove, aiPiece)
    }
  }

  fun undo() {
    val state = _uiState.value
    if (state.isReplayMode) return
    if (state.isAiThinking) return
    if (state.moveHistory.isEmpty()) return

    val history = state.moveHistory.toMutableList()

    if (state.gameMode == GameMode.PVE) {
      // If AI just won or human just won or playing:
      // Revert until it's human's turn again
      if (history.last().piece != state.humanPiece) {
        // AI made last move, undo 1 move to human turn
        val removed = history.removeAt(history.lastIndex)
        boardState[removed.point.row][removed.point.col] = Piece.NONE
      } else {
        // Human made last move (and maybe AI hadn't moved or human won)
        val removed = history.removeAt(history.lastIndex)
        boardState[removed.point.row][removed.point.col] = Piece.NONE
        // If there was an AI move right before, revert it too so human can replan
        if (history.isNotEmpty() && history.last().piece != state.humanPiece) {
          val aiRemoved = history.removeAt(history.lastIndex)
          boardState[aiRemoved.point.row][aiRemoved.point.col] = Piece.NONE
        }
      }
    } else {
      // PVP: undo single move
      val removed = history.removeAt(history.lastIndex)
      boardState[removed.point.row][removed.point.col] = Piece.NONE
    }

    val newTurn = if (history.isEmpty()) Piece.BLACK else history.last().piece.opponent()
    val lastMove = history.lastOrNull()

    _uiState.update {
      it.copy(
        board = boardStateToList(),
        currentTurn = newTurn,
        gameStatus = GameStatus.Playing,
        moveHistory = history,
        lastMove = lastMove,
        hint = null,
        showGameOverDialog = false
      )
    }
  }

  fun requestHint() {
    val state = _uiState.value
    if (state.gameStatus !is GameStatus.Playing || state.isAiThinking || state.isReplayMode) return

    viewModelScope.launch {
      val hintResult = withContext(Dispatchers.Default) {
        GomokuEngine.getHint(boardState, state.currentTurn)
      }
      _uiState.update { it.copy(hint = hintResult) }
    }
  }

  fun toggleShowMoveNumbers() {
    _uiState.update { it.copy(showMoveNumbers = !it.showMoveNumbers) }
  }

  // --- Replay Mode ---

  fun enterReplayMode() {
    val totalMoves = _uiState.value.moveHistory.size
    _uiState.update {
      it.copy(
        isReplayMode = true,
        replayStep = totalMoves,
        showGameOverDialog = false
      )
    }
    syncReplayBoard(totalMoves)
  }

  fun exitReplayMode() {
    _uiState.update { it.copy(isReplayMode = false) }
    // Restore full active board
    boardState = GomokuEngine.createEmptyBoard()
    for (m in _uiState.value.moveHistory) {
      boardState[m.point.row][m.point.col] = m.piece
    }
    _uiState.update {
      it.copy(
        board = boardStateToList(),
        lastMove = it.moveHistory.lastOrNull()
      )
    }
  }

  fun replayPrevious() {
    val current = _uiState.value.replayStep
    if (current > 0) {
      val newStep = current - 1
      _uiState.update { it.copy(replayStep = newStep) }
      syncReplayBoard(newStep)
    }
  }

  fun replayNext() {
    val current = _uiState.value.replayStep
    val maxStep = _uiState.value.moveHistory.size
    if (current < maxStep) {
      val newStep = current + 1
      _uiState.update { it.copy(replayStep = newStep) }
      syncReplayBoard(newStep)
    }
  }

  fun replayJump(step: Int) {
    val maxStep = _uiState.value.moveHistory.size
    val clamped = step.coerceIn(0, maxStep)
    _uiState.update { it.copy(replayStep = clamped) }
    syncReplayBoard(clamped)
  }

  private fun syncReplayBoard(step: Int) {
    boardState = GomokuEngine.createEmptyBoard()
    val moves = _uiState.value.moveHistory.take(step)
    for (m in moves) {
      boardState[m.point.row][m.point.col] = m.piece
    }
    _uiState.update {
      it.copy(
        board = boardStateToList(),
        lastMove = moves.lastOrNull()
      )
    }
  }

  // --- Settings & Dialogs ---

  fun setGameMode(mode: GameMode) {
    _uiState.update { it.copy(gameMode = mode) }
    repository.saveGameSettings(mode, _uiState.value.difficulty, _uiState.value.humanPiece)
    startNewGame()
  }

  fun setDifficulty(difficulty: Difficulty) {
    _uiState.update { it.copy(difficulty = difficulty) }
    repository.saveGameSettings(_uiState.value.gameMode, difficulty, _uiState.value.humanPiece)
  }

  fun setHumanPiece(piece: Piece) {
    _uiState.update { it.copy(humanPiece = piece) }
    repository.saveGameSettings(_uiState.value.gameMode, _uiState.value.difficulty, piece)
    startNewGame()
  }

  fun openSettings(open: Boolean) {
    _uiState.update { it.copy(isSettingsOpen = open) }
  }

  fun openStats(open: Boolean) {
    _uiState.update { it.copy(isStatsOpen = open) }
  }

  fun openRules(open: Boolean) {
    _uiState.update { it.copy(isRulesOpen = open) }
  }

  fun dismissGameOverDialog() {
    _uiState.update { it.copy(showGameOverDialog = false) }
  }

  fun resetStats() {
    repository.resetStats()
  }

  private fun boardStateToList(): List<List<Piece>> {
    return boardState.map { row -> row.toList() }
  }
}
