plugins {
    `kotlin-dsl`
}

group = "log.haeti.buildlogic"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

dependencies {
    compileOnly(libs.kotlin.gradlePlugin)
    compileOnly(libs.compose.gradlePlugin)
    compileOnly(libs.compose.compiler.gradlePlugin)
    compileOnly(libs.spotless.gradlePlugin)
    implementation(libs.twelvemonkeys.imageio.webp)

    testImplementation(libs.kotlin.test)
}

gradlePlugin {
    plugins {
        register("kotlinMultiplatformLibrary") {
            id = "haeti.kmp.library"
            implementationClass = "KotlinMultiplatformConventionPlugin"
        }
        register("kotlinMultiplatformCompose") {
            id = "haeti.kmp.compose"
            implementationClass = "KotlinMultiplatformComposeConventionPlugin"
        }
        register("kotlinMultiplatformFeature") {
            id = "haeti.kmp.feature"
            implementationClass = "KotlinMultiplatformFeatureConventionPlugin"
        }
        register("spotless") {
            id = "haeti.spotless"
            implementationClass = "SpotlessConventionPlugin"
        }
    }
}
