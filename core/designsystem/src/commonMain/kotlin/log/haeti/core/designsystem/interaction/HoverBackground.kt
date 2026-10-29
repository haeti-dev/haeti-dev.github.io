package log.haeti.core.designsystem.interaction

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color

private const val HoverFadeDurationMs = 150

/**
 * 호버 시 `MaterialTheme.colorScheme.surfaceVariant`(hover 토큰)로 150ms에 걸쳐
 * 페이드되는 배경색을 반환한다. 반환값을 그대로 `Modifier.background()`에 넘기면 된다.
 *
 * 꺼질 때는 즉시 지운다(나란한 요소를 훑을 때 이전 것이 남아 두 개가 함께 칠해지지 않게).
 * 꺼진 상태는 같은 색의 투명이다. 투명한 검정에서 페이드하면 중간에 어두워진다.
 *
 * @param interactionSource 호버 상태를 읽어올 소스. [haetiClickable]과 동일한 인스턴스를 공유해야 한다
 */
@Composable
fun rememberHoverBackground(interactionSource: MutableInteractionSource): State<Color> {
    val isHovered by interactionSource.collectIsHoveredAsState()
    val hover = MaterialTheme.colorScheme.surfaceVariant
    return animateColorAsState(
        targetValue = if (isHovered) hover else hover.copy(alpha = 0f),
        animationSpec = if (isHovered) tween(HoverFadeDurationMs) else snap(),
    )
}
