package log.haeti.core.markdown.model

/**
 * 이미지 지시어 `{width=60 align=center caption="설명" dark=images/a-dark.png}` 파싱 결과.
 *
 * @param width 콘텐츠 폭 대비 퍼센트 (10-100)
 * @param align 정렬 방향
 * @param caption 이미지 아래에 표시할 설명. 없으면 null
 * @param dark 다크 모드에서 대신 보여 줄 이미지 경로. 없으면 null
 */
internal data class ImageDirective(
    val width: Int = 100,
    val align: ImageAlign = ImageAlign.CENTER,
    val caption: String? = null,
    val dark: String? = null,
) {
    /** 페이지 테마에 맞는 이미지 경로. 다크 버전이 없으면 원래 경로를 쓴다. */
    fun pathFor(
        path: String,
        isDarkPage: Boolean,
    ): String = if (isDarkPage) dark ?: path else path
}
