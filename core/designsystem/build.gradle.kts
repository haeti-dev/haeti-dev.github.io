plugins {
    id("haeti.kmp.compose")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(libs.compose.runtime)
            api(libs.compose.foundation)
            api(libs.compose.material3)
            api(libs.compose.animation)
            api(libs.compose.ui)
            api(libs.compose.material.icons.extended)
        }
    }
}
