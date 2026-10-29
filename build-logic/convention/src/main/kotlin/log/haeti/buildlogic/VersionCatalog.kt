package log.haeti.buildlogic

import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.getByType

/** 컨벤션 플러그인 안에서 접근하는 `libs` 버전 카탈로그. */
internal val Project.libs: VersionCatalog
    get() = extensions.getByType<VersionCatalogsExtension>().named("libs")

/** 카탈로그의 `[versions]` 항목을 문자열로 읽는다. 없으면 즉시 실패한다. */
internal fun VersionCatalog.version(alias: String): String =
    findVersion(alias)
        .orElseThrow { NoSuchElementException("libs.versions.toml에 '$alias' 버전이 없습니다") }
        .requiredVersion
