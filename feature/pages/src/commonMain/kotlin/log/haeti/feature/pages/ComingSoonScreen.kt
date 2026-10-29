package log.haeti.feature.pages

import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import log.haeti.core.common.text.keepAll
import log.haeti.core.ui.component.LinkText
import log.haeti.core.ui.layout.CenteredContent
import log.haeti.core.ui.layout.HomeMaxWidth
import log.haeti.core.ui.layout.LocalLayoutTier
import log.haeti.core.ui.layout.isCompact
import log.haeti.core.ui.layout.topSpacing
import log.haeti.core.ui.strings.LocalHaetiStrings

/**
 * 아직 내용이 없는 페이지(프로젝트·소개). 오류처럼 보이지 않게 페이지 이름을 그대로 제목으로 두고,
 * 제목 끝 커서가 깜빡여 "쓰는 중"임을 보인다.
 *
 * @param title 페이지 이름
 * @param message 무엇이 들어올지 한두 문장
 * @param onNavigateHome 글 목록으로 가는 콜백
 */
@Composable
fun ComingSoonScreen(
    title: String,
    message: String,
    onNavigateHome: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tier = LocalLayoutTier.current
    val strings = LocalHaetiStrings.current
    val headline = MaterialTheme.typography.headlineLarge
    val titleStyle = if (tier.isCompact) headline.copy(fontSize = 28.sp, lineHeight = 38.sp) else headline
    CenteredContent(modifier = modifier.padding(top = tier.topSpacing), maxWidth = HomeMaxWidth) {
        Column {
            Text(
                text = strings.comingSoon,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = title, style = titleStyle, color = MaterialTheme.colorScheme.onBackground)
                Spacer(Modifier.width(6.dp))
                TypingCaret(titleStyle)
            }
            Spacer(Modifier.height(16.dp))
            Text(
                text = message.keepAll(),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.widthIn(max = 560.dp),
            )
            Spacer(Modifier.height(28.dp))
            LinkText(text = strings.browsePosts, onClick = onNavigateHome)
        }
    }
}

/** 글자 높이만 한 세로 커서. 편집기 커서처럼 켜졌다 꺼진다. 알파만 바꾸므로 다시 그리기만 한다. */
@Composable
private fun TypingCaret(style: TextStyle) {
    val blink = rememberInfiniteTransition()
    val alpha =
        blink.animateFloat(
            initialValue = 1f,
            targetValue = 1f,
            animationSpec =
                infiniteRepeatable(
                    keyframes {
                        durationMillis = 1_100
                        1f at 500
                        0f at 600
                        0f at 1_000
                    },
                ),
        )
    val height = with(LocalDensity.current) { style.fontSize.toDp() } * 0.85f
    Spacer(
        Modifier
            .size(width = 3.dp, height = height)
            .graphicsLayer { this.alpha = alpha.value }
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary),
    )
}
