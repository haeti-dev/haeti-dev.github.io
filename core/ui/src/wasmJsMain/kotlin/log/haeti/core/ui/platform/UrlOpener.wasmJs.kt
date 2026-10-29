package log.haeti.core.ui.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.browser.window
import kotlin.js.ExperimentalWasmJsInterop

/** 브라우저 새 탭으로 연다. 팝업 차단 시 `window.open`이 null을 돌려주며 그대로 무시된다. */
@OptIn(ExperimentalWasmJsInterop::class)
@Composable
actual fun rememberUrlOpener(): UrlOpener =
    remember {
        UrlOpener { url -> window.open(url, "_blank") }
    }
