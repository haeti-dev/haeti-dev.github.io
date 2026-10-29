package log.haeti.admin.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * 아무 글도 열지 않았을 때의 작업 영역. 다음 작업(글 선택·새 글)을 안내한다.
 *
 * @param postCount 사이드바에 있는 글 수
 */
@Composable
fun EmptyWorkspace(
    postCount: Int,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = if (postCount == 0) "첫 글을 써 볼까요" else "편집할 글을 고르세요",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                text =
                    if (postCount == 0) {
                        "왼쪽 위 새 글 버튼으로 글 폴더를 만들면 여기서 바로 편집합니다."
                    } else {
                        "왼쪽 목록에서 글을 누르면 원문과 미리보기가 나란히 열립니다."
                    },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}
