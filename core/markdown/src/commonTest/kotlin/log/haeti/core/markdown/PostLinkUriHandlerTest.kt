package log.haeti.core.markdown

import androidx.compose.ui.platform.UriHandler
import kotlin.test.Test
import kotlin.test.assertEquals

class PostLinkUriHandlerTest {
    private val opened = mutableListOf<String>()
    private val posts = mutableListOf<String>()
    private val platform =
        object : UriHandler {
            override fun openUri(uri: String) {
                opened += uri
            }
        }
    private val handler = PostLinkUriHandler(platform) { posts += it }

    @Test
    fun `글 링크는 앱 안에서 연다`() {
        handler.openUri("post/effective-kotlin")
        handler.openUri("/post/compose-snapshot-system#git")
        assertEquals(listOf("effective-kotlin", "compose-snapshot-system"), posts)
        assertEquals(emptyList(), opened)
    }

    @Test
    fun `외부 링크는 플랫폼 처리기로 넘기고 그 밖의 상대 링크는 무시한다`() {
        handler.openUri("https://kotlinlang.org")
        handler.openUri("images/01.webp")
        handler.openUri("post/a/b")
        assertEquals(listOf("https://kotlinlang.org"), opened)
        assertEquals(emptyList(), posts)
    }
}
