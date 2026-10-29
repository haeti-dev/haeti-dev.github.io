package log.haeti.feature.post

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import log.haeti.core.designsystem.categoryPalette
import log.haeti.core.designsystem.interaction.fadeInOnAppear
import log.haeti.core.ui.component.HaetiFooter
import log.haeti.core.ui.component.NotFoundNotice
import log.haeti.core.ui.component.PostSkeleton
import log.haeti.core.ui.layout.CenteredContent
import log.haeti.core.ui.layout.LocalLayoutTier
import log.haeti.core.ui.layout.chromeMaxWidth
import log.haeti.core.ui.layout.topSpacing
import log.haeti.core.ui.strings.LocalHaetiStrings

/**
 * 글 상세 화면. 슬러그는 [viewModel]이 이미 들고 있으므로 화면은 상태만 그린다.
 * 상단바는 앱 셸이 그리고, 여기서는 콘텐츠 영역·읽기 진행 바·푸터를 책임진다.
 *
 * @param onNavigateHome 글 목록으로 돌아가는 콜백
 * @param onOpenPost 앞뒤 글로 이동하는 콜백. 슬러그를 전달한다
 * @param onTitleResolved 글 제목이 정해지면 호출. 앱 셸이 문서 제목을 갱신하는 데 쓴다
 * @param viewModel 슬러그가 주입된 글 상세 ViewModel
 */
@Composable
fun PostScreen(
    onNavigateHome: () -> Unit,
    onOpenPost: (String) -> Unit,
    onTitleResolved: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PostViewModel,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val tier = LocalLayoutTier.current
    val scrollState = rememberScrollState()
    var showedSkeleton by remember { mutableStateOf(false) }
    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().verticalScroll(scrollState).padding(top = tier.topSpacing)) {
            when (val state = uiState) {
                PostUiState.Loading -> DelayedSkeleton(onShown = { showedSkeleton = true })
                is PostUiState.Success -> {
                    LaunchedEffect(state.post.meta.title) { onTitleResolved(state.post.meta.title) }
                    // 스켈레톤이 보였을 때만 본문이 나타나며 바뀐다. 바로 열린 글은 화면 전환만으로 충분하다.
                    // 글 단위로 한 번만 페이드해서, 같은 글에서 언어만 바꿀 때는 다시 페이드하지 않는다.
                    Column(Modifier.fadeInOnAppear(state.post.meta.slug, enabled = showedSkeleton)) {
                        PostBody(state, onNavigateHome, onOpenPost, viewModel::readAsset)
                    }
                }
                PostUiState.NotFound -> PostNotice(notFound = true, onNavigateHome = onNavigateHome)
                PostUiState.Error -> PostNotice(notFound = false, onNavigateHome = onNavigateHome)
            }
            // 로딩 중에 그리면 내용이 아직 없어 푸터가 위로 올라왔다가 밀려 내려간다.
            if (uiState != PostUiState.Loading) HaetiFooter(maxWidth = tier.chromeMaxWidth)
        }
        (uiState as? PostUiState.Success)?.let { state ->
            ReadingProgressBar(
                scrollState = scrollState,
                color = categoryPalette(state.post.meta.category).accent,
                modifier = Modifier.align(Alignment.TopStart),
            )
        }
    }
}

/** 글이 없거나([notFound]) 읽지 못했을 때의 안내와 목록으로 돌아가는 링크. */
@Composable
private fun PostNotice(
    notFound: Boolean,
    onNavigateHome: () -> Unit,
) {
    val strings = LocalHaetiStrings.current
    CenteredContent {
        NotFoundNotice(
            onNavigateHome = onNavigateHome,
            title = if (notFound) strings.postNotFoundTitle else strings.postErrorTitle,
            message = if (notFound) strings.postNotFoundMessage else strings.postErrorMessage,
        )
    }
}

/** 스켈레톤을 띄우기 전 기다리는 시간. 이미 읽어 둔 글이면 이 안에 본문이 와서 스켈레톤이 번쩍이지 않는다. */
private const val SKELETON_DELAY_MS = 150L

/** 로딩이 [SKELETON_DELAY_MS]를 넘길 때만 나타나는 스켈레톤. 나타나면 [onShown]을 부른다. */
@Composable
private fun DelayedSkeleton(onShown: () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(SKELETON_DELAY_MS)
        visible = true
        onShown()
    }
    if (visible) PostSkeleton(Modifier.fadeInOnAppear(Unit))
}
