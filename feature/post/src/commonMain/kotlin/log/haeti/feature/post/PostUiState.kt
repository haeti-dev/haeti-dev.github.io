package log.haeti.feature.post

import log.haeti.core.model.Post
import log.haeti.core.model.PostMeta

/**
 * 글 상세 화면 상태. "없는 글"은 실패가 아니라 정상적인 결과라서 [Error]와 따로 둔다.
 */
sealed interface PostUiState {
    /** 글을 아직 받지 못했다. 스켈레톤을 보여준다. */
    data object Loading : PostUiState

    /**
     * 글을 찾았다.
     *
     * @param post 본문까지 갖춘 글 한 편
     * @param newer 바로 다음에 쓴(더 최신) 글. 없으면 null
     * @param older 바로 앞에 쓴(더 오래된) 글. 없으면 null
     * @param translationMissing 요청한 언어의 번역본이 없어 원문을 보여주는 중인지
     */
    data class Success(
        val post: Post,
        val newer: PostMeta? = null,
        val older: PostMeta? = null,
        val translationMissing: Boolean = false,
    ) : PostUiState

    /** 슬러그에 해당하는 글이 없다. */
    data object NotFound : PostUiState

    /** 글을 불러오다 실패했다. */
    data object Error : PostUiState
}
