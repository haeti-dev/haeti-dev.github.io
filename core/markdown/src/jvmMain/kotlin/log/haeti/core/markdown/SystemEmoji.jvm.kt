package log.haeti.core.markdown

/** 데스크톱 Skia는 운영체제 글꼴을 직접 쓰므로 그림으로 바꿀 필요가 없다. */
internal actual fun systemEmojiPng(emoji: String): ByteArray? = null
