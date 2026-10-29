package log.haeti.admin.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

/**
 * 에디터용 마크다운 구문 강조 색상 세트.
 *
 * @param muted 프론트매터 펜스·문법 기호처럼 흐리게 보여야 하는 요소
 * @param accent 프론트매터 키·목록 기호·링크·인라인 코드
 * @param code 코드 펜스 안쪽 글자
 * @param mono 코드에 쓸 고정폭 글꼴
 */
class HighlightColors(
    val muted: Color,
    val accent: Color,
    val code: Color,
    val mono: FontFamily,
)

private val HEADING = Regex("""^(#{1,6})\s.*""")
private val LIST_MARKER = Regex("""^\s*([-*]|\d+\.)\s""")
private val INLINE_CODE = Regex("""`[^`]+`""")
private val BOLD = Regex("""\*\*[^*]+\*\*""")
private val LINK = Regex("""!?\[[^\]]*]\([^)]*\)(\{[^}]*})?""")

/** 화면에서만 끼워 넣는 줄바꿈 금지 문자(WORD JOINER). 파일에는 저장되지 않는다. */
private const val WORD_JOINER = '\u2060'

/** 색 하나가 칠해질 원문 구간. */
private class Span(
    val style: SpanStyle,
    val start: Int,
    val end: Int,
)

/**
 * 에디터 표시용 [VisualTransformation]. 마크다운 문법에 색·굵기를 입히고, 한글 단어 안에
 * 줄바꿈 금지 문자를 끼워 "블|로그"처럼 단어 중간에서 줄이 바뀌지 않게 한다.
 * 끼운 문자는 화면에만 있고 원문·저장 파일에는 없다. 커서 위치는 [OffsetMapping]으로 되돌린다.
 *
 * @param colors 색상 세트
 */
class MarkdownHighlighter(
    private val colors: HighlightColors,
) : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val source = text.text
        val joinAfter =
            BooleanArray(source.length) { i ->
                i + 1 < source.length &&
                    source[i].isHangul() &&
                    source[i + 1].isHangul()
            }
        val originalToShown = IntArray(source.length + 1)
        var shift = 0
        for (i in 0..source.length) {
            originalToShown[i] = i + shift
            if (i < source.length && joinAfter[i]) shift++
        }
        val shownToOriginal = IntArray(source.length + shift + 1)
        for (i in source.indices) {
            shownToOriginal[originalToShown[i]] = i
            if (joinAfter[i]) shownToOriginal[originalToShown[i] + 1] = i + 1
        }
        shownToOriginal[source.length + shift] = source.length
        val shown =
            buildAnnotatedString {
                source.forEachIndexed { i, char ->
                    append(char)
                    if (joinAfter[i]) append(WORD_JOINER)
                }
                collectSpans(source).forEach { addStyle(it.style, originalToShown[it.start], originalToShown[it.end]) }
            }
        val mapping =
            object : OffsetMapping {
                override fun originalToTransformed(offset: Int) = originalToShown[offset.coerceIn(0, source.length)]

                override fun transformedToOriginal(offset: Int) =
                    shownToOriginal[
                        offset.coerceIn(
                            0,
                            shownToOriginal.size - 1,
                        ),
                    ]
            }
        return TransformedText(shown, mapping)
    }

    /** 줄 단위로 훑으며 프론트매터·코드 펜스 상태를 추적해 칠할 구간을 모은다. */
    private fun collectSpans(source: String): List<Span> {
        val spans = mutableListOf<Span>()
        var offset = 0
        var inFrontmatter = false
        var inFence = false
        source.lines().forEachIndexed { index, line ->
            val fenceLine = line.trimStart().startsWith("```")
            when {
                line.trim() == "---" && (index == 0 || inFrontmatter) -> {
                    inFrontmatter = index == 0
                    spans.add(offset, line.length, SpanStyle(color = colors.muted))
                }
                inFrontmatter -> spans.frontmatter(line, offset)
                fenceLine || inFence -> {
                    spans.add(offset, line.length, SpanStyle(color = colors.code, fontFamily = colors.mono))
                    if (fenceLine) inFence = !inFence
                }
                else -> spans.prose(line, offset)
            }
            offset += line.length + 1
        }
        return spans
    }

    /** `key: value` 줄. 키는 강조색, 값은 그대로. */
    private fun MutableList<Span>.frontmatter(
        line: String,
        offset: Int,
    ) {
        val separator = line.indexOf(':')
        if (separator > 0) add(offset, separator + 1, SpanStyle(color = colors.accent, fontWeight = FontWeight.Bold))
    }

    /** 본문 줄. 헤딩·인용·목록 기호와 인라인 문법을 칠한다. */
    private fun MutableList<Span>.prose(
        line: String,
        offset: Int,
    ) {
        HEADING.matchEntire(line)?.let { match ->
            add(offset, line.length, SpanStyle(fontWeight = FontWeight.Bold))
            add(offset, match.groupValues[1].length, SpanStyle(color = colors.muted))
        }
        if (line.startsWith(">")) add(offset, line.length, SpanStyle(color = colors.muted))
        LIST_MARKER.find(line)?.let { add(offset + it.range.first, it.value.length, SpanStyle(color = colors.accent)) }
        BOLD
            .findAll(
                line,
            ).forEach { add(offset + it.range.first, it.value.length, SpanStyle(fontWeight = FontWeight.Bold)) }
        LINK.findAll(line).forEach { add(offset + it.range.first, it.value.length, SpanStyle(color = colors.accent)) }
        INLINE_CODE.findAll(line).forEach {
            add(offset + it.range.first, it.value.length, SpanStyle(color = colors.accent, fontFamily = colors.mono))
        }
    }

    private fun MutableList<Span>.add(
        start: Int,
        length: Int,
        style: SpanStyle,
    ) {
        if (length > 0) add(Span(style, start, start + length))
    }
}

/** 한글 음절 범위인지. */
private fun Char.isHangul(): Boolean = this in '\uAC00'..'\uD7A3'
