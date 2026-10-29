package log.haeti.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import log.haeti.core.designsystem.interaction.haetiClickable
import log.haeti.core.designsystem.interaction.rememberHoverBackground
import log.haeti.core.ui.strings.LocalHaetiStrings

private val ToggleHitBox = 36.dp
private val ToggleRadius = 18.dp

/** 다크모드 토글. 해/달 아이콘, 28dp 히트박스, 스크린리더용 semantics 설명 포함. */
@Composable
internal fun ThemeToggle(
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val hoverBackground by rememberHoverBackground(interactionSource)
    val description = LocalHaetiStrings.current.toggleTheme
    Box(
        modifier =
            Modifier
                .size(ToggleHitBox)
                .clip(RoundedCornerShape(ToggleRadius))
                .background(hoverBackground)
                .haetiClickable(onClick = onToggleTheme, interactionSource = interactionSource)
                .semantics { contentDescription = description },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = if (isDarkTheme) Icons.Outlined.LightMode else Icons.Outlined.DarkMode,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.size(18.dp),
        )
    }
}
