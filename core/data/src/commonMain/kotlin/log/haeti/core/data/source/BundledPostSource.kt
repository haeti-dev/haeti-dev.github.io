package log.haeti.core.data.source

import log.haeti.core.model.Language

/**
 * 앱 번들에 실려 있는 글 파일을 읽는다.
 *
 * 글 한 편은 `<yyyy-MM-dd-slug>` 디렉터리 하나이고, 그 안의 `index.md`가 원문이다.
 * 글 파일이 없으면 예외 대신 null을 돌려준다. 인덱스는 늘 있어야 하므로 못 읽으면 예외를 던진다.
 */
interface BundledPostSource {
    /** 발행된 글 디렉터리명을 읽는다. 읽지 못하면 예외를 던진다. */
    suspend fun readIndex(): List<String>

    /** 글 디렉터리의 원문을 읽는다. 한국어는 `index.md`, 다른 언어는 `index.<code>.md`. 없으면 null. */
    suspend fun readMarkdown(
        directory: String,
        language: Language = Language.KO,
    ): String?

    /** 글 디렉터리 기준 상대경로의 파일을 바이트로 읽는다. 없으면 null. */
    suspend fun readAsset(
        directory: String,
        relativePath: String,
    ): ByteArray?
}
