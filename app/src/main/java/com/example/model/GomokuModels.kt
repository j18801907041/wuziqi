package com.example.model

enum class Piece {
  NONE,
  BLACK,
  WHITE;

  fun opponent(): Piece = when (this) {
    BLACK -> WHITE
    WHITE -> BLACK
    NONE -> NONE
  }

  val displayName: String
    get() = when (this) {
      BLACK -> "黑棋"
      WHITE -> "白棋"
      NONE -> "空"
    }
}

data class Point(val row: Int, val col: Int) {
  val coordinateLabel: String
    get() {
      val colChar = ('A' + col.coerceIn(0, 14)).toString()
      val rowNum = (15 - row).toString()
      return "$colChar$rowNum"
    }
}

data class Move(
  val point: Point,
  val piece: Piece,
  val moveNumber: Int,
  val timestamp: Long = System.currentTimeMillis()
)

enum class GameMode(val title: String) {
  PVE("人机对战"),
  PVP("双人同屏")
}

enum class Difficulty(val title: String, val description: String) {
  EASY("初级", "适合新手，注重进攻，失误率较高"),
  MEDIUM("进阶", "攻防均衡，敏锐识破三四连子"),
  HARD("大师", "深度多步算路，擅长双三双四绝杀")
}

sealed class GameStatus {
  data object Playing : GameStatus()
  data class Won(val winner: Piece, val winningLine: List<Point>) : GameStatus()
  data object Draw : GameStatus()
}

data class GameStats(
  val totalGames: Int = 0,
  val wins: Int = 0,
  val losses: Int = 0,
  val draws: Int = 0,
  val currentWinStreak: Int = 0,
  val maxWinStreak: Int = 0
) {
  val winRate: Float
    get() = if (totalGames > 0) (wins.toFloat() / totalGames) * 100f else 0f
}
