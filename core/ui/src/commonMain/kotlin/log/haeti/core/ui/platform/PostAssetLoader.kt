package log.haeti.core.ui.platform

import androidx.compose.runtime.staticCompositionLocalOf
import kotlinx.datetime.LocalDate
import log.haeti.core.model.PostMeta

/**
 * 글 디렉터리 기준 상대경로의 파일(썸네일 등)을 읽는 방법. 앱 셸이 Repository에 연결해 제공한다.
 * 제공되지 않은 곳(미리보기·테스트)에서는 항상 null을 돌려줘 생성 커버로 대체된다.
 */
fun interface PostAssetLoader {
    /** [meta] 글의 [relativePath] 파일을 읽는다. 없으면 null. */
    suspend fun load(
        meta: PostMeta,
        relativePath: String,
    ): ByteArray?
}

/** 현재 컴포지션의 [PostAssetLoader]. */
val LocalPostAssetLoader = staticCompositionLocalOf { PostAssetLoader { _, _ -> null } }

/** 사이트가 마지막으로 갱신된 날짜(가장 최근 글의 날짜). 푸터가 읽는다. 모르면 null. */
val LocalLastUpdated = staticCompositionLocalOf<LocalDate?> { null }
