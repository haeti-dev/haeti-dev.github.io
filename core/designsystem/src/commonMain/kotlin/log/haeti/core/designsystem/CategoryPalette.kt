package log.haeti.core.designsystem

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/**
 * 카테고리 하나가 쓰는 색 조합. 블로그에서 색은 "이 글이 어디 속하는가"라는 정보로만 쓴다.
 *
 * @param accent 라벨 텍스트·점·진행 바·커버 도형에 쓰는 진한 색
 * @param tint 커버·콜아웃 배경에 쓰는 옅은 바탕색
 */
@Immutable
data class CategoryPalette(
    val accent: Color,
    val tint: Color,
)

/** 라이트/다크 한 쌍. */
private class PalettePair(
    val light: CategoryPalette,
    val dark: CategoryPalette,
)

private fun pair(
    accentLight: Long,
    tintLight: Long,
    accentDark: Long,
    tintDark: Long,
) = PalettePair(
    light = CategoryPalette(Color(accentLight), Color(tintLight)),
    dark = CategoryPalette(Color(accentDark), Color(tintDark)),
)

private val Green = pair(0xFF138A5E, 0xFFE3F4EC, 0xFF4CC995, 0xFF15302A)
private val Violet = pair(0xFF6D4CE8, 0xFFEEEAFD, 0xFFA48CFF, 0xFF262043)
private val Blue = pair(0xFF2563D9, 0xFFE6EEFD, 0xFF70A0FF, 0xFF16263F)
private val Amber = pair(0xFFB97709, 0xFFFBF0DA, 0xFFF0B43C, 0xFF35290F)
private val Rose = pair(0xFFCF3A6E, 0xFFFCE7EF, 0xFFFF7FA8, 0xFF3A1A27)
private val Teal = pair(0xFF0E8395, 0xFFE0F3F6, 0xFF4CC6D8, 0xFF132F35)

/** 이름이 정해진 카테고리. 나머지는 이름 해시로 [Fallbacks] 중 하나를 고정 배정한다. */
private val Named =
    mapOf(
        "안드로이드" to Green,
        "코틀린" to Violet,
        "컴포즈" to Blue,
        "회고" to Amber,
        "웹" to Rose,
    )

private val Fallbacks = listOf(Teal, Rose, Blue, Violet, Green, Amber)

/**
 * 카테고리 이름에 대응하는 색 조합을 현재 테마(라이트/다크)에 맞춰 돌려준다.
 * 같은 이름은 항상 같은 색이다.
 *
 * @param category 프론트매터의 카테고리 표시명
 */
@Composable
fun categoryPalette(category: String): CategoryPalette {
    val pair = Named[category.trim()] ?: Fallbacks[category.hashCode().mod(Fallbacks.size)]
    return if (LocalHaetiDarkTheme.current) pair.dark else pair.light
}
