package log.haeti.web.browser

import kotlinx.browser.document
import log.haeti.web.path.BasePath
import org.w3c.dom.url.URL

/**
 * `index.html`의 `<base href>`가 가리키는 배포 접두사를 읽는다.
 *
 * `document.baseURI`는 `<base href>`를 반영한 절대 URL이므로 경로 부분만 떼어 쓴다.
 * 해석에 실패하면 루트 배포로 간주한다.
 */
fun resolveBasePath(): BasePath {
    val pathname = runCatching { URL(document.baseURI).pathname }.getOrNull()
    return BasePath(pathname.orEmpty())
}
