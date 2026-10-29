package log.haeti.core.data.source

import log.haeti.core.model.Language

/**
 * 인메모리 Fake [BundledPostSource].
 *
 * @param index `readIndex`가 돌려줄 디렉터리명 목록
 * @param markdown 디렉터리명 → `index.md` 원문
 * @param assets `"디렉터리명/상대경로"` → 바이트
 * @param translations 디렉터리명 → 영어 번역본(`index.en.md`) 원문
 * @param indexError 있으면 `readIndex`가 이 예외를 던진다
 */
class FakePostSource(
    private val index: List<String>,
    private val markdown: Map<String, String>,
    private val assets: Map<String, ByteArray> = emptyMap(),
    private val translations: Map<String, String> = emptyMap(),
    private val indexError: Exception? = null,
) : BundledPostSource {
    /** `readIndex` 호출 횟수. 캐시가 중복 로드를 막는지 검증할 때 쓴다. */
    var indexReadCount: Int = 0
        private set

    override suspend fun readIndex(): List<String> {
        indexReadCount++
        indexError?.let { throw it }
        return index
    }

    override suspend fun readMarkdown(
        directory: String,
        language: Language,
    ): String? = if (language == Language.KO) markdown[directory] else translations[directory]

    override suspend fun readAsset(
        directory: String,
        relativePath: String,
    ): ByteArray? = assets["$directory/$relativePath"]
}
