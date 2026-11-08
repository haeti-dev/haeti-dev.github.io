package log.haeti.core.markdown

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.use
import log.haeti.core.markdown.model.ImageLoad
import org.jetbrains.skia.Bitmap
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

/** 이미지를 읽지 못하면 읽는 동안 잡아 둔 자리를 그대로 쓰면서 안내를 그려야 한다. */
@OptIn(ExperimentalComposeUiApi::class)
class ImageFailedPlaceholderTest {
    @Test
    fun 실패한_이미지는_읽는_중과_같은_높이를_차지한다() {
        // 폭 400에서 그림 자리 높이: 기록된 4:1이면 100, 기록이 없으면 16:9로 225.
        val cases = mapOf("![고양이](images/a.png){size=400x100}" to 100, "![고양이](images/a.png)" to 225)
        for ((markdown, imageHeight) in cases) {
            val loading = render(markdown, ImageLoad.Loading)
            val failed = render(markdown, ImageLoad.Failed)

            // 본문이 그려졌는지 먼저 본다. 둘 다 0이어도 같다고 통과하면 안 된다.
            assertTrue(loading.height >= imageHeight, "$markdown: ${loading.height}")
            assertEquals(loading.height, failed.height, markdown)
        }
    }

    @Test
    fun 실패한_자리에는_안내를_그린다() {
        val markdown = "![고양이](images/a.png){size=400x200}"

        val loading = render(markdown, ImageLoad.Loading)
        val failed = render(markdown, ImageLoad.Failed)

        assertNotEquals(loading.pixels.toList(), failed.pixels.toList(), "실패하면 빈 자리와 달라야 한다")
    }

    private class Rendered(
        val height: Int,
        val pixels: IntArray,
    )

    private fun render(
        markdown: String,
        load: ImageLoad,
    ): Rendered {
        var height = -1
        val pixels =
            ImageComposeScene(width = WIDTH, height = 600, density = Density(1f)) {
                // 렌더러는 평소 마크다운을 백그라운드 스레드에서 파싱한다. 가상 시계는 그 스레드를 기다리지 않아
                // 느린 CI에서는 파싱 전에 높이를 잴 수 있다. 검사 모드에서는 파싱을 동기로 해서 결과가 매번 같다.
                CompositionLocalProvider(LocalInspectionMode provides true) {
                    MaterialTheme {
                        Box(Modifier.width(WIDTH.dp).onSizeChanged { height = it.height }) {
                            MarkdownBody(markdown, imageResolver = { load })
                        }
                    }
                }
            }.use { scene ->
                scene.render(0)
                val bitmap = Bitmap.makeFromImage(scene.render(1_000))
                IntArray(WIDTH * 300) { bitmap.getColor(it % WIDTH, it / WIDTH) }
            }
        return Rendered(height, pixels)
    }

    private companion object {
        const val WIDTH = 400
    }
}
