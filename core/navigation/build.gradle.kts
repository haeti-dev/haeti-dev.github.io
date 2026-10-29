plugins {
    id("haeti.kmp.compose")
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(libs.compose.runtime)
            api(libs.androidx.navigation3.runtime)
            api(libs.androidx.navigation3.ui)
            api(libs.androidx.lifecycle.viewmodel.navigation3)
            api(libs.kotlinx.serialization.json)
        }
    }
}
