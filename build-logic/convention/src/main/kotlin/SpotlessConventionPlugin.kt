import com.diffplug.gradle.spotless.SpotlessExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

/**
 * Spotless + ktlint 포맷 검사. `./gradlew spotlessCheck`가 CI 첫 관문이고 `spotlessApply`로 자동 교정한다.
 * Composable 함수명(대문자 시작)은 ktlint 네이밍 규칙에서 제외한다.
 */
class SpotlessConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("com.diffplug.spotless")

            extensions.configure<SpotlessExtension> {
                val buildDirectory = layout.buildDirectory.asFileTree
                kotlin {
                    target("**/*.kt")
                    targetExclude(buildDirectory)
                    ktlint().editorConfigOverride(
                        mapOf(
                            "ktlint_standard_function-naming" to "disabled",
                            "ktlint_standard_property-naming" to "disabled",
                            "ktlint_function_naming_ignore_when_annotated_with" to "Composable",
                            "max_line_length" to "120",
                        ),
                    )
                    trimTrailingWhitespace()
                    endWithNewline()
                }
                format("kts") {
                    target("**/*.kts")
                    targetExclude(buildDirectory)
                    trimTrailingWhitespace()
                    endWithNewline()
                }
            }
        }
    }
}
