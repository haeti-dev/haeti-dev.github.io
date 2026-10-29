package log.haeti.web.ui

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith

/** 새 화면이 나타나는 시간. */
private const val SCREEN_FADE_IN_MS = 150

/**
 * 화면 전환. 이전 화면은 바로 감추고 새 화면만 짧게 나타나게 한다.
 * 화면들은 바탕이 투명해서, 전환 중 두 화면이 함께 보이면 글자가 겹쳐 지지직거려 보인다.
 */
internal fun screenTransition(): ContentTransform = fadeIn(tween(SCREEN_FADE_IN_MS)) togetherWith fadeOut(snap())
