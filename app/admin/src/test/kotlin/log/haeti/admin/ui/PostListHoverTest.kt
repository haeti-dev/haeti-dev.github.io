package log.haeti.admin.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.use
import log.haeti.admin.model.PostEntry
import log.haeti.core.designsystem.HaetiTheme
import org.jetbrains.skia.Bitmap
import java.nio.file.Paths
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** 사이드바 글 목록에서 마우스를 옮기면 호버 배경은 지금 가리킨 항목 하나에만 있어야 한다. */
@OptIn(ExperimentalComposeUiApi::class)
class PostListHoverTest {
    private val first = PostEntry("2026-01-01-a", Paths.get("a"), title = "A")
    private val second = PostEntry("2026-01-02-b", Paths.get("b"), title = "B")

    @Test
    fun 다음_항목으로_옮기면_이전_항목의_호버_배경이_바로_사라진다() {
        var surface = Color.Unspecified
        var hover = Color.Unspecified
        ImageComposeScene(width = 400, height = 200, density = Density(1f)) {
            HaetiTheme(darkTheme = false) {
                surface = MaterialTheme.colorScheme.surface
                hover = MaterialTheme.colorScheme.surfaceVariant
                Column(Modifier.fillMaxSize().background(surface)) {
                    PostListItem(first, isSelected = false, isTranslating = false, onClick = {
                    }, onTranslate = {}, modifier = Modifier.height(100.dp))
                    PostListItem(second, isSelected = false, isTranslating = false, onClick = {
                    }, onTranslate = {}, modifier = Modifier.height(100.dp))
                }
            }
        }.use { scene ->
            scene.render(0)
            scene.sendPointerEvent(PointerEventType.Move, Offset(200f, 50f))
            scene.renderUntil(fromMs = 0, toMs = 600) // A에 완전히 호버
            assertEquals(hover.toArgb(), scene.pixelAt(300, 90, 600))

            scene.sendPointerEvent(PointerEventType.Move, Offset(200f, 150f))
            scene.renderUntil(fromMs = 600, toMs = 630) // B로 옮긴 지 30ms
            assertEquals(surface.toArgb(), scene.pixelAt(300, 90, 630), "이전 항목(A)은 바로 비어야 한다")
            val fadingIn = Color(scene.pixelAt(300, 190, 630))
            assertTrue(fadingIn.red >= hover.red - 0.01f, "켜지는 항목(B)이 호버색보다 어두워지면 안 된다: $fadingIn")
        }
    }

    /** 16ms 간격으로 프레임을 그려 애니메이션을 진행시킨다. */
    private fun ImageComposeScene.renderUntil(
        fromMs: Long,
        toMs: Long,
    ) {
        var t = fromMs
        while (t < toMs) {
            render(t * 1_000_000)
            t += 16
        }
    }

    private fun ImageComposeScene.pixelAt(
        x: Int,
        y: Int,
        timeMs: Long,
    ): Int = Bitmap.makeFromImage(render(timeMs * 1_000_000)).getColor(x, y)
}
