package log.haeti.core.ui.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import log.haeti.core.common.text.keepAll
import log.haeti.core.designsystem.categoryPalette
import log.haeti.core.designsystem.component.CategoryChip
import log.haeti.core.designsystem.interaction.haetiClickable
import log.haeti.core.model.PostMeta
import log.haeti.core.model.dotDate
import log.haeti.core.model.readingTimeLabel
import log.haeti.core.ui.strings.LocalHaetiStrings

private const val HoverFadeMs = 160

/**
 * 글 목록의 행 하나. 왼쪽에 카테고리·제목·요약·메타, 오른쪽에 커버 썸네일을 둔다.
 * 호버하면 제목이 카테고리 색으로 바뀐다. 배경 상자 없이 정렬선을 그대로 지킨다.
 *
 * @param meta 표시할 글 메타데이터
 * @param compact 좁은 화면이면 썸네일을 줄인다
 * @param onClick 행 클릭(글 상세로 이동) 시 호출
 * @param excerpt 검색어를 본문에서 찾았을 때 그 주변 문장. 요약 아래에 보인다
 */
@Composable
fun PostListRow(
    meta: PostMeta,
    compact: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    excerpt: String? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()
    // 켜질 때만 페이드하고 꺼질 때는 즉시 돌린다. 목록을 훑을 때 이전 행이 남아 두 행이 함께 칠해지지 않게.
    val titleColor by animateColorAsState(
        targetValue = if (hovered) categoryPalette(meta.category).accent else MaterialTheme.colorScheme.onBackground,
        animationSpec = if (hovered) tween(HoverFadeMs) else snap(),
    )
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .haetiClickable(onClick = onClick, interactionSource = interactionSource)
                .padding(vertical = if (compact) 20.dp else 28.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PostListRowText(meta = meta, titleColor = titleColor, excerpt = excerpt, modifier = Modifier.weight(1f))
        Spacer(Modifier.width(if (compact) 16.dp else 32.dp))
        val coverSize = if (compact) Modifier.size(88.dp, 66.dp) else Modifier.size(168.dp, 112.dp)
        PostCover(meta = meta, modifier = coverSize, cornerRadius = 14.dp)
    }
}

/** 행의 왼쪽 글 영역. 카테고리 칩, 제목, 요약, 검색 발췌, 메타 줄을 위에서부터 쌓는다. */
@Composable
private fun PostListRowText(
    meta: PostMeta,
    titleColor: Color,
    excerpt: String?,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        CategoryChip(label = meta.category, text = LocalHaetiStrings.current.category(meta.category))
        Spacer(Modifier.height(10.dp))
        Text(text = meta.title.keepAll(), style = MaterialTheme.typography.titleLarge, color = titleColor)
        Spacer(Modifier.height(6.dp))
        Text(
            text = meta.summary.keepAll(),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        excerpt?.let {
            Spacer(Modifier.height(8.dp))
            Text(
                text = it.keepAll(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outlineVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Spacer(Modifier.height(12.dp))
        PostMetaLine(date = meta.dotDate(), readingTime = meta.readingTimeLabel())
    }
}
