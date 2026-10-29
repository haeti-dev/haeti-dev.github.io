package log.haeti.core.designsystem.interaction

import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.Clipboard
import androidx.compose.ui.platform.LocalClipboard
import kotlinx.coroutines.CancellationException

/**
 * 드래그로 글자를 골라 ⌘C/Ctrl+C로 복사할 수 있는 영역. 캔버스라 브라우저의 텍스트 선택이 없어서 직접 연다.
 * 안쪽에는 복사가 거부돼도 앱이 멈추지 않는 클립보드를 내려 준다.
 *
 * @param content 고를 수 있게 할 내용
 */
@Composable
fun SelectableArea(content: @Composable () -> Unit) {
    val clipboard = LocalClipboard.current
    val safeClipboard = remember(clipboard) { SafeClipboard(clipboard) }
    CompositionLocalProvider(LocalClipboard provides safeClipboard) {
        SelectionContainer(content = content)
    }
}

/**
 * 복사가 거부돼도 앱이 죽지 않게 감싼 클립보드.
 *
 * 웹에서 텍스트를 복사하면 Compose가 `navigator.clipboard.write`를 기다리는데, 브라우저가 권한을 거부하면
 * (권한 정책·임베드된 창·일부 Safari 설정) 그 예외가 그대로 터져 화면 전체가 멈춘다. 복사만 실패하게 삼킨다.
 */
private class SafeClipboard(
    private val delegate: Clipboard,
) : Clipboard by delegate {
    override suspend fun setClipEntry(clipEntry: ClipEntry?) {
        try {
            delegate.setClipEntry(clipEntry)
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Throwable) {
            // 권한 거부 등: 이번 복사만 포기한다.
        }
    }
}
