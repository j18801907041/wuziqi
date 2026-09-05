package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SentimentDissatisfied
import androidx.compose.material.icons.filled.SentimentVerySatisfied
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Difficulty
import com.example.model.GameMode
import com.example.model.GameStats
import com.example.model.GameStatus
import com.example.model.Piece
import com.example.ui.GomokuUiState

@Composable
fun GameOverDialog(
  uiState: GomokuUiState,
  stats: GameStats,
  onDismiss: () -> Unit,
  onRestart: () -> Unit,
  onEnterReplay: () -> Unit
) {
  val status = uiState.gameStatus
  val isWon = status is GameStatus.Won
  val winner = (status as? GameStatus.Won)?.winner ?: Piece.NONE
  val isHumanWinner = uiState.gameMode == GameMode.PVE && winner == uiState.humanPiece

  AlertDialog(
    onDismissRequest = onDismiss,
    shape = RoundedCornerShape(20.dp),
    containerColor = MaterialTheme.colorScheme.surface,
    title = {
      Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Box(
          modifier = Modifier
            .size(56.dp)
            .shadow(6.dp, CircleShape)
            .clip(CircleShape)
            .background(
              when {
                !isWon -> Color(0xFF78909C)
                isHumanWinner || uiState.gameMode == GameMode.PVP -> Color(0xFFD48B30)
                else -> Color(0xFF8D6E63)
              }
            ),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = when {
              !isWon -> Icons.Default.MilitaryTech
              isHumanWinner || uiState.gameMode == GameMode.PVP -> Icons.Default.EmojiEvents
              else -> Icons.Default.SentimentDissatisfied
            },
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(32.dp)
          )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
          text = when {
            !isWon -> "棋逢对手 · 和棋"
            uiState.gameMode == GameMode.PVP -> "${winner.displayName}获胜！"
            isHumanWinner -> "恭喜！棋开得胜！"
            else -> "惜败！AI胜出"
          },
          style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
          color = MaterialTheme.colorScheme.onSurface
        )
      }
    },
    text = {
      Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Text(
          text = when {
            !isWon -> "双方落子密不透风，棋盘已满，握手言和！"
            uiState.gameMode == GameMode.PVP -> "五子连珠，精彩对局！"
            isHumanWinner -> "精准布局，成功连成五子击败${uiState.difficulty.title}电脑！"
            else -> "电脑捕捉到胜机完成五子连线，再战一局定能取胜！"
          },
          style = MaterialTheme.typography.bodyMedium,
          textAlign = TextAlign.Center,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Match Stats Snapshot Card
        Card(
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 12.dp, horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceAround
          ) {
            StatMiniItem(label = "总手数", value = "${uiState.moveHistory.size}手")
            if (uiState.gameMode == GameMode.PVE) {
              StatMiniItem(label = "胜率", value = String.format("%.1f%%", stats.winRate))
              StatMiniItem(label = "当前连胜", value = "${stats.currentWinStreak}连胜")
            } else {
              StatMiniItem(label = "先手", value = "黑棋")
              StatMiniItem(label = "模式", value = "双人")
            }
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          onDismiss()
          onRestart()
        },
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
        modifier = Modifier.testTag("dialog_play_again_button")
      ) {
        Text(text = "再来一局", fontWeight = FontWeight.Bold)
      }
    },
    dismissButton = {
      Row {
        if (uiState.moveHistory.isNotEmpty()) {
          TextButton(
            onClick = {
              onDismiss()
              onEnterReplay()
            },
            modifier = Modifier.testTag("dialog_replay_button")
          ) {
            Text(text = "复盘回放")
          }
        }
        TextButton(onClick = onDismiss) {
          Text(text = "查看棋盘")
        }
      }
    }
  )
}

@Composable
private fun StatMiniItem(label: String, value: String) {
  Column(horizontalAlignment = Alignment.CenterHorizontally) {
    Text(
      text = value,
      style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
      color = MaterialTheme.colorScheme.onSurface
    )
    Text(
      text = label,
      style = MaterialTheme.typography.labelSmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )
  }
}

