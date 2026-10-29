package log.haeti.core.navigation

import androidx.navigation3.runtime.NavKey
import androidx.savedstate.serialization.SavedStateConfiguration
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

/**
 * 백스택 저장·복원에 쓰는 직렬화 설정.
 *
 * Navigation 3는 백스택을 [NavKey] 다형성으로 직렬화하므로 구현체를 미리 전부 등록해야 한다.
 * 새 화면 키를 추가하면 여기에도 반드시 등록한다.
 */
val HaetiSavedStateConfiguration: SavedStateConfiguration =
    SavedStateConfiguration {
        serializersModule =
            SerializersModule {
                polymorphic(NavKey::class) {
                    subclass(HomeNavKey::class)
                    subclass(PostNavKey::class)
                    subclass(ProjectsNavKey::class)
                    subclass(AboutNavKey::class)
                    subclass(NotFoundNavKey::class)
                }
            }
    }
