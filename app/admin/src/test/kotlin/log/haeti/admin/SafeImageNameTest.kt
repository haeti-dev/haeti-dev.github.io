package log.haeti.admin

import log.haeti.admin.io.safeImageName
import kotlin.test.Test
import kotlin.test.assertEquals

class SafeImageNameTest {
    @Test
    fun 맥_스크린샷_이름의_공백은_하이픈이_된다() {
        assertEquals("screenshot-2026-10-09-at-10.12.03-pm", safeImageName("Screenshot 2026-10-09 at 10.12.03 PM"))
    }

    @Test
    fun 한글만_있는_이름은_image로_바꾼다() {
        assertEquals("image", safeImageName("스크린샷"))
        assertEquals("cover-2", safeImageName("cover 사진 2"))
    }
}
