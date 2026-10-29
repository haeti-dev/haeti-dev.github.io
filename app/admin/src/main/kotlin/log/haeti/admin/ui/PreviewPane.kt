package log.haeti.admin.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import log.haeti.admin.model.PostEntry
import log.haeti.admin.ui.component.SegmentedToggle
import log.haeti.core.markdown.ImageResolver
import log.haeti.core.model.Language

private const val PREVIEW_DEBOUNCE_MS = 300L
private val WebWidth = 680.dp
private val MobileWidth = 390.dp

/** 미리보기 폭. 웹은 미리보기 칸에 맞춰 블로그 글 본문(760dp)보다 조금 좁게 줄인 폭, 모바일은 휴대폰 폭. */
private enum class PreviewWidth(
    val label: String,
) {
    Web("웹"),
    Mobile("모바일"),
}

/**
 * 원문을 300ms 디바운스한 뒤 블로그와 같은 모양으로 렌더링하는 오른쪽 미리보기.
 * 위쪽 토글로 웹 폭과 모바일 폭을 바꿔 볼 수 있다.
 *
 * @param rawText 에디터의 현재 원문 (프론트매터 포함)
 * @param entry 열려 있는 글. 날짜·카테고리·썸네일을 번역본 미리보기에도 쓴다
 * @param language 편집 중인 파일 언어
 * @param imageResolver 글 기준 이미지 resolver
 */
@Composable
fun PreviewPane(
    rawText: String,
    entry: PostEntry,
    language: Language,
    imageResolver: ImageResolver?,
    modifier: Modifier = Modifier,
) {
    val debounced = rememberDebounced(rawText, PREVIEW_DEBOUNCE_MS)
    var width by remember { mutableStateOf(PreviewWidth.Web) }
    val mobile = width == PreviewWidth.Mobile
    Column(modifier.background(MaterialTheme.colorScheme.background)) {
        PreviewToolbar(width = width, onSelect = { width = it })
        Box(
            modifier =
                Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 24.dp),
            contentAlignment = Alignment.TopCenter,
        ) {
            val frame =
                if (mobile) {
                    Modifier
                        .width(MobileWidth)
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(24.dp))
                } else {
                    Modifier.widthIn(max = WebWidth).fillMaxWidth()
                }
            PreviewArticle(
                rawText = debounced,
                entry = entry,
                language = language,
                imageResolver = imageResolver,
                compact = mobile,
                modifier =
                    frame.padding(
                        horizontal = if (mobile) 20.dp else 32.dp,
                        vertical = if (mobile) 28.dp else 8.dp,
                    ),
            )
        }
    }
}

/** 미리보기 위쪽 줄. 왼쪽 "미리보기" 라벨, 오른쪽 웹/모바일 폭 토글. */
@Composable
private fun PreviewToolbar(
    width: PreviewWidth,
    onSelect: (PreviewWidth) -> Unit,
) {
    Row(
        Modifier.fillMaxWidth().height(48.dp).padding(horizontal = 24.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "미리보기",
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.weight(1f))
        SegmentedToggle(
            options = PreviewWidth.entries.map { it.label },
            selectedIndex = width.ordinal,
            onSelect = { onSelect(PreviewWidth.entries[it]) },
        )
    }
}

/** [source]가 [delayMillis] 동안 바뀌지 않으면 반영되는 디바운스 상태값. */
@Composable
private fun rememberDebounced(
    source: String,
    delayMillis: Long,
): String {
    var debounced by remember { mutableStateOf(source) }
    LaunchedEffect(source) {
        delay(delayMillis)
        debounced = source
    }
    return debounced
}
