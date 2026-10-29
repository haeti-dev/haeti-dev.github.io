package log.haeti.core.markdown

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

/**
 * 본문 이미지를 화면 가득 크게 보여 주는 창. 아무 곳이나 누르거나 ESC로 닫는다.
 * 브라우저처럼 이미지 1px을 1dp로 보고 그보다 크게 늘리지 않아([zoomedSize]), 작은 스크린샷이 뭉개지지 않는다.
 * 애니메이션 이미지는 그대로 재생된다.
 *
 * @param painter 본문에 그린 것과 같은 이미지
 * @param caption 이미지 설명. 있으면 아래에 보인다
 * @param onDismiss 닫을 때 호출
 */
@Composable
internal fun ImageZoomDialog(
    painter: Painter,
    caption: String?,
    onDismiss: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        val closeSource = remember { MutableInteractionSource() }
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.88f))
                    .clickable(interactionSource = closeSource, indication = null, onClick = onDismiss)
                    .padding(32.dp),
            contentAlignment = Alignment.Center,
        ) {
            BoxWithConstraints(contentAlignment = Alignment.Center) {
                val imageMaxHeight = maxHeight - if (caption != null) 48.dp else 0.dp
                ZoomedImage(painter = painter, caption = caption, maxWidth = maxWidth, maxHeight = imageMaxHeight)
            }
        }
    }
}

/** [maxWidth]×[maxHeight] 안에 맞춘 이미지와, 그 폭에 맞춘 설명. */
@Composable
private fun ZoomedImage(
    painter: Painter,
    caption: String?,
    maxWidth: Dp,
    maxHeight: Dp,
) {
    val fitted = zoomedSize(painter.intrinsicSize, maxWidth, maxHeight)
    val sizeModifier =
        if (fitted != null) Modifier.size(fitted) else Modifier.sizeIn(maxWidth = maxWidth, maxHeight = maxHeight)
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Image(painter = painter, contentDescription = caption, contentScale = ContentScale.Fit, modifier = sizeModifier)
        caption?.let {
            Text(
                text = it,
                color = Color.White.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 12.dp).widthIn(max = fitted?.width ?: maxWidth),
            )
        }
    }
}

/**
 * 확대해 보일 크기. 브라우저처럼 이미지 1px을 1dp로 보고 화면에 맞춰 줄이되 그보다 키우지는 않는다.
 * (Painter의 기본 크기는 기기 픽셀이라 고밀도 화면에서 절반으로 작아진다.) 크기를 모르면 null.
 */
private fun zoomedSize(
    natural: Size,
    maxWidth: Dp,
    maxHeight: Dp,
): DpSize? {
    if (!natural.isSpecified || natural.width <= 0f || natural.height <= 0f) return null
    val scale = minOf(1f, maxWidth.value / natural.width, maxHeight.value / natural.height)
    return DpSize((natural.width * scale).dp, (natural.height * scale).dp)
}
