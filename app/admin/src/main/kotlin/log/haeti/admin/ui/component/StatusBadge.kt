package log.haeti.admin.ui.component

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private val BadgeShape = RoundedCornerShape(6.dp)

/**
 * 글 상태를 알리는 작은 테두리 배지 (초안, EN 등).
 *
 * @param text 배지 라벨
 * @param color 테두리·글자 색
 */
@Composable
fun StatusBadge(
    text: String,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
        color = color,
        modifier =
            modifier
                .border(1.dp, color.copy(alpha = 0.45f), BadgeShape)
                .padding(horizontal = 6.dp, vertical = 2.dp),
    )
}
