package log.haeti.core.ui.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import log.haeti.core.common.text.keepAll
import log.haeti.core.designsystem.categoryPalette
import log.haeti.core.designsystem.component.CategoryChip
import log.haeti.core.designsystem.interaction.haetiClickable
import log.haeti.core.model.PostMeta
import log.haeti.core.model.dotDate
import log.haeti.core.model.readingTimeLabel
import log.haeti.core.ui.strings.LocalHaetiStrings

/**
 * 홈 상단의 최신 글. 넓은 커버 아래에 카테고리·제목·요약·메타를 쌓는다.
 *
 * @param meta 최신 글 메타데이터
 * @param compact 좁은 화면이면 커버 높이를 줄인다
 * @param onClick 클릭 시 글 상세로 이동
 */
@Composable
fun FeaturedPost(
    meta: PostMeta,
    compact: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()
    val titleColor by animateColorAsState(
        targetValue = if (hovered) categoryPalette(meta.category).accent else MaterialTheme.colorScheme.onBackground,
        animationSpec = tween(160),
    )
    Column(
        modifier = modifier.fillMaxWidth().haetiClickable(onClick = onClick, interactionSource = interactionSource),
    ) {
        PostCover(
            meta = meta,
            modifier = Modifier.fillMaxWidth().aspectRatio(if (compact) 16f / 10f else 2.4f),
            cornerRadius = 24.dp,
        )
        Spacer(Modifier.height(28.dp))
        CategoryChip(label = meta.category, text = LocalHaetiStrings.current.category(meta.category))
        Spacer(Modifier.height(12.dp))
        Text(text = meta.title.keepAll(), style = MaterialTheme.typography.headlineMedium, color = titleColor)
        Spacer(Modifier.height(10.dp))
        Text(
            text = meta.summary.keepAll(),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(14.dp))
        PostMetaLine(date = meta.dotDate(), readingTime = meta.readingTimeLabel())
    }
}
