package log.haeti.core.model

import androidx.compose.runtime.Immutable

/**
 * 발행 글 한 편.
 *
 * @param meta 프론트매터에서 파싱된 메타데이터
 * @param body 프론트매터를 제외한 마크다운 본문
 * @param language 본문이 실제로 쓰인 언어. 영어를 요청했는데 번역본이 없으면 [Language.KO]로 남는다
 */
@Immutable
data class Post(
    val meta: PostMeta,
    val body: String,
    val language: Language = Language.KO,
)
