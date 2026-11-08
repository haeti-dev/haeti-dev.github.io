package log.haeti.core.markdown.model

import androidx.compose.ui.graphics.painter.Painter

/** 본문 이미지 하나를 읽은 상태. 읽는 중과 실패를 나눠야 실패한 자리에 안내를 보일 수 있다. */
sealed interface ImageLoad {
    /** 아직 읽는 중. 자리만 잡아 둔다. */
    data object Loading : ImageLoad

    /**
     * 다 읽었다.
     *
     * @param painter 그릴 이미지
     */
    data class Loaded(
        val painter: Painter,
    ) : ImageLoad

    /** 파일이 없거나 받지 못했거나 해석할 수 없다. */
    data object Failed : ImageLoad
}
