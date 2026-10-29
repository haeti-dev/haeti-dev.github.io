package log.haeti.core.navigation.path

import log.haeti.core.navigation.HomeNavKey
import log.haeti.core.navigation.PostNavKey
import log.haeti.core.navigation.ProjectsNavKey
import kotlin.test.Test
import kotlin.test.assertEquals

/** 주소창 갱신 방법(그대로·고치기·쌓기) 판단을 확인한다. */
class HistoryUpdateTest {
    private val post = PostNavKey("hello")

    @Test
    fun `주소가 지금 화면 경로와 같으면 아무것도 하지 않는다`() {
        assertEquals(HistoryUpdate.None, historyUpdateFor("/post/hello/", post))
        assertEquals(HistoryUpdate.None, historyUpdateFor("/", HomeNavKey))
    }

    @Test
    fun `같은 화면을 다른 모양으로 가리키면 항목을 늘리지 않고 주소만 고친다`() {
        assertEquals(HistoryUpdate.Replace, historyUpdateFor("/post/hello", post))
        assertEquals(HistoryUpdate.Replace, historyUpdateFor("//post//hello", post))
        assertEquals(HistoryUpdate.Replace, historyUpdateFor("/projects", ProjectsNavKey))
    }

    @Test
    fun `다른 화면으로 옮기면 새 항목을 쌓는다`() {
        assertEquals(HistoryUpdate.Push, historyUpdateFor("/", post))
        assertEquals(HistoryUpdate.Push, historyUpdateFor("/post/other/", post))
    }
}
