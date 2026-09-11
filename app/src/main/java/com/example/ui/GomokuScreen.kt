package com.example.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.GameStatus
import com.example.ui.components.GameControlBar
import com.example.ui.components.GameHeader
import com.example.ui.components.GameOverDialog
import com.example.ui.components.GameRulesDialog
import com.example.ui.components.GameSettingsDialog
import com.example.ui.components.GameStatsDialog
import com.example.ui.components.GomokuBoard

/** The game surface that connects the ViewModel state to the board and controls. */
@Composable
fun GomokuScreen(viewModel: GomokuViewModel = viewModel()) {
  val uiState by viewModel.uiState.collectAsState()
  val stats by viewModel.stats.collectAsState()
  val winningLine = (uiState.gameStatus as? GameStatus.Won)?.winningLine

  Scaffold(contentWindowInsets = WindowInsets.safeDrawing) { innerPadding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
    ) {
      GameHeader(
        uiState = uiState,
        onOpenSettings = { viewModel.openSettings(true) },
        onOpenStats = { viewModel.openStats(true) },
        onOpenRules = { viewModel.openRules(true) }
      )

      GomokuBoard(
        board = uiState.board,
        lastMove = uiState.lastMove,
        winningLine = winningLine,
        hintPoint = uiState.hint?.first,
        showMoveNumbers = uiState.showMoveNumbers,
        moveHistory = uiState.moveHistory,
        onCellClick = viewModel::onCellClicked,
        modifier = Modifier.weight(1f)
      )

      GameControlBar(
        uiState = uiState,
        onUndo = viewModel::undo,
        onRequestHint = viewModel::requestHint,
        onToggleMoveNumbers = viewModel::toggleShowMoveNumbers,
        onNewGame = viewModel::startNewGame,
        onEnterReplay = viewModel::enterReplayMode,
        onExitReplay = viewModel::exitReplayMode,
        onReplayPrev = viewModel::replayPrevious,
        onReplayNext = viewModel::replayNext,
        onReplayJump = viewModel::replayJump
      )
    }
  }

  if (uiState.showGameOverDialog) {
    GameOverDialog(
      uiState = uiState,
      stats = stats,
      onDismiss = viewModel::dismissGameOverDialog,
      onRestart = viewModel::startNewGame,
      onEnterReplay = viewModel::enterReplayMode
    )
  }

  if (uiState.isSettingsOpen) {
    GameSettingsDialog(
      uiState = uiState,
      onDismiss = { viewModel.openSettings(false) },
      onSelectMode = viewModel::setGameMode,
      onSelectDifficulty = viewModel::setDifficulty,
      onSelectHumanPiece = viewModel::setHumanPiece
    )
  }

  if (uiState.isStatsOpen) {
    GameStatsDialog(
      stats = stats,
      onDismiss = { viewModel.openStats(false) },
      onReset = viewModel::resetStats
    )
  }

  if (uiState.isRulesOpen) {
    GameRulesDialog(onDismiss = { viewModel.openRules(false) })
  }
}
