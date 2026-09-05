package com.example.game

import com.example.model.Difficulty
import com.example.model.Piece
import com.example.model.Point
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

object GomokuEngine {
  const val BOARD_SIZE = 15

  // Direction vectors: (dRow, dCol)
  private val DIRECTIONS = listOf(
    Pair(0, 1),   // Horizontal
    Pair(1, 0),   // Vertical
    Pair(1, 1),   // Diagonal \
    Pair(1, -1)   // Anti-diagonal /
  )

  fun createEmptyBoard(): Array<Array<Piece>> {
    return Array(BOARD_SIZE) { Array(BOARD_SIZE) { Piece.NONE } }
  }

  fun copyBoard(board: Array<Array<Piece>>): Array<Array<Piece>> {
    return Array(BOARD_SIZE) { r -> Array(BOARD_SIZE) { c -> board[r][c] } }
  }

  fun isValidPoint(row: Int, col: Int): Boolean {
    return row in 0 until BOARD_SIZE && col in 0 until BOARD_SIZE
  }

  /**
   * Checks if the move at [lastPoint] results in a 5-in-a-row win.
   * Returns the list of winning points if won, otherwise null.
   */
  fun checkWin(board: Array<Array<Piece>>, lastPoint: Point): List<Point>? {
    val piece = board[lastPoint.row][lastPoint.col]
    if (piece == Piece.NONE) return null

    for ((dr, dc) in DIRECTIONS) {
      val line = mutableListOf<Point>()
      line.add(lastPoint)

      // Forward direction
      var r = lastPoint.row + dr
      var c = lastPoint.col + dc
      while (isValidPoint(r, c) && board[r][c] == piece) {
        line.add(Point(r, c))
        r += dr
        c += dc
      }

      // Backward direction
      r = lastPoint.row - dr
      c = lastPoint.col - dc
      while (isValidPoint(r, c) && board[r][c] == piece) {
        line.add(Point(r, c))
        r -= dr
        c -= dc
      }

      if (line.size >= 5) {
        // Sort line by coordinates for clean visual highlight
        return line.sortedWith(compareBy({ it.row }, { it.col }))
      }
    }
    return null
  }

  fun isBoardFull(board: Array<Array<Piece>>): Boolean {
    for (r in 0 until BOARD_SIZE) {
      for (c in 0 until BOARD_SIZE) {
        if (board[r][c] == Piece.NONE) return false
      }
    }
    return true
  }

  /**
   * Find candidate points (empty points within distance 2 of existing pieces).
   * If board is empty, return center (7, 7).
   */
  fun getCandidatePoints(board: Array<Array<Piece>>, radius: Int = 2): List<Point> {
    var hasPiece = false
    val candidates = mutableSetOf<Point>()

    for (r in 0 until BOARD_SIZE) {
      for (c in 0 until BOARD_SIZE) {
        if (board[r][c] != Piece.NONE) {
          hasPiece = true
          for (dr in -radius..radius) {
            for (dc in -radius..radius) {
              val nr = r + dr
              val nc = c + dc
              if (isValidPoint(nr, nc) && board[nr][nc] == Piece.NONE) {
                candidates.add(Point(nr, nc))
              }
            }
          }
        }
      }
    }

    if (!hasPiece) {
      return listOf(Point(BOARD_SIZE / 2, BOARD_SIZE / 2))
    }
    return candidates.toList()
  }

