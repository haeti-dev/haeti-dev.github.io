package log.haeti.core.designsystem.interaction

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer

/** 준비된 내용이 나타나는 시간. 깜빡임만 가릴 만큼 짧게 둔다. */
private const val FADE_IN_MS = 180

/**
 * 처음 그려질 때 투명에서 불투명으로 짧게 나타난다. 이미지·글처럼 늦게 도착한 내용이 툭 튀어나오지 않게 한다.
 * [key]가 그대로면 다시 페이드하지 않는다(같은 글에서 언어만 바꿀 때 등).
 *
 * @param key 이 값이 바뀔 때만 다시 나타난다
 * @param enabled false면 바로 보인다(캐시에 있어 이미 보였던 내용)
 */
@Composable
fun Modifier.fadeInOnAppear(
    key: Any?,
    enabled: Boolean = true,
): Modifier {
    val alpha = remember(key) { Animatable(if (enabled) 0f else 1f) }
    LaunchedEffect(key) { alpha.animateTo(1f, tween(FADE_IN_MS)) }
    return graphicsLayer { this.alpha = alpha.value }
}
