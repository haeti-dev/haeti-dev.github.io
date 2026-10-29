package log.haeti.core.ui.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import log.haeti.core.designsystem.interaction.haetiClickable
import log.haeti.core.model.Language
import log.haeti.core.navigation.TopLevelDestination
import log.haeti.core.ui.layout.LocalLayoutTier
import log.haeti.core.ui.layout.horizontalPadding
import log.haeti.core.ui.layout.isCompact
import log.haeti.core.ui.strings.LocalHaetiStrings

private val TopBarHeight = 72.dp
private val UnderlineHeight = 2.dp
private val ItemGap = 28.dp
private val CompactRowHeight = 60.dp
private val CompactTabsHeight = 40.dp
private val CompactItemGap = 24.dp
private const val HoverFadeDurationMs = 150

/**
 * 상단바. 좌측 워드마크(`Haeti` + accent `.log`), 우측 GNB 3개와 언어·다크모드 토글을 배치한다.
 * 좁은 화면(Compact)은 한 줄에 다 들어가지 않으므로 GNB를 둘째 줄로 내린다(60dp + 40dp).
 *
 * 넓은 화면은 높이 72dp 한 줄. 콘텐츠 컬럼과 동일한 [maxWidth]·좌우 패딩으로 중앙 정렬해
 * 로고가 콘텐츠 시작선과 맞도록 한다.
 *
 * @param activeDestination 현재 활성 GNB 목적지
 * @param onNavigate GNB 탭 클릭 시 호출. 클릭된 목적지를 전달한다
 * @param isDarkTheme 현재 다크 테마 여부. 토글 아이콘(해/달) 선택에 쓰인다
 * @param onToggleTheme 테마 토글 클릭 시 호출
 * @param maxWidth 정렬 기준이 되는 콘텐츠 컬럼 폭
 * @param language 현재 화면 언어
 * @param onSelectLanguage 언어 전환 클릭 시 호출
 */
@Composable
fun HaetiTopBar(
    activeDestination: TopLevelDestination,
    onNavigate: (TopLevelDestination) -> Unit,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    maxWidth: Dp,
    language: Language,
    onSelectLanguage: (Language) -> Unit,
    modifier: Modifier = Modifier,
) {
    val hairline = MaterialTheme.colorScheme.outline
    Box(
        modifier =
            modifier.fillMaxWidth().drawBehind {
                val y = size.height - 0.5.dp.toPx()
                drawLine(hairline, Offset(0f, y), Offset(size.width, y), 1.dp.toPx())
            },
        contentAlignment = Alignment.Center,
    ) {
        val compact = LocalLayoutTier.current.isCompact
        Column(
            modifier =
                Modifier
                    .widthIn(max = maxWidth)
                    .fillMaxWidth()
                    .padding(horizontal = LocalLayoutTier.current.horizontalPadding),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().height(if (compact) CompactRowHeight else TopBarHeight),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                HaetiWordmark(modifier = Modifier.haetiClickable(onClick = { onNavigate(TopLevelDestination.Blog) }))
                TopBarActions(
                    showTabs = !compact,
                    activeDestination = activeDestination,
                    onNavigate = onNavigate,
                    isDarkTheme = isDarkTheme,
                    onToggleTheme = onToggleTheme,
                    language = language,
                    onSelectLanguage = onSelectLanguage,
                )
            }
            if (compact) {
                Row(modifier = Modifier.height(CompactTabsHeight), verticalAlignment = Alignment.Top) {
                    GnbTabs(activeDestination, onNavigate, gap = CompactItemGap)
                }
            }
        }
    }
}

/** 상단바 오른쪽 묶음. [showTabs]가 참이면 GNB 탭을 언어·테마 토글 앞에 둔다. */
@Composable
private fun TopBarActions(
    showTabs: Boolean,
    activeDestination: TopLevelDestination,
    onNavigate: (TopLevelDestination) -> Unit,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    language: Language,
    onSelectLanguage: (Language) -> Unit,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        // 좁은 화면은 한 줄에 다 들어가지 않아(375px에서 다크 모드 버튼이 화면 밖으로 밀렸다) 탭을 아래 줄로 내린다.
        if (showTabs) {
            GnbTabs(activeDestination, onNavigate, gap = ItemGap)
            Spacer(Modifier.width(ItemGap))
        }
        LanguageToggle(language = language, onSelect = onSelectLanguage)
        Spacer(Modifier.width(10.dp))
        ThemeToggle(isDarkTheme = isDarkTheme, onToggleTheme = onToggleTheme)
    }
}

/** GNB 탭 3개를 [gap] 간격으로 늘어놓는다. */
@Composable
private fun GnbTabs(
    activeDestination: TopLevelDestination,
    onNavigate: (TopLevelDestination) -> Unit,
    gap: Dp,
) {
    TopLevelDestination.entries.forEachIndexed { index, destination ->
        if (index > 0) Spacer(Modifier.width(gap))
        GnbLabel(
            destination = destination,
            active = destination == activeDestination,
            onClick = { onNavigate(destination) },
        )
    }
}

/** GNB 탭 1개. 활성=ink+아래 2dp accent 언더라인, 비활성=inkMuted, 호버 시 ink. */
@Composable
private fun GnbLabel(
    destination: TopLevelDestination,
    active: Boolean,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val textColor by animateColorAsState(
        targetValue =
            if (active || isHovered) {
                MaterialTheme.colorScheme.onBackground
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
        animationSpec = tween(HoverFadeDurationMs),
    )
    // IntrinsicSize.Max로 컬럼 폭을 라벨 폭에 고정해, 언더라인의 fillMaxWidth가 남는 공간을 먹지 않게 한다.
    val underline = if (active) MaterialTheme.colorScheme.primary else Color.Transparent
    Text(
        text = LocalHaetiStrings.current.navLabel(destination),
        style = MaterialTheme.typography.labelLarge,
        color = textColor,
        modifier =
            Modifier
                .haetiClickable(onClick = onClick, interactionSource = interactionSource)
                .drawBehind {
                    // 밑줄은 레이아웃 밖에 그려 라벨이 워드마크와 같은 세로 중심에 오게 한다.
                    val y = size.height + 6.dp.toPx()
                    drawLine(underline, Offset(0f, y), Offset(size.width, y), UnderlineHeight.toPx())
                },
    )
}
