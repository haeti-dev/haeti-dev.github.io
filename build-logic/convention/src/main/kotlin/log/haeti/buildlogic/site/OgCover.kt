package log.haeti.buildlogic.site

import java.awt.Color
import java.awt.Graphics2D
import java.awt.geom.RoundRectangle2D
import kotlin.random.Random

/** 미리보기 이미지 크기. 페이스북·카카오·X가 권장하는 1.91:1이다. */
internal const val OG_WIDTH = 1200
internal const val OG_HEIGHT = 630

/**
 * 카테고리 색. `core:designsystem`의 `CategoryPalette` 라이트 테마와 같은 값이다. 한쪽을 바꾸면 함께 바꾼다.
 *
 * @param accent 막대 색
 * @param tint 바탕색
 */
internal class CoverPalette(
    val accent: Color,
    val tint: Color,
)

private val Green = CoverPalette(Color(0x138A5E), Color(0xE3F4EC))
private val Violet = CoverPalette(Color(0x6D4CE8), Color(0xEEEAFD))
private val Blue = CoverPalette(Color(0x2563D9), Color(0xE6EEFD))
private val Amber = CoverPalette(Color(0xB97709), Color(0xFBF0DA))
private val Rose = CoverPalette(Color(0xCF3A6E), Color(0xFCE7EF))
private val Teal = CoverPalette(Color(0x0E8395), Color(0xE0F3F6))

private val Named = mapOf("안드로이드" to Green, "코틀린" to Violet, "컴포즈" to Blue, "회고" to Amber, "웹" to Rose)
private val Fallbacks = listOf(Teal, Rose, Blue, Violet, Green, Amber)

/** 카테고리 이름의 색. 앱과 같은 규칙(이름 해시 → 팔레트 인덱스)이다. */
internal fun coverPalette(category: String): CoverPalette =
    Named[category.trim()] ?: Fallbacks[category.hashCode().mod(Fallbacks.size)]

private const val LINE_COUNT = 9

/**
 * 블로그가 썸네일 없는 글에 그리는 "코드 미니맵"을 그대로 그린다. `core:ui`의 `PostCover`와 같은 시드·비율이라
 * 같은 글은 블로그 화면과 같은 그림이 된다. 한쪽을 바꾸면 함께 바꾼다.
 *
 * @param seed 글 슬러그
 * @param accent 막대 색
 * @param width 그릴 영역 폭
 * @param height 그릴 영역 높이
 */
internal fun Graphics2D.drawCodeMinimap(
    seed: String,
    accent: Color,
    width: Float,
    height: Float,
) {
    val random = Random(seed.hashCode())
    var indent = 0
    val lines =
        List(LINE_COUNT) {
            indent = (indent + random.nextInt(-1, 2)).coerceIn(0, 3)
            indent to List(1 + random.nextInt(3)) { 0.05f + random.nextFloat() * 0.17f }
        }
    val highlighted = 2 + random.nextInt(LINE_COUNT - 3)
    val lineHeight = height * 0.05f
    val maxRight = width * 0.9f
    lines.forEachIndexed { row, (lineIndent, segments) ->
        val y = height * 0.14f + row * height * 0.085f
        var x = width * 0.1f + lineIndent * lineHeight * 2.4f
        segments.forEachIndexed { index, fraction ->
            val segment = (width * fraction).coerceAtMost(maxRight - x)
            if (segment <= lineHeight) return@forEachIndexed
            val alpha = if (row == highlighted) 1f else if (index == 0) 0.55f else 0.26f
            color = Color(accent.red, accent.green, accent.blue, (alpha * 255).toInt())
            fill(RoundRectangle2D.Float(x, y, segment, lineHeight, lineHeight, lineHeight))
            x += segment + lineHeight * 0.9f
        }
        if (row == highlighted && x + lineHeight < maxRight) {
            color = accent
            fill(java.awt.geom.Rectangle2D.Float(x, y - lineHeight * 0.3f, lineHeight * 0.32f, lineHeight * 1.6f))
        }
    }
}
