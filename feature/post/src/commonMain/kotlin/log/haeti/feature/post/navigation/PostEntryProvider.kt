package log.haeti.feature.post.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import dev.zacsweers.metrox.viewmodel.assistedMetroViewModel
import log.haeti.core.navigation.HomeNavKey
import log.haeti.core.navigation.Navigator
import log.haeti.core.navigation.PostNavKey
import log.haeti.feature.post.PostScreen
import log.haeti.feature.post.PostViewModel

/**
 * 글 상세 화면을 앱 셸의 엔트리 목록에 등록한다.
 * 슬러그를 ViewModel 키로도 써서 글이 바뀌면 새 인스턴스가 만들어진다.
 *
 * @param navigator 홈으로 돌아갈 때 쓰는 내비게이터
 * @param onTitleResolved 글 제목이 정해졌을 때 앱 셸에 알리는 콜백
 */
fun EntryProviderScope<NavKey>.postEntry(
    navigator: Navigator,
    onTitleResolved: (String) -> Unit,
) {
    entry<PostNavKey> { key ->
        PostScreen(
            onNavigateHome = { navigator.navigate(HomeNavKey) },
            onOpenPost = { slug -> navigator.navigate(PostNavKey(slug)) },
            onTitleResolved = onTitleResolved,
            viewModel =
                assistedMetroViewModel<PostViewModel, PostViewModel.Factory>(key = key.slug) {
                    create(key.slug)
                },
        )
    }
}
