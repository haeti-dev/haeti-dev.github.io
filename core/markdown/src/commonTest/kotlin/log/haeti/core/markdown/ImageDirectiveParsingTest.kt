package log.haeti.core.markdown

import log.haeti.core.markdown.model.ImageAlign
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ImageDirectiveParsingTest {
    @Test
    fun `속성 문자열을 파싱한다`() {
        val d = parseImageDirective("""width=60 align=right caption="설명 글"""")
        assertEquals(60, d.width)
        assertEquals(ImageAlign.RIGHT, d.align)
        assertEquals("설명 글", d.caption)
    }

    @Test
    fun `dark 속성은 다크 모드에서만 경로를 바꾼다`() {
        val d = parseImageDirective("width=80 dark=images/flow-dark.png")
        assertEquals("images/flow-dark.png", d.pathFor("images/flow.png", isDarkPage = true))
        assertEquals("images/flow.png", d.pathFor("images/flow.png", isDarkPage = false))
        assertEquals("images/a.png", parseImageDirective("width=80").pathFor("images/a.png", isDarkPage = true))
    }

    @Test
    fun `범위 밖 width는 보정한다`() {
        assertEquals(10, parseImageDirective("width=3").width)
        assertEquals(100, parseImageDirective("width=250").width)
    }

    @Test
    fun `지시어 블록을 본문에서 제거하고 맵으로 추출한다`() {
        val md = "앞\n![alt](images/a.webp){width=50 align=left}\n뒤"
        val (cleaned, map) = extractImageDirectives(md)
        assertEquals("앞\n![alt](images/a.webp)\n뒤", cleaned)
        assertEquals(50, map.getValue("images/a.webp").width)
        assertEquals(ImageAlign.LEFT, map.getValue("images/a.webp").align)
    }

    @Test
    fun `지시어 없는 이미지는 그대로 두고 맵은 비운다`() {
        val md = "![alt](images/b.webp)"
        val (cleaned, map) = extractImageDirectives(md)
        assertEquals(md, cleaned)
        assertNull(map["images/b.webp"])
    }
}
