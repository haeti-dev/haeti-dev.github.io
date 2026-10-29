package log.haeti.admin.io

import java.awt.FileDialog
import java.awt.Frame
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.prefs.Preferences

/** 실행 위치에서 상위로 거슬러 올라가며 탐색할 최대 단계. */
private const val MAX_PARENT_DEPTH = 2

/** 빌드가 맥 앱에 넣어 두는 저장소 `content/` 경로. */
private const val CONTENT_ROOT_PROPERTY = "haeti.contentRoot"

/** 사용자가 고른 `content/` 경로를 기억해 두는 키. */
private const val SAVED_ROOT_KEY = "contentRoot"

/**
 * 글 원본이 모여 있는 `content/` 디렉터리 경로.
 *
 * DI 그래프에 [Path]를 그대로 바인딩하면 다른 경로 의존과 구분되지 않으므로 전용 타입으로 감싼다.
 *
 * @param path `content/` 디렉터리의 절대 경로
 */
data class ContentRoot(
    val path: Path,
)

/**
 * `content/` 디렉터리를 찾는다. 앞에서부터 처음 존재하는 것을 쓴다.
 *
 * 1. `-Dhaeti.contentRoot` (맥 앱은 빌드 때 저장소 경로를 넣어 둔다. Finder에서 열면 작업 디렉터리가 `/`라서 필요하다)
 * 2. 작업 디렉터리와 상위 [MAX_PARENT_DEPTH]단계 (`./gradlew :app:admin:run`)
 * 3. 예전에 골라 둔 경로
 * 4. 폴더 선택 창으로 물어보고 기억한다 (저장소를 옮긴 경우)
 *
 * 끝내 못 찾으면 작업 디렉터리 기준 `content`를 돌려준다. 이때 글 목록은 비고, 새 글을 만들면 디렉터리가 생긴다.
 */
fun locateContentRoot(): Path {
    val preferences = Preferences.userRoot().node("log/haeti/admin")
    val found =
        System.getProperty(CONTENT_ROOT_PROPERTY)?.let(Paths::get)?.takeIf(Files::isDirectory)
            ?: searchUpward()
            ?: preferences.get(SAVED_ROOT_KEY, null)?.let(Paths::get)?.takeIf(Files::isDirectory)
            ?: askForContentRoot()?.also { preferences.put(SAVED_ROOT_KEY, it.toString()) }
    return found ?: Paths.get("content").toAbsolutePath()
}

private fun searchUpward(): Path? {
    var dir: Path? = Paths.get("").toAbsolutePath()
    repeat(MAX_PARENT_DEPTH + 1) {
        val candidate = dir?.resolve("content") ?: return null
        if (Files.isDirectory(candidate)) return candidate
        dir = dir?.parent
    }
    return null
}

/** 저장소 폴더(또는 그 안의 `content` 폴더)를 고르게 한다. 취소하거나 `content`가 없으면 null. */
private fun askForContentRoot(): Path? {
    System.setProperty("apple.awt.fileDialogForDirectories", "true")
    val dialog = FileDialog(null as Frame?, "Haeti-log 저장소 폴더를 골라 주세요", FileDialog.LOAD)
    dialog.isVisible = true
    System.setProperty("apple.awt.fileDialogForDirectories", "false")
    val picked = dialog.directory?.let { Paths.get(it, dialog.file ?: "") } ?: return null
    return listOf(picked.resolve("content"), picked).firstOrNull {
        it.fileName?.toString() == "content" &&
            Files.isDirectory(it)
    }
}
