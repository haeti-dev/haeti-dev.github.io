@file:Suppress("UnstableApiUsage")

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    includeBuild("build-logic")
    repositories {
        google {
            content {
                includeGroupByRegex("androidx\\..*")
                includeGroupByRegex("com\\.android(\\..*|)")
                includeGroupByRegex("com\\.google\\.android\\..*")
            }
            mavenContent { releasesOnly() }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    // JDK 21 toolchain을 로컬에 없으면 자동으로 받아온다 (CI/새 기기 공통).
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

dependencyResolutionManagement {
    // FAIL_ON_PROJECT_REPOS 대신 PREFER_SETTINGS: Kotlin/Wasm 플러그인이 루트 프로젝트에 Node 배포 저장소를
    // 스스로 등록하는데 이를 막을 방법이 없다. settings 쪽 저장소를 우선하면 그 등록은 무시되고
    // 아래 ivy 저장소가 실제로 Node·Yarn·Binaryen을 서빙한다.
    repositoriesMode = RepositoriesMode.PREFER_SETTINGS
    repositories {
        google {
            content {
                includeGroupByRegex("androidx\\..*")
                includeGroupByRegex("com\\.android(\\..*|)")
                includeGroupByRegex("com\\.google\\.android\\..*")
            }
            mavenContent { releasesOnly() }
        }
        mavenCentral()

        // Kotlin/Wasm 브라우저 툴체인은 Node·Yarn·Binaryen을 직접 내려받는다. 저장소를 settings에서
        // 소유하므로 여기서 선언하고, 각 모듈 하나에만 스코프를 한정한다.
        ivy("https://nodejs.org/dist") {
            name = "Node Distributions"
            patternLayout { artifact("v[revision]/[artifact](-v[revision]-[classifier]).[ext]") }
            metadataSources { artifact() }
            content { includeModule("org.nodejs", "node") }
        }
        ivy("https://github.com/yarnpkg/yarn/releases/download") {
            name = "Yarn Distributions"
            patternLayout { artifact("v[revision]/[artifact](-v[revision]).[ext]") }
            metadataSources { artifact() }
            content { includeModule("com.yarnpkg", "yarn") }
        }
        ivy("https://github.com/WebAssembly/binaryen/releases/download") {
            name = "Binaryen Distributions"
            patternLayout { artifact("version_[revision]/[artifact]-version_[revision]-[classifier].[ext]") }
            metadataSources { artifact() }
            content { includeModule("com.github.webassembly", "binaryen") }
        }
    }
}

rootProject.name = "haeti-log"

include(":core:common")
include(":core:model")
include(":core:markdown")
include(":core:data")
include(":core:navigation")
include(":core:designsystem")
include(":core:ui")
include(":core:testing")

include(":feature:home")
include(":feature:post")
include(":feature:pages")

include(":app:web")
include(":app:admin")

check(JavaVersion.current().isCompatibleWith(JavaVersion.VERSION_17)) {
    """
    Haeti.log는 JDK 17+로 Gradle을 구동해야 합니다. 현재: JDK ${JavaVersion.current()}
    Java Home: [${System.getProperty("java.home")}]
    """.trimIndent()
}
