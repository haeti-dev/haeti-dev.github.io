plugins {
    id("haeti.kmp.library")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.core.data)
            api(projects.core.model)
            api(libs.kotlinx.coroutines.test)
            implementation(libs.kotlinx.coroutines.core)
        }
    }
}
