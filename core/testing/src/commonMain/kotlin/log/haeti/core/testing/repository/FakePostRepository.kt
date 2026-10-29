package log.haeti.core.testing.repository

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.map
import log.haeti.core.data.repository.PostRepository
import log.haeti.core.model.Language
import log.haeti.core.model.Post
import log.haeti.core.model.PostMeta

/**
 * 테스트가 글 목록과 에셋을 직접 넣어 주는 Fake [PostRepository].
 *
 * 목록 플로는 `replay = 1`이라 구독 전에 보낸 값도 그대로 받는다.
 */
class FakePostRepository : PostRepository {
    private val postsFlow: MutableSharedFlow<List<Post>> =
        MutableSharedFlow(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    private val assets = mutableMapOf<String, ByteArray>()

    override fun observePosts(language: Language): Flow<List<Post>> = postsFlow

    override fun observePost(
        slug: String,
        language: Language,
    ): Flow<Post?> =
        postsFlow.map { posts ->
            posts.find { it.meta.slug == slug }
        }

    override suspend fun readPostAsset(
        post: PostMeta,
        relativePath: String,
    ): ByteArray? = assets[assetKey(post, relativePath)]

    /** 테스트에서 글 목록을 방출한다. */
    fun sendPosts(posts: List<Post>) {
        postsFlow.tryEmit(posts)
    }

    /** 테스트에서 글 디렉터리 기준 상대경로에 에셋을 심는다. */
    fun putAsset(
        post: PostMeta,
        path: String,
        bytes: ByteArray,
    ) {
        assets[assetKey(post, path)] = bytes
    }

    private fun assetKey(
        post: PostMeta,
        path: String,
    ): String = "${post.directoryName}/$path"
}
