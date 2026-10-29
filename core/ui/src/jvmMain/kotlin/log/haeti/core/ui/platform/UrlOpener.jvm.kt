package log.haeti.core.ui.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import java.awt.Desktop
import java.net.URI

/**
 * 데스크톱 기본 브라우저로 연다. 헤드리스 환경처럼 브라우저 실행을 지원하지 않는 곳에서는
 * 예외를 삼키고 조용히 무시한다.
 */
@Composable
actual fun rememberUrlOpener(): UrlOpener =
    remember {
        UrlOpener { url ->
            runCatching {
                if (Desktop.isDesktopSupported() &&
                    Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)
                ) {
                    Desktop.getDesktop().browse(URI(url))
                }
            }
        }
    }
