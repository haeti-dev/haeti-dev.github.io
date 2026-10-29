package log.haeti.admin

import log.haeti.admin.io.translationSourceHash
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

/**
 * 해시 규칙은 번역 스킬의 파이썬 스크립트와 같아야 한다. 아래 기댓값은
 * `python3 .claude/skills/haeti-translate-post/scripts/translation_status.py hash-text`로 같은 입력을 넣어 얻은 값이다.
 */
class TranslationSourceTest {
    private val source = "---\ntitle: 제목\ndate: 2026-10-07\ncategory: 회고\nsummary: 요약\ndraft: true\n---\n\n본문 한 줄\n"

    @Test
    fun `지문은 제목 요약 본문만 본다`() {
        assertEquals(
            translationSourceHash(source),
            translationSourceHash(source.replace("draft: true", "draft: false")),
        )
        assertNotEquals(translationSourceHash(source), translationSourceHash(source.replace("본문 한 줄", "본문 두 줄")))
    }

    @Test
    fun `파이썬 스크립트와 같은 값을 낸다`() {
        assertEquals(EXPECTED, translationSourceHash(source))
    }

    private companion object {
        const val EXPECTED = "5b3da00b2f16"
    }
}
