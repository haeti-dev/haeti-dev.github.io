package log.haeti.core.data.preferences

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import log.haeti.core.model.Language

/** 앱 수명 동안 메모리에만 보관하는 [UserPreferencesRepository]. 기본 언어는 한국어다. */
@Inject
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class InMemoryUserPreferencesRepository : UserPreferencesRepository {
    private val languageState = MutableStateFlow(Language.KO)

    override val language: StateFlow<Language> = languageState.asStateFlow()

    override fun setLanguage(language: Language) {
        languageState.value = language
    }
}
