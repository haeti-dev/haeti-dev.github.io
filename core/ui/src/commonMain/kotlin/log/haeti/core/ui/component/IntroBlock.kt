package log.haeti.core.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import log.haeti.core.common.text.keepAll
import log.haeti.core.designsystem.interaction.SelectableArea

/**
 * 홈 상단 소개. 블로그가 무엇을 다루는지 한 문장으로 크게 말하고, 쓰는 사람과 링크를 덧붙인다.
 *
 * @param headline 큰 소개 문장
 * @param description 쓰는 사람과 다루는 주제를 설명하는 문단
 * @param links 라벨 to URL 목록
 * @param onOpenLink 링크 클릭 시 호출. URL을 전달한다
 * @param compact 좁은 화면이면 소개 문장을 줄인다
 */
@Composable
fun IntroBlock(
    headline: String,
    description: String,
    links: List<Pair<String, String>>,
    onOpenLink: (String) -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
) {
    val headlineStyle = MaterialTheme.typography.headlineLarge
    Column(modifier = modifier) {
        SelectableArea {
            Column {
                Text(
                    text = headline.keepAll(),
                    style = if (compact) headlineStyle.copy(fontSize = 28.sp, lineHeight = 38.sp) else headlineStyle,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    text = description.keepAll(),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.widthIn(max = 720.dp),
                )
            }
        }
        Spacer(Modifier.height(20.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            links.forEach { (label, url) -> LinkText(text = label, onClick = { onOpenLink(url) }) }
        }
    }
}
