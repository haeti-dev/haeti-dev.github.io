package log.haeti.feature.post

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import log.haeti.core.markdown.ImageResolver
import log.haeti.core.markdown.model.ImageLoad
import log.haeti.core.markdown.rememberEncodedImagePainter
import kotlin.coroutines.cancellation.CancellationException

/**
 * 글 본문의 상대경로 이미지를 [readAsset]으로 읽어 그리는 resolver. 애니메이션 GIF·WebP는 재생한다.
 * 읽기에 실패하거나(파일 없음, 네트워크 끊김) 해석할 수 없는 바이트면 [ImageLoad.Failed]다.
 *
 * 한 번 읽은 바이트는 기억한다. 언어를 바꿔 본문을 다시 그릴 때 이미지가 잠깐 비면서
 * 본문이 짧아져 읽던 위치가 당겨지는 일을 막는다. 읽기 실패(null, 예외)는 기억하지 않아
 * 본문이 화면에 다시 들어오면 한 번 더 읽는다. 받았지만 해석할 수 없는 바이트는 다시 받아도 같으니 그대로 둔다.
 *
 * @param readAsset 글 디렉터리 기준 상대경로를 바이트로 읽는 함수. 없으면 null
 */
class PostImageResolver(
    private val readAsset: suspend (String) -> ByteArray?,
) : ImageResolver {
    private val loaded = mutableMapOf<String, ByteArray>()

    /** [path]의 바이트를 읽어 그린다. 처음 읽는 동안에는 [ImageLoad.Loading]이다. */
    @Composable
    override fun load(path: String): ImageLoad {
        // 키에 resolver를 넣는다. 글마다 이미지 이름이 겹치니(images/01.webp) 같은 자리에서 글이 바뀌면
        // 이전 글의 바이트나 실패를 이어받지 않고, 이전 글의 읽기도 취소해야 한다.
        var bytes by remember(this, path) { mutableStateOf(loaded[path]) }
        var failed by remember(this, path) { mutableStateOf(false) }
        LaunchedEffect(this, path) {
            if (bytes != null) return@LaunchedEffect
            val read = readOrNull(path)
            if (read == null) {
                failed = true
            } else {
                loaded[path] = read
                bytes = read
            }
        }
        val current = bytes ?: return if (failed) ImageLoad.Failed else ImageLoad.Loading
        return rememberEncodedImagePainter(current)?.let { ImageLoad.Loaded(it) } ?: ImageLoad.Failed
    }

    /** 취소는 삼키지 않는다. 삼키면 화면을 떠나며 끊긴 읽기가 실패로 보인다. */
    private suspend fun readOrNull(path: String): ByteArray? =
        try {
            readAsset(path)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            null
        }
}
