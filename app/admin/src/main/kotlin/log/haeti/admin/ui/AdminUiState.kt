package log.haeti.admin.ui

import androidx.compose.ui.text.input.TextFieldValue
import log.haeti.admin.io.IncomingImages
import log.haeti.admin.model.PostEntry
import log.haeti.admin.translate.TranslationNotice
import log.haeti.core.markdown.ImageResolver
import log.haeti.core.model.Language

/**
 * 어드민 화면 한 장을 그리는 데 필요한 값. ViewModel 없이도(스크린샷 렌더 등) 화면을 그릴 수 있게 분리했다.
 *
 * @param posts 사이드바 글 목록
 * @param selected 열려 있는 글
 * @param language 편집 중인 파일 언어
 * @param fileName 편집 중인 파일 이름
 * @param fileExists 편집 중인 파일이 디스크에 있는지 (번역본을 처음 만들 때 false)
 * @param editorValue 에디터 원문과 커서
 * @param isDirty 저장 안 된 변경이 있는지
 * @param imageResolver 미리보기 이미지 resolver
 * @param isDarkTheme 다크 테마 여부
 * @param translating 번역이 돌고 있는 글 디렉터리명
 * @param notice 파일 저장·열기 실패 또는 마지막 번역 결과 안내
 * @param isEditorLocked 번역 CLI가 지금 이 파일을 쓰고 있어 편집을 막아야 하는지
 */
data class AdminUiState(
    val posts: List<PostEntry>,
    val selected: PostEntry?,
    val language: Language,
    val fileName: String,
    val fileExists: Boolean,
    val editorValue: TextFieldValue,
    val isDirty: Boolean,
    val imageResolver: ImageResolver?,
    val isDarkTheme: Boolean,
    val translating: Set<String> = emptySet(),
    val notice: TranslationNotice? = null,
    val isEditorLocked: Boolean = false,
)

/**
 * 어드민 화면에서 일어나는 사용자 액션 모음.
 *
 * @param onSelect 글 선택
 * @param onCreate 새 글 생성. 디렉터리명과 제목을 받아 성공 여부를 돌려준다
 * @param onSelectLanguage 편집 언어 전환
 * @param onEditorChange 에디터 변경
 * @param onSave 저장
 * @param onInsertImages 이미지 삽입 (버튼·드래그·붙여넣기)
 * @param onTranslate 영어 번역본 만들기·갱신
 * @param onDismissNotice 안내 닫기
 * @param onToggleTheme 테마 전환
 */
class AdminActions(
    val onSelect: (PostEntry) -> Unit = {},
    val onCreate: (String, String) -> Boolean = { _, _ -> false },
    val onSelectLanguage: (Language) -> Unit = {},
    val onEditorChange: (TextFieldValue) -> Unit = {},
    val onSave: () -> Unit = {},
    val onInsertImages: (IncomingImages) -> Unit = {},
    val onTranslate: (PostEntry) -> Unit = {},
    val onDismissNotice: () -> Unit = {},
    val onToggleTheme: () -> Unit = {},
)
