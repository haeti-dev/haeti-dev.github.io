import log.haeti.buildlogic.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

/**
 * `feature:*` 모듈의 공통 설정. 모든 feature는 같은 의존성이 필요하다:
 * 디자인 시스템·공용 UI·데이터 계층·내비게이션 키, 멀티플랫폼 ViewModel, Navigation 3 엔트리,
 * 그리고 Metro DI. 여기서 한 번 선언해 feature 스크립트는 `plugins { }`만 남긴다.
 */
class KotlinMultiplatformFeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("haeti.kmp.compose")
            pluginManager.apply("org.jetbrains.kotlin.plugin.serialization")
            pluginManager.apply("dev.zacsweers.metro")

            dependencies {
                add("commonMainApi", project(":core:common"))
                add("commonMainApi", project(":core:model"))
                add("commonMainApi", project(":core:data"))
                add("commonMainApi", project(":core:navigation"))
                add("commonMainApi", project(":core:designsystem"))
                add("commonMainApi", project(":core:ui"))

                add("commonMainApi", libs.findLibrary("androidx-lifecycle-viewmodel").get())
                add("commonMainApi", libs.findLibrary("androidx-lifecycle-viewmodel-compose").get())
                add("commonMainApi", libs.findLibrary("androidx-lifecycle-runtime-compose").get())
                add("commonMainApi", libs.findLibrary("androidx-navigation3-ui").get())
                add("commonMainApi", libs.findLibrary("androidx-lifecycle-viewmodel-navigation3").get())
                add("commonMainApi", libs.findLibrary("metro-runtime").get())
                add("commonMainApi", libs.findLibrary("metro-viewmodel").get())
                add("commonMainApi", libs.findLibrary("metro-viewmodel-compose").get())
                add("commonMainApi", libs.findLibrary("kotlinx-immutable-collections").get())

                add("commonTestImplementation", libs.findLibrary("kotlinx-coroutines-test").get())
                add("commonTestImplementation", libs.findLibrary("turbine").get())
                add("commonTestImplementation", project(":core:testing"))
            }
        }
    }
}
