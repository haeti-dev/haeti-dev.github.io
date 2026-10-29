package log.haeti.core.designsystem

import androidx.compose.runtime.Composable

/**
 * 번들 폰트(Pretendard·JetBrains Mono)가 렌더링 가능 상태인지 반환한다.
 * 웹(wasm)은 폰트를 비동기로 받아오므로, false인 동안 본문 렌더를 미뤄야
 * 한글이 □(tofu)로 굳는 첫 프레임 레이스를 막을 수 있다. JVM은 항상 true.
 */
@Composable
expect fun rememberHaetiFontsReady(): Boolean
