package log.haeti.feature.post

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.use
import kotlinx.coroutines.CompletableDeferred
import log.haeti.core.markdown.model.ImageLoad
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.io.IOException
import javax.imageio.ImageIO
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

/** 글 본문 이미지는 읽는 중, 성공, 실패를 구분해야 실패한 자리에 안내를 보일 수 있다. */
@OptIn(ExperimentalComposeUiApi::class)
class PostImageResolverTest {
    @Test
    fun 읽는_동안에는_Loading이고_다_읽으면_Loaded다() {
        val arrived = CompletableDeferred<ByteArray?>()
        val states = mutableListOf<ImageLoad>()

        scene(PostImageResolver { arrived.await() }, states).use { scene ->
            scene.render(0)
            assertEquals(ImageLoad.Loading, states.last())
            arrived.complete(png())
            scene.render(100)
            assertIs<ImageLoad.Loaded>(states.last())
        }
    }

    @Test
    fun 없는_파일_읽기_예외_해석할_수_없는_바이트는_Failed다() {
        val readers: List<suspend (String) -> ByteArray?> =
            listOf({ null }, { throw IOException("끊김") }, { "not an image".encodeToByteArray() })

        for (reader in readers) {
            val states = mutableListOf<ImageLoad>()
            scene(PostImageResolver(reader), states).use { scene ->
                scene.render(0)
                scene.render(100)
                assertEquals(ImageLoad.Failed, states.last())
            }
        }
    }

    private fun scene(
        resolver: PostImageResolver,
        states: MutableList<ImageLoad>,
    ) = ImageComposeScene(width = 10, height = 10) { states += resolver.load("images/a.png") }

    private fun png(): ByteArray =
        ByteArrayOutputStream()
            .also { ImageIO.write(BufferedImage(4, 3, BufferedImage.TYPE_INT_RGB), "png", it) }
            .toByteArray()
}
