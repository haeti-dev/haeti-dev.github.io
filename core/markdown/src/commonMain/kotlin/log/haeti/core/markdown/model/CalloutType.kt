package log.haeti.core.markdown.model

/** 콜아웃 타입. content-format.md 기준 3종이며, 알 수 없는 마커는 [NOTE]로 취급한다. */
internal enum class CalloutType {
    NOTE,
    TIP,
    WARNING,
    ;

    companion object {
        /** `note`/`tip`/`warning` 마커 문자열을 [CalloutType]으로 변환한다. 알 수 없으면 [NOTE]. */
        fun from(marker: String): CalloutType = entries.find { it.name.equals(marker, ignoreCase = true) } ?: NOTE
    }
}
