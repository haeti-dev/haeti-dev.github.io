package log.haeti.core.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import log.haeti.core.designsystem.categoryPalette
import log.haeti.core.designsystem.interaction.fadeInOnAppear
import log.haeti.core.model.PostMeta
import log.haeti.core.ui.platform.LocalPostAssetLoader
import kotlin.random.Random

/**
 * 글의 대표 이미지. 프론트매터 `thumbnail`이 있으면 그 이미지를 꽉 채워 보여주고,
 * 없거나 읽지 못했으면 카테고리 바탕색 위에 글마다 다른 "코드 미니맵"을 그린다.
 * 읽는 동안에는 바탕색만 두고, 이미지가 오면 짧게 페이드인한다(미니맵이 사진으로 바뀌며 깜빡이지 않게).
 * 같은 글은 항상 같은 그림이라 목록과 상세에서 같은 글로 알아볼 수 있다.
 *
 * @param meta 대표 이미지를 고를 글
 * @param cornerRadius 모서리 반경
 */
@Composable
fun PostCover(
    meta: PostMeta,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 20.dp,
) {
    val palette = categoryPalette(meta.category)
    val layout = remember(meta.slug) { CoverLayout.from(meta.slug) }
    BoxWithConstraints(modifier = modifier.clip(RoundedCornerShape(cornerRadius)).background(palette.tint)) {
        val widthPx = if (constraints.hasBoundedWidth) constraints.maxWidth else WIDE_FALLBACK_PX
        val load = rememberThumbnail(meta, ThumbnailCache.bucketFor(widthPx))
        val bitmap = load.bitmap
        when {
            bitmap != null ->
                Image(
                    bitmap = bitmap,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.matchParentSize().fadeInOnAppear(bitmap, enabled = !load.fromCache),
                )
            load.failed -> Canvas(modifier = Modifier.matchParentSize()) { drawCover(layout, palette.accent) }
            else -> Unit
        }
    }
}

/** 폭을 모르는 자리(무한 폭)에서 쓸 디코딩 폭. */
private const val WIDE_FALLBACK_PX = 1024

/** 썸네일 읽기 결과. [bitmap]이 있으면 그리고, [failed]면 미니맵으로 대신한다. 둘 다 아니면 읽는 중이다. */
private class ThumbnailLoad(
    val bitmap: ImageBitmap? = null,
    val failed: Boolean = false,
    val fromCache: Boolean = false,
)

private val NoThumbnail = ThumbnailLoad(failed = true)
private val LoadingThumbnail = ThumbnailLoad()

/**
 * 썸네일을 [widthPx] 폭으로 줄여 읽는다. 한 번 읽은 것은 [ThumbnailCache]에서 바로 꺼내 깜빡이지 않는다.
 * 썸네일이 없거나 읽기·디코딩에 실패하면 [ThumbnailLoad.failed].
 */
@Composable
private fun rememberThumbnail(
    meta: PostMeta,
    widthPx: Int,
): ThumbnailLoad {
    val path = meta.thumbnail ?: return NoThumbnail
    val loader = LocalPostAssetLoader.current
    val key = "${meta.directoryName}/$path@$widthPx"
    // 상태를 key에 묶는다. produceState는 key가 바뀌어도 이전 값을 들고 있어, 같은 자리에 다른 글이 오면
    // 이전 글의 썸네일이 남는다(목록 필터·검색, 어드민 글 전환).
    val state = remember(key) { mutableStateOf(ThumbnailCache[key]?.let { ThumbnailLoad(it, fromCache = true) }) }
    LaunchedEffect(key) {
        if (state.value != null) return@LaunchedEffect
        val loaded = runCatching { loader.load(meta, path)?.let { decodeThumbnail(it, widthPx) } }.getOrNull()
        loaded?.let { ThumbnailCache[key] = it }
        state.value = loaded?.let { ThumbnailLoad(it) } ?: NoThumbnail
    }
    return state.value ?: LoadingThumbnail
}

/** 한 줄의 코드 조각. 들여쓰기 단계와 조각별 폭(캔버스 폭 비율)을 가진다. */
private class CoverLine(
    val indent: Int,
    val segments: List<Float>,
)

/**
 * 시드에서 뽑은 "코드 미니맵" 배치. 줄마다 들여쓰기와 단어 조각 길이가 다르고,
 * 그중 한 줄이 강조된 현재 줄이다.
 */
private class CoverLayout(
    val lines: List<CoverLine>,
    val highlighted: Int,
) {
    companion object {
        private const val LINE_COUNT = 9

        /** 같은 시드는 같은 배치를 만든다. 들여쓰기는 코드처럼 한 단계씩만 오르내린다. */
        fun from(seed: String): CoverLayout {
            val random = Random(seed.hashCode())
            var indent = 0
            val lines =
                List(LINE_COUNT) {
                    indent = (indent + random.nextInt(-1, 2)).coerceIn(0, 3)
                    val segments = List(1 + random.nextInt(3)) { 0.05f + random.nextFloat() * 0.17f }
                    CoverLine(indent, segments)
                }
            return CoverLayout(lines, highlighted = 2 + random.nextInt(LINE_COUNT - 3))
        }
    }
}

/** 줄 단위로 둥근 막대를 그린다. 첫 조각은 키워드처럼 진하게, 강조 줄은 끝에 커서를 붙인다. */
private fun DrawScope.drawCover(
    layout: CoverLayout,
    accent: Color,
) {
    val lineHeight = size.height * 0.05f
    val pitch = size.height * 0.085f
    val left = size.width * 0.1f
    val maxRight = size.width * 0.9f
    layout.lines.forEachIndexed { row, line ->
        val y = size.height * 0.14f + row * pitch
        val isCurrent = row == layout.highlighted
        var x = left + line.indent * lineHeight * 2.4f
        line.segments.forEachIndexed { index, fraction ->
            val width = (size.width * fraction).coerceAtMost(maxRight - x)
            if (width <= lineHeight) return@forEachIndexed
            val alpha =
                if (isCurrent) {
                    1f
                } else if (index == 0) {
                    0.55f
                } else {
                    0.26f
                }
            drawRoundRect(
                accent.copy(alpha = alpha),
                Offset(x, y),
                Size(width, lineHeight),
                CornerRadius(lineHeight / 2),
            )
            x += width + lineHeight * 0.9f
        }
        if (isCurrent && x + lineHeight < maxRight) {
            drawRect(accent, Offset(x, y - lineHeight * 0.3f), Size(lineHeight * 0.32f, lineHeight * 1.6f))
        }
    }
}
