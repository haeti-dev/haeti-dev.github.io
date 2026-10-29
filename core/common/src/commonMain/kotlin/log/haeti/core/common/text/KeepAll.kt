package log.haeti.core.common.text

/** 줄바꿈을 막는 폭 없는 문자(WORD JOINER, U+2060). 화면에는 그려지지 않는다. */
private const val WORD_JOINER = '⁠'

/**
 * 한글 단어 중간에서 줄이 바뀌지 않게 한다 (CSS `word-break: keep-all`과 같은 효과).
 *
 * 캔버스 렌더러는 한글 음절 사이 어디서든 줄을 바꿀 수 있어서 "나|눴다"처럼 단어가 쪼개진다.
 * 공백이 아닌 글자 사이마다 WORD JOINER를 넣어 줄바꿈 후보를 공백으로만 제한한다.
 * 화면 표시용 문자열에만 쓴다. 복사·검색에 쓰이는 원문에는 넣지 않는다.
 */
fun String.keepAll(): String = joinWordChars(this) { a, b -> a.isHangul() || b.isHangul() }

/**
 * 마크다운 원문용 [keepAll]. 코드 펜스(` ``` `) 안쪽 줄은 건드리지 않고, 한글과 한글 사이에만
 * 줄바꿈 금지 문자를 넣는다. 강조 기호(`**`)·링크 같은 마크다운 문법 옆에는 넣지 않아 파싱이 바뀌지 않는다.
 */
fun String.keepAllMarkdown(): String {
    var inFence = false
    return lines().joinToString("\n") { line ->
        if (line.trimStart().startsWith("```")) inFence = !inFence
        if (inFence ||
            line.trimStart().startsWith("```")
        ) {
            line
        } else {
            joinWordChars(line) { a, b -> a.isHangul() && b.isHangul() }
        }
    }
}

/** 공백이 아닌 두 글자 사이 중 [shouldJoin]이 참인 곳에 WORD JOINER를 넣는다. */
private inline fun joinWordChars(
    text: String,
    shouldJoin: (Char, Char) -> Boolean,
): String {
    if (text.length < 2) return text
    val builder = StringBuilder(text.length * 2)
    for (index in text.indices) {
        val char = text[index]
        builder.append(char)
        val next = text.getOrNull(index + 1) ?: continue
        if (!char.isWhitespace() && !next.isWhitespace() && shouldJoin(char, next)) {
            builder.append(WORD_JOINER)
        }
    }
    return builder.toString()
}

/** 한글 음절·자모 범위인지. 영문 단어는 원래 공백에서만 끊기므로 손대지 않는다. */
private fun Char.isHangul(): Boolean = this in '가'..'힣' || this in 'ᄀ'..'ᇿ' || this in '㄰'..'㆏'
