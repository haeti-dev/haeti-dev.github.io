package log.haeti.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

private val SkeletonRadius = 8.dp

/**
 * 로딩 상태 표시용 스켈레톤 블록. surface 색 배경의 둥근 사각형이며 애니메이션은 없다
 * (상시 모션을 줄이려고 스피너 대신 쓴다).
 *
 * @param widthFraction 부모 폭 대비 비율 (0~1)
 * @param height 블록 높이
 */
@Composable
fun SkeletonBlock(
    widthFraction: Float,
    height: Dp,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .fillMaxWidth(widthFraction)
                .height(height)
                .clip(RoundedCornerShape(SkeletonRadius))
                .background(MaterialTheme.colorScheme.surface),
    )
}
