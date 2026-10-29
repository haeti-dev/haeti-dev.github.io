package log.haeti.admin

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import log.haeti.admin.image.AdminImageResolver
import log.haeti.admin.io.ImageImporter
import log.haeti.admin.io.PostFileStore
import log.haeti.admin.io.emptyPostText
import log.haeti.admin.io.failureNotice
import log.haeti.admin.io.readPostSafely
import log.haeti.admin.io.writePostSafely
import log.haeti.admin.model.PostEntry
import log.haeti.admin.translate.TranslationNotice
import log.haeti.admin.translate.TranslationQueue
import log.haeti.core.markdown.ImageResolver
import log.haeti.core.model.Language

/**
 * 어드민 앱 전체 상태를 관리하는 ViewModel.
 *
 * 글 목록·현재 글·편집 언어(원문/번역본)·더티 여부를 관리하며, 파일 IO는 모두 [fileStore]에 위임한다.
 *
 * @param fileStore 글 디렉터리 읽기/쓰기를 담당하는 [PostFileStore]
 * @param images 이미지를 다듬어 `images/`에 넣는 [ImageImporter]
 * @param translations 배지·버튼으로 시작한 번역 작업 상태. 화면이 진행 여부와 결과 안내를 직접 읽는다
 */
@Inject
@SingleIn(AppScope::class)
class AdminViewModel(
    private val fileStore: PostFileStore,
    internal val images: ImageImporter,
    val translations: TranslationQueue,
) : ViewModel() {
    /** 사이드바에 표시할 글 목록. 생성 직후 비동기로 채워진다. */
    var posts by mutableStateOf<List<PostEntry>>(emptyList())
        private set

    /** 현재 에디터에 열려 있는 글. 선택된 글이 없으면 null. */
    var selected by mutableStateOf<PostEntry?>(null)
        private set

    /** 편집 중인 파일의 언어. 한국어는 `index.md`, 영어는 `index.en.md`. */
    var language by mutableStateOf(Language.KO)
        private set

    /** 편집 중인 파일이 디스크에 있는지. 번역본을 처음 만들 때 false다. */
    var fileExists by mutableStateOf(true)
        private set

    /** 에디터의 현재 텍스트와 커서/선택 상태. */
    var editorValue by mutableStateOf(TextFieldValue(""))
        private set

    private var savedText by mutableStateOf("")

    /** 파일을 열거나 저장하지 못했을 때의 안내. 번역 결과 안내보다 먼저 보인다. */
    var fileNotice by mutableStateOf<TranslationNotice?>(null)
        private set

    /** 마지막 저장 이후 편집됐는지 여부. */
    val isDirty: Boolean get() = editorValue.text != savedText

    /** 편집 중인 파일 이름 (`index.md`, `index.en.md`). */
    val fileName: String get() = fileStore.fileNameOf(language)

    /** 번역이 도는 동안 그 글의 EN 파일은 CLI가 쓰므로 에디터를 잠근다. */
    val isEditorLocked: Boolean get() = language == Language.EN && selected?.let(translations::isRunning) == true

    /** 선택된 글 기준 이미지 리졸버. 선택된 글이 없으면 null. */
    val imageResolver: ImageResolver? get() = selected?.let { AdminImageResolver(it.path) }

    init {
        viewModelScope.launch { reloadPosts() }
    }

    /** 사이드바에서 글을 선택해 원문(한국어)을 연다. 편집 중이던 내용을 먼저 저장하고, 실패하면 옮기지 않는다. */
    fun open(entry: PostEntry) {
        viewModelScope.launch { if (saveIfDirty()) load(entry, Language.KO) }
    }

    /** 같은 글의 다른 언어 파일로 전환한다. 편집 중이던 내용을 먼저 저장하고, 실패하면 옮기지 않는다. */
    fun selectLanguage(target: Language) {
        val entry = selected ?: return
        if (target == language) return
        viewModelScope.launch { if (saveIfDirty()) load(entry, target) }
    }

    /** 에디터 내용(텍스트+커서)이 바뀔 때마다 호출된다. */
    fun updateEditor(value: TextFieldValue) {
        editorValue = value
    }

    /** 에디터 원문을 언어에 맞는 파일에 저장한다. 바뀐 게 없거나 번역 CLI가 쓰는 중이면 쓰지 않는다. */
    fun save() {
        if (!isDirty || isEditorLocked) return
        viewModelScope.launch { persist() }
    }

    /** 저장 안 된 변경을 저장한 뒤 [onSaved]를 부른다. 저장에 실패하면 부르지 않고 안내만 남긴다(창 닫기). */
    fun saveThen(onSaved: () -> Unit) {
        viewModelScope.launch { if (saveIfDirty()) onSaved() }
    }

    /** 파일 안내와 번역 결과 안내를 닫는다. */
    fun dismissNotice() {
        fileNotice = null
        translations.dismissNotice()
    }

    /**
     * 새 글 디렉터리를 만들고 바로 연다. 다이얼로그가 성공 여부를 즉시 알아야 해서 생성만 동기로 처리한다.
     *
     * @param dirName `yyyy-MM-dd-slug` 형식 이름
     * @param title 프론트매터에 미리 채울 제목
     */
    fun createPost(
        dirName: String,
        title: String,
    ): Boolean {
        val entry = runCatching { fileStore.createPost(dirName, title) }.getOrNull() ?: return false
        viewModelScope.launch { reloadPosts() }
        open(entry)
        return true
    }

    /** CLI로 [entry]의 영어 번역본을 만들거나 갱신한다. 열려 있는 글의 저장 안 된 원문은 먼저 저장한다. */
    fun translate(entry: PostEntry) {
        if (!translations.begin(entry)) return
        viewModelScope.launch {
            if (selected?.dirName == entry.dirName && isDirty) persist()
            translations.translate(entry)
            reloadPosts()
            val current = selected?.takeIf { it.dirName == entry.dirName } ?: return@launch
            if (language == Language.EN) load(current, Language.EN)
        }
    }

    /** 에디터 원문을 지금 언어 파일에 그대로 쓰고 목록을 다시 읽는다. 실패하면 안내를 남기고 false. */
    private suspend fun persist(): Boolean {
        val entry = selected ?: return true
        val text = editorValue.text
        val target = language
        val failure = fileStore.writePostSafely(entry.path, target, text)
        fileNotice = failure ?: fileNotice
        // 쓰는 사이 다른 글·언어로 옮겼으면 그 화면의 저장 기준을 건드리지 않는다.
        if (failure == null && selected?.dirName == entry.dirName && language == target) {
            savedText = text
            fileExists = true
        }
        reloadPosts()
        return failure == null
    }

    /** 바뀐 게 없거나 잠겨 있으면 저장할 것이 없으므로 true. */
    private suspend fun saveIfDirty(): Boolean = !isDirty || isEditorLocked || persist()

    /** 파일을 에디터에 연다. 없으면 빈 원문(번역본은 틀)으로 시작하고, 읽지 못하면 지금 글에 머문다. */
    private suspend fun load(
        entry: PostEntry,
        target: Language,
    ) {
        val read = fileStore.readPostSafely(entry.path, target)
        read.exceptionOrNull()?.let {
            fileNotice = failureNotice("열지 못했습니다", fileStore.fileNameOf(target), it)
            return
        }
        val text = read.getOrNull()
        selected = entry
        language = target
        fileExists = text != null
        val initial = text ?: emptyPostText(target)
        editorValue = TextFieldValue(initial)
        savedText = initial
    }

    private suspend fun reloadPosts() {
        posts = withContext(Dispatchers.IO) { runCatching { fileStore.listPosts() }.getOrDefault(emptyList()) }
        selected = selected?.let { current -> posts.firstOrNull { it.dirName == current.dirName } ?: current }
    }
}
