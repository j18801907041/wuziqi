package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FirstPage
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LastPage
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.GameStatus
import com.example.ui.GomokuUiState
import com.example.ui.theme.SleekOutlineVariant
import com.example.ui.theme.SleekPrimary
import com.example.ui.theme.SleekPrimaryContainer
import com.example.ui.theme.SleekSecondaryContainer

@Composable
fun GameControlBar(
  uiState: GomokuUiState,
  onUndo: () -> Unit,
  onRequestHint: () -> Unit,
  onToggleMoveNumbers: () -> Unit,
  onNewGame: () -> Unit,
  onEnterReplay: () -> Unit,
  onExitReplay: () -> Unit,
  onReplayPrev: () -> Unit,
  onReplayNext: () -> Unit,
  onReplayJump: (Int) -> Unit,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp, vertical = 4.dp)
  ) {
    // Strategy Hint Banner (Sleek card)
    AnimatedVisibility(
      visible = uiState.hint != null && !uiState.isReplayMode,
      enter = fadeIn(),
      exit = fadeOut()
    ) {
      if (uiState.hint != null) {
        val (point, reason) = uiState.hint
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(
            containerColor = SleekPrimaryContainer.copy(alpha = 0.8f)
          ),
          modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.Lightbulb,
              contentDescription = "提示建议",
              tint = SleekPrimary,
              modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text(
                text = "战略要点 [${point.coordinateLabel}]",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onPrimaryContainer
              )
              Text(
                text = reason,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
              )
            }
          }
        }
      }
    }

    if (uiState.isReplayMode) {
      // Sleek Replay Control Panel
      SleekReplayPanel(
        currentStep = uiState.replayStep,
        totalSteps = uiState.moveHistory.size,
        onPrev = onReplayPrev,
        onNext = onReplayNext,
        onJump = onReplayJump,
        onExit = onExitReplay
      )
    } else {
      // Sleek 3-Action Primary Buttons: 悔棋 / 新游戏 / 提示
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Undo Button
        SleekActionButton(
          icon = Icons.AutoMirrored.Filled.Undo,
          label = "悔棋",
          enabled = uiState.moveHistory.isNotEmpty() && !uiState.isAiThinking,
          isPrimary = false,
          onClick = onUndo,
          testTag = "undo_button",
          modifier = Modifier.weight(1f)
        )

        // New Game Button (Primary filled purple)
        SleekActionButton(
          icon = Icons.Default.PlayArrow,
          label = "新游戏",
          enabled = true,
          isPrimary = true,
          onClick = onNewGame,
          testTag = "new_game_button",
          modifier = Modifier.weight(1.1f)
        )

        // Hint Button
        SleekActionButton(
          icon = Icons.Default.Lightbulb,
          label = "提示",
          enabled = uiState.gameStatus is GameStatus.Playing && !uiState.isAiThinking,
          isPrimary = false,
          onClick = onRequestHint,
          testTag = "hint_button",
          modifier = Modifier.weight(1f)
        )
      }

      // Secondary Utility Bar: Toggle Move Numbers & Quick Replay Entry
      Spacer(modifier = Modifier.height(10.dp))
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Toggle Move Numbers Pill
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = if (uiState.showMoveNumbers) SleekPrimary else MaterialTheme.colorScheme.surfaceVariant,
          modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onToggleMoveNumbers)
            .testTag("numbers_button")
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            Icon(
              imageVector = Icons.Default.FormatListNumbered,
              contentDescription = "序号",
              tint = if (uiState.showMoveNumbers) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.size(16.dp)
            )
            Text(
              text = if (uiState.showMoveNumbers) "隐藏序号" else "显示序号",
              fontSize = 12.sp,
              fontWeight = FontWeight.Medium,
              color = if (uiState.showMoveNumbers) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        // Replay Button (when game has moves)
        if (uiState.moveHistory.isNotEmpty()) {
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = SleekSecondaryContainer,
            modifier = Modifier
              .clip(RoundedCornerShape(12.dp))
              .clickable(onClick = onEnterReplay)
              .testTag("enter_replay_button")
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Icon(
                imageVector = Icons.Default.History,
                contentDescription = "复盘",
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.size(16.dp)
              )
              Text(
                text = "对局复盘 (${uiState.moveHistory.size}手)",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
              )
            }
          }
        }
      }
    }
  }
}

