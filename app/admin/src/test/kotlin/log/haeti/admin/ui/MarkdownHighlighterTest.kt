package log.haeti.admin.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import kotlin.test.Test
import kotlin.test.assertEquals

class MarkdownHighlighterTest {
    private val highlighter =
        MarkdownHighlighter(HighlightColors(Color.Gray, Color.Blue, Color.Black, FontFamily.Monospace))

    @Test
    fun `한글 단어 안에만 화면용 줄바꿈 금지 문자를 넣고 커서 위치는 원문으로 되돌린다`() {
        val result = highlighter.filter(AnnotatedString("블로그 ab"))

        assertEquals("블⁠로⁠그 ab", result.text.text)
        for (offset in 0..6) {
            assertEquals(
                offset,
                result.offsetMapping.transformedToOriginal(result.offsetMapping.originalToTransformed(offset)),
            )
        }
        assertEquals(1, result.offsetMapping.transformedToOriginal(2))
    }
}
