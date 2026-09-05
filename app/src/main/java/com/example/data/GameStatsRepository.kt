package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.Difficulty
import com.example.model.GameMode
import com.example.model.GameStats
import com.example.model.Piece
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class GameStatsRepository(context: Context) {
  private val prefs: SharedPreferences =
    context.getSharedPreferences("gomoku_stats_prefs", Context.MODE_PRIVATE)

  private val _stats = MutableStateFlow(loadStats())
  val stats: StateFlow<GameStats> = _stats.asStateFlow()

  private fun loadStats(): GameStats {
    return GameStats(
      totalGames = prefs.getInt(KEY_TOTAL_GAMES, 0),
      wins = prefs.getInt(KEY_WINS, 0),
      losses = prefs.getInt(KEY_LOSSES, 0),
      draws = prefs.getInt(KEY_DRAWS, 0),
      currentWinStreak = prefs.getInt(KEY_CURRENT_STREAK, 0),
      maxWinStreak = prefs.getInt(KEY_MAX_STREAK, 0)
    )
  }

  fun recordResult(winner: Piece, humanPiece: Piece, mode: GameMode) {
    // Only count stats for PvE (against AI)
    if (mode != GameMode.PVE) return

    val current = _stats.value
    val isWin = winner == humanPiece
    val isDraw = winner == Piece.NONE
    val isLoss = !isWin && !isDraw

    val newTotal = current.totalGames + 1
    val newWins = if (isWin) current.wins + 1 else current.wins
    val newLosses = if (isLoss) current.losses + 1 else current.losses
    val newDraws = if (isDraw) current.draws + 1 else current.draws

    val newStreak = if (isWin) current.currentWinStreak + 1 else 0
    val newMaxStreak = maxOf(newStreak, current.maxWinStreak)

    prefs.edit().apply {
      putInt(KEY_TOTAL_GAMES, newTotal)
      putInt(KEY_WINS, newWins)
      putInt(KEY_LOSSES, newLosses)
      putInt(KEY_DRAWS, newDraws)
      putInt(KEY_CURRENT_STREAK, newStreak)
      putInt(KEY_MAX_STREAK, newMaxStreak)
      apply()
    }

    _stats.value = GameStats(
      totalGames = newTotal,
      wins = newWins,
      losses = newLosses,
      draws = newDraws,
      currentWinStreak = newStreak,
      maxWinStreak = newMaxStreak
    )
  }

  fun resetStats() {
    prefs.edit().clear().apply()
    _stats.value = GameStats()
  }

  fun saveGameSettings(mode: GameMode, difficulty: Difficulty, humanPiece: Piece) {
    prefs.edit()
      .putString(KEY_MODE, mode.name)
      .putString(KEY_DIFFICULTY, difficulty.name)
      .putString(KEY_HUMAN_PIECE, humanPiece.name)
      .apply()
  }

  fun getSavedGameSettings(): Triple<GameMode, Difficulty, Piece> {
    val modeName = prefs.getString(KEY_MODE, GameMode.PVE.name) ?: GameMode.PVE.name
    val diffName = prefs.getString(KEY_DIFFICULTY, Difficulty.MEDIUM.name) ?: Difficulty.MEDIUM.name
    val pieceName = prefs.getString(KEY_HUMAN_PIECE, Piece.BLACK.name) ?: Piece.BLACK.name

    val mode = try { GameMode.valueOf(modeName) } catch (_: Exception) { GameMode.PVE }
    val difficulty = try { Difficulty.valueOf(diffName) } catch (_: Exception) { Difficulty.MEDIUM }
    val piece = try { Piece.valueOf(pieceName) } catch (_: Exception) { Piece.BLACK }

    return Triple(mode, difficulty, piece)
  }

  companion object {
    private const val KEY_TOTAL_GAMES = "total_games"
    private const val KEY_WINS = "wins"
    private const val KEY_LOSSES = "losses"
    private const val KEY_DRAWS = "draws"
    private const val KEY_CURRENT_STREAK = "current_streak"
    private const val KEY_MAX_STREAK = "max_streak"

    private const val KEY_MODE = "pref_mode"
    private const val KEY_DIFFICULTY = "pref_difficulty"
    private const val KEY_HUMAN_PIECE = "pref_piece"
  }
}
