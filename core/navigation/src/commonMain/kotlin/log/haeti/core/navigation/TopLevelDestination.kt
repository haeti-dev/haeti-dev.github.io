package log.haeti.core.navigation

import androidx.navigation3.runtime.NavKey

/**
 * 상단바 GNB에 노출되는 최상위 목적지 3종.
 * @param navKey 이 목적지로 이동할 때 쓰는 내비게이션 키
 * @param label GNB에 표시할 한글 라벨
 */
enum class TopLevelDestination(
    val navKey: NavKey,
    val label: String,
) {
    Blog(HomeNavKey, "블로그"),
    Projects(ProjectsNavKey, "프로젝트"),
    About(AboutNavKey, "소개"),
}

/** 현재 키가 속한 GNB 목적지. 글 상세·알 수 없는 경로는 블로그 섹션 소속으로 간주한다. */
fun NavKey.toTopLevelDestination(): TopLevelDestination =
    when (this) {
        ProjectsNavKey -> TopLevelDestination.Projects
        AboutNavKey -> TopLevelDestination.About
        else -> TopLevelDestination.Blog
    }
