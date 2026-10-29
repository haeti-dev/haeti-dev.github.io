package log.haeti.core.markdown

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mikepenz.markdown.compose.components.MarkdownComponentModel
import com.mikepenz.markdown.compose.elements.MarkdownBlockQuote
import log.haeti.core.markdown.model.CalloutType

private val CALLOUT_MARKER_REGEX = Regex("""^\[!(\w+)]\s*(.*)$""")

/**
 * blockquote 첫 줄이 `[!note]`/`[!tip]`/`[!warning]` 마커면 색 박스 콜아웃으로,
 * 아니면 기본 blockquote 스타일로 렌더링한다.
 * `MarkdownBody`가 `markdownComponents(blockQuote = ...)`로 연결한다.
 */
@Composable
internal fun MarkdownCalloutComponent(
    model: MarkdownComponentModel,
    isDarkPage: Boolean,
) {
    val raw = model.content.substring(model.node.startOffset, model.node.endOffset)
    val lines = raw.lines().map { it.removePrefix(">").removePrefix(" ") }
    val match = lines.firstOrNull()?.let { CALLOUT_MARKER_REGEX.find(it) }

    if (match == null) {
        MarkdownBlockQuote(content = model.content, node = model.node)
        return
    }

    val type = CalloutType.from(match.groupValues[1])
    val title =
        match.groupValues[2].ifBlank { null }
            ?: type.name.lowercase().replaceFirstChar(Char::uppercase)
    val body = lines.drop(1).joinToString("\n").trim()

    CalloutBox(type = type, title = title, body = body, isDarkPage = isDarkPage)
}

/** 좌측 액센트 바 + 타입별 옅은 바탕의 콜아웃 박스. */
@Composable
private fun CalloutBox(
    type: CalloutType,
    title: String,
    body: String,
    isDarkPage: Boolean,
) {
    val (accent, tint) = type.colors(isDarkPage)
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .height(IntrinsicSize.Min)
                .clip(RoundedCornerShape(12.dp))
                .background(tint),
    ) {
        Box(modifier = Modifier.width(4.dp).fillMaxHeight().background(accent))
        Column(modifier = Modifier.padding(start = 18.dp, top = 16.dp, end = 20.dp, bottom = 16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = accent,
            )
            if (body.isNotBlank()) {
                Text(
                    text = body,
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 16.sp, lineHeight = 27.sp),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(top = 6.dp),
                )
            }
        }
    }
}

/** 타입별 (강조색, 바탕색). 노트는 파랑, 팁은 초록, 경고는 주황색이다. */
private fun CalloutType.colors(isDark: Boolean): Pair<Color, Color> =
    when (this) {
        CalloutType.NOTE ->
            if (isDark) {
                Color(0xFF70A0FF) to Color(0xFF172238)
            } else {
                Color(0xFF2563D9) to
                    Color(0xFFEEF3FD)
            }
        CalloutType.TIP ->
            if (isDark) {
                Color(0xFF4CC995) to Color(0xFF142C24)
            } else {
                Color(0xFF138A5E) to
                    Color(0xFFE9F6F0)
            }
        CalloutType.WARNING ->
            if (isDark) {
                Color(0xFFF0B43C) to Color(0xFF2E2410)
            } else {
                Color(0xFFB97709) to
                    Color(0xFFFCF3E0)
            }
    }