  /**
   * Evaluate a single point for a player.
   * Returns a score reflecting how advantageous placing [player] at [point] is.
   */
  fun evaluatePoint(board: Array<Array<Piece>>, point: Point, player: Piece): Int {
    var totalScore = 0
    val opponent = player.opponent()

    for ((dr, dc) in DIRECTIONS) {
      // Analyze a window of length 9 centered at point
      var countPlayer = 1
      var openEnds = 0

      // Forward check
      var blockedForward = false
      var forwardSteps = 0
      var r = point.row + dr
      var c = point.col + dc
      while (forwardSteps < 4 && isValidPoint(r, c)) {
        if (board[r][c] == player) {
          countPlayer++
          forwardSteps++
          r += dr
          c += dc
        } else if (board[r][c] == Piece.NONE) {
          openEnds++
          break
        } else {
          blockedForward = true
          break
        }
      }
      if (!isValidPoint(r, c)) blockedForward = true

      // Backward check
      var blockedBackward = false
      var backwardSteps = 0
      r = point.row - dr
      c = point.col - dc
      while (backwardSteps < 4 && isValidPoint(r, c)) {
        if (board[r][c] == player) {
          countPlayer++
          backwardSteps++
          r -= dr
          c -= dc
        } else if (board[r][c] == Piece.NONE) {
          openEnds++
          break
        } else {
          blockedBackward = true
          break
        }
      }
      if (!isValidPoint(r, c)) blockedBackward = true

      // Score evaluation based on Gomoku patterns
      val score = when {
        countPlayer >= 5 -> 100000 // Win
        countPlayer == 4 && openEnds == 2 -> 15000 // Open 4 (Double win threat)
        countPlayer == 4 && openEnds == 1 -> 3500  // Rush 4
        countPlayer == 3 && openEnds == 2 -> 2500  // Open 3
        countPlayer == 3 && openEnds == 1 -> 400   // Sleeping 3
        countPlayer == 2 && openEnds == 2 -> 200   // Open 2
        countPlayer == 2 && openEnds == 1 -> 50    // Sleeping 2
        countPlayer == 1 && openEnds == 2 -> 10    // Single open stone
        else -> 2
      }
      totalScore += score
    }

    // Positional bonus: Center preference
    val centerDist = abs(point.row - 7) + abs(point.col - 7)
    totalScore += (14 - centerDist)

    return totalScore
  }

  /**
   * Determine the best move for [aiPiece] given the [difficulty].
   */
  fun calculateBestMove(
    board: Array<Array<Piece>>,
    aiPiece: Piece,
    difficulty: Difficulty
  ): Point {
    val candidates = getCandidatePoints(board)
    if (candidates.isEmpty()) {
      return Point(BOARD_SIZE / 2, BOARD_SIZE / 2)
    }

    val opponent = aiPiece.opponent()

    // 1. Check for immediate winning move
    for (pt in candidates) {
      board[pt.row][pt.col] = aiPiece
      val win = checkWin(board, pt)
      board[pt.row][pt.col] = Piece.NONE
      if (win != null) return pt
    }

    // 2. Check for immediate blocking opponent win
    for (pt in candidates) {
      board[pt.row][pt.col] = opponent
      val oppWin = checkWin(board, pt)
      board[pt.row][pt.col] = Piece.NONE
      if (oppWin != null) return pt
    }

    when (difficulty) {
      Difficulty.EASY -> {
        // Evaluate moves with some randomness / mistakes
        val scored = candidates.map { pt ->
          val attack = evaluatePoint(board, pt, aiPiece)
          val defense = evaluatePoint(board, pt, opponent)
          // In easy mode, add random variance and slightly lower defense weight
          val noise = Random.nextInt(-150, 150)
          val total = (attack + (defense * 0.85).toInt()) + noise
          Pair(pt, total)
        }.sortedByDescending { it.second }

        // Pick among top 3 moves randomly to simulate beginner level
        val topN = min(3, scored.size)
        return scored[Random.nextInt(topN)].first
      }

      Difficulty.MEDIUM -> {
        // Balanced tactical evaluation
        var bestMove = candidates.first()
        var bestScore = Int.MIN_VALUE

        for (pt in candidates) {
          val attack = evaluatePoint(board, pt, aiPiece)
          val defense = evaluatePoint(board, pt, opponent)
          // Weight defense slightly higher to block opponent's open 3s and rush 4s
          val score = (attack * 1.05 + defense * 1.25).toInt()
          if (score > bestScore) {
            bestScore = score
            bestMove = pt
          }
        }
        return bestMove
      }

      Difficulty.HARD -> {
        // Master AI: 2-ply Minimax with Alpha-Beta pruning over top candidate moves
        return findMasterMove(board, aiPiece, candidates)
      }
    }
  }

