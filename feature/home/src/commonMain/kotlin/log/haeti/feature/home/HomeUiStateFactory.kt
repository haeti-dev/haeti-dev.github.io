package log.haeti.feature.home

import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap
import log.haeti.core.model.CategoryCount
import log.haeti.core.model.Post

/**
 * 최신순 글 목록과 고른 카테고리·검색어로 [HomeUiState.Success]를 만든다.
 *
 * 전체 보기에서는 최신 글 하나를 [HomeUiState.Success.featured]로 떼어 내고 나머지를 목록에 둔다.
 * 카테고리를 고르거나 검색 중이면 강조 없이 걸러진 글만 목록에 둔다. 없는 카테고리를 고른 상태라면 전체로 되돌린다.
 *
 * @param index 발행 글(최신순)과 검색 색인
 * @param selectedCategory 고른 카테고리. null이면 전체
 * @param query 검색어. 비어 있으면 검색하지 않는다
 */
internal fun homeUiStateOf(
    index: SearchIndex,
    selectedCategory: String?,
    query: String = "",
): HomeUiState.Success {
    val posts = index.posts
    val metas = posts.map(Post::meta)
    val categories =
        metas
            .groupingBy { it.category }
            .eachCount()
            .map { (name, count) -> CategoryCount(name, count) }
            .sortedWith(compareByDescending<CategoryCount> { it.count }.thenBy { it.name })
    val selected = selectedCategory?.takeIf { name -> categories.any { it.name == name } }
    val inCategory = if (selected == null) posts else posts.filter { it.meta.category == selected }
    val searching = query.isNotBlank()
    val hits = if (searching) searchPosts(index, query, selected) else emptyList()
    val featured = if (selected == null && !searching) metas.firstOrNull() else null
    val listed =
        when {
            searching -> hits.map { it.post.meta }
            selected == null -> metas.drop(1)
            else -> inCategory.map(Post::meta)
        }
    return HomeUiState.Success(
        featured = featured,
        posts = listed.toImmutableList(),
        categories = categories.toImmutableList(),
        total = metas.size,
        selectedCategory = selected,
        query = query,
        excerpts = hits.mapNotNull { hit -> hit.excerpt?.let { hit.post.meta.slug to it } }.toMap().toImmutableMap(),
    )
}
