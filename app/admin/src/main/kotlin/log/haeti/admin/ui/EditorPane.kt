package log.haeti.admin.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.draganddrop.dragAndDropTarget
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draganddrop.DragAndDropEvent
import androidx.compose.ui.draganddrop.DragAndDropTarget
import androidx.compose.ui.draganddrop.awtTransferable
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import log.haeti.admin.io.IncomingImages
import log.haeti.admin.io.toIncomingImages
import log.haeti.core.data.parser.PostParser
import log.haeti.core.designsystem.haetiMonoFontFamily
import log.haeti.core.model.readingTimeLabel

/**
 * 마크다운 원문 에디터. 본문 글꼴로 쓰되 문법은 색으로 구분하고, 아래에 글자 수·분량·커서 위치를 보인다.
 *
 * Finder에서 이미지를 끌어다 놓으면 [onDropImages]로 넘겨 커서 위치에 들어가게 한다.
 *
 * @param value 원문과 커서
 * @param onValueChange 원문이 바뀔 때 호출
 * @param onDropImages 이미지 파일을 놓았을 때 호출
 * @param readOnly 번역 CLI가 이 파일을 쓰는 중이라 편집을 막을지
 */
@Composable
fun EditorPane(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    onDropImages: (IncomingImages) -> Unit,
    modifier: Modifier = Modifier,
    readOnly: Boolean = false,
) {
    val colors = MaterialTheme.colorScheme
    val mono = haetiMonoFontFamily()
    val highlighter =
        remember(colors.primary, colors.outlineVariant, mono) {
            MarkdownHighlighter(HighlightColors(colors.outlineVariant, colors.primary, colors.onSurfaceVariant, mono))
        }
    var dragging by remember { mutableStateOf(false) }
    val dropTarget = remember(onDropImages) { imageDropTarget(onDropImages, onHover = { dragging = it }) }
    val dropBorder = if (dragging) Modifier.border(2.dp, colors.primary) else Modifier
    val accepts: (DragAndDropEvent) -> Boolean = { !readOnly && it.incomingImages() != null }
    Column(modifier.dragAndDropTarget(shouldStartDragAndDrop = accepts, target = dropTarget)) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            readOnly = readOnly,
            visualTransformation = highlighter,
            textStyle =
                MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 16.sp,
                    lineHeight = 28.sp,
                    color = colors.onBackground,
                ),
            cursorBrush = SolidColor(colors.primary),
            modifier =
                Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .then(dropBorder)
                    .padding(horizontal = 32.dp, vertical = 28.dp),
        )
        EditorStats(value = value)
    }
}

/** 이미지 파일만 받는 드롭 대상. 끌고 들어오면 [onHover]로 테두리를 켜고, 나가거나 놓으면 끈다. */
private fun imageDropTarget(
    onDrop: (IncomingImages) -> Unit,
    onHover: (Boolean) -> Unit,
): DragAndDropTarget =
    object : DragAndDropTarget {
        override fun onEntered(event: DragAndDropEvent) = onHover(true)

        override fun onExited(event: DragAndDropEvent) = onHover(false)

        override fun onEnded(event: DragAndDropEvent) = onHover(false)

        override fun onDrop(event: DragAndDropEvent): Boolean {
            onHover(false)
            val images = event.incomingImages() ?: return false
            onDrop(images)
            return true
        }
    }

@OptIn(ExperimentalComposeUiApi::class)
private fun DragAndDropEvent.incomingImages(): IncomingImages? =
    runCatching {
        awtTransferable.toIncomingImages()
    }.getOrNull()

/** 에디터 아래 상태 줄. `글자 1,234 | 3 min read | 12행 5열`. 읽기 시간은 블로그와 같은 표기. */
@Composable
private fun EditorStats(value: TextFieldValue) {
    val text = value.text
    val chars = remember(text) { text.count { !it.isWhitespace() } }
    val readingTime = remember(text) { readingTimeLabel(PostParser.readingMinutesOf(text)) }
    val cursor = value.selection.start.coerceIn(0, text.length)
    val line = text.take(cursor).count { it == '\n' } + 1
    val column = cursor - text.lastIndexOf('\n', cursor - 1)
    val color = MaterialTheme.colorScheme.outlineVariant
    Column {
        Box(Modifier.fillMaxWidth().height(1.dp).background(MaterialTheme.colorScheme.outline))
        Row(
            Modifier.fillMaxWidth().height(36.dp).padding(horizontal = 32.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            listOf("글자 ${"%,d".format(chars)}", readingTime, "${line}행 ${column}열").forEachIndexed {
                index,
                item,
                ->
                if (index >
                    0
                ) {
                    Text(
                        "  |  ",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.outline,
                    )
                }
                Text(item, style = MaterialTheme.typography.labelMedium, color = color)
            }
        }
    }
}