  private fun findMasterMove(
    board: Array<Array<Piece>>,
    aiPiece: Piece,
    candidates: List<Point>
  ): Point {
    val opponent = aiPiece.opponent()

    // Preliminary scoring of candidates to select top candidates for deeper search
    val scoredCandidates = candidates.map { pt ->
      val attack = evaluatePoint(board, pt, aiPiece)
      val defense = evaluatePoint(board, pt, opponent)
      // Check for double threat (double 3, double 4, four-three)
      val comboBonus = if (attack >= 4000 || defense >= 4000) 8000 else 0
      val score = (attack * 1.1 + defense * 1.3).toInt() + comboBonus
      Pair(pt, score)
    }.sortedByDescending { it.second }

    val topCandidates = scoredCandidates.take(min(10, scoredCandidates.size)).map { it.first }

    var bestMove = topCandidates.first()
    var bestValue = Int.MIN_VALUE
    var alpha = Int.MIN_VALUE
    val beta = Int.MAX_VALUE

    for (pt in topCandidates) {
      board[pt.row][pt.col] = aiPiece
      val win = checkWin(board, pt)
      val value = if (win != null) {
        1000000
      } else {
        // Opponent's turn (minimize)
        minimax(board, depth = 1, isMaximizing = false, aiPiece = aiPiece, alpha = alpha, beta = beta)
      }
      board[pt.row][pt.col] = Piece.NONE

      if (value > bestValue) {
        bestValue = value
        bestMove = pt
      }
      alpha = max(alpha, bestValue)
    }

    return bestMove
  }

  private fun minimax(
    board: Array<Array<Piece>>,
    depth: Int,
    isMaximizing: Boolean,
    aiPiece: Piece,
    alpha: Int,
    beta: Int
  ): Int {
    val opponent = aiPiece.opponent()

    if (depth == 0) {
      // Static evaluation
      var totalAiScore = 0
      var totalOppScore = 0
      for (r in 0 until BOARD_SIZE) {
        for (c in 0 until BOARD_SIZE) {
          if (board[r][c] == aiPiece) {
            totalAiScore += evaluatePoint(board, Point(r, c), aiPiece)
          } else if (board[r][c] == opponent) {
            totalOppScore += evaluatePoint(board, Point(r, c), opponent)
          }
        }
      }
      return totalAiScore - (totalOppScore * 1.2).toInt()
    }

    var currentAlpha = alpha
    var currentBeta = beta
    val candidates = getCandidatePoints(board).take(8)

    if (isMaximizing) {
      var maxEval = Int.MIN_VALUE
      for (pt in candidates) {
        board[pt.row][pt.col] = aiPiece
        val win = checkWin(board, pt)
        val evaluation = if (win != null) {
          100000
        } else {
          minimax(board, depth - 1, false, aiPiece, currentAlpha, currentBeta)
        }
        board[pt.row][pt.col] = Piece.NONE

        maxEval = max(maxEval, evaluation)
        currentAlpha = max(currentAlpha, evaluation)
        if (currentBeta <= currentAlpha) break
      }
      return maxEval
    } else {
      var minEval = Int.MAX_VALUE
      for (pt in candidates) {
        board[pt.row][pt.col] = opponent
        val win = checkWin(board, pt)
        val evaluation = if (win != null) {
          -100000
        } else {
          minimax(board, depth - 1, true, aiPiece, currentAlpha, currentBeta)
        }
        board[pt.row][pt.col] = Piece.NONE

        minEval = min(minEval, evaluation)
        currentBeta = min(currentBeta, evaluation)
        if (currentBeta <= currentAlpha) break
      }
      return minEval
    }
  }

  /**
   * Suggests a move with a strategic reason for the user.
   */
  fun getHint(board: Array<Array<Piece>>, currentPiece: Piece): Pair<Point, String> {
    val bestMove = calculateBestMove(board, currentPiece, Difficulty.HARD)
    val attack = evaluatePoint(board, bestMove, currentPiece)
    val defense = evaluatePoint(board, bestMove, currentPiece.opponent())

    val reason = when {
      attack >= 50000 -> "致胜之着：形成五子连线！"
      defense >= 50000 -> "紧要防守：拦截对手绝杀点！"
      attack >= 10000 -> "进攻良机：形成活四必胜局面！"
      defense >= 10000 -> "防守关键：阻止对手形成活四！"
      attack >= 2000 -> "拓展攻势：形成活三威胁"
      defense >= 2000 -> "化解危机：拆解对手活三攻势"
      else -> "抢占棋盘要道与战略连接点 (${bestMove.coordinateLabel})"
    }

    return Pair(bestMove, reason)
  }
}
