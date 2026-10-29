package log.haeti.admin.io

import java.awt.FileDialog
import java.awt.Frame
import java.io.File

/**
 * 네이티브 파일 선택 다이얼로그로 이미지 파일 하나를 고른다.
 * 취소하면 null을 반환한다.
 */
fun pickImageFile(): File? {
    val dialog = FileDialog(null as Frame?, "이미지 선택", FileDialog.LOAD)
    dialog.setFilenameFilter { _, name -> File(name).isImageFile() }
    dialog.isVisible = true
    val fileName = dialog.file ?: return null
    val directory = dialog.directory ?: return null
    return File(directory, fileName)
}

/** 블로그에 넣을 수 있는 확장자([IMAGE_EXTENSIONS])의 파일인지. */
internal fun File.isImageFile(): Boolean = extension.lowercase() in IMAGE_EXTENSIONS
