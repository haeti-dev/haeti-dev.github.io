import log.haeti.buildlogic.configureKotlinMultiplatform
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/**
 * 순수 KMP 라이브러리 모듈(`core:*`)의 공통 설정. 타깃(jvm·wasmJs)·툴체인·컴파일러 플래그·
 * 테스트 의존성을 한 곳에서 선언해 모듈 스크립트를 `plugins { }` 블록 수준으로 줄인다.
 */
class KotlinMultiplatformConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("org.jetbrains.kotlin.multiplatform")
            pluginManager.apply("haeti.spotless")

            extensions.configure<KotlinMultiplatformExtension> { configureKotlinMultiplatform(this) }
        }
    }
}
