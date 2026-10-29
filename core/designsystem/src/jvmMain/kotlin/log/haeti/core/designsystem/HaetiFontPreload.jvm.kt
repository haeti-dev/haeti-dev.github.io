package log.haeti.core.designsystem

import androidx.compose.runtime.Composable

/** JVM은 폰트를 동기 로드하므로 항상 준비 상태다. */
@Composable
actual fun rememberHaetiFontsReady(): Boolean = true
