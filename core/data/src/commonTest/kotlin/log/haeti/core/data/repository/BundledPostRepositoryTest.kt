package log.haeti.core.data.repository

import app.cash.turbine.test
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import log.haeti.core.data.source.FakePostSource
import log.haeti.core.model.Language
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull

/** 프론트매터가 온전한 글 원문을 만든다. */
private fun markdown(
    title: String,
    date: String,
    draft: Boolean = false,
): String =
    """
    ---
    title: $title
    date: $date
    category: 안드로이드
    summary: 요약 한 줄
    draft: $draft
    ---

    본문
    """.trimIndent()

class BundledPostRepositoryTest {
    private val assetBytes = byteArrayOf(1, 2, 3)

    /** 정렬 검증을 위해 인덱스는 일부러 뒤섞어 둔다. 초안·깨진 글·원문 없는 글도 함께 넣는다. */
    private val source =
        FakePostSource(
            index =
                listOf(
                    "2026-01-02-old",
                    "2026-05-05-hidden",
                    "2026-08-18-hello",
                    "2026-03-03-broken",
                    "2026-06-06-mid",
                    "2026-07-07-gone",
                ),
            markdown =
                mapOf(
                    "2026-01-02-old" to markdown("옛 글", "2026-01-02"),
                    "2026-05-05-hidden" to markdown("초안", "2026-05-05", draft = true),
                    "2026-08-18-hello" to markdown("첫 글", "2026-08-18"),
                    "2026-03-03-broken" to "펜스가 없는 원문",
                    "2026-06-06-mid" to markdown("가운데 글", "2026-06-06"),
                ),
            assets = mapOf("2026-08-18-hello/images/sample.webp" to assetBytes),
        )

    private val repository = BundledPostRepository(source)

    @Test
    fun `발행 글을 최신순으로 방출한다`() =
        runTest {
            repository.observePosts().test {
                val slugs = awaitItem().map { it.meta.slug }
                assertEquals(listOf("hello", "mid", "old"), slugs)
                awaitComplete()
            }
        }

    @Test
    fun `초안과 파싱 실패 글과 원문 없는 글은 건너뛴다`() =
        runTest {
            val slugs = repository.observePosts().first().map { it.meta.slug }
            assertEquals(false, slugs.contains("hidden"))
            assertEquals(false, slugs.contains("broken"))
            assertEquals(false, slugs.contains("gone"))
        }

    @Test
    fun `슬러그로 글 하나를 찾는다`() =
        runTest {
            val post = repository.observePost("mid").first()
            assertNotNull(post)
            assertEquals("가운데 글", post.meta.title)
        }

    @Test
    fun `없는 슬러그는 null을 방출한다`() =
        runTest {
            assertNull(repository.observePost("없는슬러그").first())
        }

    @Test
    fun `에셋은 글 디렉터리 기준 상대경로로 읽는다`() =
        runTest {
            val post = assertNotNull(repository.observePost("hello").first())
            assertContentEquals(assetBytes, repository.readPostAsset(post.meta, "images/sample.webp"))
            assertNull(repository.readPostAsset(post.meta, "images/없음.webp"))
        }

    @Test
    fun `여러 번 구독해도 번들은 한 번만 읽는다`() =
        runTest {
            val complete =
                FakePostSource(
                    index = listOf("2026-08-18-hello"),
                    markdown = mapOf("2026-08-18-hello" to markdown("첫 글", "2026-08-18")),
                )
            val repository = BundledPostRepository(complete)
            repository.observePosts().first()
            repository.observePost("hello").first()
            repository.observePosts().first()
            assertEquals(1, complete.indexReadCount)
        }

    @Test
    fun `원문을 못 읽은 글이 있으면 캐시하지 않고 다음 구독에서 다시 읽는다`() =
        runTest {
            repository.observePosts().first()
            repository.observePosts().first()
            assertEquals(2, source.indexReadCount)
        }

    @Test
    fun `인덱스를 못 읽으면 빈 목록 대신 예외를 올리고 캐시하지 않는다`() =
        runTest {
            val failing = FakePostSource(index = emptyList(), markdown = emptyMap(), indexError = Exception("fetch"))
            val repository = BundledPostRepository(failing)
            assertFailsWith<Exception> { repository.observePosts().first() }
            assertFailsWith<Exception> { repository.observePosts().first() }
            assertEquals(2, failing.indexReadCount)
        }

    @Test
    fun `영어로 보면 번역본이 있는 글만 바뀌고 나머지는 원문이다`() =
        runTest {
            val translated =
                FakePostSource(
                    index = listOf("2026-01-02-old", "2026-03-03-mid"),
                    markdown =
                        mapOf(
                            "2026-01-02-old" to markdown("옛 글", "2026-01-02"),
                            "2026-03-03-mid" to markdown("중간 글", "2026-03-03"),
                        ),
                    translations = mapOf("2026-03-03-mid" to "---\ntitle: Middle post\n---\n\nBody"),
                )
            val posts = BundledPostRepository(translated).observePosts(Language.EN).first()

            assertEquals(listOf("Middle post", "옛 글"), posts.map { it.meta.title })
            assertEquals(listOf(Language.EN, Language.KO), posts.map { it.language })
            assertEquals("요약 한 줄", posts.first().meta.summary)
        }
}
