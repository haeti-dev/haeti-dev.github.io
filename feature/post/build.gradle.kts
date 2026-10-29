plugins {
    id("haeti.kmp.feature")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.markdown)
        }
    }
}
