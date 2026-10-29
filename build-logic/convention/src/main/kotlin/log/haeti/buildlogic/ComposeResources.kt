package log.haeti.buildlogic

import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.jetbrains.compose.ComposeExtension
import org.jetbrains.compose.resources.ResourcesExtension

/**
 * 모듈마다 Gradle 경로에서 유도한 고유 `Res` 패키지를 부여한다.
 * `:core:designsystem` → `log.haeti.core.designsystem.generated.resources`.
 * 같은 FQN을 공유하면 마지막에 컴파일된 모듈이 이기므로 반드시 모듈별로 달라야 한다.
 */
internal fun Project.configureComposeResources() {
    val compose = extensions.getByType(ComposeExtension::class.java)
    val resources = (compose as ExtensionAware).extensions.getByName("resources") as ResourcesExtension

    resources.publicResClass = true
    resources.packageOfResClass = "log.haeti" +
        path.replace(":", ".").replace("-", "") +
        ".generated.resources"
    resources.generateResClass = ResourcesExtension.ResourceClassGeneration.Always
}
