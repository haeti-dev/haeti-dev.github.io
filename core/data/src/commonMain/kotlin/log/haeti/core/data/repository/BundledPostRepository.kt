package log.haeti.core.data.repository

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import log.haeti.core.data.parser.PostParser
import log.haeti.core.data.source.BundledPostSource
import log.haeti.core.model.Language
import log.haeti.core.model.Post
import log.haeti.core.model.PostMeta

/** 디렉터리명(`yyyy-MM-dd-slug`)에서 날짜 접두사가 차지하는 길이. */
private const val DATE_PREFIX_LENGTH = 11

/**
 * [items]를 동시에 읽는다. 웹에서는 파일 하나가 요청 하나라, 차례로 읽으면 글 수만큼 기다린다.
 * 결과 순서는 [items]와 같다.
 */
private suspend fun <T, R> readAll(
    items: List<T>,
    read: suspend (T) -> R,
): List<R> = coroutineScope { items.map { item -> async { read(item) } }.awaitAll() }

/**
 * 번들 리소스에서 글을 읽는 [PostRepository].
 *
 * 글은 빌드에 고정돼 있으므로 언어별로 최초 1회만 파싱해 메모리에 캐시한다.
 */
@Inject
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class BundledPostRepository(
    private val source: BundledPostSource,
) : PostRepository {
    private val loadMutex = Mutex()
    private val cache = mutableMapOf<Language, List<Post>>()

    override fun observePosts(language: Language): Flow<List<Post>> = flow { emit(loadedPosts(language)) }

    override fun observePost(
        slug: String,
        language: Language,
    ): Flow<Post?> =
        flow {
            emit(loadedPosts(language).firstOrNull { it.meta.slug == slug })
        }

    override suspend fun readPostAsset(
        post: PostMeta,
        relativePath: String,
    ): ByteArray? = source.readAsset(post.directoryName, relativePath)

    /** 언어별로 한 번만 번들을 읽는다. 동시 호출은 [loadMutex]로 한 번의 로드에 합친다. */
    private suspend fun loadedPosts(language: Language): List<Post> {
        cache[language]?.let { return it }
        return loadMutex.withLock { cache[language] ?: load(language) }
    }

    /**
     * 원문 목록을 만들고, 다른 언어면 번역본이 있는 글만 덮어쓴다. 없는 글은 원문 그대로 남긴다.
     * 원문을 하나라도 못 읽었으면(일시적인 네트워크 실패 등) 캐시하지 않아 다음 구독에서 다시 읽는다.
     */
    private suspend fun load(language: Language): List<Post> {
        val (originals, complete) = cache[Language.KO]?.let { it to true } ?: readOriginals()
        val posts =
            if (language == Language.KO) originals else readAll(originals) { translationOrNull(it, language) ?: it }
        if (complete) {
            cache[Language.KO] = originals
            cache[language] = posts
        }
        return posts
    }

    /** 인덱스의 글을 읽어 발행 글만 최신순으로 돌려준다. 두 번째 값은 원문을 빠짐없이 읽었는지 여부. */
    private suspend fun readOriginals(): Pair<List<Post>, Boolean> {
        val raws = readAll(source.readIndex()) { directory -> directory to source.readMarkdown(directory) }
        val posts =
            raws
                .mapNotNull { (directory, raw) -> raw?.let { parseOrNull(it, directory) } }
                .filterNot { it.meta.draft }
                .sortedByDescending { it.meta.date }
        return posts to raws.all { (_, raw) -> raw != null }
    }

    /** 글 하나를 파싱한다. 프론트매터가 깨졌으면 null. 한 편 때문에 목록 전체를 잃지 않게 한다. */
    private fun parseOrNull(
        raw: String,
        directory: String,
    ): Post? = runCatching { PostParser.parse(raw = raw, slug = directory.drop(DATE_PREFIX_LENGTH)) }.getOrNull()

    /** [post]의 번역본을 읽는다. 없거나 깨졌으면 null. */
    private suspend fun translationOrNull(
        post: Post,
        language: Language,
    ): Post? {
        val raw = source.readMarkdown(post.meta.directoryName, language) ?: return null
        return runCatching { PostParser.parseTranslation(raw, post, language) }.getOrNull()
    }
}
