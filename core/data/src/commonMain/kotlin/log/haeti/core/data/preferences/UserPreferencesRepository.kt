package log.haeti.core.data.preferences

import kotlinx.coroutines.flow.StateFlow
import log.haeti.core.model.Language

/**
 * 방문자가 고른 설정. 지금은 화면 언어 하나다.
 * 저장(브라우저 localStorage 등)은 플랫폼 셸이 맡고, 여기서는 현재 값만 들고 있다.
 */
interface UserPreferencesRepository {
    /** 현재 화면 언어. */
    val language: StateFlow<Language>

    /** 화면 언어를 바꾼다. */
    fun setLanguage(language: Language)
}
