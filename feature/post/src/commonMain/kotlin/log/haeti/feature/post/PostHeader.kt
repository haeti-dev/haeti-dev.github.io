package log.haeti.feature.post

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Translate
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import log.haeti.core.common.text.keepAll
import log.haeti.core.designsystem.component.CategoryChip
import log.haeti.core.designsystem.interaction.SelectableArea
import log.haeti.core.designsystem.interaction.haetiClickable
import log.haeti.core.model.PostMeta
import log.haeti.core.model.dotDate
import log.haeti.core.model.readingTimeLabel
import log.haeti.core.ui.component.PostMetaLine
import log.haeti.core.ui.layout.LocalLayoutTier
import log.haeti.core.ui.layout.isCompact
import log.haeti.core.ui.strings.LocalHaetiStrings

/**
 * 글 머리말. 목록으로 돌아가는 링크, 카테고리, 제목, 요약, 날짜·분량을 쌓는다.
 *
 * @param meta 글 메타데이터
 * @param onNavigateHome 글 목록으로 돌아가는 콜백
 * @param translationMissing 번역본이 없어 원문을 보여주는 중이면 안내를 덧붙인다
 */
@Composable
internal fun PostHeader(
    meta: PostMeta,
    onNavigateHome: () -> Unit,
    translationMissing: Boolean = false,
) {
    val compact = LocalLayoutTier.current.isCompact
    Column {
        BackLink(onClick = onNavigateHome)
        Spacer(Modifier.height(if (compact) 28.dp else 40.dp))
        CategoryChip(label = meta.category, text = LocalHaetiStrings.current.category(meta.category))
        Spacer(Modifier.height(16.dp))
        // 본문처럼 제목과 요약도 드래그해 복사할 수 있게 한다.
        SelectableArea {
            Column {
                AnimatedTitle(title = meta.title.keepAll(), compact = compact)
                Spacer(Modifier.height(16.dp))
                Text(
                    text = meta.summary.keepAll(),
                    style =
                        MaterialTheme.typography.bodyLarge.copy(
                            fontSize = if (compact) 17.sp else 19.sp,
                            lineHeight = 31.sp,
                        ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Spacer(Modifier.height(24.dp))
        PostMetaLine(date = meta.dotDate(), readingTime = meta.readingTimeLabel())
        if (translationMissing) {
            Spacer(Modifier.height(20.dp))
            TranslationNotice()
        }
    }
}

/** 영어로 보는 중인데 번역본이 없을 때의 안내 띠. */
@Composable
private fun TranslationNotice() {
    Row(
        modifier =
            Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val color = MaterialTheme.colorScheme.onSurfaceVariant
        Icon(Icons.Outlined.Translate, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(8.dp))
        Text(LocalHaetiStrings.current.translationMissing, style = MaterialTheme.typography.labelLarge, color = color)
    }
}

/** 글 목록으로 돌아가는 작은 링크. */
@Composable
private fun BackLink(onClick: () -> Unit) {
    val color = MaterialTheme.colorScheme.onSurfaceVariant
    Row(modifier = Modifier.haetiClickable(onClick = onClick), verticalAlignment = Alignment.CenterVertically) {
        Icon(
            Icons.AutoMirrored.Outlined.ArrowBack,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(16.dp),
        )
        Spacer(Modifier.width(6.dp))
        Text(text = LocalHaetiStrings.current.backToList, style = MaterialTheme.typography.labelLarge, color = color)
    }
}

/**
 * 진입 1회 모션: 제목이 12dp 슬라이드업 + 페이드로 나타난다.
 * AnimatedVisibility는 wasm에서 enter가 시작되지 않는 경우가 있어 Animatable+graphicsLayer로 구현한다.
 */
@Composable
private fun AnimatedTitle(
    title: String,
    compact: Boolean,
) {
    val progress = remember(title) { Animatable(0f) }
    LaunchedEffect(title) { progress.animateTo(1f, animationSpec = tween(320)) }
    val slidePx = with(LocalDensity.current) { 12.dp.toPx() }
    val base = MaterialTheme.typography.displaySmall
    Text(
        text = title,
        style = if (compact) base.copy(fontSize = 31.sp, lineHeight = 41.sp) else base,
        color = MaterialTheme.colorScheme.onBackground,
        modifier =
            Modifier.graphicsLayer {
                alpha = 0.2f + 0.8f * progress.value
                translationY = slidePx * (1f - progress.value)
            },
    )
}
