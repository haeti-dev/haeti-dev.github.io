package log.haeti.core.model

/**
 * 화면·글 언어. 한국어가 원문이고 영어는 번역본이 있을 때만 쓴다.
 *
 * @param code BCP-47 언어 코드. 번역본 파일명(`index.en.md`)과 `<html lang>`에 쓴다
 */
enum class Language(
    val code: String,
) {
    KO("ko"),
    EN("en"),
    ;

    companion object {
        /** 언어 코드로 찾는다. 모르는 코드는 null. */
        fun fromCode(code: String?): Language? =
            entries.firstOrNull { it.code.equals(code?.take(2), ignoreCase = true) }
    }
}
