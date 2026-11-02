package log.haeti.core.markdown

import log.haeti.core.markdown.model.TextSegment

private const val VARIATION_SELECTOR = 0xFE0F
private const val TEXT_SELECTOR = 0xFE0E
private const val KEYCAP = 0x20E3
private const val ZERO_WIDTH_JOINER = 0x200D
private val KeycapBases = (0x30..0x39).toSet() + setOf(0x23, 0x2A)
private val RegionalIndicators = 0x1F1E6..0x1F1FF
private val SkinTones = 0x1F3FB..0x1F3FF

/** 잉글랜드처럼 깃발 뒤에 붙어 지역을 정하는 태그 글자. U+E007F로 끝난다. */
private val Tags = 0xE0020..0xE007F
private val EmojiBlock = 0x1F300..0x1FAFF

/** 변이 선택자 없이도 기본 표시가 이모지인 BMP 글자(Emoji_Presentation). */
private val PresentationBmp =
    listOf(
        0x231A..0x231B,
        0x23E9..0x23EC,
        0x23F0..0x23F0,
        0x23F3..0x23F3,
        0x25FD..0x25FE,
        0x2614..0x2615,
        0x2648..0x2653,
        0x267F..0x267F,
        0x2693..0x2693,
        0x26A1..0x26A1,
        0x26AA..0x26AB,
        0x26BD..0x26BE,
        0x26C4..0x26C5,
        0x26CE..0x26CE,
        0x26D4..0x26D4,
        0x26EA..0x26EA,
        0x26F2..0x26F3,
        0x26F5..0x26F5,
        0x26FA..0x26FA,
        0x26FD..0x26FD,
        0x2705..0x2705,
        0x270A..0x270B,
        0x2728..0x2728,
        0x274C..0x274C,
        0x274E..0x274E,
        0x2753..0x2755,
        0x2757..0x2757,
        0x2795..0x2797,
        0x27B0..0x27B0,
        0x27BF..0x27BF,
        0x2B1B..0x2B1C,
        0x2B50..0x2B50,
        0x2B55..0x2B55,
    )

/** [text]를 일반 글과 이모지 조각으로 순서대로 나눈다. 조각을 이으면 원문과 같다. */
internal fun splitEmoji(text: String): List<TextSegment> {
    val segments = mutableListOf<TextSegment>()
    var plainStart = 0
    var index = 0
    while (index < text.length) {
        val end = text.emojiEndOrNull(index)
        if (end == null) {
            index += charCount(text.codePointAt(index))
            continue
        }
        if (index > plainStart) segments += TextSegment(text.substring(plainStart, index), isEmoji = false)
        segments += TextSegment(text.substring(index, end), isEmoji = true)
        index = end
        plainStart = end
    }
    if (plainStart < text.length) segments += TextSegment(text.substring(plainStart), isEmoji = false)
    return segments
}

/** [start]에서 이모지가 시작되면 그 끝 위치를, 아니면 null을 돌려준다. */
private fun String.emojiEndOrNull(start: Int): Int? {
    val first = codePointAt(start)
    val afterFirst = start + charCount(first)
    val next = codePointOrNull(afterFirst)
    return when {
        first in RegionalIndicators -> if (next in RegionalIndicators) afterFirst + charCount(next!!) else null
        first in KeycapBases -> keycapEndOrNull(afterFirst)
        first in EmojiBlock || PresentationBmp.any { first in it } || next == VARIATION_SELECTOR ->
            sequenceEnd(afterFirst)
        else -> null
    }
}

/** 숫자 뒤에 키캡이 붙어 있을 때만 끝 위치를 돌려준다. 그냥 숫자를 이모지로 잡지 않기 위해서다. */
private fun String.keycapEndOrNull(afterBase: Int): Int? {
    val keycapAt = if (codePointOrNull(afterBase) == VARIATION_SELECTOR) afterBase + 1 else afterBase
    return if (codePointOrNull(keycapAt) == KEYCAP) keycapAt + 1 else null
}

/**
 * 변이 선택자·피부색·태그·ZWJ로 이어지는 뒷부분까지 포함한 끝 위치를 돌려준다.
 * 글자 모양 선택자(U+FE0E)도 남겨야 브라우저가 요청대로 글자 모양으로 그린다.
 */
private fun String.sequenceEnd(from: Int): Int {
    var end = from
    while (true) {
        val next = codePointOrNull(end) ?: return end
        end +=
            when {
                next == VARIATION_SELECTOR || next == TEXT_SELECTOR || next == KEYCAP -> charCount(next)
                next in SkinTones || next in Tags -> charCount(next)
                next == ZERO_WIDTH_JOINER -> {
                    val joined = codePointOrNull(end + 1) ?: return end
                    1 + charCount(joined)
                }
                else -> return end
            }
    }
}

private fun String.codePointOrNull(index: Int): Int? = if (index < length) codePointAt(index) else null

private fun String.codePointAt(index: Int): Int {
    val high = this[index]
    val low = getOrNull(index + 1)
    if (!high.isHighSurrogate() || low == null || !low.isLowSurrogate()) return high.code
    return ((high.code - 0xD800) shl 10) + (low.code - 0xDC00) + 0x10000
}

private fun charCount(codePoint: Int): Int = if (codePoint >= 0x10000) 2 else 1
