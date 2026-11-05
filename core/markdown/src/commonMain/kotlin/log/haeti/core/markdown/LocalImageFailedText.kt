package log.haeti.core.markdown

import androidx.compose.runtime.staticCompositionLocalOf

/** 이미지를 읽지 못한 자리에 보일 안내 문구. [MarkdownBody]가 화면 언어에 맞춰 받은 값을 제공한다. */
internal val LocalImageFailedText = staticCompositionLocalOf { DEFAULT_IMAGE_FAILED_TEXT }

/** 호출부가 문구를 넘기지 않을 때(어드민 미리보기) 쓰는 한국어 문구. */
internal const val DEFAULT_IMAGE_FAILED_TEXT = "이미지를 불러오지 못했습니다"
