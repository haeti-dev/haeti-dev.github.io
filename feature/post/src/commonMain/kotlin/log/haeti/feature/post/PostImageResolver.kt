package log.haeti.feature.post

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.painter.Painter
import log.haeti.core.markdown.ImageResolver
import log.haeti.core.markdown.rememberEncodedImagePainter

/**
 * 글 본문의 상대경로 이미지를 [readAsset]으로 읽어 [Painter]로 바꾸는 resolver. 애니메이션 GIF·WebP는 재생한다.
 * 읽기 실패(파일 없음 등)에는 null을 돌려줘 마크다운 렌더러가 이미지를 건너뛰게 한다.
 *
 * 한 번 읽은 바이트는 기억한다. 언어를 바꿔 본문을 다시 그릴 때 이미지가 잠깐 비면서
 * 본문이 짧아져 읽던 위치가 당겨지는 일을 막는다.
 *
 * @param readAsset 글 디렉터리 기준 상대경로를 바이트로 읽는 함수
 */
class PostImageResolver(
    private val readAsset: suspend (String) -> ByteArray?,
) : ImageResolver {
    private val loaded = mutableMapOf<String, ByteArray>()

    /** [path]의 바이트를 읽어 그린다. 처음 읽는 동안에는 null이다. */
    @Composable
    override fun painterFor(path: String): Painter? {
        val bytes = remember(path) { mutableStateOf(loaded[path]) }
        LaunchedEffect(path) {
            if (bytes.value != null) return@LaunchedEffect
            bytes.value = runCatching { readAsset(path) }.getOrNull()?.also { loaded[path] = it }
        }
        return bytes.value?.let { rememberEncodedImagePainter(it) }
    }
}
