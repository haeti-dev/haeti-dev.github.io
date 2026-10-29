import log.haeti.buildlogic.configureComposeResources
import log.haeti.buildlogic.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

/**
 * Compose를 쓰는 KMP 라이브러리 모듈의 공통 설정. [KotlinMultiplatformConventionPlugin] 위에
 * Compose 플러그인 두 개와 리소스(`Res`) 패키지 규칙을 얹는다.
 */
class KotlinMultiplatformComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("haeti.kmp.library")
            pluginManager.apply("org.jetbrains.compose")
            pluginManager.apply("org.jetbrains.kotlin.plugin.compose")

            configureComposeResources()

            // 생성되는 Res 클래스는 리소스 유무와 무관하게 Compose Resources 런타임을 참조한다.
            // Compose 컴파일러 플러그인은 클래스패스에 compose.runtime이 없으면 ICE
            // (IncompatibleComposeRuntimeVersionException)로 죽는다. components-resources는 runtime을
            // 전이시키지 않으므로 UI 없는 모듈(core:data처럼 Res만 쓰는 경우)을 위해 여기서 보장한다.
            dependencies {
                add("commonMainApi", libs.findLibrary("compose-components-resources").get())
                add("commonMainImplementation", libs.findLibrary("compose-runtime").get())
            }
        }
    }
}
