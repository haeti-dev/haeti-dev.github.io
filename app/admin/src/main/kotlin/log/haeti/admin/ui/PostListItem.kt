package log.haeti.admin.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import log.haeti.admin.model.PostEntry
import log.haeti.admin.model.TranslationStatus
import log.haeti.admin.ui.component.StatusBadge
import log.haeti.core.common.text.keepAll
import log.haeti.core.designsystem.component.CategoryChip
import log.haeti.core.designsystem.interaction.haetiClickable

private val ItemShape = RoundedCornerShape(12.dp)
private const val HOVER_IN_MS = 90

/**
 * 번역 상태 배지. 최신이면 파랑 `EN`, 원문이 바뀌었으면 주황색 `EN 갱신`,
 * 발행 글인데 번역본이 없으면 회색 `EN 없음`. 초안에 번역이 없는 건 정상이라 표시하지 않는다.
 * `EN 갱신`·`EN 없음`은 누르면 번역을 시작하고, 마우스를 올리면 `번역하기`로 바뀌어 누를 수 있음을 알린다.
 */
@Composable
private fun TranslationBadge(
    entry: PostEntry,
    isTranslating: Boolean,
    onTranslate: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    when {
        isTranslating -> StatusBadge("번역 중…", colors.primary)
        entry.translation == TranslationStatus.UpToDate -> StatusBadge("EN", colors.primary)
        entry.translation == TranslationStatus.Stale -> ActionBadge("EN 갱신", DraftColor, onTranslate)
        !entry.draft -> ActionBadge("EN 없음", colors.outlineVariant, onTranslate)
    }
}

/** 누를 수 있는 배지. 마우스를 올리면 라벨이 `번역하기`로 바뀐다. */
@Composable
private fun ActionBadge(
    label: String,
    color: Color,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()
    StatusBadge(
        text = if (hovered) "번역하기" else label,
        color = if (hovered) MaterialTheme.colorScheme.primary else color,
        modifier = Modifier.clip(BadgeShape).haetiClickable(onClick = onClick, interactionSource = interactionSource),
    )
}

private val BadgeShape = RoundedCornerShape(6.dp)

private val DraftColor = Color(0xFFB97709)

/**
 * 사이드바 글 항목. 카테고리 색 라벨과 날짜, 제목, 상태 배지(초안·EN)를 쌓는다.
 * 선택되면 브랜드 색이 옅게 깔리고, 호버하면 hover 토큰 바탕이 깔린다.
 *
 * @param entry 표시할 글
 * @param isSelected 지금 열려 있는 글인지
 * @param isTranslating 이 글의 번역이 돌고 있는지
 * @param onClick 클릭 시 호출
 * @param onTranslate 번역 배지를 눌렀을 때 호출
 */
@Composable
fun PostListItem(
    entry: PostEntry,
    isSelected: Boolean,
    isTranslating: Boolean,
    onClick: () -> Unit,
    onTranslate: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()
    // 꺼진 상태를 투명한 "검정"(Color.Transparent)으로 두면 페이드 중간에 검정이 섞여 더 어두워진다. 같은 색의 투명으로 둔다.
    val off = colors.surfaceVariant.copy(alpha = 0f)
    val target =
        when {
            isSelected -> colors.primary.copy(alpha = 0.1f)
            hovered -> colors.surfaceVariant
            else -> off
        }
    // 켜질 때만 짧게 페이드하고 꺼질 때는 즉시 지운다. 꺼질 때도 페이드하면 마우스를 훑을 때 이전 항목이
    // 아직 사라지는 중에 다음 항목이 켜져 두 개가 함께 칠해져 보인다.
    val fill by animateColorAsState(
        targetValue = target,
        animationSpec = if (target == off) snap() else tween(HOVER_IN_MS),
    )
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(ItemShape)
                .background(fill)
                .haetiClickable(onClick = onClick, interactionSource = interactionSource)
                .padding(horizontal = 12.dp, vertical = 12.dp),
    ) {
        PostListItemLabels(entry = entry, isTranslating = isTranslating, onTranslate = onTranslate)
        Spacer(Modifier.height(6.dp))
        Text(
            text = entry.title.ifBlank { "제목 없음" }.keepAll(),
            style = MaterialTheme.typography.titleMedium,
            color = if (entry.title.isBlank()) colors.outlineVariant else colors.onBackground,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/** 항목 윗줄. 카테고리 칩, 초안·번역 배지, 오른쪽 끝 날짜. */
@Composable
private fun PostListItemLabels(
    entry: PostEntry,
    isTranslating: Boolean,
    onTranslate: () -> Unit,
) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        if (entry.category.isNotBlank()) CategoryChip(label = entry.category)
        if (entry.draft) StatusBadge("초안", DraftColor)
        TranslationBadge(entry, isTranslating, onTranslate)
        Spacer(Modifier.weight(1f))
        Text(
            entry.date.replace('-', '.'),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.outlineVariant,
        )
    }
}
