package log.haeti.admin

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import dev.zacsweers.metro.createGraph
import log.haeti.admin.di.AdminAppGraph
import log.haeti.admin.io.readClipboardImages
import log.haeti.admin.ui.AdminApp
import log.haeti.admin.ui.isPasteShortcut
import log.haeti.admin.ui.isSaveShortcut
import log.haeti.core.designsystem.HaetiTheme
import log.haeti.core.designsystem.rememberHaetiFontsReady

private const val WINDOW_WIDTH_DP = 1440
private const val WINDOW_HEIGHT_DP = 900

/** admin 데스크톱 앱 진입점. Metro 그래프를 만든 뒤 1440x900 창을 열고 [AdminApp]을 그린다. */
fun main() {
    val graph = createGraph<AdminAppGraph>()
    application {
        val viewModel = graph.viewModel
        Window(
            // 저장 안 된 글이 있으면 저장한 뒤 닫는다. 저장에 실패하면 닫지 않고 안내를 띄운다.
            onCloseRequest = { viewModel.saveThen(::exitApplication) },
            title = windowTitle(viewModel),
            state = rememberWindowState(size = DpSize(WINDOW_WIDTH_DP.dp, WINDOW_HEIGHT_DP.dp)),
            onPreviewKeyEvent = { event -> handleShortcut(event, viewModel) },
        ) {
            val systemDark = isSystemInDarkTheme()
            var isDark by remember { mutableStateOf(systemDark) }
            HaetiTheme(darkTheme = isDark) {
                // 웹과 같은 번들 폰트가 준비되기 전 첫 프레임을 비워 둬 글꼴이 바뀌며 깜빡이지 않게 한다.
                if (rememberHaetiFontsReady()) {
                    AdminApp(viewModel, isDarkTheme = isDark, onToggleTheme = {
                        isDark =
                            !isDark
                    })
                }
            }
        }
    }
}

private fun handleShortcut(
    event: KeyEvent,
    viewModel: AdminViewModel,
): Boolean {
    if (event.isSaveShortcut()) {
        viewModel.save()
        return true
    }
    // 클립보드에 이미지(스크린샷·Finder에서 복사한 파일)가 있을 때만 가로챈다. 글자는 에디터가 평소대로 붙여넣는다.
    if (event.isPasteShortcut() && viewModel.selected != null) {
        val images = readClipboardImages() ?: return false
        viewModel.insertImages(images)
        return true
    }
    return false
}

private fun windowTitle(viewModel: AdminViewModel): String {
    val base = "Haeti.log Admin"
    val withPost = viewModel.selected?.let { "$base | ${it.dirName}" } ?: base
    return if (viewModel.isDirty) "• $withPost" else withPost
}
