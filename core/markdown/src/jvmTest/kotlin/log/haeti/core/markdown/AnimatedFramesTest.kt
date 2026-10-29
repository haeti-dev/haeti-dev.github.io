package log.haeti.core.markdown

import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.Codec
import org.jetbrains.skia.Color
import org.jetbrains.skia.Data
import org.jetbrains.skia.Image
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * 3프레임 GIF·WebP: 0=빨강 전체, 1=왼쪽 위만 초록, 2=오른쪽 아래만 파랑. 뒤 프레임은 앞 프레임 위에 겹친다.
 * GIF에는 투명 픽셀이 있고, WebP는 코덱이 "불투명"이라 알려도 프레임을 알파로 섞는다.
 */
class AnimatedFramesTest {
    private fun frames(name: String = "three-frames.gif") =
        AnimatedFrames(Codec.makeFromData(Data.makeFromBytes(javaClass.getResource("/$name")!!.readBytes())))

    @Test
    fun 움직이는_WebP도_프레임을_겹쳐_그린다() {
        val frames = frames("three-frames.webp")

        repeat(2) { frames.render(it) }
        val third = frames.render(2)
        // 손실 압축이라 색이 1~2 어긋날 수 있어 주된 채널로 판정한다.
        assertTrue(Color.getG(third.pixel(5, 5)) > 200 && Color.getR(third.pixel(5, 5)) < 50, "왼쪽 위는 초록")
        assertTrue(Color.getB(third.pixel(30, 30)) > 200 && Color.getR(third.pixel(30, 30)) < 50, "오른쪽 아래는 파랑")
    }

    @Test
    fun 부분_프레임은_직전_프레임_위에_겹쳐_그린다() {
        val frames = frames()

        assertEquals(3, frames.count)
        assertEquals(Color.RED, frames.render(0).pixel(30, 30))
        val second = frames.render(1)
        assertEquals(Color.GREEN, second.pixel(5, 5))
        assertEquals(Color.RED, second.pixel(30, 30))
        val third = frames.render(2)
        assertEquals(Color.GREEN, third.pixel(5, 5))
        assertEquals(Color.BLUE, third.pixel(30, 30))
    }

    @Test
    fun 다시_처음_프레임으로_돌아오면_겹친_흔적이_없다() {
        val frames = frames()
        repeat(3) { frames.render(it) }

        assertEquals(Color.RED, frames.render(0).pixel(5, 5))
    }

    @Test
    fun 직전_프레임_없이_중간_프레임을_그려도_필요한_프레임부터_합성한다() {
        assertEquals(Color.GREEN, frames().render(2).pixel(5, 5))
    }

    @Test
    fun 프레임_시간은_적힌_값을_쓰고_너무_짧으면_100ms로_본다() {
        val frames = frames()

        assertEquals(100, frames.durationMillis(0))
        assertEquals(80, frames.durationMillis(1))
        assertEquals(120, frames.durationMillis(2))
    }

    private fun Image.pixel(
        x: Int,
        y: Int,
    ): Int = Bitmap.makeFromImage(this).getColor(x, y)
}
