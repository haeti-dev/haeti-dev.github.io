package log.haeti.core.markdown.model

/**
 * 글을 이어 붙인 조각 하나.
 *
 * @param text 조각 내용
 * @param isEmoji 이모지 한 글자면 true
 */
internal data class TextSegment(
    val text: String,
    val isEmoji: Boolean,
)
