// 컨벤션 플러그인(build-logic)이 id로 적용하는 플러그인은 여기서 클래스패스에 올려둔다 (apply false).
plugins {
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.compose.multiplatform) apply false
    alias(libs.plugins.metro) apply false
    alias(libs.plugins.spotless) apply false
}
