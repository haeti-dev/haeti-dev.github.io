import log.haeti.buildlogic.SiteFilesTask
import org.gradle.language.jvm.tasks.ProcessResources

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.metro)
    id("haeti.spotless")
}

// `haeti.kmp.compose`를 쓰지 않는다: 그 컨벤션은 jvm 타깃까지 선언하는데 이 모듈은 브라우저 번들뿐이다.
kotlin {
    wasmJs {
        browser {
            commonWebpackConfig {
                outputFileName = "haeti.js"
            }
        }
        binaries.executable()
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.common)
            implementation(projects.core.model)
            implementation(projects.core.data)
            implementation(projects.core.navigation)
            implementation(projects.core.designsystem)
            implementation(projects.core.ui)
            implementation(projects.feature.home)
            implementation(projects.feature.post)
            implementation(projects.feature.pages)

            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.viewmodel.compose)
            implementation(libs.androidx.lifecycle.runtime.compose)
            implementation(libs.androidx.navigation3.ui)
            implementation(libs.androidx.lifecycle.viewmodel.navigation3)
            implementation(libs.metro.runtime)
            implementation(libs.metro.viewmodel)
            implementation(libs.metro.viewmodel.compose)
        }
        wasmJsMain.dependencies {
            implementation(libs.kotlinx.browser)
        }
    }
}

compose.resources {
    publicResClass = true
    packageOfResClass = "log.haeti.web.generated.resources"
}

// 배포 경로 접두사. 루트 도메인이면 "/", GitHub Pages 프로젝트 사이트면 "/<repo>/".
// index.html의 <base href>에 주입되고, 앱은 그 값을 읽어 라우팅·리소스 경로를 계산한다.
val basePath = providers.gradleProperty("haeti.basePath").orElse("/")

tasks.named<ProcessResources>("wasmJsProcessResources") {
    // 람다가 스크립트 최상위 val을 직접 읽으면 스크립트 객체까지 붙잡아 configuration cache에 저장되지 않는다.
    val taskBasePath = basePath
    inputs.property("basePath", taskBasePath)
    filesMatching("index.html") {
        filter { line -> line.replace("%BASE_PATH%", taskBasePath.get()) }
    }
}

// 배포 사이트 절대 주소(끝 `/` 없음). OG·사이트맵·RSS가 쓴다. CI는 `https://<owner>.github.io/<repo>`를 넘긴다.
val deploySiteUrl = providers.gradleProperty("haeti.siteUrl").orElse(basePath.map { "http://localhost:8080${it.trimEnd('/')}" })

// 링크 미리보기(OG 셸)·sitemap.xml·feed.xml·robots.txt. 캔버스 앱은 HTML에 본문이 없어 이 파일들이 검색·공유·구독을 맡는다.
val generateSiteFiles = tasks.register<SiteFilesTask>("generateSiteFiles") {
    val fonts = rootProject.layout.projectDirectory.dir("core/designsystem/src/commonMain/composeResources/font")
    dependsOn("wasmJsBrowserDistribution")
    contentDirectory.set(rootProject.layout.projectDirectory.dir("content/posts"))
    template.set(layout.buildDirectory.file("dist/wasmJs/productionExecutable/index.html"))
    boldFont.set(fonts.file("pretendard_bold.ttf"))
    regularFont.set(fonts.file("pretendard_regular.ttf"))
    siteUrl.set(deploySiteUrl)
    siteName.set("Haeti.log")
    siteDescription.set("답보다 과정을 남깁니다. 직접 만들고, 내부를 뜯어보고, 공부하며 헤맨 길을 쓰는 기술 블로그")
    staticPages.set(mapOf("/projects/" to "프로젝트", "/about/" to "소개"))
    outputDirectory.set(layout.buildDirectory.dir("site"))
}

// GitHub Pages용 산출물: 번들 위에 [generateSiteFiles] 결과(index.html·404.html·글별 셸 등)를 겹치고,
// Jekyll이 `_`로 시작하는 파일을 버리지 않게 막는다. 알 수 없는 경로에는 404.html(앱 셸)이 내려가 클라이언트 라우팅된다.
tasks.register<Sync>("prepareGitHubPages") {
    val distribution = layout.buildDirectory.dir("dist/wasmJs/productionExecutable")
    dependsOn("wasmJsBrowserDistribution")
    from(distribution) { exclude("index.html") }
    from(generateSiteFiles)
    into(layout.buildDirectory.dir("pages"))
    doLast {
        destinationDir.resolve(".nojekyll").writeText("")
    }
}
