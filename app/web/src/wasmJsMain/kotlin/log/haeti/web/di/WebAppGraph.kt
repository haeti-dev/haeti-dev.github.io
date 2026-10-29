package log.haeti.web.di

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.SingleIn

/**
 * 브라우저용 객체 그래프. 데스크톱과 마찬가지로 플랫폼 핸들이 필요 없어 팩토리 없이 만들 수 있다.
 */
@DependencyGraph(AppScope::class)
@SingleIn(AppScope::class)
interface WebAppGraph : AppGraph
