package log.haeti.feature.home

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentMapOf
import log.haeti.core.model.CategoryCount
import log.haeti.core.model.PostMeta

/**
 * 홈 화면 상태. 로딩·성공·실패 세 갈래다.
 */
sealed interface HomeUiState {
    /** 글 목록을 아직 받지 못했다. 목록 자리에 스켈레톤을 보여준다. */
    data object Loading : HomeUiState

    /**
     * 글 목록을 받았다. 빈 목록도 성공이다.
     *
     * @param featured 상단에 크게 보여줄 최신 글. 카테고리를 고른 동안에는 null
     * @param posts 목록에 보여줄 글(최신순). 전체 보기에서는 [featured]를 뺀 나머지다
     * @param categories 카테고리별 글 수(많은 순)
     * @param total 발행 글 전체 수
     * @param selectedCategory 고른 카테고리. null이면 전체
     * @param query 검색어. 비어 있으면 검색 중이 아니다
     * @param excerpts 본문에서만 찾은 글의 슬러그별 발췌
     */
    data class Success(
        val featured: PostMeta?,
        val posts: ImmutableList<PostMeta>,
        val categories: ImmutableList<CategoryCount>,
        val total: Int,
        val selectedCategory: String?,
        val query: String = "",
        val excerpts: ImmutableMap<String, String> = persistentMapOf(),
    ) : HomeUiState

    /** 글 목록을 불러오지 못했다. */
    data object Error : HomeUiState
}
