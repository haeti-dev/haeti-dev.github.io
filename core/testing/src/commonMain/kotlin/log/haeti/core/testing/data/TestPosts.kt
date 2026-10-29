package log.haeti.core.testing.data

import kotlinx.datetime.LocalDate
import log.haeti.core.model.Post
import log.haeti.core.model.PostMeta

/**
 * 화면·ViewModel 테스트가 공유하는 고정 글 데이터.
 *
 * [all]은 실제 Repository와 같은 최신순이다.
 */
object TestPosts {
    /** 가장 최근 글. */
    val hello: Post =
        Post(
            meta =
                PostMeta(
                    title = "Haeti.log를 CMP로 다시 짓다",
                    date = LocalDate(2026, 8, 18),
                    category = "안드로이드",
                    summary = "팀 블로그 종료를 계기로 블로그를 직접 만들기 시작한 기록",
                    slug = "hello-haeti-log",
                    thumbnail = "images/sample.webp",
                ),
            body = "## 왜 직접 만드는가\n\n글의 보관처를 직접 소유할 필요가 생겼다.",
        )

    /** 두 번째로 오래된 글. 썸네일이 없는 경우를 대표한다. */
    val second: Post =
        Post(
            meta =
                PostMeta(
                    title = "Kotlin/Wasm으로 블로그 띄우기",
                    date = LocalDate(2026, 6, 6),
                    category = "코틀린",
                    summary = "브라우저에서 캔버스 한 장으로 도는 Compose 앱을 배포한 이야기",
                    slug = "wasm-deploy",
                ),
            body = "## 배포\n\n정적 호스팅만으로 충분했다.",
        )

    /** 발행 글 전체(최신순). */
    val all: List<Post> = listOf(hello, second)
}
