package log.haeti.core.markdown

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.HtmlElementView
import kotlinx.browser.document
import org.w3c.dom.Element
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLIFrameElement
import org.w3c.dom.events.Event
import kotlin.js.ExperimentalWasmJsInterop

/** 재생 버튼(유튜브 빨간 둥근 사각형 + 흰 삼각형). */
private const val PLAY_BUTTON_SVG =
    """<svg viewBox="0 0 68 48" width="68" height="48" style="position:absolute;left:50%;top:50%;""" +
        """transform:translate(-50%,-50%);pointer-events:none"><path d="M66.5 7.7c-.8-2.9-3-5.2-5.9-6""" +
        """C55.4.3 34 .3 34 .3S12.6.3 7.4 1.7c-2.9.8-5.1 3.1-5.9 6C.1 13 .1 24 .1 24s0 11 1.4 16.3c.8 2.9 3 """ +
        """5.2 5.9 6C12.6 47.7 34 47.7 34 47.7s21.4 0 26.6-1.4c2.9-.8 5.1-3.1 5.9-6C67.9 35 67.9 24 67.9 24s0""" +
        """-11-1.4-16.3z" fill="#f03"/><path d="M45 24 27 14v20z" fill="#fff"/></svg>"""

/**
 * 유튜브 영상을 캔버스 위 이 자리에 HTML로 겹쳐 띄운다.
 *
 * 처음에는 썸네일과 재생 버튼만 둔다(가벼운 유튜브 방식): 플레이어 스크립트를 미리 받지 않고, 이 위에서 굴린 휠은
 * 블로그 캔버스로 넘겨 페이지가 스크롤된다. 누르면 그 자리를 youtube-nocookie 플레이어로 바꿔 바로 재생한다.
 * 플레이어가 뜬 뒤 그 위의 입력은 플레이어 몫이다.
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
internal actual fun YouTubePlayer(
    videoId: String,
    fallback: @Composable () -> Unit,
) {
    HtmlElementView(factory = { youtubeFacade(videoId) }, modifier = Modifier.fillMaxSize())
}

private fun youtubeFacade(videoId: String): HTMLDivElement {
    val facade = document.createElement("div") as HTMLDivElement
    facade.setAttribute("role", "button")
    facade.setAttribute("aria-label", "YouTube 영상 재생")
    facade.setAttribute(
        "style",
        "position:relative;width:100%;height:100%;cursor:pointer;background:#0f0f0f center/cover no-repeat " +
            "url('https://i.ytimg.com/vi/$videoId/hqdefault.jpg')",
    )
    facade.innerHTML = PLAY_BUTTON_SVG
    facade.addEventListener("wheel", { event -> forwardToCanvas(facade, event) })
    // Compose가 관리하는 이 요소는 그대로 두고 안쪽만 플레이어로 바꾼다(떠날 때 함께 정리된다).
    facade.addEventListener("click", { _ ->
        if (facade.firstElementChild !is HTMLIFrameElement) {
            facade.innerHTML = ""
            facade.style.cursor = "auto"
            facade.appendChild(youtubePlayer(videoId))
        }
    })
    return facade
}

private fun youtubePlayer(videoId: String): HTMLIFrameElement =
    (document.createElement("iframe") as HTMLIFrameElement).apply {
        src = "https://www.youtube-nocookie.com/embed/$videoId?autoplay=1&rel=0"
        title = "YouTube"
        setAttribute(
            "allow",
            "accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture",
        )
        setAttribute("allowfullscreen", "true")
        setAttribute("style", "border:0;width:100%;height:100%")
    }

/** 썸네일 위에서 굴린 휠을 같은 위치·양으로 Compose 캔버스에 다시 보낸다. 캔버스는 같은 shadow root에 있다. */
@OptIn(ExperimentalWasmJsInterop::class)
private fun forwardToCanvas(
    from: Element,
    event: Event,
): Unit =
    js(
        """{
        event.preventDefault();
        const canvas = from.getRootNode().querySelector('canvas');
        if (canvas) canvas.dispatchEvent(new WheelEvent('wheel', event));
    }""",
    )
