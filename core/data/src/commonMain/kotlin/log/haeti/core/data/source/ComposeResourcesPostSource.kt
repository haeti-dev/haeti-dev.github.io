package log.haeti.core.data.source

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.json.Json
import log.haeti.core.data.generated.resources.Res
import log.haeti.core.model.Language

/**
 * Compose 리소스 `files/posts` 아래에서 글을 읽는 [BundledPostSource].
 *
 * 리소스는 빌드 태스크 `generatePostsResources`가 `content/posts`에서 만들어 두며,
 * jvm·wasmJs 양쪽 모두 같은 경로로 번들에 실린다.
 */
@Inject
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class ComposeResourcesPostSource : BundledPostSource {
    private val json = Json { ignoreUnknownKeys = true }

    // 인덱스는 빌드가 항상 만든다. 못 읽었다면 일시적인 실패이므로 예외를 그대로 올려 화면이 오류 상태를 보이게 한다.
    override suspend fun readIndex(): List<String> =
        json.decodeFromString<List<String>>(Res.readBytes(INDEX_PATH).decodeToString())

    override suspend fun readMarkdown(
        directory: String,
        language: Language,
    ): String? {
        val fileName = if (language == Language.KO) MARKDOWN_FILE else "index.${language.code}.md"
        return readOrNull("$POSTS_ROOT/$directory/$fileName")?.decodeToString()
    }

    override suspend fun readAsset(
        directory: String,
        relativePath: String,
    ): ByteArray? = readOrNull("$POSTS_ROOT/$directory/$relativePath")

    /**
     * 없는 리소스는 예외를 던진다. 빠진 이미지 하나로 화면 전체가 죽지 않도록 null로 처리한다.
     * 취소는 삼키지 않는다. 삼키면 읽던 도중 취소된 결과가 "글 없음"으로 캐시된다.
     */
    private suspend fun readOrNull(path: String): ByteArray? =
        try {
            Res.readBytes(path)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            null
        }

    private companion object {
        const val POSTS_ROOT = "files/posts"
        const val INDEX_PATH = "$POSTS_ROOT/index.json"
        const val MARKDOWN_FILE = "index.md"
    }
}
