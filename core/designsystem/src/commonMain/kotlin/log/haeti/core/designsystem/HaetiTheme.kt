package log.haeti.core.designsystem

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/** 키보드 포커스 링 색. [HaetiTheme]이 현재 다크/라이트 여부에 맞는 값을 공급한다. */
internal val LocalHaetiFocusRingColor = staticCompositionLocalOf<Color> { HaetiColors.Light.focusRing }

/** 현재 다크 테마 여부. 카테고리 색처럼 M3 ColorScheme 밖의 토큰을 고를 때 쓴다. */
val LocalHaetiDarkTheme = staticCompositionLocalOf { false }

/**
 * Haeti.log 앱 전체 테마.
 *
 * 블로그의 컬러·타이포 토큰을 M3 ColorScheme/Typography로 매핑해
 * 하위 컴포저블에 `MaterialTheme.colorScheme`·`MaterialTheme.typography`로 공급한다.
 * 포커스 링 색(브랜드 색 70%/75%)도 [LocalHaetiFocusRingColor]로 함께 공급한다.
 *
 * @param darkTheme 다크 테마 적용 여부
 * @param content 테마가 적용될 하위 컴포저블
 */
@Composable
fun HaetiTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) haetiDarkColorScheme() else haetiLightColorScheme()
    val typography = haetiTypography(pretendard = haetiPretendardFontFamily())
    val focusRingColor = if (darkTheme) HaetiColors.Dark.focusRing else HaetiColors.Light.focusRing

    CompositionLocalProvider(
        LocalHaetiFocusRingColor provides focusRingColor,
        LocalHaetiDarkTheme provides darkTheme,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = typography,
            content = content,
        )
    }
}
