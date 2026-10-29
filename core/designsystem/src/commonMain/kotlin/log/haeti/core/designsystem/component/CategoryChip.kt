package log.haeti.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import log.haeti.core.designsystem.categoryPalette

/**
 * 카테고리 라벨. 카테고리 색 점 + 같은 색 텍스트로, 배경 없이 표기한다.
 *
 * @param label 색을 고르는 카테고리 원래 이름
 * @param text 화면에 보일 이름. 언어에 따라 [label]과 다를 수 있다
 * @param style 라벨 글자 스타일. 기본은 labelMedium 굵게
 */
@Composable
fun CategoryChip(
    label: String,
    modifier: Modifier = Modifier,
    text: String = label,
    style: TextStyle = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
) {
    val accent = categoryPalette(label).accent
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Spacer(modifier = Modifier.size(7.dp).background(accent, CircleShape))
        Spacer(modifier = Modifier.width(7.dp))
        Text(text = text, style = style, color = accent)
    }
}
