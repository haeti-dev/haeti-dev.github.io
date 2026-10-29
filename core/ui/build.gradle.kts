plugins {
    id("haeti.kmp.compose")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.core.designsystem)
            api(projects.core.model)
            api(projects.core.common)
            api(projects.core.navigation)
            implementation(libs.kotlinx.coroutines.core)
        }
        wasmJsMain.dependencies {
            // UrlOpener.wasmJs의 window.open
            implementation(libs.kotlinx.browser)
        }
    }
}
