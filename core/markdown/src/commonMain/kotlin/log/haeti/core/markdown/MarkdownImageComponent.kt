package log.haeti.core.markdown

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mikepenz.markdown.compose.components.MarkdownComponentModel
import log.haeti.core.designsystem.interaction.fadeInOnAppear
import log.haeti.core.markdown.model.ImageAlign
import log.haeti.core.markdown.model.ImageDirective
import log.haeti.core.markdown.model.ImageLoad

/**
 * 이미지 지시어(width·align·caption·dark)를 반영해 이미지를 렌더링하는 custom image component.
 * 누르면 [ImageZoomDialog]로 크게 본다. 읽는 동안에는 같은 폭의 자리를 먼저 잡아 두고, 다 읽으면 페이드인한다.
 * 읽지 못하면 그 자리에 안내 문구와 대체텍스트를 보인다.
 * `MarkdownBody`가 `markdownComponents(image = ...)`로 연결한다.
 *
 * @param model mikepenz 렌더러가 넘겨주는 현재 이미지 노드 정보
 * @param imageResolver 상대경로를 [androidx.compose.ui.graphics.painter.Painter]로 바꾸는 resolver
 * @param directives 경로별로 미리 추출해 둔 이미지 지시어 맵
 * @param isDarkPage 다크 모드면 지시어의 `dark` 이미지를 쓴다
 */
@Composable
internal fun MarkdownImageComponent(
    model: MarkdownComponentModel,
    imageResolver: ImageResolver,
    directives: Map<String, ImageDirective>,
    isDarkPage: Boolean,
) {
    val path = model.node.imagePath(model.content) ?: return
    val alt = model.node.imageAlt(model.content)
    val directive = directives[path] ?: ImageDirective()
    val load = imageResolver.load(directive.pathFor(path, isDarkPage))
    var zoomed by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Box(modifier = Modifier.fillMaxWidth()) {
            val sized = Modifier.fillMaxWidth(directive.width / 100f).align(directive.align.toBoxAlignment())
            LoadedImage(load, alt, directive.aspectRatio, modifier = sized, onClick = { zoomed = true })
        }
        if (zoomed && load is ImageLoad.Loaded) {
            ImageZoomDialog(load.painter, directive.caption, onDismiss = { zoomed = false })
        }
        directive.caption?.let { caption ->
            Text(
                text = caption,
                style = MaterialTheme.typography.bodySmall,
                color = LocalContentColor.current.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
            )
        }
    }
}

/**
 * 다 읽은 이미지는 짧게 페이드인하고, 읽는 중에는 같은 폭과 비율의 자리를 먼저 차지한다.
 * 비율은 배포 빌드가 기록한 [aspectRatio]를 쓰고, 없으면(어드민 미리보기) 16:9로 잡는다.
 * 다 읽은 뒤에도 같은 비율을 지켜 자리와 이미지 높이가 어긋나 아래 글이 밀리지 않게 한다.
 * 읽지 못하면 같은 자리에 [ImageFailedPlaceholder]를 보인다.
 */
@Composable
private fun LoadedImage(
    load: ImageLoad,
    alt: String?,
    aspectRatio: Float?,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    val reserved = modifier.aspectRatio(aspectRatio ?: PLACEHOLDER_ASPECT_RATIO)
    val painter =
        when (load) {
            ImageLoad.Loading -> {
                Box(reserved.clip(RoundedCornerShape(8.dp)).background(MaterialTheme.colorScheme.surface))
                return
            }
            ImageLoad.Failed -> {
                ImageFailedPlaceholder(alt, reserved)
                return
            }
            is ImageLoad.Loaded -> load.painter
        }
    Image(
        painter = painter,
        contentDescription = alt,
        contentScale = ContentScale.FillWidth,
        modifier =
            modifier
                .then(if (aspectRatio != null) Modifier.aspectRatio(aspectRatio) else Modifier)
                .fadeInOnAppear(painter)
                .pauseWhenOffScreen(painter)
                .pointerHoverIcon(PointerIcon.Hand)
                .clickable(onClickLabel = "이미지 크게 보기", onClick = onClick),
    )
}

/** 크기가 기록되지 않은 이미지의 자리 비율. */
private const val PLACEHOLDER_ASPECT_RATIO = 16f / 9f

/** 애니메이션 이미지면 화면에 보일 때만 재생하게 한다. 스크롤 영역이 잘라 낸 이미지는 보이는 크기가 0이다. */
private fun Modifier.pauseWhenOffScreen(painter: Painter): Modifier {
    if (painter !is AnimatedImagePainter) return this
    return onGloballyPositioned { coordinates ->
        val visible = coordinates.boundsInWindow()
        painter.isOnScreen = visible.width > 0f && visible.height > 0f
    }
}

private fun ImageAlign.toBoxAlignment(): Alignment =
    when (this) {
        ImageAlign.LEFT -> Alignment.CenterStart
        ImageAlign.CENTER -> Alignment.Center
        ImageAlign.RIGHT -> Alignment.CenterEnd
    }
