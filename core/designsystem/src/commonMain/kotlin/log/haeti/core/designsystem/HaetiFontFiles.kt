package log.haeti.core.designsystem

import org.jetbrains.compose.resources.FontResource

/**
 * 플랫폼이 쓰는 번들 폰트 파일. 웹은 같은 글자를 담은 WOFF2를, 데스크톱은 TTF를 쓴다.
 * 링크 미리보기 이미지(AWT)는 WOFF2를 읽지 못해 TTF가 원본으로 남는다.
 */
internal expect object HaetiFontFiles {
    /** Pretendard 400. */
    val regular: FontResource

    /** Pretendard 700. */
    val bold: FontResource

    /** JetBrains Mono 400. */
    val mono: FontResource
}
