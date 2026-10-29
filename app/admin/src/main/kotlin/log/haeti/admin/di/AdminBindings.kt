package log.haeti.admin.di

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import log.haeti.admin.io.ContentRoot
import log.haeti.admin.io.locateContentRoot

/**
 * admin 앱 전역 바인딩 모음.
 *
 * [ContentRoot]는 생성자 주입으로 만들 수 없는 값(실행 위치 탐색 결과)이라 여기서 직접 공급한다.
 * 디렉터리 탐색은 앱 수명 동안 한 번이면 충분하므로 [SingleIn]으로 묶는다.
 */
@BindingContainer
@ContributesTo(AppScope::class)
object AdminBindings {
    /** 실행 위치를 기준으로 찾은 `content/` 디렉터리를 공급한다. */
    @Provides
    @SingleIn(AppScope::class)
    fun provideContentRoot(): ContentRoot =
        ContentRoot(locateContentRoot()).also {
            // 맥 앱에서 글이 안 보일 때 원인을 바로 알 수 있게 어느 폴더를 쓰는지 남긴다.
            System.err.println("[Haeti.log Admin] content: ${it.path}")
        }
}
