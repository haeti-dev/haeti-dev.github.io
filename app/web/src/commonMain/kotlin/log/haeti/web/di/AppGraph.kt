package log.haeti.web.di

import dev.zacsweers.metrox.viewmodel.ViewModelGraph
import log.haeti.core.data.preferences.UserPreferencesRepository
import log.haeti.core.data.repository.PostRepository

/**
 * UI가 DI 그래프에서 가져다 쓰는 의존성.
 *
 * 플랫폼마다 필요한 핸들(브라우저 `window`, 데스크톱 파일 경로 등)이 달라
 * 실제 `@DependencyGraph` 선언은 각 플랫폼 소스셋이 맡는다. 셸은 이 인터페이스만 본다.
 */
interface AppGraph : ViewModelGraph {
    /** 글 Repository. 셸이 직접 쓰지는 않지만 그래프가 실제로 구성되는지 확인하는 접점이다. */
    val postRepository: PostRepository

    /** 화면 언어 같은 방문자 설정. 셸이 언어 전환 토글과 UI 문자열 세트 선택에 쓴다. */
    val userPreferences: UserPreferencesRepository
}
