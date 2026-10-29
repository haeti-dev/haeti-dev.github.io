package log.haeti.feature.home

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.zacsweers.metrox.viewmodel.metroViewModel
import log.haeti.core.ui.HaetiLinks
import log.haeti.core.ui.component.HaetiFooter
import log.haeti.core.ui.component.IntroBlock
import log.haeti.core.ui.layout.CenteredContent
import log.haeti.core.ui.layout.HomeMaxWidth
import log.haeti.core.ui.layout.LocalLayoutTier
import log.haeti.core.ui.layout.chromeMaxWidth
import log.haeti.core.ui.layout.isCompact
import log.haeti.core.ui.layout.topSpacing
import log.haeti.core.ui.platform.rememberUrlOpener
import log.haeti.core.ui.strings.LocalHaetiStrings

/**
 * 홈 화면. 소개, 최신 글, 카테고리로 거를 수 있는 글 목록을 한 컬럼에 쌓는다.
 * 상단바는 앱 셸이 그리므로 여기서는 콘텐츠 영역만 책임진다.
 *
 * @param onPostClick 글 클릭 시 호출. 글 슬러그를 전달한다
 * @param viewModel 글 목록을 공급하는 ViewModel. 기본값은 Metro 그래프에서 가져온다
 */
@Composable
fun HomeScreen(
    onPostClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = metroViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val urlOpener = rememberUrlOpener()
    HomeContent(
        uiState = uiState,
        onPostClick = onPostClick,
        onSelectCategory = viewModel::selectCategory,
        onSearch = viewModel::search,
        onOpenLink = urlOpener::open,
        modifier = modifier,
    )
}

/**
 * 상태만 받아 그리는 홈 본문. 미리보기·테스트가 ViewModel 없이 쓸 수 있게 분리했다.
 *
 * @param uiState 홈 상태
 * @param onPostClick 글 클릭 콜백
 * @param onSelectCategory 카테고리 필터 클릭 콜백
 * @param onSearch 검색어 변경 콜백
 * @param onOpenLink 소개 블록의 외부 링크 클릭 콜백
 */
@Composable
internal fun HomeContent(
    uiState: HomeUiState,
    onPostClick: (String) -> Unit,
    onSelectCategory: (String?) -> Unit,
    onSearch: (String) -> Unit,
    onOpenLink: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    // 목록 영역 최소 높이를 화면 높이로 잡으려고 바깥에서 화면 크기를 잰다.
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val viewportHeight = maxHeight
        HomeColumn(uiState, onPostClick, onSelectCategory, onSearch, onOpenLink, viewportHeight)
    }
}

/** 소개 → 글 영역 → 푸터를 한 컬럼으로 스크롤한다. */
@Composable
private fun HomeColumn(
    uiState: HomeUiState,
    onPostClick: (String) -> Unit,
    onSelectCategory: (String?) -> Unit,
    onSearch: (String) -> Unit,
    onOpenLink: (String) -> Unit,
    viewportHeight: Dp,
) {
    val tier = LocalLayoutTier.current
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = tier.topSpacing),
    ) {
        CenteredContent(maxWidth = HomeMaxWidth) {
            val strings = LocalHaetiStrings.current
            IntroBlock(
                headline = strings.introHeadline,
                description = strings.introDescription,
                links = HaetiLinks.all(strings.linkEmail),
                onOpenLink = onOpenLink,
                compact = tier.isCompact,
            )
            Spacer(Modifier.height(if (tier.isCompact) 56.dp else 88.dp))
            HomeFeed(uiState, onPostClick, onSelectCategory, onSearch, minListHeight = viewportHeight)
        }
        HaetiFooter(maxWidth = tier.chromeMaxWidth)
    }
}
