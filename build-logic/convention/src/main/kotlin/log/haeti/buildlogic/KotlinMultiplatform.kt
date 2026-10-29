package log.haeti.buildlogic

import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/**
 * 두 타깃(jvm·wasmJs)과 공통 컴파일 설정을 선언한다.
 *
 * jvm은 admin 데스크톱 앱과 단위 테스트(`jvmTest`)가, wasmJs는 블로그가 쓴다.
 * 브라우저 테스트 태스크는 Chrome 없이는 돌지 않아 비활성화한다. 테스트 소스는 그래도
 * wasmJs로 컴파일되므로 타깃 호환성은 계속 검증된다.
 */
internal fun Project.configureKotlinMultiplatform(extension: KotlinMultiplatformExtension) {
    disableComposeUiTestExecutableCheck()
    extension.apply {
        jvmToolchain(libs.version("jvmToolchain").toInt())

        jvm()

        wasmJs {
            browser {
                testTask { enabled = false }
            }
        }

        applyDefaultHierarchyTemplate()

        compilerOptions {
            freeCompilerArgs.addAll("-Xexpect-actual-classes")
        }

        sourceSets.commonTest.dependencies {
            implementation(libs.findLibrary("kotlin-test").get())
        }
    }
}

/**
 * Compose 플러그인이 wasmJs 브라우저 테스트 앞에 거는 `checkComposeUiTestConfigurationFor<Target>`을 끈다.
 *
 * 이 검사는 Skiko에 의존하는 라이브러리 모듈에 실행 바이너리가 없으면 실패한다 (CMP-4906). Skiko 런타임을
 * 브라우저 UI 테스트용 webpack 번들에 싣기 위한 것이다. 여기서는 브라우저 테스트 태스크를 끄고
 * `runComposeUiTest`도 쓰지 않으니 지킬 대상이 없다. 그런데 꺼진 태스크도 의존 태스크는 실행하므로 `build`만
 * 실패한다. 라이브러리마다 `binaries.executable()`을 붙이면 모듈마다 webpack 산출물이 생겨 이 방식을 택했다.
 * 브라우저 UI 테스트를 켜는 날에는 이 함수를 지우고 해당 모듈에 `binaries.executable()`을 추가한다.
 */
private fun Project.disableComposeUiTestExecutableCheck() {
    tasks.matching { it.name.startsWith("checkComposeUiTestConfigurationFor") }
        .configureEach { enabled = false }
}
