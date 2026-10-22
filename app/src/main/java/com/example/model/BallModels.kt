package com.example.model

import androidx.compose.ui.graphics.Color

data class GameBallColor(
    val id: Int,
    val name: String,
    val primary: Color,
    val highlight: Color,
    val shadow: Color,
    val glow: Color,
    val emojiGlyph: String,
    val sportsGlyph: String,
    val gemGlyph: String
)

object BallPalette {
    val colors = listOf(
        GameBallColor(
            id = 0,
            name = "Blaze Red",
            primary = Color(0xFFFF2A55),
            highlight = Color(0xFFFF859E),
            shadow = Color(0xFF9E0B2B),
            glow = Color(0xFFFF5277),
            emojiGlyph = "😈",
            sportsGlyph = "⚽",
            gemGlyph = "💎"
        ),
        GameBallColor(
            id = 1,
            name = "Solar Orange",
            primary = Color(0xFFFF8C00),
            highlight = Color(0xFFFFC078),
            shadow = Color(0xFFA85100),
            glow = Color(0xFFFFA726),
            emojiGlyph = "🦁",
            sportsGlyph = "🏀",
            gemGlyph = "🔶"
        ),
        GameBallColor(
            id = 2,
            name = "Laser Yellow",
            primary = Color(0xFFFFEA00),
            highlight = Color(0xFFFFF799),
            shadow = Color(0xFFA69800),
            glow = Color(0xFFFFF176),
            emojiGlyph = "😎",
            sportsGlyph = "🎾",
            gemGlyph = "🌟"
        ),
        GameBallColor(
            id = 3,
            name = "Acid Green",
            primary = Color(0xFF00E676),
            highlight = Color(0xFF8CFAB7),
            shadow = Color(0xFF008F47),
            glow = Color(0xFF69F0AE),
            emojiGlyph = "👽",
            sportsGlyph = "⚾",
            gemGlyph = "💠"
        ),
        GameBallColor(
            id = 4,
            name = "Cyber Cyan",
            primary = Color(0xFF00E5FF),
            highlight = Color(0xFF99F5FF),
            shadow = Color(0xFF008394),
            glow = Color(0xFF80D8FF),
            emojiGlyph = "🤖",
            sportsGlyph = "🏐",
            gemGlyph = "🔮"
        ),
        GameBallColor(
            id = 5,
            name = "Electric Blue",
            primary = Color(0xFF2979FF),
            highlight = Color(0xFF96BBFF),
            shadow = Color(0xFF0A44B0),
            glow = Color(0xFF82B1FF),
            emojiGlyph = "🥶",
            sportsGlyph = "🎳",
            gemGlyph = "🔷"
        ),
        GameBallColor(
            id = 6,
            name = "Neon Violet",
            primary = Color(0xFFD500F9),
            highlight = Color(0xFFF180FF),
            shadow = Color(0xFF7B0091),
            glow = Color(0xFFEA80FC),
            emojiGlyph = "🤩",
            sportsGlyph = "🎱",
            gemGlyph = "💍"
        ),
        GameBallColor(
            id = 7,
            name = "Hot Pink",
            primary = Color(0xFFFF1784),
            highlight = Color(0xFFFF85BC),
            shadow = Color(0xFF9E004B),
            glow = Color(0xFFFF4081),
            emojiGlyph = "😜",
            sportsGlyph = "🏈",
            gemGlyph = "✨"
        )
    )

    fun getColor(id: Int): GameBallColor {
        return colors.getOrElse(id % colors.size) { colors[0] }
    }
}

enum class BallSkin(val id: String, val title: String, val price: Int, val description: String) {
    NEON("neon", "Cyber Glow", 0, "Ultra-bright neon spheres with glowing specular reflections"),
    EMOJI("emoji", "Crazy Emojis", 150, "Expressive cartoon faces with loud personalities"),
    SPORTS("sports", "Sports Mania", 300, "Soccer, basketball, 8-ball and more"),
    GEMS("gems", "Cosmic Jewels", 450, "Radiant gemstones and cosmic diamonds")
}

enum class TubeTheme(val id: String, val title: String, val price: Int, val rimColor: Long) {
    CYBER("cyber", "Cyber Neon", 0, 0xFF00E5FF),
    MAGIC("magic", "Magic Flask", 200, 0xFFD500F9),
    GOLD("gold", "Golden Luxury", 350, 0xFFFFD700)
}

enum class GameMode(val title: String, val subtitle: String) {
    CLASSIC("Classic Quest", "Solve puzzles at your own pace"),
    RUSH("Crazy Rush", "90s Blitz! Earn time for every tube"),
    DAILY("Daily Mastery", "Challenging curated puzzle")
}

data class TubeSnapshot(
    val tubes: List<List<Int>>,
    val moves: Int,
    val extraTubes: Int
)

data class HintMove(
    val fromTubeIndex: Int,
    val toTubeIndex: Int
)
