plugins {
    id("haeti.kmp.compose")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.core.model)
            api(projects.core.common)
            implementation(projects.core.designsystem)
            api(libs.compose.runtime)
            api(libs.compose.foundation)
            api(libs.compose.material3)
            api(libs.compose.ui)
            implementation(libs.compose.material.icons.extended)
            api(libs.markdown.renderer.m3)
            api(libs.markdown.renderer.code)
        }
        // AnimatedFramesTest가 Skia 코덱을 직접 돌린다. 네이티브 런타임은 데스크톱 의존성에 들어 있다.
        jvmTest.dependencies {
            implementation(compose.desktop.currentOs)
        }
        wasmJsMain.dependencies {
            // 코드 카드 복사 버튼이 브라우저 Clipboard API(navigator.clipboard)에 접근한다.
            implementation(libs.kotlinx.browser)
        }
    }
}
