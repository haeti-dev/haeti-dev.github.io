package log.haeti.core.markdown

import androidx.compose.runtime.Composable

/** 데스크톱(어드민 미리보기)은 웹 플레이어를 띄울 수 없어 유튜브로 여는 카드를 그린다. */
@Composable
internal actual fun YouTubePlayer(
    videoId: String,
    fallback: @Composable () -> Unit,
) = fallback()
