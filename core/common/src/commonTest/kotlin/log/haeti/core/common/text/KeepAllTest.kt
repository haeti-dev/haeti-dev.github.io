package log.haeti.core.common.text

import kotlin.test.Test
import kotlin.test.assertEquals

class KeepAllTest {
    @Test
    fun `한글 단어 안에만 줄바꿈 금지 문자를 넣는다`() {
        assertEquals("나⁠눴⁠다 abc", "나눴다 abc".keepAll())
    }

    @Test
    fun `영문 단어와 공백은 그대로 둔다`() {
        assertEquals("Kotlin Wasm", "Kotlin Wasm".keepAll())
    }

    @Test
    fun `한글과 붙은 영문 사이도 묶는다`() {
        assertEquals("CMP⁠로", "CMP로".keepAll())
    }

    @Test
    fun `마크다운은 코드 펜스를 건너뛰고 강조 기호 옆에는 넣지 않는다`() {
        val source = "**굵게**로\n```\n코드 줄\n```"
        assertEquals("**굵\u2060게**로\n```\n코드 줄\n```", source.keepAllMarkdown())
    }
}
