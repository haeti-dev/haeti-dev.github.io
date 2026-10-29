package log.haeti.core.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/** 홈(인트로 + 글 목록). 백스택의 루트이자 시작 키. */
@Serializable
data object HomeNavKey : NavKey

/**
 * 글 상세.
 * @param slug 글 디렉터리명에서 날짜 접두사를 뺀 URL 슬러그
 */
@Serializable
data class PostNavKey(
    val slug: String,
) : NavKey

/** 프로젝트 페이지. 아직 콘텐츠가 없어 준비 중 안내만 보여준다. */
@Serializable
data object ProjectsNavKey : NavKey

/** 소개 페이지. 아직 콘텐츠가 없어 준비 중 안내만 보여준다. */
@Serializable
data object AboutNavKey : NavKey

/** 알 수 없는 경로. 브라우저 주소를 해석하지 못했을 때만 쓰인다. */
@Serializable
data object NotFoundNavKey : NavKey
