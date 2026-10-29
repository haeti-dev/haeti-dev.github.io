package log.haeti.admin.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private val FieldShape = RoundedCornerShape(10.dp)

/**
 * 라벨이 위에 붙은 한 줄 입력칸. 포커스되면 테두리가 브랜드 색이 된다.
 *
 * @param label 입력칸 위 라벨
 * @param value 현재 값
 * @param onValueChange 값이 바뀔 때 호출
 * @param placeholder 비어 있을 때 흐리게 보이는 예시
 * @param isError true면 테두리를 경고색으로 칠한다
 */
@Composable
fun AdminTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    isError: Boolean = false,
) {
    val colors = MaterialTheme.colorScheme
    val interactionSource = remember { MutableInteractionSource() }
    val focused by interactionSource.collectIsFocusedAsState()
    val borderColor =
        if (isError) {
            colors.error
        } else if (focused) {
            colors.primary
        } else {
            colors.outline
        }
    Column(modifier = modifier) {
        Text(
            label,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = colors.onSurfaceVariant,
        )
        Spacer(Modifier.height(6.dp))
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            interactionSource = interactionSource,
            textStyle = MaterialTheme.typography.bodyMedium.copy(color = colors.onBackground),
            cursorBrush = SolidColor(colors.primary),
            modifier =
                Modifier
                    .fillMaxWidth()
                    .background(colors.background, FieldShape)
                    .border(1.dp, borderColor, FieldShape)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
            decorationBox = { inner ->
                if (value.isEmpty()) {
                    Text(placeholder, style = MaterialTheme.typography.bodyMedium, color = colors.outlineVariant)
                }
                inner()
            },
        )
    }
}
