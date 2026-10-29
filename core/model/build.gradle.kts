plugins {
    id("haeti.kmp.library")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(libs.kotlinx.datetime)
            // Compose 런타임의 어노테이션 조각만 쓴다. 순수 데이터 모듈을 유지하면서 @Immutable을 쓴다.
            api(libs.compose.runtime.annotation)
        }
    }
}
