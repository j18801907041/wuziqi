package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessHistory
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.GameStatus
import com.example.ui.components.GameControlBar
import com.example.ui.components.GameHeader
import com.example.ui.components.GameOverDialog
import com.example.ui.components.GameRulesDialog
import com.example.ui.components.GameSettingsDialog
import com.example.ui.components.GameStatsDialog
import com.example.ui.components.GomokuBoard
import com.example.ui.theme.SleekBackground
import com.example.ui.theme.SleekOnPrimaryContainer
import com.example.ui.theme.SleekOnSurfaceVariant
import com.example.ui.theme.SleekOutlineVariant
import com.example.ui.theme.SleekSecondaryContainer
import com.example.ui.theme.SleekSurfaceVariant

@Composable
fun GomokuScreen(
  viewModel: GomokuViewModel = viewModel(),
  modifier: Modifier = Modifier
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  val stats by viewModel.stats.collectAsStateWithLifecycle()

  val winningLine = (uiState.gameStatus as? GameStatus.Won)?.winningLine

  Scaffold(
    contentWindowInsets = WindowInsets.safeDrawing,
    modifier = modifier.fillMaxSize(),
    containerColor = SleekBackground,
    bottomBar = {
      SleekBottomNavBar(
        isReplayMode = uiState.isReplayMode,
        onSelectBattle = {
          if (uiState.isReplayMode) {
            viewModel.exitReplayMode()
          }
        },
        onSelectReplay = {
          if (!uiState.isReplayMode) {
            viewModel.enterReplayMode()
          }
        },
        onSelectStats = { viewModel.openStats(true) },
        onSelectSettings = { viewModel.openSettings(true) }
      )
    }
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
        .background(SleekBackground),
      contentAlignment = Alignment.TopCenter
    ) {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .widthIn(max = 600.dp)
          .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
      ) {
        // Header with status
        GameHeader(
          uiState = uiState,
          onOpenSettings = { viewModel.openSettings(true) },
          onOpenStats = { viewModel.openStats(true) },
          onOpenRules = { viewModel.openRules(true) }
        )

        Spacer(modifier = Modifier.height(4.dp))

        // 15x15 Interactive Gobang Canvas Board (Sleek Aesthetic)
        GomokuBoard(
          board = uiState.board,
          lastMove = uiState.lastMove,
          winningLine = winningLine,
          hintPoint = uiState.hint?.first,
          showMoveNumbers = uiState.showMoveNumbers,
          moveHistory = uiState.moveHistory,
          onCellClick = { r, c -> viewModel.onCellClicked(r, c) }
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Sleek Game Controls
        GameControlBar(
          uiState = uiState,
          onUndo = { viewModel.undo() },
          onRequestHint = { viewModel.requestHint() },
          onToggleMoveNumbers = { viewModel.toggleShowMoveNumbers() },
          onNewGame = { viewModel.startNewGame() },
          onEnterReplay = { viewModel.enterReplayMode() },
          onExitReplay = { viewModel.exitReplayMode() },
          onReplayPrev = { viewModel.replayPrevious() },
          onReplayNext = { viewModel.replayNext() },
          onReplayJump = { viewModel.replayJump(it) }
        )

        Spacer(modifier = Modifier.height(16.dp))
      }
    }

    // Dialogs
    if (uiState.showGameOverDialog && !uiState.isReplayMode) {
      GameOverDialog(
        uiState = uiState,
        stats = stats,
        onDismiss = { viewModel.dismissGameOverDialog() },
        onRestart = { viewModel.startNewGame() },
        onEnterReplay = { viewModel.enterReplayMode() }
      )
    }

    if (uiState.isSettingsOpen) {
      GameSettingsDialog(
        uiState = uiState,
        onDismiss = { viewModel.openSettings(false) },
        onSelectMode = { viewModel.setGameMode(it) },
        onSelectDifficulty = { viewModel.setDifficulty(it) },
        onSelectHumanPiece = { viewModel.setHumanPiece(it) }
      )
    }

    if (uiState.isStatsOpen) {
      GameStatsDialog(
        stats = stats,
        onDismiss = { viewModel.openStats(false) },
        onReset = { viewModel.resetStats() }
      )
    }

    if (uiState.isRulesOpen) {
      GameRulesDialog(
        onDismiss = { viewModel.openRules(false) }
      )
    }
  }
}

@Composable
fun SleekBottomNavBar(
  isReplayMode: Boolean,
  onSelectBattle: () -> Unit,
  onSelectReplay: () -> Unit,
  onSelectStats: () -> Unit,
  onSelectSettings: () -> Unit,
  modifier: Modifier = Modifier
) {
  Surface(
    modifier = modifier
      .fillMaxWidth()
      .border(
        width = 1.dp,
        color = SleekOutlineVariant,
        shape = RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp)
      ),
    color = SleekSurfaceVariant
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .navigationBarsPadding()
        .padding(horizontal = 16.dp, vertical = 8.dp),
      horizontalArrangement = Arrangement.SpaceAround,
      verticalAlignment = Alignment.CenterVertically
    ) {
      // 1. 对战 (Battle)
      SleekNavItem(
        icon = Icons.Default.SportsEsports,
        label = "对战",
        isActive = !isReplayMode,
        onClick = onSelectBattle,
        testTag = "nav_battle"
      )

      // 2. 复盘 (Replay)
      SleekNavItem(
        icon = Icons.Default.AccessHistory,
        label = "复盘",
        isActive = isReplayMode,
        onClick = onSelectReplay,
        testTag = "nav_replay"
      )

      // 3. 排行 / 战绩 (Stats)
      SleekNavItem(
        icon = Icons.Default.Leaderboard,
        label = "排行",
        isActive = false,
        onClick = onSelectStats,
        testTag = "nav_stats"
      )

      // 4. 我的 / 设置 (Settings)
      SleekNavItem(
        icon = Icons.Default.Person,
        label = "我的",
        isActive = false,
        onClick = onSelectSettings,
        testTag = "nav_settings"
      )
    }
  }
}

@Composable
private fun SleekNavItem(
  icon: ImageVector,
  label: String,
  isActive: Boolean,
  onClick: () -> Unit,
  testTag: String,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier
      .clip(RoundedCornerShape(16.dp))
      .clickable(onClick = onClick)
      .padding(horizontal = 8.dp, vertical = 4.dp)
      .testTag(testTag),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center
  ) {
    Box(
      modifier = Modifier
        .then(
          if (isActive) {
            Modifier
              .background(SleekSecondaryContainer, RoundedCornerShape(16.dp))
              .padding(horizontal = 18.dp, vertical = 3.dp)
          } else {
            Modifier.padding(horizontal = 18.dp, vertical = 3.dp)
          }
        ),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = icon,
        contentDescription = label,
        tint = if (isActive) SleekOnPrimaryContainer else SleekOnSurfaceVariant,
        modifier = Modifier.size(22.dp)
      )
    }

    Spacer(modifier = Modifier.height(2.dp))

    Text(
      text = label,
      fontSize = 11.sp,
      fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
      color = if (isActive) SleekOnPrimaryContainer else SleekOnSurfaceVariant
    )
  }
}
