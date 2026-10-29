package log.haeti.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import log.haeti.core.designsystem.component.SkeletonBlock
import log.haeti.core.ui.layout.CenteredContent
import log.haeti.core.ui.layout.ContentMaxWidth
import log.haeti.core.ui.layout.HomeMaxWidth
import log.haeti.core.ui.layout.LocalLayoutTier
import log.haeti.core.ui.layout.isCompact

/**
 * 글 로딩 중 자리 표시. 글 상세와 같은 배치(카테고리, 두 줄 제목, 요약, 날짜, 커버, 본문)라
 * 본문으로 바뀔 때 화면이 덜 출렁인다. 좌우 패딩은 [CenteredContent]가 `LocalLayoutTier`에서 읽는다.
 */
@Composable
fun PostSkeleton(modifier: Modifier = Modifier) {
    val compact = LocalLayoutTier.current.isCompact
    Column(modifier = modifier) {
        CenteredContent(maxWidth = ContentMaxWidth) {
            Spacer(Modifier.height(if (compact) 48.dp else 60.dp))
            SkeletonBlock(widthFraction = 0.15f, height = 16.dp)
            Spacer(Modifier.height(16.dp))
            SkeletonBlock(widthFraction = 0.9f, height = if (compact) 31.dp else 44.dp)
            Spacer(Modifier.height(10.dp))
            SkeletonBlock(widthFraction = 0.55f, height = if (compact) 31.dp else 44.dp)
            Spacer(Modifier.height(20.dp))
            SkeletonBlock(widthFraction = 0.8f, height = 19.dp)
            Spacer(Modifier.height(24.dp))
            SkeletonBlock(widthFraction = 0.25f, height = 14.dp)
        }
        Spacer(Modifier.height(if (compact) 32.dp else 48.dp))
        CenteredContent(maxWidth = HomeMaxWidth) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .aspectRatio(if (compact) 16f / 10f else 2.2f)
                    .clip(RoundedCornerShape(if (compact) 16.dp else 24.dp))
                    .background(MaterialTheme.colorScheme.surface),
            )
        }
        Spacer(Modifier.height(if (compact) 32.dp else 56.dp))
        CenteredContent(maxWidth = ContentMaxWidth) {
            listOf(1f, 1f, 0.7f).forEach { fraction ->
                SkeletonBlock(widthFraction = fraction, height = 17.dp)
                Spacer(Modifier.height(12.dp))
            }
        }
    }
}
