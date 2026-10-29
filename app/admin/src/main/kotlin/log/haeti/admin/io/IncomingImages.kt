package log.haeti.admin.io

import java.awt.Image
import java.awt.Toolkit
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.Transferable
import java.io.File

/** 클립보드나 드래그로 들어온 이미지. 파일 목록이거나 비트맵 한 장이다. */
sealed interface IncomingImages {
    /**
     * Finder에서 복사하거나 끌어 온 이미지 파일들.
     *
     * @param files 이미지 확장자만 남긴 파일 목록
     */
    data class Files(
        val files: List<File>,
    ) : IncomingImages

    /**
     * 스크린샷처럼 파일 없이 픽셀만 있는 이미지.
     *
     * @param image 클립보드 비트맵
     */
    data class Bitmap(
        val image: Image,
    ) : IncomingImages
}

/** 시스템 클립보드의 이미지. 이미지가 아니면(글자 등) null이라 평소 붙여넣기는 그대로 동작한다. */
fun readClipboardImages(): IncomingImages? =
    runCatching {
        Toolkit
            .getDefaultToolkit()
            .systemClipboard
            .getContents(null)
            ?.toIncomingImages()
    }.getOrNull()

/**
 * AWT 전송 데이터에서 이미지를 꺼낸다. 파일 목록을 비트맵보다 먼저 본다.
 * Finder에서 파일을 복사하면 아이콘 비트맵도 함께 실리기 때문이다. 글자도 함께 실린 비트맵
 * (Numbers·Keynote 표 복사 등)은 글자 붙여넣기로 양보한다.
 */
fun Transferable.toIncomingImages(): IncomingImages? =
    when {
        isDataFlavorSupported(DataFlavor.javaFileListFlavor) ->
            (getTransferData(DataFlavor.javaFileListFlavor) as List<*>)
                .filterIsInstance<File>()
                .filter { it.isImageFile() }
                .takeIf { it.isNotEmpty() }
                ?.let(IncomingImages::Files)
        isDataFlavorSupported(DataFlavor.imageFlavor) && !isDataFlavorSupported(DataFlavor.stringFlavor) ->
            (getTransferData(DataFlavor.imageFlavor) as? Image)?.let(IncomingImages::Bitmap)
        else -> null
    }
