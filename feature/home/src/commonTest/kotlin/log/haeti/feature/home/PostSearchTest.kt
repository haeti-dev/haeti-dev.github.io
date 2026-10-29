package log.haeti.feature.home

import kotlinx.datetime.LocalDate
import log.haeti.core.model.Post
import log.haeti.core.model.PostMeta
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PostSearchTest {
    private val compose = post("compose", "Snapshot System을 분석해보자", "Compose 상태", "본문에 **MVCC** 이야기가 나온다.")
    private val kotlin =
        post("kotlin", "inline 함수", "crossinline 키워드", "![](images/a.webp){width=100}\n\n람다를 `inline`으로 펼친다.")

    @Test
    fun 제목과_요약에서_찾으면_발췌_없이_돌려준다() {
        val hits = searchPosts(SearchIndex(listOf(compose, kotlin)), "snapshot")

        assertEquals(listOf("compose"), hits.map { it.post.meta.slug })
        assertNull(hits.single().excerpt)
    }

    @Test
    fun 본문에서만_찾으면_마크다운_기호를_걷은_발췌를_붙인다() {
        val hit = searchPosts(SearchIndex(listOf(compose, kotlin)), "mvcc").single()

        assertEquals("compose", hit.post.meta.slug)
        assertEquals("본문에 MVCC 이야기가 나온다.", hit.excerpt)
    }

    @Test
    fun 낱말이_모두_있어야_하고_대소문자는_가리지_않는다() {
        assertEquals(1, searchPosts(SearchIndex(listOf(compose, kotlin)), "INLINE 람다").size)
        assertTrue(searchPosts(SearchIndex(listOf(compose, kotlin)), "inline mvcc").isEmpty())
        assertTrue(searchPosts(SearchIndex(listOf(compose, kotlin)), "   ").isEmpty())
    }

    @Test
    fun 이미지_경로는_검색되지_않는다() {
        assertTrue(searchPosts(SearchIndex(listOf(kotlin)), "webp").isEmpty())
    }

    private fun post(
        slug: String,
        title: String,
        summary: String,
        body: String,
    ) = Post(
        meta = PostMeta(title, LocalDate(2026, 1, 1), "컴포즈", summary, slug),
        body = body,
    )
}
