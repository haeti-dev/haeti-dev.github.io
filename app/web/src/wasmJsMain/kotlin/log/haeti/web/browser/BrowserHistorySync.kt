package log.haeti.web.browser

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import kotlinx.browser.window
import log.haeti.core.navigation.NavigationState
import log.haeti.core.navigation.Navigator
import log.haeti.core.navigation.NotFoundNavKey
import log.haeti.core.navigation.path.HistoryUpdate
import log.haeti.core.navigation.path.backStackFor
import log.haeti.core.navigation.path.historyUpdateFor
import log.haeti.core.navigation.path.navKeyFromPath
import log.haeti.core.navigation.path.toPath
import log.haeti.web.path.BasePath
import log.haeti.web.path.toAppPath
import log.haeti.web.path.toBrowserPath
import org.w3c.dom.events.Event
import kotlin.js.ExperimentalWasmJsInterop

/**
 * 백스택과 브라우저 히스토리를 양방향으로 묶는다.
 *
 * 앱이 화면을 옮기면 주소창에 `pushState`로 반영하고, 사용자가 뒤로·앞으로 가면
 * `popstate`를 받아 백스택을 통째로 교체한다. 주소가 이미 같은 화면을 가리키면 항목을 쌓지 않아
 * 두 방향이 서로를 다시 호출하는 무한 루프와, 외부 유입 때 뒤로가기가 한 번 헛도는 일을 막는다.
 *
 * @param basePath 배포 경로 접두사
 * @param navigationState 현재 백스택
 * @param navigator 백스택 교체 명령 수신자
 */
@OptIn(ExperimentalWasmJsInterop::class)
@Composable
fun BrowserHistorySync(
    basePath: BasePath,
    navigationState: NavigationState,
    navigator: Navigator,
) {
    val currentKey = navigationState.currentKey
    LaunchedEffect(currentKey) {
        // 알 수 없는 경로는 사용자가 입력한 주소를 그대로 둔다. `/404`로 바꿔 쓰면 히스토리만 늘어난다.
        if (currentKey == NotFoundNavKey) return@LaunchedEffect
        val target = basePath.toBrowserPath(currentKey.toPath())
        when (historyUpdateFor(basePath.toAppPath(window.location.pathname), currentKey)) {
            HistoryUpdate.None -> Unit
            // 같은 화면이면 쿼리와 해시는 살려 두고 경로 모양만 고친다.
            HistoryUpdate.Replace ->
                window.history.replaceState(null, "", target + window.location.search + window.location.hash)
            HistoryUpdate.Push -> window.history.pushState(null, "", target)
        }
    }
    DisposableEffect(basePath, navigator) {
        val listener: (Event) -> Unit = {
            val key = navKeyFromPath(basePath.toAppPath(window.location.pathname))
            navigator.replaceAll(backStackFor(key))
        }
        window.addEventListener("popstate", listener)
        onDispose { window.removeEventListener("popstate", listener) }
    }
}
