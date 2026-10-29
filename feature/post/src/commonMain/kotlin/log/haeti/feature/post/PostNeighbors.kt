package log.haeti.feature.post

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import log.haeti.core.common.text.keepAll
import log.haeti.core.designsystem.categoryPalette
import log.haeti.core.designsystem.interaction.haetiClickable
import log.haeti.core.model.PostMeta
import log.haeti.core.ui.layout.LocalLayoutTier
import log.haeti.core.ui.layout.isCompact
import log.haeti.core.ui.strings.LocalHaetiStrings

private val CardShape = RoundedCornerShape(16.dp)

/**
 * 본문 끝의 앞뒤 글 이동. 왼쪽이 더 오래된 글, 오른쪽이 더 최신 글이다. 한쪽이 없으면 빈 자리로 둔다.
 *
 * @param newer 더 최신 글
 * @param older 더 오래된 글
 * @param onOpenPost 클릭한 글의 슬러그를 넘기는 콜백
 */
@Composable
internal fun PostNeighbors(
    newer: PostMeta?,
    older: PostMeta?,
    onOpenPost: (String) -> Unit,
) {
    if (newer == null && older == null) return
    val strings = LocalHaetiStrings.current
    if (LocalLayoutTier.current.isCompact) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            older?.let { NeighborCard(strings.previousPost, it, false, onOpenPost, Modifier.fillMaxWidth()) }
            newer?.let { NeighborCard(strings.nextPost, it, true, onOpenPost, Modifier.fillMaxWidth()) }
        }
        return
    }
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        val cell = Modifier.weight(1f)
        older?.let { NeighborCard(strings.previousPost, it, false, onOpenPost, cell) } ?: Spacer(cell)
        newer?.let { NeighborCard(strings.nextPost, it, true, onOpenPost, cell) } ?: Spacer(cell)
    }
}

/** 앞뒤 글 카드 하나. 호버하면 테두리와 제목이 그 글의 카테고리 색으로 바뀐다. */
@Composable
private fun NeighborCard(
    label: String,
    meta: PostMeta,
    alignEnd: Boolean,
    onOpenPost: (String) -> Unit,
    modifier: Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()
    val accent = categoryPalette(meta.category).accent
    val border by animateColorAsState(if (hovered) accent else MaterialTheme.colorScheme.outline)
    val align = if (alignEnd) TextAlign.End else TextAlign.Start
    Column(
        modifier =
            modifier
                .border(1.dp, border, CardShape)
                .haetiClickable(onClick = { onOpenPost(meta.slug) }, interactionSource = interactionSource)
                .padding(horizontal = 22.dp, vertical = 20.dp),
        horizontalAlignment = if (alignEnd) Alignment.End else Alignment.Start,
    ) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.outlineVariant)
        Spacer(Modifier.height(6.dp))
        Text(
            text = meta.title.keepAll(),
            style = MaterialTheme.typography.titleMedium,
            color = if (hovered) accent else MaterialTheme.colorScheme.onBackground,
            textAlign = align,
        )
    }
}
