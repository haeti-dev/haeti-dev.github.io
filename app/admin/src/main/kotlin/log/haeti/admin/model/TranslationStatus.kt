package log.haeti.admin.model

/** 영어 번역본 상태. 발행 전에 [Missing]·[Stale]을 없애는 것이 글 발행 절차의 마지막 단계다. */
enum class TranslationStatus {
    /** `index.en.md`가 없다. */
    Missing,

    /** 번역본이 지금 원문(제목·요약·본문)을 기준으로 만들어졌다. */
    UpToDate,

    /** 번역본을 만든 뒤 원문이 바뀌었다(또는 기준 해시가 없다). 번역 갱신이 필요하다. */
    Stale,
}
