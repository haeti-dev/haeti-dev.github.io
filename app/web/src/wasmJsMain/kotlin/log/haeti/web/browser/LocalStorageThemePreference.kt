package log.haeti.web.browser

import kotlinx.browser.localStorage
import log.haeti.web.ui.ThemePreference

/** 테마 설정을 담는 `localStorage` 키. */
private const val THEME_KEY = "haeti.theme"

/** 다크 테마를 뜻하는 저장 값. */
private const val DARK_VALUE = "dark"

/** 라이트 테마를 뜻하는 저장 값. */
private const val LIGHT_VALUE = "light"

/**
 * 테마 설정을 브라우저 `localStorage`에 남긴다.
 *
 * 시크릿 모드나 저장 공간 차단 설정에서는 접근 자체가 예외를 던지므로 모두 삼키고
 * "설정 없음"으로 처리한다. 테마는 시스템 설정으로 되돌아갈 뿐 앱은 계속 뜬다.
 */
object LocalStorageThemePreference : ThemePreference {
    override fun load(): Boolean? {
        val stored = runCatching { localStorage.getItem(THEME_KEY) }.getOrNull()
        return when (stored) {
            DARK_VALUE -> true
            LIGHT_VALUE -> false
            else -> null
        }
    }

    override fun save(isDark: Boolean) {
        runCatching { localStorage.setItem(THEME_KEY, if (isDark) DARK_VALUE else LIGHT_VALUE) }
    }
}
