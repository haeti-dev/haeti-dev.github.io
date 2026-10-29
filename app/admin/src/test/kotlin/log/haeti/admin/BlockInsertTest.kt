package log.haeti.admin

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import log.haeti.admin.editor.insertBlock
import kotlin.test.Test
import kotlin.test.assertEquals

class BlockInsertTest {
    @Test
    fun 문단_사이_빈_줄에_넣으면_앞뒤로_빈_줄을_맞춘다() {
        val text = "## 제목\n\n본문"
        val result = TextFieldValue(text, TextRange(text.indexOf("\n\n") + 1)).insertBlock("![](images/a.webp)")

        assertEquals("## 제목\n\n![](images/a.webp)\n\n본문", result.text)
        assertEquals(TextRange("## 제목\n\n![](images/a.webp)".length), result.selection)
    }

    @Test
    fun 문장_가운데에_넣으면_양쪽에_빈_줄을_만든다() {
        val result = TextFieldValue("앞뒤", TextRange(1)).insertBlock("X")

        assertEquals("앞\n\nX\n\n뒤", result.text)
    }

    @Test
    fun 빈_문서와_끝에서는_줄바꿈_하나로_마친다() {
        assertEquals("X\n", TextFieldValue("").insertBlock("X").text)
        assertEquals("본문\n\nX\n", TextFieldValue("본문\n", TextRange(3)).insertBlock("X").text)
    }

    @Test
    fun 선택_영역은_블록으로_바뀐다() {
        val result = TextFieldValue("a\n\nOLD\n\nb", TextRange(3, 6)).insertBlock("X")

        assertEquals("a\n\nX\n\nb", result.text)
    }
}
