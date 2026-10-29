package log.haeti.core.navigation.path

import androidx.navigation3.runtime.NavKey
import log.haeti.core.navigation.AboutNavKey
import log.haeti.core.navigation.HomeNavKey
import log.haeti.core.navigation.NotFoundNavKey
import log.haeti.core.navigation.PostNavKey
import log.haeti.core.navigation.ProjectsNavKey

/** 문서 제목 꼬리에 붙는 사이트 이름. */
private const val SITE_NAME = "Haeti.log"

/** 제목과 사이트 이름을 잇는 구분자. */
private const val TITLE_SEPARATOR = " | "

/** 글 상세 경로의 첫 세그먼트. */
private const val POST_SEGMENT = "post"

/**
 * 브라우저 주소창에 넣을 절대 경로. 항상 `/`로 시작한다.
 * 배포 셸이 `post/<slug>/index.html`처럼 디렉터리에 놓이고 canonical도 끝 슬래시를 쓰므로 같은 모양으로 맞춘다.
 */
fun NavKey.toPath(): String =
    when (this) {
        HomeNavKey -> "/"
        is PostNavKey -> "/$POST_SEGMENT/$slug/"
        ProjectsNavKey -> "/projects/"
        AboutNavKey -> "/about/"
        else -> "/404"
    }

/**
 * 브라우저 경로를 화면 키로 해석한다.
 *
 * 선행·후행 슬래시와 빈 세그먼트는 무시하며, 해석하지 못한 경로는 [NotFoundNavKey]가 된다.
 *
 * @param path `/post/hello-haeti-log` 같은 경로 문자열
 */
fun navKeyFromPath(path: String): NavKey {
    val segments = path.split('/').filter { it.isNotEmpty() }
    return when {
        segments.isEmpty() -> HomeNavKey
        segments.size == 2 && segments[0] == POST_SEGMENT -> PostNavKey(segments[1])
        segments.size > 1 -> NotFoundNavKey
        segments[0] == "projects" -> ProjectsNavKey
        segments[0] == "about" -> AboutNavKey
        else -> NotFoundNavKey
    }
}

/**
 * 주소로 바로 들어왔을 때 복원할 백스택.
 *
 * 홈이 아닌 화면은 아래에 홈을 깔아 뒤로가기가 목록으로 이어지게 한다.
 *
 * @param key 진입 화면 키
 */
fun backStackFor(key: NavKey): List<NavKey> = if (key == HomeNavKey) listOf(HomeNavKey) else listOf(HomeNavKey, key)

/**
 * 문서 제목에 쓰는 페이지 이름 세트. 화면 언어마다 하나씩 둔다.
 *
 * @param projects 프로젝트 페이지 이름
 * @param about 소개 페이지 이름
 * @param notFound 없는 페이지 이름
 */
data class PageTitles(
    val projects: String,
    val about: String,
    val notFound: String,
) {
    companion object {
        /** 한국어 페이지 이름. */
        val Korean = PageTitles(projects = "프로젝트", about = "소개", notFound = "페이지를 찾을 수 없음")

        /** 영어 페이지 이름. */
        val English = PageTitles(projects = "Projects", about = "About", notFound = "Page not found")
    }
}

/**
 * `document.title`에 넣을 문자열.
 *
 * @param postTitle 글 상세에서 쓸 글 제목. 아직 못 불러왔으면 null을 넘긴다.
 * @param titles 화면 언어에 맞는 페이지 이름
 */
fun NavKey.documentTitle(
    postTitle: String? = null,
    titles: PageTitles = PageTitles.Korean,
): String =
    when (this) {
        HomeNavKey -> SITE_NAME
        is PostNavKey -> postTitle?.let { "$it$TITLE_SEPARATOR$SITE_NAME" } ?: SITE_NAME
        ProjectsNavKey -> "${titles.projects}$TITLE_SEPARATOR$SITE_NAME"
        AboutNavKey -> "${titles.about}$TITLE_SEPARATOR$SITE_NAME"
        else -> "${titles.notFound}$TITLE_SEPARATOR$SITE_NAME"
    }
