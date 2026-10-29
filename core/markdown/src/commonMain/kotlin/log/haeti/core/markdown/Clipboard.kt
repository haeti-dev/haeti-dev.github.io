package log.haeti.core.markdown

/**
 * 텍스트를 시스템/브라우저 클립보드에 복사한다. 플랫폼 구현은 `wasmJsMain`(브라우저 Clipboard API)과
 * `jvmMain`(AWT Toolkit)의 actual을 참고.
 *
 * @param text 복사할 텍스트
 */
internal expect fun copyToClipboard(text: String)
