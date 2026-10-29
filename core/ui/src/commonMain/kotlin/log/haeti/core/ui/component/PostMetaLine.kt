package log.haeti.core.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp

/**
 * 날짜와 읽기 분량을 한 줄에 두고 `|`로 나눈다.
 *
 * @param date 표시용 날짜 문자열
 * @param readingTime 표시용 분량 문자열 (예: `6 min read`)
 * @param style 글자 스타일
 */
@Composable
fun PostMetaLine(
    date: String,
    readingTime: String,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.labelMedium,
) {
    val color = MaterialTheme.colorScheme.outlineVariant
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(text = date, style = style, color = color)
        Text(text = "|", style = style, color = MaterialTheme.colorScheme.outline)
        Text(text = readingTime, style = style, color = color)
    }
}
