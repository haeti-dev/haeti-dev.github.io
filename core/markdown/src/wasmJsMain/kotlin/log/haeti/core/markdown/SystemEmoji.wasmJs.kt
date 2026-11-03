package log.haeti.core.markdown

import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.js.ExperimentalWasmJsInterop

private val rendered = mutableMapOf<String, ByteArray>()

/** 브라우저 2D 캔버스에 그려 PNG로 받는다. 2D 캔버스는 운영체제 글꼴을 쓰므로 맥에서는 Apple 이모지가 나온다. */
@OptIn(ExperimentalEncodingApi::class)
internal actual fun systemEmojiPng(emoji: String): ByteArray? =
    rendered.getOrPut(emoji) { Base64.decode(emojiDataUrl(emoji).substringAfter(',')) }

@OptIn(ExperimentalWasmJsInterop::class)
private fun emojiDataUrl(emoji: String): String =
    js(
        """(() => {
        const size = 128;
        const canvas = document.createElement('canvas');
        canvas.width = size;
        canvas.height = size;
        const context = canvas.getContext('2d');
        context.font = '104px "Apple Color Emoji", "Segoe UI Emoji", "Noto Color Emoji", sans-serif';
        context.textAlign = 'center';
        const box = context.measureText(emoji);
        context.fillText(emoji, size / 2, (size + box.actualBoundingBoxAscent - box.actualBoundingBoxDescent) / 2);
        return canvas.toDataURL('image/png');
    })()""",
    )
