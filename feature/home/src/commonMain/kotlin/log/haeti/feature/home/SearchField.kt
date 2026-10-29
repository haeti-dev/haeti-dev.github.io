package log.haeti.feature.home

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import log.haeti.core.designsystem.interaction.haetiClickable
import log.haeti.core.ui.strings.LocalHaetiStrings

private val FieldShape = RoundedCornerShape(12.dp)

/**
 * 글 검색창. 입력하는 대로 목록이 걸러진다. 글자가 있으면 오른쪽에 지우기 버튼이 생긴다.
 *
 * @param query 지금 검색어
 * @param onQueryChange 검색어가 바뀔 때 호출
 */
@Composable
internal fun SearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val strings = LocalHaetiStrings.current
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .height(48.dp)
                .border(1.dp, colors.outline, FieldShape)
                .padding(start = 16.dp, end = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            Icons.Outlined.Search,
            contentDescription = null,
            tint = colors.outlineVariant,
            modifier = Modifier.size(20.dp),
        )
        Spacer(Modifier.width(10.dp))
        SearchInput(
            query = query,
            onQueryChange = onQueryChange,
            placeholder = strings.searchPlaceholder,
            modifier = Modifier.weight(1f),
        )
        if (query.isNotEmpty()) {
            Icon(
                imageVector = Icons.Outlined.Close,
                contentDescription = strings.clearSearch,
                tint = colors.onSurfaceVariant,
                modifier =
                    Modifier
                        .clip(CircleShape)
                        .haetiClickable(onClick = { onQueryChange("") })
                        .padding(8.dp)
                        .size(16.dp),
            )
        }
    }
}

/** 한 줄 입력 칸. 비어 있으면 [placeholder]를 겹쳐 보인다. */
@Composable
private fun SearchInput(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    Box(modifier = modifier, contentAlignment = Alignment.CenterStart) {
        if (query.isEmpty()) {
            Text(placeholder, style = MaterialTheme.typography.bodyMedium, color = colors.outlineVariant)
        }
        BasicTextField(
            value = query,
            onValueChange = onQueryChange,
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyMedium.copy(color = colors.onBackground),
            cursorBrush = SolidColor(colors.primary),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
