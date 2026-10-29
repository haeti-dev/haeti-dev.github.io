import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.metro)
    id("haeti.spotless")
}

// 로컬 전용 집필 도구. 절대 배포하지 않는다 (.claude/rules/architecture.md).
kotlin {
    jvmToolchain(libs.versions.jvmToolchain.get().toInt())

    dependencies {
        implementation(projects.core.model)
        implementation(projects.core.common)
        implementation(projects.core.data)
        implementation(projects.core.markdown)
        implementation(projects.core.designsystem)
        implementation(projects.core.ui)
        implementation(compose.desktop.currentOs)
        implementation(libs.compose.components.resources)
        implementation(libs.kotlinx.coroutines.swing)
        implementation(libs.androidx.lifecycle.viewmodel)
        implementation(libs.metro.runtime)

        testImplementation(libs.kotlin.test)
        testImplementation(libs.kotlinx.coroutines.test)
    }
}

// 어드민 화면 PNG 렌더(디자인 검토용). `./gradlew :app:admin:test -Phaeti.renderScreens=true`일 때만 그린다.
tasks.withType<Test>().configureEach {
    systemProperty("haeti.renderScreens", providers.gradleProperty("haeti.renderScreens").getOrElse("false"))
    systemProperty("haeti.contentRoot", rootProject.layout.projectDirectory.dir("content").asFile.absolutePath)
    systemProperty("haeti.screensDir", layout.buildDirectory.dir("admin-screens").get().asFile.absolutePath)
    inputs.property("renderScreens", providers.gradleProperty("haeti.renderScreens").getOrElse("false"))
}

// 맥 앱 패키징에는 jpackage가 있는 JDK가 필요하다. 데몬이 Android Studio 내장 JBR(jpackage 없음)로 떠 있으면
// Gradle이 받아 둔 JDK 21(~/.gradle/jdks)을 대신 쓴다. 둘 다 없으면 기본값(데몬 JDK)으로 두고 checkRuntime이 알린다.
val packagingJdk: String? =
    File(System.getProperty("java.home")).takeUnless { it.resolve("bin/jpackage").exists() }?.let {
        gradle.gradleUserHomeDir
            .resolve("jdks")
            .walkTopDown()
            .maxDepth(4)
            .firstOrNull { home ->
                home.name == "Home" &&
                    home.resolve("bin/jpackage").exists() &&
                    home.resolve("release").let { it.exists() && it.readText().contains("JAVA_VERSION=\"21") }
            }?.absolutePath
    }

compose.desktop {
    application {
        mainClass = "log.haeti.admin.MainKt"
        packagingJdk?.let { javaHome = it }
        // 맥 앱은 Finder에서 열면 작업 디렉터리가 `/`라 content/를 못 찾는다. 빌드한 저장소 경로를 넣어 둔다.
        jvmArgs += "-Dhaeti.contentRoot=${rootProject.layout.projectDirectory.dir("content").asFile.absolutePath}"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg)
            packageName = "Haeti.log Admin"
            packageVersion = "1.0.0"
            description = "Haeti.log 로컬 집필 도구"
            // suggestRuntimeModules 결과 + 고른 폴더를 기억하는 java.prefs
            modules("java.instrument", "jdk.unsupported", "java.prefs")
            macOS {
                bundleID = "log.haeti.admin"
                dockName = "Haeti.log Admin"
                iconFile.set(project.file("icons/HaetiAdmin.icns"))
            }
        }
    }
}

// 맥 앱을 만들어 ~/Applications에 설치한다. 어드민 코드를 고친 뒤 다시 돌리면 앱이 갱신된다.
// ditto로 복사해 앱 번들의 실행 권한·심볼릭 링크를 그대로 지킨다.
val adminAppName = "Haeti.log Admin.app"
val builtAdminApp = layout.buildDirectory.dir("compose/binaries/main/app/$adminAppName")
val installedAdminApp = "${System.getProperty("user.home")}/Applications/$adminAppName"
tasks.register<Exec>("installAdminApp") {
    group = "distribution"
    description = "어드민 맥 앱을 만들어 ~/Applications에 설치한다"
    dependsOn("createDistributable")
    // doLast가 스크립트 최상위 값을 직접 읽으면 configuration cache에 저장되지 않아 지역 값으로 받는다.
    val target = installedAdminApp
    commandLine(
        "sh",
        "-c",
        "rm -rf \"\$1\" && mkdir -p \"\$(dirname \"\$1\")\" && ditto \"\$0\" \"\$1\"",
        builtAdminApp.get().asFile.absolutePath,
        target,
    )
    doLast { println("설치됨: $target") }
}
