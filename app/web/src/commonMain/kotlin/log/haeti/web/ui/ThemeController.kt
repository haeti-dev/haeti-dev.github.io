package log.haeti.web.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

/**
 * 다크 테마 상태 홀더. 토글할 때마다 [preference]에 즉시 기록해 새로고침 뒤에도 선택이 남는다.
 *
 * @param isDark 초기 다크 테마 여부
 * @param preference 테마 설정을 저장할 곳
 */
class ThemeController(
    isDark: Boolean,
    private val preference: ThemePreference,
) {
    /** 현재 다크 테마 여부. */
    var isDark: Boolean by mutableStateOf(isDark)
        private set

    /** 테마를 뒤집고 바뀐 값을 저장한다. */
    fun toggle() {
        isDark = !isDark
        preference.save(isDark)
    }
}

/**
 * [ThemeController]를 컴포지션에 기억시킨다.
 *
 * 초기값은 저장된 설정을 우선하고, 없으면 시스템 설정을 따른다.
 *
 * @param preference 테마 설정을 읽고 쓸 곳
 */
@Composable
fun rememberThemeController(preference: ThemePreference): ThemeController {
    val systemDark = isSystemInDarkTheme()
    return remember(preference) { ThemeController(preference.load() ?: systemDark, preference) }
}
