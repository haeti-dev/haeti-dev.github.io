package log.haeti.core.ui.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import log.haeti.core.designsystem.categoryPalette
import log.haeti.core.designsystem.interaction.haetiClickable
import log.haeti.core.model.CategoryCount
import log.haeti.core.ui.strings.LocalHaetiStrings

private val PillShape = RoundedCornerShape(percent = 50)

/**
 * 카테고리 필터 칩 목록. 맨 앞은 전체, 이어서 카테고리별 글 수를 보여준다.
 * 선택된 칩만 카테고리 색 바탕으로 채워진다.
 *
 * @param total 전체 글 수
 * @param categories 카테고리별 글 수
 * @param selected 선택된 카테고리. null이면 전체
 * @param onSelect 칩 클릭 시 호출. 전체는 null을 넘긴다
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CategoryFilterRow(
    total: Int,
    categories: List<CategoryCount>,
    selected: String?,
    onSelect: (String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        val strings = LocalHaetiStrings.current
        FilterPill(label = strings.all, count = total, dot = null, selected = selected == null) { onSelect(null) }
        categories.forEach { category ->
            val accent = categoryPalette(category.name).accent
            val label = strings.category(category.name)
            FilterPill(label, category.count, accent, selected == category.name) { onSelect(category.name) }
        }
    }
}

/** 칩 하나. 선택되면 점 색(전체는 기본 텍스트 색)으로 채우고 글자를 바탕색으로 뒤집는다. */
@Composable
private fun FilterPill(
    label: String,
    count: Int,
    dot: Color?,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val fillTarget = dot ?: MaterialTheme.colorScheme.onBackground
    val fill by animateColorAsState(if (selected) fillTarget else Color.Transparent)
    val textColor = if (selected) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.onBackground
    Row(
        modifier =
            Modifier
                .clip(PillShape)
                .background(fill)
                .border(1.dp, if (selected) fill else MaterialTheme.colorScheme.outline, PillShape)
                .haetiClickable(onClick = onClick)
                .padding(horizontal = 14.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (dot != null && !selected) {
            Spacer(Modifier.size(7.dp).background(dot, CircleShape))
            Spacer(Modifier.width(7.dp))
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = textColor,
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) textColor.copy(alpha = 0.75f) else MaterialTheme.colorScheme.outlineVariant,
        )
    }
}
