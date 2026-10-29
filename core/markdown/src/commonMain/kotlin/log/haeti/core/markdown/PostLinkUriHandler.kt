package log.haeti.core.markdown

import androidx.compose.ui.platform.UriHandler

/** 블로그 안의 다른 글을 가리키는 상대 링크(`post/<slug>`). 배포 주소가 바뀌어도 그대로 쓸 수 있다. */
private val postLink = Regex("""^/?post/([a-z0-9-]+)/?(#.*)?$""")

/**
 * 본문 링크 처리기. 블로그 안의 글 링크는 [onOpenPost]로 앱 안에서 열고, 나머지는 [fallback]에 넘긴다.
 * 그 밖의 상대 링크는 열 곳이 없으므로 무시한다(데스크톱에서는 열다 실패한다).
 *
 * @param fallback 외부 링크를 열 플랫폼 처리기
 * @param onOpenPost 글 링크를 눌렀을 때 슬러그를 넘기는 콜백
 */
internal class PostLinkUriHandler(
    private val fallback: UriHandler,
    private val onOpenPost: (String) -> Unit,
) : UriHandler {
    override fun openUri(uri: String) {
        val slug = postSlugOf(uri)
        when {
            slug != null -> onOpenPost(slug)
            uri.contains("://") || uri.startsWith("mailto:") -> fallback.openUri(uri)
        }
    }
}

/** [uri]가 블로그 안의 글 링크면 슬러그, 아니면 null. */
internal fun postSlugOf(uri: String): String? = postLink.find(uri)?.groupValues?.get(1)
