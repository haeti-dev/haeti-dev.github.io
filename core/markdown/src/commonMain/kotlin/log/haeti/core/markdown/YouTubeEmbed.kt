package log.haeti.core.markdown

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp

private val YOUTUBE_DIRECTIVE = Regex("""^::youtube\[([A-Za-z0-9_-]{6,20})]$""")
private val EmbedShape = RoundedCornerShape(16.dp)

/**
 * `::youtube[영상ID]` 한 줄이면 영상 ID를 돌려준다. 그 밖의 문단이면 null.
 *
 * @param line 문단 원문
 */
internal fun youtubeVideoIdOrNull(line: String): String? = YOUTUBE_DIRECTIVE.find(line.trim())?.groupValues?.get(1)

/** 유튜브에서 이 영상을 여는 주소. */
internal fun youtubeWatchUrl(videoId: String): String = "https://www.youtube.com/watch?v=$videoId"

/**
 * 본문 속 유튜브 영상. 웹은 플레이어를 그 자리에 띄우고, 데스크톱(어드민 미리보기)은 유튜브로 여는 카드를 그린다.
 *
 * @param videoId 유튜브 영상 ID
 */
@Composable
internal fun YouTubeEmbed(
    videoId: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .padding(vertical = 8.dp)
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .clip(EmbedShape)
                .background(Color(0xFF0F0F0F)),
    ) {
        YouTubePlayer(videoId = videoId, fallback = { YouTubeLinkCard(videoId) })
    }
}

/**
 * 플랫폼별 플레이어. 띄울 수 없으면 [fallback]을 그린다.
 *
 * @param videoId 유튜브 영상 ID
 * @param fallback 플레이어 대신 그릴 카드
 */
@Composable
internal expect fun YouTubePlayer(
    videoId: String,
    fallback: @Composable () -> Unit,
)

/** 재생 버튼과 "YouTube에서 보기" 문구. 누르면 유튜브를 새로 연다. */
@Composable
internal fun YouTubeLinkCard(videoId: String) {
    val uriHandler = LocalUriHandler.current
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .aspectRatio(
                    16f / 9f,
                ).clickable { uriHandler.openUri(youtubeWatchUrl(videoId)) },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier =
                Modifier
                    .size(
                        width = 68.dp,
                        height = 48.dp,
                    ).background(Color(0xFFFF0033), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Filled.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
        }
        Spacer(Modifier.height(12.dp))
        Text("YouTube에서 보기", color = Color.White.copy(alpha = 0.85f))
    }
}
