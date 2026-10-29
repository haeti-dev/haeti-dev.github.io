package log.haeti.core.data.repository

import kotlinx.coroutines.flow.Flow
import log.haeti.core.model.Language
import log.haeti.core.model.Post
import log.haeti.core.model.PostMeta

/**
 * 발행 글을 읽어오는 단일 진입점. 초안(`draft: true`)은 절대 노출하지 않는다.
 */
interface PostRepository {
    /**
     * 발행 글 전체를 최신순으로 방출한다.
     *
     * @param language 보고 싶은 언어. 번역본이 없는 글은 원문(한국어)으로 담긴다
     */
    fun observePosts(language: Language = Language.KO): Flow<List<Post>>

    /**
     * 슬러그로 글 하나를 방출한다. 없으면 null.
     *
     * @param language 보고 싶은 언어. 번역본이 없으면 원문(한국어)
     */
    fun observePost(
        slug: String,
        language: Language = Language.KO,
    ): Flow<Post?>

    /** 글 디렉터리 기준 상대경로의 에셋(이미지 등)을 읽는다. 없으면 null. */
    suspend fun readPostAsset(
        post: PostMeta,
        relativePath: String,
    ): ByteArray?
}
