package log.haeti.admin.ui.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import log.haeti.core.designsystem.interaction.haetiClickable

private val ButtonShape = RoundedCornerShape(10.dp)

/** 버튼 모양. 화면마다 주요 액션 하나만 [Primary]로 둔다. */
enum class AdminButtonStyle { Primary, Ghost }

/**
 * 어드민 공용 버튼. Primary 버튼은 브랜드 색으로 채우고, Secondary 버튼은 테두리만 둔다.
 *
 * @param text 버튼 라벨 (동작을 나타내는 동사)
 * @param onClick 클릭 시 호출
 * @param icon 라벨 앞 아이콘
 * @param hint 라벨 뒤에 흐리게 붙는 단축키 표기 (예: `⌘S`)
 * @param enabled false면 흐리게 보이고 눌리지 않는다
 */
@Composable
fun AdminButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: AdminButtonStyle = AdminButtonStyle.Ghost,
    icon: ImageVector? = null,
    hint: String? = null,
    enabled: Boolean = true,
) {
    val colors = MaterialTheme.colorScheme
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()
    val primary = style == AdminButtonStyle.Primary
    val base = if (primary) colors.primary else Color.Transparent
    val fill by animateColorAsState(if (hovered && enabled) hoverOf(primary) else base)
    val content = if (primary) colors.onPrimary else colors.onBackground
    Row(
        modifier =
            modifier
                .alpha(if (enabled) 1f else 0.45f)
                .clip(ButtonShape)
                .background(fill)
                .border(1.dp, if (primary) Color.Transparent else colors.outline, ButtonShape)
                .haetiClickable(onClick = onClick, interactionSource = interactionSource, enabled = enabled)
                .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = content, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(7.dp))
        }
        Text(text, style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold), color = content)
        if (hint != null) {
            Spacer(Modifier.width(8.dp))
            Text(hint, style = MaterialTheme.typography.labelMedium, color = content.copy(alpha = 0.6f))
        }
    }
}

/** 호버 바탕색. 주 버튼은 살짝 어둡게, 보조 버튼은 hover 토큰. */
@Composable
private fun hoverOf(primary: Boolean): Color =
    if (primary) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.88f)
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
