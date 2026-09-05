package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.GameMode
import com.example.model.GameStatus
import com.example.model.Piece
import com.example.ui.GomokuUiState
import com.example.ui.theme.SleekOutline
import com.example.ui.theme.SleekOutlineVariant
import com.example.ui.theme.SleekPrimary
import com.example.ui.theme.SleekPrimaryContainer
import com.example.ui.theme.SleekTextMuted

@Composable
fun GameHeader(
  uiState: GomokuUiState,
  onOpenSettings: () -> Unit,
  onOpenStats: () -> Unit,
  onOpenRules: () -> Unit,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp, vertical = 6.dp)
  ) {
    // Top Bar: Sleek Interface style header
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        // App brand emblem with sleek violet gradient
        Box(
          modifier = Modifier
            .size(40.dp)
            .shadow(2.dp, CircleShape)
            .clip(CircleShape)
            .background(
              Brush.linearGradient(
                colors = listOf(SleekPrimary, Color(0xFF4F378B))
              )
            ),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = "弈",
            color = Color.White,
            fontSize = 19.sp,
            fontWeight = FontWeight.Bold
          )
        }

        Column {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = "五子棋",
              style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Medium,
                letterSpacing = (-0.5).sp
              ),
              color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.width(8.dp))
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = SleekPrimaryContainer,
              modifier = Modifier.padding(vertical = 2.dp)
            ) {
              val tagText = if (uiState.gameMode == GameMode.PVE) {
                "人机 · ${uiState.difficulty.title}"
              } else {
                "双人同屏"
              }
              Text(
                text = tagText,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
              )
            }
          }
        }
      }

      // Action buttons styled as sleek rounded buttons
      Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        IconButton(
          onClick = onOpenRules,
          modifier = Modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .testTag("rules_button")
        ) {
          Icon(
            imageVector = Icons.Default.HelpOutline,
            contentDescription = "规则说明",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp)
          )
        }

        IconButton(
          onClick = onOpenStats,
          modifier = Modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .testTag("stats_button")
        ) {
          Icon(
            imageVector = Icons.Default.Leaderboard,
            contentDescription = "对局战绩",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp)
          )
        }

        IconButton(
          onClick = onOpenSettings,
          modifier = Modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(SleekPrimaryContainer)
            .testTag("settings_button")
        ) {
          Icon(
            imageVector = Icons.Default.Settings,
            contentDescription = "游戏设置",
            tint = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.size(18.dp)
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(12.dp))

    // Player Status Match Row (Sleek Interface: Two symmetric cards with "VS")
    SleekPlayerMatchRow(uiState = uiState)
  }
}

