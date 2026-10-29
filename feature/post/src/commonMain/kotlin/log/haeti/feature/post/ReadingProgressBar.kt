package log.haeti.feature.post

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.dp

/**
 * 읽은 만큼 왼쪽부터 차오르는 얇은 진행 바. 글의 카테고리 색으로 칠한다.
 * 스크롤 값은 레이아웃 단계에서만 읽어 스크롤마다 리컴포지션하지 않는다.
 *
 * @param scrollState 글 화면의 세로 스크롤 상태
 * @param color 바 색
 */
@Composable
internal fun ReadingProgressBar(
    scrollState: ScrollState,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .height(3.dp)
                .layout { measurable, constraints ->
                    val max = scrollState.maxValue
                    val fraction = if (max <= 0) 0f else scrollState.value.toFloat() / max
                    val width = (constraints.maxWidth * fraction).toInt()
                    val placeable = measurable.measure(constraints.copy(minWidth = width, maxWidth = width))
                    layout(constraints.maxWidth, placeable.height) { placeable.place(0, 0) }
                }.background(color),
    )
}
