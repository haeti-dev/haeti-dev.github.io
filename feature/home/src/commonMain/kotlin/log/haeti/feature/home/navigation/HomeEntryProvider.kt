package log.haeti.feature.home.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import log.haeti.core.navigation.HomeNavKey
import log.haeti.core.navigation.Navigator
import log.haeti.core.navigation.PostNavKey
import log.haeti.feature.home.HomeScreen

/**
 * 홈 화면을 앱 셸의 엔트리 목록에 등록한다.
 *
 * @param navigator 목록 행 클릭을 글 상세 이동으로 옮길 내비게이터
 */
fun EntryProviderScope<NavKey>.homeEntry(navigator: Navigator) {
    entry<HomeNavKey> {
        HomeScreen(onPostClick = { slug -> navigator.navigate(PostNavKey(slug)) })
    }
}
