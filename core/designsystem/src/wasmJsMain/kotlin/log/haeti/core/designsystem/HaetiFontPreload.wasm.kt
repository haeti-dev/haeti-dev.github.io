package log.haeti.core.designsystem

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.preloadFont

/** 세 폰트를 프리로드하고 전부 도착했을 때만 true. 도착 시 자동 리컴포지션된다. */
@OptIn(ExperimentalResourceApi::class)
@Composable
actual fun rememberHaetiFontsReady(): Boolean {
    val regular by preloadFont(HaetiFontFiles.regular)
    val bold by preloadFont(HaetiFontFiles.bold)
    val mono by preloadFont(HaetiFontFiles.mono)
    return regular != null && bold != null && mono != null
}
