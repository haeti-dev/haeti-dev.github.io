package log.haeti.buildlogic.site

import log.haeti.buildlogic.checkPost
import java.io.File
import java.time.LocalDate

/**
 * 배포 파일(OG 셸·사이트맵·RSS)을 만드는 데 필요한 글 정보. 앱의 `PostMeta`와 같은 프론트매터를 읽는다.
 *
 * @param directory 글 디렉터리 (`content/posts/<yyyy-MM-dd-slug>`)
 * @param slug URL 슬러그 (디렉터리명에서 날짜를 뗀 것)
 * @param title 제목
 * @param summary 한 줄 요약
 * @param category 카테고리 표시명
 * @param date 발행일
 * @param thumbnail 썸네일 상대 경로. 없으면 null
 */
internal data class SitePost(
    val directory: File,
    val slug: String,
    val title: String,
    val summary: String,
    val category: String,
    val date: LocalDate,
    val thumbnail: String?,
) {
    /** 사이트 루트 기준 글 경로. 셸 파일이 `post/<slug>/index.html`이라 끝에 `/`를 붙인다. */
    val path: String get() = "/post/$slug/"
}

/**
 * `content/posts`에서 발행 글(`draft: true`가 아닌 글)을 최신순으로 읽는다.
 * 규칙을 어긴 글은 건너뛰지 않고 [checkPost]가 빌드를 멈춘다. 앱과 배포 파일이 같은 글 목록을 갖게 하려는 것이다.
 *
 * @param postsDirectory `content/posts`
 */
internal fun publishedPosts(postsDirectory: File): List<SitePost> =
    postsDirectory.listFiles().orEmpty()
        .filter { it.isDirectory && it.resolve("index.md").isFile }
        .sortedByDescending { it.name }
        .map(::checkPost)
        .filterNot { it.draft }
        .map { post ->
            SitePost(
                directory = post.directory,
                slug = post.slug,
                title = post.fields.getValue("title"),
                summary = post.fields.getValue("summary"),
                category = post.fields.getValue("category"),
                date = post.date,
                thumbnail = post.fields["thumbnail"]?.takeIf { it.isNotBlank() },
            )
        }
