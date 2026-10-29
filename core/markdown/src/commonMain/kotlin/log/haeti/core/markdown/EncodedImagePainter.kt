package log.haeti.core.markdown

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.unit.IntSize
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.Codec
import org.jetbrains.skia.Data
import org.jetbrains.skia.Image
import org.jetbrains.skia.ImageInfo
import kotlin.math.roundToInt

/** 브라우저와 같은 규칙: 10ms 이하로 적힌 프레임은 너무 빨라 100ms로 본다. */
private const val MIN_FRAME_MILLIS = 20
private const val FALLBACK_FRAME_MILLIS = 100

/**
 * 인코딩된 이미지 바이트(png·jpg·webp·gif)를 [Painter]로 만든다. 애니메이션 GIF·WebP는 프레임을 재생한다.
 * 해석할 수 없는 바이트면 null.
 *
 * 프레임을 미리 다 풀면 큰 GIF는 수백 MB가 되므로, 재생하면서 한 장씩 디코딩해 비트맵 하나를 재사용한다.
 *
 * @param bytes 이미지 파일 내용
 */
@Composable
fun rememberEncodedImagePainter(bytes: ByteArray): Painter? {
    val codec = remember(bytes) { runCatching { Codec.makeFromData(Data.makeFromBytes(bytes)) }.getOrNull() }
    if (codec == null || codec.frameCount <= 1) {
        return remember(bytes) {
            runCatching { BitmapPainter(Image.makeFromEncoded(bytes).toComposeImageBitmap()) }.getOrNull()
        }
    }
    val painter = remember(codec) { AnimatedImagePainter(codec) }
    LaunchedEffect(painter) { painter.play() }
    return painter
}

/** 애니메이션 이미지. [play]가 도는 동안 프레임을 바꿔 그리고, 화면 밖이면 디코딩을 쉰다. */
internal class AnimatedImagePainter(
    codec: Codec,
) : Painter() {
    private val frames = AnimatedFrames(codec)
    private var frame by mutableStateOf<ImageBitmap?>(null)

    /** 화면에 보이는지. 이미지를 그리는 쪽이 위치가 바뀔 때마다 알려 준다. */
    var isOnScreen by mutableStateOf(true)

    override val intrinsicSize: Size = Size(codec.width.toFloat(), codec.height.toFloat())

    override fun DrawScope.onDraw() {
        val image = frame ?: return
        drawImage(
            image,
            dstSize = IntSize(size.width.roundToInt(), size.height.roundToInt()),
            filterQuality = FilterQuality.Medium,
        )
    }

    /**
     * 반복 횟수(무한이면 -1)만큼 프레임을 돈다. 컴포지션을 떠나면 코루틴과 함께 멈춘다.
     * 프레임을 풀지 못하면 그 자리에서 멈춘다. 예외가 새면 앱 전체가 죽는다.
     */
    suspend fun play() {
        val loops = frames.repetitionCount
        var played = 0
        while (loops < 0 || played <= loops) {
            for (index in 0 until frames.count) {
                if (!isOnScreen) snapshotFlow { isOnScreen }.first { it }
                frame = runCatching { frames.render(index).toComposeImageBitmap() }.getOrNull() ?: return
                delay(frames.durationMillis(index).toLong())
            }
            played++
        }
    }
}

/**
 * 애니메이션 이미지의 프레임을 비트맵 하나에 차례로 합성한다.
 *
 * @param codec 프레임이 둘 이상인 GIF·WebP 코덱
 */
internal class AnimatedFrames(
    private val codec: Codec,
) {
    // 코덱이 "불투명"이라고 알려도 애니메이션 WebP는 프레임끼리 알파로 섞는다. 겹쳐 그리기는
    // 프리멀티플라이드 알파 비트맵에서만 되므로 코덱 기본 정보 대신 N32 premul로 잡는다.
    private val canvas = Bitmap().apply { allocPixels(ImageInfo.makeN32Premul(codec.width, codec.height)) }
    private val infos = codec.framesInfo
    private var lastRendered = -1

    /** 프레임 수. */
    val count: Int get() = codec.frameCount

    /** 반복 횟수. 무한이면 -1. */
    val repetitionCount: Int get() = codec.repetitionCount

    /** [index] 프레임을 보여 줄 시간(ms). 10ms 이하로 적힌 프레임은 브라우저처럼 100ms로 본다. */
    fun durationMillis(index: Int): Int =
        infos[index].duration.takeIf { it > 10 }?.coerceAtLeast(MIN_FRAME_MILLIS) ?: FALLBACK_FRAME_MILLIS

    /**
     * [index] 프레임을 그린 스냅샷. 순서대로 재생 중이면 비트맵에 남은 직전 프레임 위에 겹쳐 그린다.
     * 그게 안 되면(직전 프레임이 "이전 상태로 되돌리기"라 바탕이 될 수 없는 GIF 등) 이 프레임이 기대는
     * 의존 프레임 체인(`requiredFrame`)을 처음부터 다시 합성한다. Skia는 이 체인을 대신 그려 주지 않는다.
     */
    fun render(index: Int): Image {
        val onPrior =
            index > 0 &&
                lastRendered == index - 1 &&
                runCatching { codec.readPixels(canvas, index, index - 1) }.isSuccess
        if (!onPrior) {
            val chain =
                generateSequence(index) { infos[it].requiredFrame.takeIf { required -> required >= 0 } }
                    .toList()
                    .asReversed()
            chain.forEachIndexed { step, frame ->
                if (step == 0) codec.readPixels(canvas, frame) else codec.readPixels(canvas, frame, chain[step - 1])
            }
        }
        lastRendered = index
        // 가변 비트맵에서 만든 Image는 픽셀을 복사하므로 다음 프레임을 그려도 이번 프레임이 그대로 남는다.
        return Image.makeFromBitmap(canvas)
    }
}
