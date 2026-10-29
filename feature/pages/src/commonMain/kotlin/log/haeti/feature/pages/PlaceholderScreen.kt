package log.haeti.feature.pages

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import log.haeti.core.ui.component.NotFoundNotice
import log.haeti.core.ui.layout.CenteredContent
import log.haeti.core.ui.layout.HomeMaxWidth
import log.haeti.core.ui.layout.LocalLayoutTier
import log.haeti.core.ui.layout.topSpacing

/**
 * 알 수 없는 경로의 안내 화면.
 *
 * @param onNavigateHome 글 목록으로 가는 콜백
 * @param title 무엇이 없는지 한 줄
 * @param message 이유나 다음에 할 일 한 줄
 */
@Composable
fun PlaceholderScreen(
    onNavigateHome: () -> Unit,
    title: String,
    message: String,
    modifier: Modifier = Modifier,
) {
    val tier = LocalLayoutTier.current
    CenteredContent(modifier = modifier.padding(top = tier.topSpacing), maxWidth = HomeMaxWidth) {
        NotFoundNotice(onNavigateHome = onNavigateHome, title = title, message = message)
    }
}
