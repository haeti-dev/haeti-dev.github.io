package log.haeti.core.designsystem.interaction

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.input.InputMode
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.platform.LocalInputModeManager
import androidx.compose.ui.unit.dp
import log.haeti.core.designsystem.LocalHaetiFocusRingColor

private val FocusRingWidth = 2.dp
private val FocusRingRadius = 8.dp

/**
 * 리플 없는 클릭 modifier. `indication = null`로 클릭 파동을 제거하고 마우스 커서를
 * 손 모양으로 바꾸며, 호버·포커스 가능 상태를 부여한다.
 *
 * 키보드 탐색(Tab)으로 얻은 포커스에만 2dp 브랜드 색 70%(다크 75%) 외곽선을 그린다. 마우스 클릭으로
 * 얻은 포커스는 링을 표시하지 않는다. `LocalInputModeManager`로 현재 입력 모드가
 * 키보드인지 판별해서 구분한다.
 *
 * @param onClick 클릭 시 호출
 * @param interactionSource 호버·포커스 상태를 다른 modifier(예: `rememberHoverBackground`)와
 * 공유하려면 전달. 생략하면 내부에서 새로 만든다
 * @param enabled false면 클릭·호버·포커스·커서 변경이 모두 비활성
 */
fun Modifier.haetiClickable(
    onClick: () -> Unit,
    interactionSource: MutableInteractionSource? = null,
    enabled: Boolean = true,
): Modifier =
    composed {
        val source = interactionSource ?: remember { MutableInteractionSource() }
        val isFocused by source.collectIsFocusedAsState()
        val isKeyboardInput = LocalInputModeManager.current.inputMode == InputMode.Keyboard
        val ring =
            if (isFocused && isKeyboardInput) {
                Modifier.border(FocusRingWidth, LocalHaetiFocusRingColor.current, RoundedCornerShape(FocusRingRadius))
            } else {
                Modifier
            }

        this
            // clickable이 포커스·호버를 직접 받아 [source]로 보낸다. focusable·hoverable을 또 붙이면 포커스 대상이
            // 둘이 되어 Tab을 항목마다 두 번 눌러야 했다.
            .clickable(interactionSource = source, indication = null, enabled = enabled, onClick = onClick)
            .pointerHoverIcon(PointerIcon.Hand)
            .then(ring)
    }
