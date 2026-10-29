package log.haeti.core.ui.platform

import androidx.compose.runtime.Composable

/**
 * 외부 링크를 여는 방법. "링크를 연다"는 동작이 플랫폼마다 달라서 인터페이스로 분리한다
 * (브라우저는 새 탭, 데스크톱은 기본 브라우저).
 */
fun interface UrlOpener {
    /** [url]을 플랫폼 기본 방식으로 연다. 열 수 없으면 아무 일도 하지 않는다. */
    fun open(url: String)
}

/** 현재 플랫폼의 [UrlOpener]를 기억해 돌려준다. */
@Composable
expect fun rememberUrlOpener(): UrlOpener
