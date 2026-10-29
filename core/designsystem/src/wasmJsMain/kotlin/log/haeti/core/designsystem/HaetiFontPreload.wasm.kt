package log.haeti.core.designsystem

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import log.haeti.core.designsystem.generated.resources.Res
import log.haeti.core.designsystem.generated.resources.jetbrains_mono_regular
import log.haeti.core.designsystem.generated.resources.pretendard_bold
import log.haeti.core.designsystem.generated.resources.pretendard_regular
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.preloadFont

/** 세 폰트를 프리로드하고 전부 도착했을 때만 true. 도착 시 자동 리컴포지션된다. */
@OptIn(ExperimentalResourceApi::class)
@Composable
actual fun rememberHaetiFontsReady(): Boolean {
    val regular by preloadFont(Res.font.pretendard_regular)
    val bold by preloadFont(Res.font.pretendard_bold)
    val mono by preloadFont(Res.font.jetbrains_mono_regular)
    return regular != null && bold != null && mono != null
}
