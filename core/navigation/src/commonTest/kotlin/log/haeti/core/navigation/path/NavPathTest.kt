package log.haeti.core.navigation.path

import androidx.navigation3.runtime.NavKey
import log.haeti.core.navigation.AboutNavKey
import log.haeti.core.navigation.HomeNavKey
import log.haeti.core.navigation.NotFoundNavKey
import log.haeti.core.navigation.PostNavKey
import log.haeti.core.navigation.ProjectsNavKey
import kotlin.test.Test
import kotlin.test.assertEquals

/** 경로 ↔ 화면 키 변환과 문서 제목 규칙을 확인한다. */
class NavPathTest {
    private val allKeys: List<NavKey> =
        listOf(
            HomeNavKey,
            PostNavKey("hello-haeti-log"),
            ProjectsNavKey,
            AboutNavKey,
            NotFoundNavKey,
        )

    @Test
    fun toPathProducesExpectedPaths() {
        assertEquals("/", HomeNavKey.toPath())
        assertEquals("/post/hello-haeti-log/", PostNavKey("hello-haeti-log").toPath())
        assertEquals("/projects/", ProjectsNavKey.toPath())
        assertEquals("/about/", AboutNavKey.toPath())
        assertEquals("/404", NotFoundNavKey.toPath())
    }

    @Test
    fun pathRoundTripsBackToSameKey() {
        allKeys.forEach { key ->
            assertEquals(key, navKeyFromPath(key.toPath()), "왕복 변환 실패: $key")
        }
    }

    @Test
    fun slashVariantsResolveToSameKey() {
        val expected = PostNavKey("hello-haeti-log")

        assertEquals(expected, navKeyFromPath("post/hello-haeti-log"))
        assertEquals(expected, navKeyFromPath("/post/hello-haeti-log/"))
        assertEquals(expected, navKeyFromPath("//post//hello-haeti-log//"))
    }

    @Test
    fun emptyPathResolvesToHome() {
        assertEquals(HomeNavKey, navKeyFromPath(""))
        assertEquals(HomeNavKey, navKeyFromPath("/"))
        assertEquals(HomeNavKey, navKeyFromPath("///"))
    }

    @Test
    fun unknownPathResolvesToNotFound() {
        assertEquals(NotFoundNavKey, navKeyFromPath("/nope"))
        assertEquals(NotFoundNavKey, navKeyFromPath("/post"))
        assertEquals(NotFoundNavKey, navKeyFromPath("/post/a/b"))
        assertEquals(NotFoundNavKey, navKeyFromPath("/projects/extra"))
    }

    @Test
    fun backStackForKeepsHomeBelowOtherKeys() {
        assertEquals(listOf(HomeNavKey), backStackFor(HomeNavKey))
        assertEquals(listOf(HomeNavKey, AboutNavKey), backStackFor(AboutNavKey))

        val post = PostNavKey("hello-haeti-log")
        assertEquals(listOf(HomeNavKey, post), backStackFor(post))
    }

    @Test
    fun documentTitleFollowsSiteConvention() {
        assertEquals("Haeti.log", HomeNavKey.documentTitle())
        assertEquals("프로젝트 | Haeti.log", ProjectsNavKey.documentTitle())
        assertEquals("소개 | Haeti.log", AboutNavKey.documentTitle())
        assertEquals("페이지를 찾을 수 없음 | Haeti.log", NotFoundNavKey.documentTitle())
    }

    @Test
    fun documentTitleUsesPostTitleWhenAvailable() {
        val post = PostNavKey("hello-haeti-log")

        assertEquals("첫 글 | Haeti.log", post.documentTitle("첫 글"))
        assertEquals("Haeti.log", post.documentTitle())
    }

    @Test
    fun documentTitleIgnoresPostTitleOnOtherKeys() {
        assertEquals("Haeti.log", HomeNavKey.documentTitle("무시됨"))
        assertEquals("소개 | Haeti.log", AboutNavKey.documentTitle("무시됨"))
    }
}
