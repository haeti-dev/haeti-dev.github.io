package log.haeti.buildlogic

import org.gradle.api.GradleException
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/** 리소스·사이트 파일·번역 검사가 함께 쓰는 글 검사 규칙을 확인한다. */
class PostCheckTest {
    private val root = Files.createTempDirectory("post-check").toFile().resolve("posts")

    @Test
    fun 발행_글은_슬러그와_필드를_읽는다() {
        val post = checkPost(post("2026-08-18-hello", valid("2026-08-18")))

        assertEquals("hello", post.slug)
        assertEquals(false, post.draft)
        assertEquals("제목", post.fields["title"])
    }

    @Test
    fun 초안은_필수_키가_비어도_통과한다() {
        assertTrue(checkPost(post("2026-08-18-wip", "title: \ndraft: true")).draft)
    }

    @Test
    fun draft가_true나_false가_아니면_멈춘다() {
        assertFails("2026-08-18-a", valid("2026-08-18") + "\ndraft: True", "draft는 true 또는 false")
        assertFails("2026-08-18-b", valid("2026-08-18") + "\ndraft: true   # 검토 중", "draft는 true 또는 false")
    }

    @Test
    fun 발행_글의_필수_키가_비어_있으면_멈춘다() {
        assertFails("2026-08-18-c", "title: 제목\ndate: 2026-08-18\ncategory: 웹\nsummary: ", "summary")
    }

    @Test
    fun date가_디렉터리_날짜와_다르면_멈춘다() {
        assertFails("2026-08-18-d", valid("2026-08-20"), "디렉터리 날짜")
    }

    @Test
    fun 디렉터리_이름과_펜스_형식이_틀리면_멈춘다() {
        assertFails("2026-9-6-e", valid("2026-09-06"), "yyyy-MM-dd-slug")
        val noFence = root.resolve("2026-08-18-f").apply { mkdirs() }.resolve("index.md")
        noFence.writeText("--- \n${valid("2026-08-18")}\n---\n")
        assertTrue("'---'로 시작" in assertFailsWith<GradleException> { checkPost(noFence.parentFile) }.message!!)
    }

    private fun valid(date: String) = "title: 제목\ndate: $date\ncategory: 웹\nsummary: 요약"

    private fun assertFails(
        name: String,
        frontmatter: String,
        expected: String,
    ) {
        val error = assertFailsWith<GradleException> { checkPost(post(name, frontmatter)) }
        assertTrue(expected in error.message!!, "메시지에 '$expected'가 없다: ${error.message}")
    }

    private fun post(
        name: String,
        frontmatter: String,
    ) = root.resolve(name).apply { mkdirs() }.also { it.resolve("index.md").writeText("---\n$frontmatter\n---\n\n본문") }
}
