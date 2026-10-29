package log.haeti.feature.pages.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import log.haeti.core.navigation.AboutNavKey
import log.haeti.core.navigation.HomeNavKey
import log.haeti.core.navigation.Navigator
import log.haeti.core.navigation.NotFoundNavKey
import log.haeti.core.navigation.ProjectsNavKey
import log.haeti.core.ui.strings.LocalHaetiStrings
import log.haeti.feature.pages.ComingSoonScreen
import log.haeti.feature.pages.PlaceholderScreen

/**
 * 준비 중 페이지(프로젝트·소개)와 알 수 없는 경로 안내를 앱 셸의 엔트리 목록에 등록한다.
 *
 * @param navigator "홈으로"를 처리할 내비게이터
 */
fun EntryProviderScope<NavKey>.pagesEntries(navigator: Navigator) {
    val navigateHome: () -> Unit = { navigator.navigate(HomeNavKey) }
    entry<ProjectsNavKey> {
        val strings = LocalHaetiStrings.current
        ComingSoonScreen(strings.navProjects, strings.projectsMessage, navigateHome)
    }
    entry<AboutNavKey> {
        val strings = LocalHaetiStrings.current
        ComingSoonScreen(strings.navAbout, strings.aboutMessage, navigateHome)
    }
    entry<NotFoundNavKey> {
        val strings = LocalHaetiStrings.current
        PlaceholderScreen(navigateHome, strings.notFoundTitle, strings.notFoundMessage)
    }
}
