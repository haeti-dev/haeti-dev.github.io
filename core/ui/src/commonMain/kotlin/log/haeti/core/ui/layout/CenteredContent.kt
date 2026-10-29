package log.haeti.core.ui.layout

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp

/**
 * 콘텐츠를 [maxWidth]로 제한하고 화면 중앙에 배치한다. 좌우 패딩은 [LocalLayoutTier]에서 유도한다.
 * 모든 화면이 공유하는 단일 중앙 컬럼 레이아웃이다.
 *
 * @param maxWidth 본문 컬럼의 최대 폭. 기본값은 [ContentMaxWidth](760dp).
 * @param content 중앙 컬럼에 쌓을 내용. [ColumnScope] 안이라 `weight`·`align`을 바로 쓸 수 있다.
 */
@Composable
fun CenteredContent(
    modifier: Modifier = Modifier,
    maxWidth: Dp = ContentMaxWidth,
    content: @Composable ColumnScope.() -> Unit,
) {
    val tier = LocalLayoutTier.current
    Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
        Column(
            modifier =
                Modifier
                    .widthIn(max = maxWidth)
                    .fillMaxWidth()
                    .padding(horizontal = tier.horizontalPadding),
            content = content,
        )
    }
}
