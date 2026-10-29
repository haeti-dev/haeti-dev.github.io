package log.haeti.core.ui.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import log.haeti.core.designsystem.interaction.haetiClickable
import log.haeti.core.model.Language
import log.haeti.core.ui.strings.LocalHaetiStrings

private val ToggleShape = RoundedCornerShape(percent = 50)

/**
 * 화면 언어 전환 `KO | EN`. 지금 언어 칸이 브랜드 색으로 채워져 있어 상태가 한눈에 보이고,
 * 다른 칸을 누르면 바로 바뀐다. 캔버스 화면은 브라우저 자동 번역이 되지 않아 직접 둔다.
 *
 * @param language 현재 화면 언어
 * @param onSelect 언어 칸 클릭 시 호출
 */
@Composable
fun LanguageToggle(
    language: Language,
    onSelect: (Language) -> Unit,
    modifier: Modifier = Modifier,
) {
    val description = LocalHaetiStrings.current.chooseLanguage
    Row(
        modifier =
            modifier
                .clip(ToggleShape)
                .border(1.dp, MaterialTheme.colorScheme.outline, ToggleShape)
                .padding(3.dp)
                .semantics { contentDescription = description },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Language.entries.forEach { option ->
            Segment(option = option, selected = option == language, onClick = { onSelect(option) })
        }
    }
}

/** 언어 칸 하나. 선택되면 브랜드 색 바탕에 흰 글자. */
@Composable
private fun Segment(
    option: Language,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val fill by animateColorAsState(if (selected) MaterialTheme.colorScheme.primary else Color.Transparent)
    Text(
        text = option.code.uppercase(),
        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
        color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
        modifier =
            Modifier
                .clip(ToggleShape)
                .background(fill)
                .haetiClickable(onClick = onClick)
                .semantics { this.selected = selected }
                .padding(horizontal = 9.dp, vertical = 4.dp),
    )
}
