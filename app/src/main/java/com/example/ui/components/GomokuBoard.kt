package com.example.ui.components

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.game.GomokuEngine
import com.example.model.Move
import com.example.model.Piece
import com.example.model.Point
import com.example.ui.theme.SleekBoardBackground
import com.example.ui.theme.SleekBoardBorder
import com.example.ui.theme.SleekOutline
import com.example.ui.theme.SleekPrimary
import com.example.ui.theme.SleekPrimaryContainer
import kotlin.math.roundToInt

@Composable
fun GomokuBoard(
  board: List<List<Piece>>,
  lastMove: Move?,
  winningLine: List<Point>?,
  hintPoint: Point?,
  showMoveNumbers: Boolean,
  moveHistory: List<Move>,
  onCellClick: (row: Int, col: Int) -> Unit,
  modifier: Modifier = Modifier
) {
  val haptic = LocalHapticFeedback.current

  // Pulse animation for hint and last move
  val infiniteTransition = rememberInfiniteTransition(label = "pulse")
  val pulseProgress by infiniteTransition.animateFloat(
    initialValue = 0.9f,
    targetValue = 1.22f,
    animationSpec = infiniteRepeatable(
      animation = tween(850, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "pulseScale"
  )

  val moveNumberMap = remember(moveHistory) {
    val map = mutableMapOf<Pair<Int, Int>, Int>()
    moveHistory.forEach { move ->
      map[Pair(move.point.row, move.point.col)] = move.moveNumber
    }
    map
  }

  // Text paints
  val textPaintCoord = remember {
    Paint().apply {
      isAntiAlias = true
      textSize = 26f
      color = android.graphics.Color.argb(175, 121, 116, 126) // SleekTextMuted
      textAlign = Paint.Align.CENTER
      typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
    }
  }

  val textPaintMoveWhite = remember {
    Paint().apply {
      isAntiAlias = true
      textSize = 30f
      color = android.graphics.Color.WHITE
      textAlign = Paint.Align.CENTER
      typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
    }
  }

  val textPaintMoveBlack = remember {
    Paint().apply {
      isAntiAlias = true
      textSize = 30f
      color = android.graphics.Color.argb(235, 29, 27, 32)
      textAlign = Paint.Align.CENTER
      typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
    }
  }

  Box(
    modifier = modifier
      .fillMaxWidth()
      .widthIn(max = 540.dp)
      .aspectRatio(1f)
      .padding(horizontal = 8.dp, vertical = 2.dp),
    contentAlignment = Alignment.Center
  ) {
    Surface(
      modifier = Modifier
        .fillMaxSize()
        .shadow(
          elevation = 8.dp,
          shape = RoundedCornerShape(24.dp),
          ambientColor = Color(0x1A000000),
          spotColor = Color(0x26000000)
        )
        .border(1.dp, SleekBoardBorder, RoundedCornerShape(24.dp))
        .clip(RoundedCornerShape(24.dp))
        .testTag("gomoku_board"),
      color = SleekBoardBackground
    ) {
      Canvas(
        modifier = Modifier
          .fillMaxSize()
          .pointerInput(Unit) {
            detectTapGestures { offset ->
              val boardSize = GomokuEngine.BOARD_SIZE
              val padding = size.width * 0.065f
              val playableWidth = size.width - (2 * padding)
              val cellSize = playableWidth / (boardSize - 1)

              val col = ((offset.x - padding) / cellSize).roundToInt()
              val row = ((offset.y - padding) / cellSize).roundToInt()

              if (row in 0 until boardSize && col in 0 until boardSize) {
                val targetX = padding + col * cellSize
                val targetY = padding + row * cellSize
                val distSq = (offset.x - targetX) * (offset.x - targetX) +
                  (offset.y - targetY) * (offset.y - targetY)

                if (distSq <= (cellSize * 0.65f) * (cellSize * 0.65f)) {
                  haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                  onCellClick(row, col)
                }
              }
            }
          }
      ) {
        val width = size.width
        val height = size.height
        val boardSize = GomokuEngine.BOARD_SIZE

        // 1. Sleek pristine board background with delicate gradient
        drawRoundRect(
          brush = Brush.linearGradient(
            colors = listOf(
              Color(0xFFFFFFFF),
              Color(0xFFFAF7FD)
            ),
            start = Offset(0f, 0f),
            end = Offset(width, height)
          ),
          size = Size(width, height),
          cornerRadius = CornerRadius(24.dp.toPx(), 24.dp.toPx())
        )

        // Board geometry
        val margin = width * 0.068f
        val playableWidth = width - (2 * margin)
        val cellSize = playableWidth / (boardSize - 1)
        val stoneRadius = cellSize * 0.44f

        textPaintCoord.textSize = cellSize * 0.32f
        val moveTextSize = cellSize * 0.44f
        textPaintMoveWhite.textSize = moveTextSize
        textPaintMoveBlack.textSize = moveTextSize

        val gridLineColor = Color(0x6679747E)
        val outerBorderColor = Color(0xFF79747E)

        // 2. Outer boundary line
        drawRect(
          color = outerBorderColor,
          topLeft = Offset(margin, margin),
          size = Size(playableWidth, playableWidth),
          style = Stroke(width = 2.2f)
        )

        // 3. Sleek Grid lines
        for (i in 0 until boardSize) {
          val pos = margin + i * cellSize

          // Horizontal
          drawLine(
            color = gridLineColor,
            start = Offset(margin, pos),
            end = Offset(width - margin, pos),
            strokeWidth = if (i == 0 || i == boardSize - 1) 2.2f else 1.2f
          )

          // Vertical
          drawLine(
            color = gridLineColor,
            start = Offset(pos, margin),
            end = Offset(pos, height - margin),
            strokeWidth = if (i == 0 || i == boardSize - 1) 2.2f else 1.2f
          )
        }

        // 4. Sleek Coordinates (Top: A-O, Left: 15-1)
        for (i in 0 until boardSize) {
          val pos = margin + i * cellSize
          val colChar = ('A' + i).toString()
          val rowNum = (15 - i).toString()

          // Column Letter
          drawContext.canvas.nativeCanvas.drawText(
            colChar,
            pos,
            margin * 0.65f,
            textPaintCoord
          )
          // Row Number
          drawContext.canvas.nativeCanvas.drawText(
            rowNum,
            margin * 0.45f,
            pos + textPaintCoord.textSize * 0.35f,
            textPaintCoord
          )
        }

        // 5. Star Points (星位): (3,3), (3,11), (7,7), (11,3), (11,11)
        val starPoints = listOf(
          Pair(3, 3), Pair(3, 11),
          Pair(7, 7),
          Pair(11, 3), Pair(11, 11)
        )
        val starRadius = cellSize * 0.11f
        starPoints.forEach { (r, c) ->
          val cx = margin + c * cellSize
          val cy = margin + r * cellSize
          drawCircle(
            color = Color(0xFF49454F),
            radius = starRadius,
            center = Offset(cx, cy)
          )
        }

        // 6. Draw hint ring in Sleek Violet
        if (hintPoint != null && board[hintPoint.row][hintPoint.col] == Piece.NONE) {
          val cx = margin + hintPoint.col * cellSize
          val cy = margin + hintPoint.row * cellSize
          val hintR = stoneRadius * pulseProgress

          drawCircle(
            color = SleekPrimaryContainer.copy(alpha = 0.6f),
            radius = hintR,
            center = Offset(cx, cy)
          )
          drawCircle(
            color = SleekPrimary,
            radius = stoneRadius * 0.85f,
            center = Offset(cx, cy),
            style = Stroke(width = 3f)
          )
          drawCircle(
            color = SleekPrimary,
            radius = 5.dp.toPx(),
            center = Offset(cx, cy)
          )
        }

        // 7. Draw Stones
        for (r in 0 until boardSize) {
          for (c in 0 until boardSize) {
            val piece = board[r][c]
            if (piece == Piece.NONE) continue

            val cx = margin + c * cellSize
            val cy = margin + r * cellSize
            val center = Offset(cx, cy)

            // Soft shadow under stone
            drawCircle(
              color = Color(0x33000000),
              radius = stoneRadius * 1.02f,
              center = Offset(cx + 2f, cy + 3f)
            )

            if (piece == Piece.BLACK) {
              // Sleek Black stone: smooth deep glossy sheen
              drawCircle(
                brush = Brush.radialGradient(
                  colors = listOf(
                    Color(0xFF4A4A4A),
                    Color(0xFF222222),
                    Color(0xFF0D0D0D)
                  ),
                  center = Offset(cx - stoneRadius * 0.35f, cy - stoneRadius * 0.35f),
                  radius = stoneRadius * 1.25f
                ),
                radius = stoneRadius,
                center = center
              )

              // Subtle highlight curve
              drawCircle(
                brush = Brush.radialGradient(
                  colors = listOf(Color(0x40FFFFFF), Color(0x00FFFFFF)),
                  center = Offset(cx - stoneRadius * 0.35f, cy - stoneRadius * 0.35f),
                  radius = stoneRadius * 0.5f
                ),
                radius = stoneRadius * 0.45f,
                center = Offset(cx - stoneRadius * 0.35f, cy - stoneRadius * 0.35f)
              )
            } else {
              // Sleek White stone: pure porcelain with subtle grey rim
              drawCircle(
                brush = Brush.radialGradient(
                  colors = listOf(
                    Color(0xFFFFFFFF),
                    Color(0xFFF6F5F8),
                    Color(0xFFE6E1E5)
                  ),
                  center = Offset(cx - stoneRadius * 0.35f, cy - stoneRadius * 0.35f),
                  radius = stoneRadius * 1.25f
                ),
                radius = stoneRadius,
                center = center
              )

              // Delicate perimeter border
              drawCircle(
                color = SleekOutline,
                radius = stoneRadius,
                center = center,
                style = Stroke(width = 1.2f)
              )
            }

            // Move numbers
            if (showMoveNumbers) {
              val moveNum = moveNumberMap[Pair(r, c)]
              if (moveNum != null) {
                val paint = if (piece == Piece.BLACK) textPaintMoveWhite else textPaintMoveBlack
                val textY = cy - (paint.descent() + paint.ascent()) / 2f
                drawContext.canvas.nativeCanvas.drawText(
                  moveNum.toString(),
                  cx,
                  textY,
                  paint
                )
              }
            }
          }
        }

        // 8. Sleek Interface ring indicator for last move: ring-2 ring-[#6750A4] ring-offset-2
        if (lastMove != null && !showMoveNumbers) {
          val cx = margin + lastMove.point.col * cellSize
          val cy = margin + lastMove.point.row * cellSize
          val center = Offset(cx, cy)

          // Outer ring with ring-offset
          drawCircle(
            color = SleekPrimary,
            radius = stoneRadius + 3.dp.toPx(),
            center = center,
            style = Stroke(width = 2.5f)
          )

          // Center accent dot
          drawCircle(
            color = if (lastMove.piece == Piece.BLACK) SleekPrimaryContainer else SleekPrimary,
            radius = stoneRadius * 0.22f,
            center = center
          )
        }

        // 9. Winning line sleek violet beam & halo
        if (winningLine != null && winningLine.size >= 5) {
          val first = winningLine.first()
          val last = winningLine.last()
          val startOffset = Offset(margin + first.col * cellSize, margin + first.row * cellSize)
          val endOffset = Offset(margin + last.col * cellSize, margin + last.row * cellSize)

          // Sleek glowing lavender beam
          drawLine(
            color = SleekPrimary.copy(alpha = 0.4f),
            start = startOffset,
            end = endOffset,
            strokeWidth = 12f,
            cap = StrokeCap.Round
          )
          // Solid Sleek violet beam
          drawLine(
            color = SleekPrimary,
            start = startOffset,
            end = endOffset,
            strokeWidth = 4.5f,
            cap = StrokeCap.Round
          )

          // Glowing halos around winning stones
          winningLine.forEach { pt ->
            val cx = margin + pt.col * cellSize
            val cy = margin + pt.row * cellSize
            drawCircle(
              color = SleekPrimary,
              radius = stoneRadius * 1.15f,
              center = Offset(cx, cy),
              style = Stroke(width = 3.5f)
            )
          }
        }
      }
    }
  }
}
