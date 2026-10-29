package log.haeti.core.markdown

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

/** "복사됨" 표시가 유지되는 시간. */
private const val COPIED_LABEL_MILLIS = 1500L

/** 복사 버튼 히트박스. 아이콘([COPY_ICON_SIZE])은 언어 라벨 글자 높이에 맞추고 주변에 여유를 둔다. */
private val COPY_HIT_BOX_SIZE = 30.dp
private val COPY_ICON_SIZE = 16.dp
private val HOVER_FILL = Color(0x1FFFFFFF)
private val COPIED_TINT = Color(0xFF6AAB73)

/**
 * 코드 카드 우상단, 언어 라벨 옆에 놓는 복사 버튼. 클릭하면 [text]를 클립보드에 복사하고
 * 1.5초간 체크 아이콘을 보여준다(언어와 무관한 표시). 호버하면 둥근 바탕이 옅게 깔린다.
 *
 * @param text 복사할 코드 원문 (펜스/언어 태그가 제거된 순수 코드)
 */
@Composable
internal fun CodeCopyButton(text: String) {
    var copiedTick by remember { mutableStateOf(0) }
    val copied = copiedTick > 0

    LaunchedEffect(copiedTick) {
        if (copied) {
            delay(COPIED_LABEL_MILLIS)
            copiedTick = 0
        }
    }

    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()
    Box(
        modifier =
            Modifier
                .size(COPY_HIT_BOX_SIZE)
                .clip(RoundedCornerShape(8.dp))
                .background(if (hovered) HOVER_FILL else Color.Transparent)
                .hoverable(interactionSource)
                .clickable(
                    indication = null,
                    interactionSource = interactionSource,
                ) {
                    copyToClipboard(text)
                    copiedTick++
                }.pointerHoverIcon(PointerIcon.Hand),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = if (copied) Icons.Outlined.Check else Icons.Outlined.ContentCopy,
            contentDescription = if (copied) "Copied" else "Copy code",
            tint = if (copied) COPIED_TINT else CODE_LABEL_COLOR,
            modifier = Modifier.size(COPY_ICON_SIZE),
        )
    }
}
