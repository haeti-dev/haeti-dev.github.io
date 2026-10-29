package log.haeti.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import log.haeti.core.designsystem.component.SkeletonBlock
import log.haeti.core.ui.component.CategoryFilterRow
import log.haeti.core.ui.component.FeaturedPost
import log.haeti.core.ui.component.PostListRow
import log.haeti.core.ui.component.SectionHeading
import log.haeti.core.ui.layout.LocalLayoutTier
import log.haeti.core.ui.layout.isCompact
import log.haeti.core.ui.strings.LocalHaetiStrings

/**
 * 소개 아래의 글 영역. 상태에 따라 스켈레톤·안내·최신 글과 목록을 그린다.
 *
 * @param uiState 홈 상태
 * @param onPostClick 글 클릭 콜백
 * @param onSelectCategory 카테고리 필터 클릭 콜백
 * @param onSearch 검색어 변경 콜백
 * @param minListHeight 목록 영역의 최소 높이. 보통 화면 높이를 넘긴다
 */
@Composable
internal fun ColumnScope.HomeFeed(
    uiState: HomeUiState,
    onPostClick: (String) -> Unit,
    onSelectCategory: (String?) -> Unit,
    onSearch: (String) -> Unit,
    minListHeight: Dp = 0.dp,
) {
    when (uiState) {
        HomeUiState.Loading -> FeedSkeleton()
        HomeUiState.Error -> FeedMessage(LocalHaetiStrings.current.feedError)
        is HomeUiState.Success -> FeedContent(uiState, onPostClick, onSelectCategory, onSearch, minListHeight)
    }
}

/**
 * 검색·필터 → (전체 보기면) 최신 글 → 섹션 제목 → 목록 순서. 글이 하나도 없으면 안내만 보여준다.
 *
 * 검색·필터로 바뀌는 것은 모두 컨트롤 아래에 있어 검색창이 제자리에 머문다. 결과가 적어도 목록 영역이
 * [minListHeight]만큼은 차지해, 페이지가 짧아지며 스크롤이 당겨져 검색창이 튀는 일도 없다.
 */
@Composable
private fun FeedContent(
    state: HomeUiState.Success,
    onPostClick: (String) -> Unit,
    onSelectCategory: (String?) -> Unit,
    onSearch: (String) -> Unit,
    minListHeight: Dp,
) {
    val compact = LocalLayoutTier.current.isCompact
    val strings = LocalHaetiStrings.current
    if (state.total == 0) {
        FeedMessage(strings.feedEmpty)
        return
    }
    SearchField(query = state.query, onQueryChange = onSearch)
    Spacer(Modifier.height(12.dp))
    CategoryFilterRow(
        total = state.total,
        categories = state.categories,
        selected = state.selectedCategory,
        onSelect = onSelectCategory,
    )
    Spacer(Modifier.height(if (compact) 40.dp else 56.dp))
    FeedList(state = state, compact = compact, onPostClick = onPostClick, minListHeight = minListHeight)
}

/** 컨트롤 아래 영역. 최신 글, 섹션 제목, 검색 결과 안내, 목록 행을 [minListHeight] 이상 높이로 쌓는다. */
@Composable
private fun FeedList(
    state: HomeUiState.Success,
    compact: Boolean,
    onPostClick: (String) -> Unit,
    minListHeight: Dp,
) {
    val strings = LocalHaetiStrings.current
    Column(modifier = Modifier.fillMaxWidth().heightIn(min = minListHeight)) {
        state.featured?.let { featured ->
            FeaturedPost(meta = featured, compact = compact, onClick = { onPostClick(featured.slug) })
            Spacer(Modifier.height(if (compact) 64.dp else 96.dp))
        }
        val heading =
            if (state.query.isNotBlank()) {
                strings.searchResults
            } else {
                state.selectedCategory?.let(
                    strings::category,
                )
            }
        SectionHeading(title = heading ?: strings.pastPosts)
        Spacer(Modifier.height(12.dp))
        if (state.query.isNotBlank() && state.posts.isEmpty()) {
            Spacer(Modifier.height(16.dp))
            FeedMessage(strings.searchEmpty)
        }
        state.posts.forEachIndexed { index, meta ->
            if (index > 0) Divider()
            // 필터·검색으로 순서가 바뀌어도 행 상태(썸네일·호버)가 자리 대신 글을 따라가게 한다.
            key(meta.slug) {
                PostListRow(
                    meta = meta,
                    compact = compact,
                    onClick = { onPostClick(meta.slug) },
                    excerpt = state.excerpts[meta.slug],
                )
            }
        }
    }
}

/** 목록 행 사이 구분선. */
@Composable
private fun Divider() {
    Box(Modifier.fillMaxWidth().height(1.dp).background(MaterialTheme.colorScheme.outline))
}

/** 상태 안내 한 줄. */
@Composable
private fun FeedMessage(text: String) {
    Text(text = text, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
}

/** 최신 글 커버와 목록 행 자리를 흉내 내는 스켈레톤. */
@Composable
private fun FeedSkeleton() {
    Column {
        SkeletonBlock(widthFraction = 1f, height = 300.dp)
        Spacer(Modifier.height(28.dp))
        SkeletonBlock(widthFraction = 0.7f, height = 32.dp)
        Spacer(Modifier.height(12.dp))
        SkeletonBlock(widthFraction = 0.9f, height = 18.dp)
    }
}
