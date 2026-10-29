package log.haeti.admin.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import log.haeti.core.designsystem.interaction.haetiClickable

private val FailedAccent = Color(0xFFB97709)
private val DoneAccent = Color(0xFF138A5E)

/**
 * 사이드바 아래에 띄우는 번역 결과 안내. 성공은 초록, 실패는 주황색이고 X로 닫는다.
 *
 * @param message 안내 문장 (실패면 로그 경로 포함)
 * @param failed 실패 안내인지
 * @param onDismiss 닫기
 */
@Composable
fun NoticeBanner(
    message: String,
    failed: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val accent = if (failed) FailedAccent else DoneAccent
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .background(accent.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                .padding(start = 14.dp, end = 6.dp, top = 10.dp, bottom = 10.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Icon(
            imageVector = if (failed) Icons.Outlined.Info else Icons.Outlined.CheckCircle,
            contentDescription = null,
            tint = accent,
            modifier = Modifier.padding(top = 2.dp).size(16.dp),
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.weight(1f),
        )
        Icon(
            imageVector = Icons.Outlined.Close,
            contentDescription = "안내 닫기",
            tint = MaterialTheme.colorScheme.outlineVariant,
            modifier =
                Modifier
                    .clip(CircleShape)
                    .haetiClickable(onClick = onDismiss)
                    .padding(4.dp)
                    .size(14.dp),
        )
    }
}
