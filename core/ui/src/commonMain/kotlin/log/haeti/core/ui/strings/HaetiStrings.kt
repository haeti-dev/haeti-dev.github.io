package log.haeti.core.ui.strings

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import log.haeti.core.model.Language
import log.haeti.core.navigation.TopLevelDestination
import log.haeti.core.navigation.path.PageTitles

/**
 * 화면에 나오는 UI 문자열 세트. 캔버스로 그리는 웹은 브라우저 자동 번역이 닿지 않아서,
 * 언어 전환은 이 세트를 통째로 바꾸는 방식으로 한다. 값은 [KoreanStrings]·[EnglishStrings] 참고.
 */
@Immutable
data class HaetiStrings(
    val language: Language,
    val navBlog: String,
    val navProjects: String,
    val navAbout: String,
    val toggleTheme: String,
    val chooseLanguage: String,
    val introHeadline: String,
    val introDescription: String,
    val linkEmail: String,
    val pastPosts: String,
    val all: String,
    val feedError: String,
    val feedEmpty: String,
    val searchPlaceholder: String,
    val searchResults: String,
    val searchEmpty: String,
    val clearSearch: String,
    val backToList: String,
    val previousPost: String,
    val nextPost: String,
    val translationMissing: String,
    val postNotFoundTitle: String,
    val postNotFoundMessage: String,
    val postErrorTitle: String,
    val postErrorMessage: String,
    val notFoundTitle: String,
    val notFoundMessage: String,
    val goToList: String,
    val comingSoon: String,
    val browsePosts: String,
    val projectsMessage: String,
    val aboutMessage: String,
    val pageTitles: PageTitles,
    val categories: Map<String, String>,
) {
    /** GNB 라벨. */
    fun navLabel(destination: TopLevelDestination): String =
        when (destination) {
            TopLevelDestination.Blog -> navBlog
            TopLevelDestination.Projects -> navProjects
            TopLevelDestination.About -> navAbout
        }

    /** 카테고리 표시명. 색·필터는 원래 이름을 쓰고 화면에만 이 이름을 보인다. 모르는 이름은 그대로. */
    fun category(name: String): String = categories[name] ?: name
}

/** 언어에 맞는 UI 문자열 세트. */
fun haetiStrings(language: Language): HaetiStrings =
    when (language) {
        Language.KO -> KoreanStrings
        Language.EN -> EnglishStrings
    }

/** 현재 컴포지션의 UI 문자열 세트. 앱 셸이 화면 언어에 맞춰 제공한다. */
val LocalHaetiStrings = staticCompositionLocalOf { KoreanStrings }