@Composable
private fun SleekActionButton(
  icon: ImageVector,
  label: String,
  enabled: Boolean,
  isPrimary: Boolean,
  onClick: () -> Unit,
  testTag: String,
  modifier: Modifier = Modifier
) {
  val bgColor = when {
    !enabled -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    isPrimary -> SleekPrimary
    else -> SleekPrimaryContainer
  }

  val contentColor = when {
    !enabled -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
    isPrimary -> Color.White
    else -> MaterialTheme.colorScheme.onPrimaryContainer
  }

  Surface(
    modifier = modifier
      .shadow(if (isPrimary && enabled) 4.dp else 0.dp, RoundedCornerShape(16.dp))
      .clip(RoundedCornerShape(16.dp))
      .clickable(enabled = enabled, onClick = onClick)
      .testTag(testTag),
    shape = RoundedCornerShape(16.dp),
    color = bgColor
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 12.dp, horizontal = 4.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      Icon(
        imageVector = icon,
        contentDescription = label,
        tint = contentColor,
        modifier = Modifier.size(24.dp)
      )
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = label,
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium,
        color = contentColor
      )
    }
  }
}

@Composable
private fun SleekReplayPanel(
  currentStep: Int,
  totalSteps: Int,
  onPrev: () -> Unit,
  onNext: () -> Unit,
  onJump: (Int) -> Unit,
  onExit: () -> Unit
) {
  Surface(
    shape = RoundedCornerShape(18.dp),
    color = MaterialTheme.colorScheme.surface,
    modifier = Modifier
      .fillMaxWidth()
      .shadow(2.dp, RoundedCornerShape(18.dp)),
    border = androidx.compose.foundation.BorderStroke(1.dp, SleekOutlineVariant)
  ) {
    Column(
      modifier = Modifier.padding(14.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "对局复盘",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
          )
          Text(
            text = "当前步数: $currentStep / $totalSteps 手",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        IconButton(
          onClick = onExit,
          modifier = Modifier
            .size(32.dp)
            .clip(androidx.compose.foundation.shape.CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
          Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "退出复盘",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(16.dp)
          )
        }
      }

      // Timeline slider
      if (totalSteps > 0) {
        Slider(
          value = currentStep.toFloat(),
          onValueChange = { onJump(it.toInt()) },
          valueRange = 0f..totalSteps.toFloat(),
          steps = if (totalSteps > 1) totalSteps - 1 else 0,
          colors = SliderDefaults.colors(
            thumbColor = SleekPrimary,
            activeTrackColor = SleekPrimary,
            inactiveTrackColor = SleekPrimaryContainer
          ),
          modifier = Modifier.fillMaxWidth()
        )
      }

      // Step Navigation Buttons
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
      ) {
        IconButton(
          onClick = { onJump(0) },
          enabled = currentStep > 0
        ) {
          Icon(Icons.Default.FirstPage, contentDescription = "开局", tint = SleekPrimary)
        }

        IconButton(
          onClick = onPrev,
          enabled = currentStep > 0
        ) {
          Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "上一步", tint = SleekPrimary)
        }

        IconButton(
          onClick = onNext,
          enabled = currentStep < totalSteps
        ) {
          Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "下一步", tint = SleekPrimary)
        }

        IconButton(
          onClick = { onJump(totalSteps) },
          enabled = currentStep < totalSteps
        ) {
          Icon(Icons.Default.LastPage, contentDescription = "终局", tint = SleekPrimary)
        }
      }
    }
  }
}
