package log.haeti.core.markdown

import kotlinx.browser.window
import kotlin.js.ExperimentalWasmJsInterop

/**
 * 브라우저 Clipboard API(`navigator.clipboard.writeText`)로 복사한다.
 * 반환되는 Promise는 UI가 이미 "복사됨"으로 낙관적 표시를 하므로 결과를 기다리지 않고 무시한다.
 */
@OptIn(ExperimentalWasmJsInterop::class)
internal actual fun copyToClipboard(text: String) {
    window.navigator.clipboard.writeText(text)
}
