package log.haeti.admin.editor

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue

/**
 * 선택 영역을 [block]으로 바꾸되, 앞뒤에 빈 줄이 하나씩 있도록 줄바꿈을 추가한다.
 * 이미지처럼 문단과 붙으면 문단 안 인라인으로 읽히는 블록을 넣을 때 쓴다. 커서는 블록 바로 뒤에 둔다.
 *
 * @param block 넣을 마크다운 블록 (앞뒤 줄바꿈 없이)
 */
fun TextFieldValue.insertBlock(block: String): TextFieldValue {
    val before = text.substring(0, selection.min)
    val after = text.substring(selection.max)
    val prefix =
        when {
            before.isEmpty() || before.endsWith("\n\n") -> ""
            before.endsWith("\n") -> "\n"
            else -> "\n\n"
        }
    val suffix =
        when {
            after.startsWith("\n\n") -> ""
            after.startsWith("\n") || after.isEmpty() -> "\n"
            else -> "\n\n"
        }
    val inserted = prefix + block + suffix
    return TextFieldValue(before + inserted + after, TextRange(before.length + prefix.length + block.length))
}
