package log.haeti.admin.ui.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import log.haeti.core.designsystem.interaction.haetiClickable

private val ToggleShape = RoundedCornerShape(percent = 50)

/**
 * 둘 이상 중 하나를 고르는 pill 형태 토글. 선택한 칸만 기본 텍스트 색으로 채운다.
 *
 * @param options 칸 라벨
 * @param selectedIndex 선택한 칸 인덱스
 * @param onSelect 칸 클릭 시 인덱스를 넘긴다
 */
@Composable
fun SegmentedToggle(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    Row(modifier.clip(ToggleShape).border(1.dp, colors.outline, ToggleShape).padding(3.dp)) {
        options.forEachIndexed { index, label ->
            val selected = index == selectedIndex
            val fill by animateColorAsState(if (selected) colors.onBackground else Color.Transparent)
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = if (selected) colors.background else colors.onSurfaceVariant,
                modifier =
                    Modifier
                        .clip(ToggleShape)
                        .background(fill)
                        .haetiClickable(onClick = { onSelect(index) })
                        .padding(horizontal = 12.dp, vertical = 4.dp),
            )
        }
    }
}
