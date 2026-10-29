package log.haeti.core.data.parser

import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull

class PostParserTest {
    private val valid =
        """
        ---
        title: 제목
        date: 2026-08-18
        category: 안드로이드
        summary: 요약 한 줄
        ---

        본문 첫 줄
        """.trimIndent()

    @Test
    fun `정상 프론트매터를 파싱한다`() {
        val post = PostParser.parse(valid, slug = "hello")
        assertEquals("제목", post.meta.title)
        assertEquals(LocalDate(2026, 8, 18), post.meta.date)
        assertEquals("hello", post.meta.slug)
        assertEquals("본문 첫 줄", post.body)
    }

    @Test
    fun `draft 미지정이면 false다`() {
        val post = PostParser.parse(valid, slug = "s")
        assertFalse(post.meta.draft)
        assertNull(post.meta.thumbnail)
    }

    @Test
    fun `필수 키 누락이면 키 이름을 담아 실패한다`() {
        val raw = valid.replace("category: 안드로이드\n", "")
        val e = assertFailsWith<IllegalArgumentException> { PostParser.parse(raw, "s") }
        assertEquals(true, e.message?.contains("category"))
    }

    @Test
    fun `date 형식이 잘못되면 원본 값을 담아 실패한다`() {
        val raw = valid.replace("date: 2026-08-18", "date: 2026.08.18")
        val e = assertFailsWith<IllegalArgumentException> { PostParser.parse(raw, "s") }
        assertEquals(true, e.message?.contains("2026.08.18"))
    }

    @Test
    fun `존재하지 않는 날짜면 실패한다`() {
        val raw = valid.replace("date: 2026-08-18", "date: 2026-02-30")
        assertFailsWith<IllegalArgumentException> { PostParser.parse(raw, "s") }
    }

    @Test
    fun `펜스가 없으면 실패한다`() {
        assertFailsWith<IllegalArgumentException> { PostParser.parse("본문뿐", "s") }
    }

    @Test
    fun `읽기 분량은 한글 700음절, 영어 300단어를 1분으로 치고 최소 1분이다`() {
        assertEquals(1, PostParser.readingMinutesOf("짧은 글"))
        assertEquals(2, PostParser.readingMinutesOf("가".repeat(701)))
        assertEquals(1, PostParser.readingMinutesOf("가 ".repeat(700)))
        assertEquals(1, PostParser.readingMinutesOf("word ".repeat(300)))
        assertEquals(2, PostParser.readingMinutesOf("가".repeat(350) + " word".repeat(151)))
    }

    @Test
    fun `읽기 분량은 이미지·링크 주소·마크다운 기호를 세지 않는다`() {
        val markup = "![](images/a-long-image-name.webp){width=60 caption=\"x\"}\n"
        val link = "[글](https://example.com/a/very/long/path)"
        val body = (markup + link + "\n**---**\n").repeat(400)
        assertEquals(1, PostParser.readingMinutesOf(body))
    }
}