@Composable
private fun SleekPlayerMatchRow(uiState: GomokuUiState) {
  val isPlaying = uiState.gameStatus is GameStatus.Playing
  val isBlackTurn = uiState.currentTurn == Piece.BLACK && isPlaying
  val isWhiteTurn = uiState.currentTurn == Piece.WHITE && isPlaying

  val blackLabel = if (uiState.gameMode == GameMode.PVE) {
    if (uiState.humanPiece == Piece.BLACK) "执黑 (我)" else "执黑 (电脑)"
  } else {
    "执黑 (黑方)"
  }

  val whiteLabel = if (uiState.gameMode == GameMode.PVE) {
    if (uiState.humanPiece == Piece.WHITE) "执白 (我)" else "执白 (电脑)"
  } else {
    "执白 (白方)"
  }

  val blackSubtext = when {
    uiState.gameStatus is GameStatus.Won && uiState.gameStatus.winner == Piece.BLACK -> "获胜 ★"
    uiState.gameStatus is GameStatus.Won -> "惜败"
    uiState.gameStatus is GameStatus.Draw -> "和棋"
    isBlackTurn && uiState.isAiThinking -> "思考中..."
    isBlackTurn -> "落子中"
    else -> "等待"
  }

  val whiteSubtext = when {
    uiState.gameStatus is GameStatus.Won && uiState.gameStatus.winner == Piece.WHITE -> "获胜 ★"
    uiState.gameStatus is GameStatus.Won -> "惜败"
    uiState.gameStatus is GameStatus.Draw -> "和棋"
    isWhiteTurn && uiState.isAiThinking -> "思考中..."
    isWhiteTurn -> "落子中"
    else -> "等待"
  }

  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    // Left: Black Player Card
    SleekPlayerCard(
      piece = Piece.BLACK,
      label = blackLabel,
      subtext = blackSubtext,
      isActive = isBlackTurn,
      isWinner = uiState.gameStatus is GameStatus.Won && uiState.gameStatus.winner == Piece.BLACK,
      modifier = Modifier.weight(1f)
    )

    // Center VS badge
    Box(
      modifier = Modifier.padding(horizontal = 10.dp),
      contentAlignment = Alignment.Center
    ) {
      Text(
        text = "VS",
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        fontStyle = FontStyle.Italic,
        color = SleekTextMuted
      )
    }

    // Right: White Player Card
    SleekPlayerCard(
      piece = Piece.WHITE,
      label = whiteLabel,
      subtext = whiteSubtext,
      isActive = isWhiteTurn,
      isWinner = uiState.gameStatus is GameStatus.Won && uiState.gameStatus.winner == Piece.WHITE,
      modifier = Modifier.weight(1f)
    )
  }
}

@Composable
private fun SleekPlayerCard(
  piece: Piece,
  label: String,
  subtext: String,
  isActive: Boolean,
  isWinner: Boolean,
  modifier: Modifier = Modifier
) {
  val cardBorderModifier = when {
    isWinner -> Modifier.border(2.dp, Color(0xFF4CAF50), RoundedCornerShape(16.dp))
    isActive -> Modifier.border(2.dp, SleekPrimary, RoundedCornerShape(16.dp))
    else -> Modifier.border(1.dp, SleekOutlineVariant, RoundedCornerShape(16.dp))
  }

  Surface(
    modifier = modifier
      .shadow(if (isActive) 3.dp else 0.dp, RoundedCornerShape(16.dp))
      .then(cardBorderModifier)
      .alpha(if (isActive || isWinner) 1f else 0.65f),
    shape = RoundedCornerShape(16.dp),
    color = MaterialTheme.colorScheme.surface
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 12.dp, horizontal = 8.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      // 44.dp Stone with active ring
      Box(
        modifier = Modifier
          .size(46.dp)
          .then(
            if (isActive) {
              Modifier
                .border(3.5.dp, SleekPrimaryContainer, CircleShape)
                .padding(2.dp)
            } else {
              Modifier.padding(2.dp)
            }
          )
          .shadow(if (isActive) 4.dp else 2.dp, CircleShape)
          .clip(CircleShape)
          .background(
            if (piece == Piece.BLACK) {
              Brush.radialGradient(
                colors = listOf(Color(0xFF424242), Color(0xFF101010))
              )
            } else {
              Brush.radialGradient(
                colors = listOf(Color(0xFFFFFFFF), Color(0xFFEDEDED))
              )
            }
          )
          .then(
            if (piece == Piece.WHITE) {
              Modifier.border(1.5.dp, SleekOutline, CircleShape)
            } else {
              Modifier
            }
          )
      )

      Spacer(modifier = Modifier.height(6.dp))

      Text(
        text = label,
        style = MaterialTheme.typography.bodyMedium.copy(
          fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal
        ),
        color = MaterialTheme.colorScheme.onSurface
      )

      Text(
        text = subtext,
        style = MaterialTheme.typography.labelMedium.copy(
          fontFamily = FontFamily.Monospace,
          fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal
        ),
        color = when {
          isWinner -> Color(0xFF2E7D32)
          isActive -> SleekPrimary
          else -> SleekTextMuted
        }
      )
    }
  }
}
