package log.haeti.web

import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import dev.zacsweers.metro.createGraph
import kotlinx.browser.document
import kotlinx.browser.window
import log.haeti.core.navigation.Navigator
import log.haeti.core.navigation.path.backStackFor
import log.haeti.core.navigation.path.navKeyFromPath
import log.haeti.core.navigation.rememberNavigationState
import log.haeti.web.browser.BrowserHistorySync
import log.haeti.web.browser.LocalStorageThemePreference
import log.haeti.web.browser.applyDocumentLanguage
import log.haeti.web.browser.initialLanguage
import log.haeti.web.browser.resolveBasePath
import log.haeti.web.browser.saveLanguage
import log.haeti.web.di.WebAppGraph
import log.haeti.web.path.toAppPath
import log.haeti.web.path.toBrowserPath
import log.haeti.web.ui.HaetiAppShell
import log.haeti.web.ui.rememberThemeController
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.configureWebResources

/**
 * wasmJs 진입점. `<base href>`로 배포 접두사를 정한 뒤 `document.body`에 컴포지션을 마운트한다.
 *
 * Compose Resources가 요청하는 경로는 `composeResources/...` 같은 상대 경로다. 딥링크
 * (`/post/<slug>`)로 들어온 상태에서 그대로 두면 `/post/composeResources/...`로 해석돼 404가 나므로
 * 접두사를 붙여 항상 번들 루트를 가리키게 만든다.
 */
@OptIn(ExperimentalComposeUiApi::class, ExperimentalResourceApi::class)
fun main() {
    val basePath = resolveBasePath()
    configureWebResources { resourcePathMapping { path -> basePath.toBrowserPath(path) } }

    val appGraph = createGraph<WebAppGraph>()
    appGraph.userPreferences.setLanguage(initialLanguage())
    val initialKey = navKeyFromPath(basePath.toAppPath(window.location.pathname))

    ComposeViewport(document.body!!) {
        val navigationState = rememberNavigationState(backStackFor(initialKey))
        val navigator = remember(navigationState) { Navigator(navigationState) }
        val themeController = rememberThemeController(LocalStorageThemePreference)

        BrowserHistorySync(basePath, navigationState, navigator)
        HaetiAppShell(
            appGraph = appGraph,
            navigationState = navigationState,
            navigator = navigator,
            themeController = themeController,
            onTitleChanged = { title -> document.title = title },
            onLanguageChanged = ::applyDocumentLanguage,
            onLanguageSelected = ::saveLanguage,
        )
    }
}
