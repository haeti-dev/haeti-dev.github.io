package log.haeti.web.di

import androidx.lifecycle.ViewModel
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactory
import dev.zacsweers.metrox.viewmodel.MetroViewModelFactory
import kotlin.reflect.KClass

/**
 * Metro가 모아둔 ViewModel 멀티바인딩을 `ViewModelProvider.Factory`로 바꿔준다.
 *
 * 안드로이드의 `@HiltViewModel` 생성 팩토리가 하던 일을 대신하며, `@AssistedInject`
 * ViewModel(글 상세의 slug 같은 런타임 인자)도 함께 처리한다.
 *
 * @param viewModelProviders 인자 없는 ViewModel 생성자 맵
 * @param manualAssistedFactoryProviders assisted 인자를 받는 ViewModel 팩토리 맵
 */
@Inject
@SingleIn(AppScope::class)
class HaetiViewModelFactory(
    override val viewModelProviders: Map<KClass<out ViewModel>, () -> ViewModel>,
    override val manualAssistedFactoryProviders:
        Map<KClass<out ManualViewModelAssistedFactory>, () -> ManualViewModelAssistedFactory>,
) : MetroViewModelFactory()

/** [HaetiViewModelFactory]를 [MetroViewModelFactory] 타입으로 그래프에 묶는다. */
@BindingContainer
@ContributesTo(AppScope::class)
object ViewModelBindings {
    /** 앱 스코프에 하나만 두는 ViewModel 팩토리. */
    @Provides
    @SingleIn(AppScope::class)
    fun provideMetroViewModelFactory(factory: HaetiViewModelFactory): MetroViewModelFactory = factory
}
