plugins {
    id("haeti.kmp.feature")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.markdown)
        }
        // PostImageResolverTest가 컴포저블을 화면 없이 그린다. Skia 네이티브 런타임은 데스크톱 의존성에 들어 있다.
        jvmTest.dependencies {
            implementation(compose.desktop.currentOs)
        }
    }
}
