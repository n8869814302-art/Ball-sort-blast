package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.BallSkin
import com.example.model.TubeTheme

class GamePreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("ball_sort_prefs", Context.MODE_PRIVATE)

    var currentLevel: Int
        get() = prefs.getInt("current_level", 1)
        set(value) = prefs.edit().putInt("current_level", value).apply()

    var maxUnlockedLevel: Int
        get() = prefs.getInt("max_unlocked_level", 1)
        set(value) = prefs.edit().putInt("max_unlocked_level", value).apply()

    var coins: Int
        get() = prefs.getInt("player_coins", 250)
        set(value) = prefs.edit().putInt("player_coins", value).apply()

    var volumeScale: Float
        get() = prefs.getFloat("volume_scale", 1.25f)
        set(value) = prefs.edit().putFloat("volume_scale", value).apply()

    var isCrazyModeEnabled: Boolean
        get() = prefs.getBoolean("is_crazy_mode", true)
        set(value) = prefs.edit().putBoolean("is_crazy_mode", value).apply()

    var isHapticsEnabled: Boolean
        get() = prefs.getBoolean("is_haptics", true)
        set(value) = prefs.edit().putBoolean("is_haptics", value).apply()

    var selectedBallSkin: BallSkin
        get() {
            val id = prefs.getString("selected_ball_skin", BallSkin.NEON.id)
            return BallSkin.entries.find { it.id == id } ?: BallSkin.NEON
        }
        set(value) = prefs.edit().putString("selected_ball_skin", value.id).apply()

    var selectedTubeTheme: TubeTheme
        get() {
            val id = prefs.getString("selected_tube_theme", TubeTheme.CYBER.id)
            return TubeTheme.entries.find { it.id == id } ?: TubeTheme.CYBER
        }
        set(value) = prefs.edit().putString("selected_tube_theme", value.id).apply()

    fun isSkinUnlocked(skin: BallSkin): Boolean {
        if (skin == BallSkin.NEON) return true
        val set = prefs.getStringSet("unlocked_skins", setOf(BallSkin.NEON.id)) ?: emptySet()
        return set.contains(skin.id)
    }

    fun unlockSkin(skin: BallSkin) {
        val current = prefs.getStringSet("unlocked_skins", setOf(BallSkin.NEON.id))?.toMutableSet()
            ?: mutableSetOf(BallSkin.NEON.id)
        current.add(skin.id)
        prefs.edit().putStringSet("unlocked_skins", current).apply()
    }

    fun isTubeUnlocked(theme: TubeTheme): Boolean {
        if (theme == TubeTheme.CYBER) return true
        val set = prefs.getStringSet("unlocked_tubes", setOf(TubeTheme.CYBER.id)) ?: emptySet()
        return set.contains(theme.id)
    }

    fun unlockTube(theme: TubeTheme) {
        val current = prefs.getStringSet("unlocked_tubes", setOf(TubeTheme.CYBER.id))?.toMutableSet()
            ?: mutableSetOf(TubeTheme.CYBER.id)
        current.add(theme.id)
        prefs.edit().putStringSet("unlocked_tubes", current).apply()
    }

    fun getBestMoves(level: Int): Int {
        return prefs.getInt("best_moves_lvl_$level", -1)
    }

    fun saveBestMoves(level: Int, moves: Int) {
        val current = getBestMoves(level)
        if (current == -1 || moves < current) {
            prefs.edit().putInt("best_moves_lvl_$level", moves).apply()
        }
    }
}
