package log.haeti.core.testing.repository

import kotlinx.coroutines.flow.MutableStateFlow
import log.haeti.core.data.preferences.UserPreferencesRepository
import log.haeti.core.model.Language

/** 테스트용 [UserPreferencesRepository]. 바꾼 언어가 바로 흐름에 반영된다. */
class TestUserPreferencesRepository : UserPreferencesRepository {
    override val language: MutableStateFlow<Language> = MutableStateFlow(Language.KO)

    override fun setLanguage(language: Language) {
        this.language.value = language
    }
}
