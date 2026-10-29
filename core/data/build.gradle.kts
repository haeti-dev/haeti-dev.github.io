import log.haeti.buildlogic.PostsResourcesTask
import log.haeti.buildlogic.TranslationCheckTask

plugins {
    id("haeti.kmp.compose")
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.metro)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.core.model)
            api(projects.core.common)
            // PostRepository가 Flow를 공개 API로 내보내므로 api여야 소비 모듈에서 타입이 보인다.
            api(libs.kotlinx.coroutines.core)
            // 이 모듈에는 컴포저블이 없지만, haeti.kmp.compose가 붙이는 Compose 컴파일러 플러그인이
            // 런타임을 클래스패스에서 찾지 못하면 컴파일 자체가 실패한다(components-resources는 안 끌어온다).
            implementation(libs.compose.runtime)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.metro.runtime)
        }
        commonTest.dependencies {
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.turbine)
        }
    }
}

// content/posts/**가 원본(source of truth)이다. 빌드 시점에 초안을 제외한 글과 index.json을 생성해
// Compose 리소스로 연결한다. 소스 트리에는 아무것도 복사하지 않는다.
val generatePostsResources = tasks.register<PostsResourcesTask>("generatePostsResources") {
    contentDirectory.set(rootProject.layout.projectDirectory.dir("content/posts"))
    outputDirectory.set(layout.buildDirectory.dir("generated/postsResources"))
    // `-Phaeti.samples=true`(개발 서버 launch 설정)일 때만 레이아웃 확인용 샘플 글을 섞는다.
    if (providers.gradleProperty("haeti.samples").orNull == "true") {
        sampleDirectory.set(rootProject.layout.projectDirectory.dir("content/samples"))
    }
}

compose.resources {
    customDirectory(
        sourceSetName = "commonMain",
        directoryProvider = generatePostsResources.map { it.outputDirectory.get() },
    )
}

// 글 발행 절차의 마지막 점검: 발행 글마다 최신 영어 번역본이 있는지. 기본은 경고, -Phaeti.strictTranslations=true면 실패.
tasks.register<TranslationCheckTask>("checkTranslations") {
    group = "verification"
    description = "발행 글의 영어 번역본(index.en.md)이 있고 최신인지 점검한다"
    contentDirectory.set(rootProject.layout.projectDirectory.dir("content/posts"))
    strict.set(providers.gradleProperty("haeti.strictTranslations").map(String::toBoolean).orElse(false))
}
