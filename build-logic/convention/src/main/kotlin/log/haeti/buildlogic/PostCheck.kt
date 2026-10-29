package log.haeti.buildlogic

import org.gradle.api.GradleException
import java.io.File
import java.time.LocalDate

/** 글 디렉터리 이름 규칙: `yyyy-MM-dd-slug`, 슬러그는 소문자·숫자·하이픈. */
private val POST_DIRECTORY = Regex("^(\\d{4}-\\d{2}-\\d{2})-([a-z0-9]+(?:-[a-z0-9]+)*)$")
private val REQUIRED_KEYS = listOf("title", "date", "category", "summary")

/**
 * 빌드가 확인한 글 하나.
 *
 * @param directory 글 디렉터리
 * @param slug 디렉터리명에서 날짜를 뗀 URL 슬러그
 * @param fields 프론트매터 `key: value`
 * @param draft 초안이면 true. 초안은 필수 키를 검사하지 않는다
 */
internal class CheckedPost(
    val directory: File,
    val slug: String,
    val fields: Map<String, String>,
    val draft: Boolean,
) {
    /** 발행일. 발행 글은 검사를 거쳐 늘 있다. */
    val date: LocalDate get() = LocalDate.parse(fields.getValue("date"))
}

/**
 * 글 디렉터리를 앱의 `PostParser`와 같은 규칙으로 검사한다. 리소스 생성·사이트 파일·번역 검사가 모두 이 함수를 쓴다.
 * 애매한 글을 조용히 건너뛰지 않고 빌드를 멈춘다. 건너뛰면 초안이 번들에 실리거나(`draft: True`),
 * 앱에는 보이는데 OG·RSS에서 빠지거나(빈 요약), 이미지와 번역본을 못 찾는(날짜 불일치) 글이 그대로 배포된다.
 *
 * @param directory 글 디렉터리 (`index.md`가 있어야 한다)
 * @throws GradleException 규칙을 어긴 경우. 어느 글의 무엇이 틀렸는지 메시지에 담는다
 */
internal fun checkPost(directory: File): CheckedPost {
    val name = "${directory.parentFile.name}/${directory.name}"
    val match = POST_DIRECTORY.matchEntire(directory.name) ?: fail(name, "디렉터리 이름이 yyyy-MM-dd-slug 형식이 아닙니다")
    val fields = frontmatterOf(directory.resolve("index.md").readText(), name)
    val draft =
        when (val value = fields["draft"]) {
            null, "false" -> false
            "true" -> true
            else -> fail(name, "draft는 true 또는 false여야 합니다: '$value'")
        }
    if (!draft) checkPublished(name, fields, datePrefix = match.groupValues[1])
    return CheckedPost(directory, slug = match.groupValues[2], fields = fields, draft = draft)
}

/** 발행 글은 필수 키가 모두 채워져 있고, `date`가 디렉터리 날짜와 같아야 한다. */
private fun checkPublished(
    name: String,
    fields: Map<String, String>,
    datePrefix: String,
) {
    val missing = REQUIRED_KEYS.filter { fields[it].isNullOrBlank() }
    if (missing.isNotEmpty()) fail(name, "프론트매터에 비어 있는 필수 키가 있습니다: ${missing.joinToString(", ")}")
    val date = fields.getValue("date")
    if (runCatching { LocalDate.parse(date) }.isFailure) fail(name, "date가 yyyy-MM-dd 형식이 아닙니다: '$date'")
    if (date != datePrefix) fail(name, "date($date)가 디렉터리 날짜($datePrefix)와 다릅니다. 이미지와 번역본 경로가 어긋납니다")
}

/** `---` 펜스 사이의 `key: value` 줄을 읽는다. 앱과 같이 펜스는 정확히 `---`, 콜론 없는 줄은 오류다. */
private fun frontmatterOf(
    text: String,
    name: String,
): Map<String, String> {
    val lines = text.lines()
    if (lines.firstOrNull() != "---") fail(name, "프론트매터가 '---'로 시작하지 않습니다")
    val closing = lines.drop(1).indexOf("---")
    if (closing < 0) fail(name, "프론트매터를 닫는 '---'가 없습니다")
    return lines.subList(1, closing + 1).filter { it.isNotBlank() }.associate { line ->
        if (line.indexOf(':') <= 0) fail(name, "프론트매터 줄 형식이 잘못됐습니다: '$line'")
        line.substringBefore(':').trim() to line.substringAfter(':').trim()
    }
}

private fun fail(
    name: String,
    reason: String,
): Nothing = throw GradleException("content/$name: $reason")
