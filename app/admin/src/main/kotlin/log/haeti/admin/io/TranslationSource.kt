package log.haeti.admin.io

import java.security.MessageDigest

/** 번역본 프론트매터에서 원문 해시를 담는 키. */
internal const val SOURCE_KEY = "source"

/**
 * 원문 해시. 번역 대상인 제목·요약·본문만으로 만든 SHA-256 앞 12자리다.
 * `draft`·날짜처럼 번역과 무관한 필드는 넣지 않아, 발행 표시만 바꿔도 번역이 낡았다고 뜨지 않는다.
 *
 * 같은 규칙이 세 곳에 있다: 여기, `build-logic`의 `TranslationCheckTask`, 번역 스킬의
 * `scripts/translation_status.py`. 셋 중 하나를 바꾸면 나머지도 함께 바꾼다.
 *
 * @param koreanSource `index.md` 원문 전체
 */
internal fun translationSourceHash(koreanSource: String): String {
    val normalized = koreanSource.replace("\r\n", "\n")
    val fields = peekFrontmatter(normalized)
    val body = bodyAfterFrontmatter(normalized).trim()
    val input = listOf(fields["title"].orEmpty(), fields["summary"].orEmpty(), body).joinToString("\n")
    val digest = MessageDigest.getInstance("SHA-256").digest(input.toByteArray(Charsets.UTF_8))
    return digest.joinToString("") { "%02x".format(it) }.take(12)
}

/** 프론트매터 펜스 뒤의 본문. 펜스가 없으면 원문 전체. */
internal fun bodyAfterFrontmatter(text: String): String {
    val lines = text.lines()
    if (lines.firstOrNull()?.trim() != "---") return text
    val closing = lines.drop(1).indexOfFirst { it.trim() == "---" }
    return if (closing < 0) text else lines.drop(closing + 2).joinToString("\n")
}
