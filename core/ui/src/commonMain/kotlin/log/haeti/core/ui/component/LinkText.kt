package log.haeti.core.ui.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import log.haeti.core.designsystem.interaction.haetiClickable

/**
 * 텍스트 링크. 평소에도 연한 밑줄이 있어 링크임을 알 수 있고, 호버하면 밑줄이 브랜드 색으로 진해진다.
 * 밑줄은 글자 색과 따로 칠하려고 기준선 아래에 직접 그린다.
 *
 * @param text 링크 라벨
 * @param onClick 클릭 시 호출
 * @param style 글자 스타일
 * @param color 글자 색
 */
@Composable
fun LinkText(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.labelLarge,
    color: Color = MaterialTheme.colorScheme.onBackground,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()
    val underline by animateColorAsState(
        if (hovered) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
    )
    var baseline by remember { mutableFloatStateOf(0f) }
    Text(
        text = text,
        style = style,
        color = color,
        onTextLayout = { layout -> baseline = layout.lastBaseline },
        modifier =
            modifier
                .haetiClickable(onClick = onClick, interactionSource = interactionSource)
                .drawBehind {
                    val y = baseline + 4.dp.toPx()
                    drawLine(underline, Offset(0f, y), Offset(size.width, y), 1.5.dp.toPx())
                },
    )
}
