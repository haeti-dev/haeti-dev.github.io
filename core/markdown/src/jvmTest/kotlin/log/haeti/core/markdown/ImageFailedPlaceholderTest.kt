package log.haeti.core.markdown

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.use
import log.haeti.core.markdown.model.ImageLoad
import org.jetbrains.skia.Bitmap
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

/** 이미지를 읽지 못하면 읽는 동안 잡아 둔 자리를 그대로 쓰면서 안내를 그려야 한다. */
@OptIn(ExperimentalComposeUiApi::class)
class ImageFailedPlaceholderTest {
    @Test
    fun 실패한_이미지는_읽는_중과_같은_높이를_차지한다() {
        for (markdown in listOf("![고양이](images/a.png){size=400x100}", "![고양이](images/a.png)")) {
            val loading = render(markdown, ImageLoad.Loading)
            val failed = render(markdown, ImageLoad.Failed)

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
                MaterialTheme {
                    Box(Modifier.width(WIDTH.dp).onSizeChanged { height = it.height }) {
                        MarkdownBody(markdown, imageResolver = { load })
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
