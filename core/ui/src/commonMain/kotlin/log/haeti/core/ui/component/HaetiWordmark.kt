package log.haeti.core.ui.component

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

/**
 * 워드마크 `Haeti.log`. `.log`만 브랜드 색으로 분리 표기한다.
 *
 * @param fontSize 글자 크기
 */
@Composable
fun HaetiWordmark(
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 20.sp,
) {
    val style =
        MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.Bold,
            fontSize = fontSize,
            lineHeight = fontSize * 1.2f,
            letterSpacing = (-0.02).em,
        )
    Row(modifier = modifier) {
        Text(text = "Haeti", style = style, color = MaterialTheme.colorScheme.onBackground)
        Text(text = ".log", style = style, color = MaterialTheme.colorScheme.primary)
    }
}
