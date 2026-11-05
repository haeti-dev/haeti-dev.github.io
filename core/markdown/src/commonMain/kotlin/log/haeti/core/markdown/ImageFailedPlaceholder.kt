package log.haeti.core.markdown

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BrokenImage
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

/**
 * 이미지를 읽지 못한 자리. 읽는 동안 잡아 둔 자리와 같은 크기([modifier])에 안내 문구와 대체텍스트를 보인다.
 * 크기가 같아야 실패로 바뀌는 순간에도 아래 글이 움직이지 않는다.
 */
@Composable
internal fun ImageFailedPlaceholder(
    alt: String?,
    modifier: Modifier,
) {
    val faded = LocalContentColor.current.copy(alpha = 0.55f)
    Column(
        modifier =
            modifier
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(Icons.Outlined.BrokenImage, contentDescription = null, tint = faded, modifier = Modifier.size(24.dp))
        Text(
            text = LocalImageFailedText.current,
            style = MaterialTheme.typography.bodySmall,
            color = faded,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (!alt.isNullOrBlank()) {
            Text(
                text = alt,
                style = MaterialTheme.typography.bodySmall,
                color = LocalContentColor.current.copy(alpha = 0.4f),
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
