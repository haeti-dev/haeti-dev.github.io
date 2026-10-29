package log.haeti.admin.di

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.SingleIn
import log.haeti.admin.AdminViewModel

/**
 * admin 앱의 루트 의존성 그래프.
 *
 * 화면이 하나뿐이라 밖으로 내보내는 진입점도 [viewModel] 하나다.
 * `PostFileStore`는 생성자 주입으로, `ContentRoot`는 [AdminBindings]로 연결된다.
 */
@DependencyGraph(AppScope::class)
@SingleIn(AppScope::class)
interface AdminAppGraph {
    /** 앱 전역에서 공유하는 단일 ViewModel. */
    val viewModel: AdminViewModel
}
