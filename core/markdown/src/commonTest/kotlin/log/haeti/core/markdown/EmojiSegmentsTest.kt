package log.haeti.core.markdown

import log.haeti.core.markdown.model.TextSegment
import kotlin.test.Test
import kotlin.test.assertEquals

class EmojiSegmentsTest {
    private fun emojiIn(text: String): List<String> = splitEmoji(text).filter { it.isEmoji }.map { it.text }

    @Test
    fun `이모지가 없으면 글 전체가 한 조각이다`() {
        assertEquals(listOf(TextSegment("Navigation 3 문제점", isEmoji = false)), splitEmoji("Navigation 3 문제점"))
    }

    @Test
    fun `빈 글은 조각이 없다`() {
        assertEquals(emptyList(), splitEmoji(""))
    }

    @Test
    fun `글과 이모지를 순서대로 나눈다`() {
        assertEquals(
            listOf(
                TextSegment("앞 ", isEmoji = false),
                TextSegment("🎯", isEmoji = true),
                TextSegment(" 뒤", isEmoji = false),
            ),
            splitEmoji("앞 🎯 뒤"),
        )
    }

    @Test
    fun `변이 선택자가 붙은 기호는 이모지다`() {
        assertEquals(listOf("⚠️"), emojiIn("⚠️기존 방식"))
    }

    @Test
    fun `기본 표시가 이모지인 기호는 선택자 없이도 이모지다`() {
        assertEquals(listOf("⭐", "❗"), emojiIn("⭐ 중요 ❗"))
    }

    @Test
    fun `글자로 쓰는 기호는 이모지가 아니다`() {
        assertEquals(emptyList(), emojiIn("★ 별점 → 화살표 ⇒ 결과"))
    }

    @Test
    fun `키캡은 한 글자로 묶고 그냥 숫자는 두지 않는다`() {
        assertEquals(listOf("1️⃣", "2⃣"), emojiIn("1️⃣ 첫째 2⃣ 둘째 3 셋째"))
    }

    @Test
    fun `피부색과 ZWJ 조합은 한 글자로 묶는다`() {
        val thumbsUp = "👍🏽"
        val family = "👨‍👩‍👧"
        assertEquals(listOf(thumbsUp, family), emojiIn("$thumbsUp 그리고 $family"))
    }

    @Test
    fun `글자 모양 선택자가 붙으면 이모지로 바꾸지 않고 글자로 둔다`() {
        assertEquals(emptyList(), emojiIn("🏳︎ 깃발 ⚠︎ 주의"))
        assertEquals("🏳︎ 깃발", splitEmoji("🏳︎ 깃발").joinToString("") { it.text })
    }

    @Test
    fun `태그로 이어지는 지역 깃발은 한 글자로 묶는다`() {
        val england = "🏴󠁧󠁢󠁥󠁮󠁧󠁿"
        assertEquals(listOf(england), emojiIn("$england 잉글랜드"))
    }

    @Test
    fun `국기는 지역 문자 두 개를 묶고 하나만 있으면 두지 않는다`() {
        val korea = "🇰🇷"
        assertEquals(listOf(korea), emojiIn("$korea 🇰"))
    }
}
