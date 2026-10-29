package log.haeti.feature.post

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import log.haeti.core.designsystem.haetiMonoFontFamily
import log.haeti.core.markdown.MarkdownBody
import log.haeti.core.ui.component.PostCover
import log.haeti.core.ui.layout.CenteredContent
import log.haeti.core.ui.layout.ContentMaxWidth
import log.haeti.core.ui.layout.HomeMaxWidth
import log.haeti.core.ui.layout.LocalLayoutTier
import log.haeti.core.ui.layout.isCompact

/**
 * 글 상세 본문. 머리말(제목·요약·메타) → 본문보다 넓게 튀어나온 커버 → 본문 → 앞뒤 글 순서다.
 *
 * @param state 글과 앞뒤 글
 * @param onNavigateHome 글 목록으로 돌아가는 콜백
 * @param onOpenPost 앞뒤 글·본문 글 링크 클릭 시 슬러그를 넘기는 콜백
 * @param readAsset 본문 이미지 바이트를 읽는 함수. 글이 바뀔 때만 resolver를 다시 만든다
 */
@Composable
internal fun PostBody(
    state: PostUiState.Success,
    onNavigateHome: () -> Unit,
    onOpenPost: (String) -> Unit,
    readAsset: suspend (String) -> ByteArray?,
) {
    val post = state.post
    val compact = LocalLayoutTier.current.isCompact
    CenteredContent(maxWidth = ContentMaxWidth) {
        PostHeader(meta = post.meta, onNavigateHome = onNavigateHome, translationMissing = state.translationMissing)
    }
    Spacer(Modifier.height(if (compact) 32.dp else 48.dp))
    CenteredContent(maxWidth = HomeMaxWidth) {
        PostCover(
            meta = post.meta,
            modifier = Modifier.fillMaxWidth().aspectRatio(if (compact) 16f / 10f else 2.2f),
            cornerRadius = if (compact) 16.dp else 24.dp,
        )
    }
    Spacer(Modifier.height(if (compact) 32.dp else 56.dp))
    CenteredContent(maxWidth = ContentMaxWidth) {
        MarkdownBody(
            markdown = post.body,
            // 언어를 바꿔도 같은 글이면 resolver(읽어 둔 이미지)를 그대로 쓴다.
            imageResolver = remember(post.meta.directoryName) { PostImageResolver(readAsset) },
            modifier = Modifier.fillMaxWidth(),
            compact = compact,
            codeFontFamily = haetiMonoFontFamily(),
            onOpenPost = onOpenPost,
        )
        Spacer(Modifier.height(80.dp))
        PostNeighbors(newer = state.newer, older = state.older, onOpenPost = onOpenPost)
    }
}