@Composable
fun GameSettingsDialog(
  uiState: GomokuUiState,
  onDismiss: () -> Unit,
  onSelectMode: (GameMode) -> Unit,
  onSelectDifficulty: (Difficulty) -> Unit,
  onSelectHumanPiece: (Piece) -> Unit
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    shape = RoundedCornerShape(20.dp),
    containerColor = MaterialTheme.colorScheme.surface,
    title = {
      Text(
        text = "对局设置",
        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
      )
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState())
      ) {
        // Mode Selection
        Text(
          text = "对弈模式",
          style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
          color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          GameMode.entries.forEach { mode ->
            val isSelected = uiState.gameMode == mode
            Surface(
              shape = RoundedCornerShape(10.dp),
              color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
              border = BorderStroke(
                width = if (isSelected) 1.5.dp else 0.5.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
              ),
              modifier = Modifier
                .weight(1f)
                .clickable { onSelectMode(mode) }
            ) {
              Box(
                modifier = Modifier.padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = mode.title,
                  style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                  ),
                  color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }
          }
        }

        if (uiState.gameMode == GameMode.PVE) {
          Spacer(modifier = Modifier.height(18.dp))

          // Player Piece Selection
          Text(
            text = "玩家阵营 (执子)",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary
          )
          Spacer(modifier = Modifier.height(8.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            val pieces = listOf(
              Pair(Piece.BLACK, "执黑 (先手)"),
              Pair(Piece.WHITE, "执白 (后手)")
            )
            pieces.forEach { (p, label) ->
              val isSelected = uiState.humanPiece == p
              Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                border = BorderStroke(
                  width = if (isSelected) 1.5.dp else 0.5.dp,
                  color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
                ),
                modifier = Modifier
                  .weight(1f)
                  .clickable { onSelectHumanPiece(p) }
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Box(
                    modifier = Modifier
                      .size(16.dp)
                      .clip(CircleShape)
                      .background(if (p == Piece.BLACK) Color.Black else Color.White)
                      .border(0.8.dp, Color(0x66888888), CircleShape)
                  )
                  Spacer(modifier = Modifier.width(6.dp))
                  Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall.copy(
                      fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    ),
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }
              }
            }
          }

          Spacer(modifier = Modifier.height(18.dp))

          // Difficulty Selection
          Text(
            text = "AI 难度",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary
          )
          Spacer(modifier = Modifier.height(8.dp))
          Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Difficulty.entries.forEach { diff ->
              val isSelected = uiState.difficulty == diff
              Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                border = BorderStroke(
                  width = if (isSelected) 1.5.dp else 0.5.dp,
                  color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
                ),
                modifier = Modifier
                  .fillMaxWidth()
                  .clickable { onSelectDifficulty(diff) }
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  RadioButton(
                    selected = isSelected,
                    onClick = { onSelectDifficulty(diff) },
                    modifier = Modifier.size(20.dp)
                  )
                  Spacer(modifier = Modifier.width(8.dp))
                  Column {
                    Text(
                      text = diff.title,
                      style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                      color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                      text = diff.description,
                      style = MaterialTheme.typography.bodySmall,
                      color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                  }
                }
              }
            }
          }
        }
      }
    },
    confirmButton = {
      Button(onClick = onDismiss) {
        Text(text = "完成")
      }
    }
  )
}

@Composable
fun GameStatsDialog(
  stats: GameStats,
  onDismiss: () -> Unit,
  onReset: () -> Unit
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    shape = RoundedCornerShape(20.dp),
    containerColor = MaterialTheme.colorScheme.surface,
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = Icons.Default.EmojiEvents,
          contentDescription = null,
          tint = Color(0xFFD48B30)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "人机对弈战绩",
          style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
        )
      }
    },
    text = {
      Column(modifier = Modifier.fillMaxWidth()) {
        Card(
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Text(
              text = String.format("%.1f%%", stats.winRate),
              style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold),
              color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
              text = "综合胜率",
              style = MaterialTheme.typography.labelMedium,
              color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
            )
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          StatBox(label = "总局数", value = "${stats.totalGames}", modifier = Modifier.weight(1f))
          StatBox(label = "胜局", value = "${stats.wins}", modifier = Modifier.weight(1f))
          StatBox(label = "败局", value = "${stats.losses}", modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          StatBox(label = "和棋", value = "${stats.draws}", modifier = Modifier.weight(1f))
          StatBox(label = "当前连胜", value = "${stats.currentWinStreak}", modifier = Modifier.weight(1f))
          StatBox(label = "历史最高", value = "${stats.maxWinStreak}", modifier = Modifier.weight(1f))
        }
      }
    },
    confirmButton = {
      Button(onClick = onDismiss) {
        Text(text = "确定")
      }
    },
    dismissButton = {
      TextButton(onClick = onReset) {
        Text(text = "重置战绩", color = MaterialTheme.colorScheme.error)
      }
    }
  )
}

@Composable
private fun StatBox(label: String, value: String, modifier: Modifier = Modifier) {
  Surface(
    shape = RoundedCornerShape(10.dp),
    color = MaterialTheme.colorScheme.surfaceVariant,
    modifier = modifier
  ) {
    Column(
      modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Text(
        text = value,
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
      Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.outline
      )
    }
  }
}

@Composable
fun GameRulesDialog(onDismiss: () -> Unit) {
  AlertDialog(
    onDismissRequest = onDismiss,
    shape = RoundedCornerShape(20.dp),
    containerColor = MaterialTheme.colorScheme.surface,
    title = {
      Text(
        text = "五子棋规则与技巧",
        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
      )
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        RuleSection(
          title = "一、基本规则",
          body = "1. 棋盘规格为15×15标准网格。\n2. 对弈双方轮流在交叉点落子，黑方先行，白方后行。\n3. 先在横向、纵向或对角线任意方向连成五颗同色棋子者胜。"
        )
        RuleSection(
          title = "二、关键棋形",
          body = "• 活四：两端畅通的四个同色子，对方已无法阻拦，必胜！\n• 冲四：一端被挡的四连子，对方必须立即补防。\n• 活三：两端畅通的三连子，下一步可变为活四。\n• 眠三：一端被阻挡的三连子。"
        )
        RuleSection(
          title = "三、致胜战术",
          body = "• 双三杀：同时制造出两个活三，使对手无法同时兼顾防守。\n• 四三杀：一招走成冲四的同时形成活三，令对手疲于奔命。\n• 攻防兼备：落子时兼顾拓展自己的棋路并封堵对手的延伸。"
        )
      }
    },
    confirmButton = {
      Button(onClick = onDismiss) {
        Text(text = "我明白了")
      }
    }
  )
}

@Composable
private fun RuleSection(title: String, body: String) {
  Column {
    Text(
      text = title,
      style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
      color = MaterialTheme.colorScheme.primary
    )
    Spacer(modifier = Modifier.height(3.dp))
    Text(
      text = body,
      style = MaterialTheme.typography.bodySmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      lineHeight = 18.sp
    )
  }
}
