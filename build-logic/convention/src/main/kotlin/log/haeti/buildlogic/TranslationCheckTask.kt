package log.haeti.buildlogic

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.UntrackedTask
import java.io.File
import java.security.MessageDigest

/**
 * 발행 글(`draft: true`가 아닌 글)마다 영어 번역본이 있고 최신인지 점검한다.
 *
 * 번역본(`index.en.md`)의 `source`는 번역 당시 원문 해시다. 지금 원문 해시와 다르면 원문이 바뀐 뒤
 * 번역을 갱신하지 않은 것이다. 해시 규칙은 어드민 `TranslationSource.kt`, 번역 스킬
 * `translation_status.py`와 같다. 제목·요약·본문(앞뒤 공백 제거)을 줄바꿈으로 이은 SHA-256 앞 12자리.
 *
 * 기본은 경고만 남긴다. [strict]가 참이면(`-Phaeti.strictTranslations=true`) 빌드를 실패시킨다.
 */
@UntrackedTask(because = "결과를 로그로만 남기는 점검이라 캐시할 산출물이 없다")
abstract class TranslationCheckTask : DefaultTask() {
    /** `content/posts` 디렉터리. */
    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val contentDirectory: DirectoryProperty

    /** 문제가 있으면 빌드를 실패시킬지. */
    @get:Input
    abstract val strict: Property<Boolean>

    @TaskAction
    fun check() {
        val problems =
            contentDirectory.get().asFile.listFiles().orEmpty()
                .filter { it.isDirectory && it.resolve("index.md").isFile }
                .sortedByDescending { it.name }
                .mapNotNull(::problemOf)
        if (problems.isEmpty()) {
            logger.lifecycle("번역본 점검: 발행 글 모두 최신 번역본이 있습니다.")
            return
        }
        val report = problems.joinToString("\n") { "  - $it" }
        val message = "번역본 점검: ${problems.size}편 확인 필요 (번역 스킬로 갱신하세요)\n$report"
        if (strict.get()) throw GradleException(message) else logger.warn(message)
    }

    /** 문제 한 줄. 초안이거나 번역이 최신이면 null. */
    private fun problemOf(postDir: File): String? {
        val korean = postDir.resolve("index.md").readText()
        if (checkPost(postDir).draft) return null
        val english = postDir.resolve("index.en.md").takeIf { it.isFile } ?: return "${postDir.name}: 번역본 없음"
        val recorded = frontmatter(english.readText())["source"]
        return if (recorded == sourceHash(korean)) null else "${postDir.name}: 원문이 바뀐 뒤 번역이 갱신되지 않음"
    }

    private fun sourceHash(korean: String): String {
        val text = korean.replace("\r\n", "\n")
        val fields = frontmatter(text)
        val payload = listOf(fields["title"].orEmpty(), fields["summary"].orEmpty(), body(text).trim()).joinToString("\n")
        val digest = MessageDigest.getInstance("SHA-256").digest(payload.toByteArray(Charsets.UTF_8))
        return digest.joinToString("") { "%02x".format(it) }.take(12)
    }

    private fun frontmatter(text: String): Map<String, String> {
        val lines = text.replace("\r\n", "\n").lines()
        if (lines.firstOrNull()?.trim() != "---") return emptyMap()
        return lines.drop(1).takeWhile { it.trim() != "---" }
            .filter { it.indexOf(':') > 0 }
            .associate { it.substringBefore(':').trim() to it.substringAfter(':').trim() }
    }

    private fun body(text: String): String {
        val lines = text.lines()
        if (lines.firstOrNull()?.trim() != "---") return text
        val closing = lines.drop(1).indexOfFirst { it.trim() == "---" }
        return if (closing < 0) text else lines.drop(closing + 2).joinToString("\n")
    }
}
