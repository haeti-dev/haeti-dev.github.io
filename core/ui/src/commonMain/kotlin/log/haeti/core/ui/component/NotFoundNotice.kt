package log.haeti.core.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import log.haeti.core.ui.strings.LocalHaetiStrings

/**
 * 보여줄 콘텐츠가 없을 때의 안내. 무엇이 없는지와 어디로 가면 되는지를 말한다.
 * 없는 글·준비 중 페이지·알 수 없는 경로가 같은 모양을 공유한다.
 *
 * @param onNavigateHome 액션 링크 클릭 시 호출
 * @param title 무엇이 없는지 한 줄
 * @param message 이유나 다음에 할 일을 설명하는 한 줄
 * @param actionLabel 액션 링크 라벨
 */
@Composable
fun NotFoundNotice(
    onNavigateHome: () -> Unit,
    modifier: Modifier = Modifier,
    title: String = LocalHaetiStrings.current.notFoundTitle,
    message: String = LocalHaetiStrings.current.notFoundMessage,
    actionLabel: String = LocalHaetiStrings.current.goToList,
) {
    Column(modifier = modifier) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(24.dp))
        LinkText(
            text = actionLabel,
            onClick = onNavigateHome,
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
        )
    }
}
