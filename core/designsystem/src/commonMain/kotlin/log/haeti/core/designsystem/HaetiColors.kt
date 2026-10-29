package log.haeti.core.designsystem

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/**
 * Haeti.log 뉴트럴·브랜드 컬러 토큰.
 *
 * 화면의 바탕은 차가운 뉴트럴로 조용히 두고, 색은 카테고리([CategoryPalette])와
 * 브랜드 파랑(워드마크 `.log`·활성 메뉴·언어 토글·링크 호버·포커스)에만 쓴다.
 */
object HaetiColors {
    /** 라이트 테마 토큰. */
    object Light {
        val bg = Color(0xFFFCFCFD)
        val surface = Color(0xFFF3F4F7)
        val hover = Color(0xFFEDEFF3)
        val line = Color(0xFFE5E7EC)
        val ink = Color(0xFF16181D)
        val inkMuted = Color(0xFF5F6675)
        val inkSubtle = Color(0xFF959BA8)
        val brand = Color(0xFF2B6BEF)

        /** 키보드 포커스 링 색. */
        val focusRing = brand.copy(alpha = 0.7f)
    }

    /** 다크 테마 토큰. */
    object Dark {
        val bg = Color(0xFF131519)
        val surface = Color(0xFF1C1F25)
        val hover = Color(0xFF23272E)
        val line = Color(0xFF2B2F37)
        val ink = Color(0xFFEDEEF1)
        val inkMuted = Color(0xFFA3A9B5)
        val inkSubtle = Color(0xFF6E7480)
        val brand = Color(0xFF6FA3FF)

        /** 키보드 포커스 링 색. */
        val focusRing = brand.copy(alpha = 0.75f)
    }
}

/**
 * [HaetiColors.Light]를 M3 [ColorScheme]으로 매핑한다.
 *
 * background=bg, surface=surface, surfaceVariant=hover, outline=line, outlineVariant=inkSubtle,
 * onBackground/onSurface=ink, onSurfaceVariant=inkMuted, primary=brand.
 */
internal fun haetiLightColorScheme(): ColorScheme =
    lightColorScheme(
        primary = HaetiColors.Light.brand,
        onPrimary = HaetiColors.Light.bg,
        background = HaetiColors.Light.bg,
        onBackground = HaetiColors.Light.ink,
        surface = HaetiColors.Light.surface,
        onSurface = HaetiColors.Light.ink,
        surfaceVariant = HaetiColors.Light.hover,
        onSurfaceVariant = HaetiColors.Light.inkMuted,
        outline = HaetiColors.Light.line,
        outlineVariant = HaetiColors.Light.inkSubtle,
    )

/** [HaetiColors.Dark]를 M3 [ColorScheme]으로 매핑한다. 규칙은 [haetiLightColorScheme]과 같다. */
internal fun haetiDarkColorScheme(): ColorScheme =
    darkColorScheme(
        primary = HaetiColors.Dark.brand,
        onPrimary = Color(0xFF0E1830),
        background = HaetiColors.Dark.bg,
        onBackground = HaetiColors.Dark.ink,
        surface = HaetiColors.Dark.surface,
        onSurface = HaetiColors.Dark.ink,
        surfaceVariant = HaetiColors.Dark.hover,
        onSurfaceVariant = HaetiColors.Dark.inkMuted,
        outline = HaetiColors.Dark.line,
        outlineVariant = HaetiColors.Dark.inkSubtle,
    )
