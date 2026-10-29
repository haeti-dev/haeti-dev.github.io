package log.haeti.web.browser

import kotlinx.browser.document
import kotlinx.browser.localStorage
import kotlinx.browser.window
import log.haeti.core.model.Language

/** 화면 언어 설정을 담는 `localStorage` 키. */
private const val LANGUAGE_KEY = "haeti.lang"

/**
 * 처음 보여줄 화면 언어. 저장된 선택이 있으면 그것, 없으면 브라우저 언어가 영어일 때만 영어.
 * `localStorage`를 쓸 수 없는 환경(시크릿 모드 등)의 예외는 삼키고 브라우저 언어로 넘어간다.
 */
fun initialLanguage(): Language {
    val stored = runCatching { localStorage.getItem(LANGUAGE_KEY) }.getOrNull()
    Language.fromCode(stored)?.let { return it }
    return if (Language.fromCode(window.navigator.language) == Language.EN) Language.EN else Language.KO
}

/** `<html lang>`을 화면 언어에 맞춘다(스크린 리더·브라우저가 읽는 문서 언어). */
fun applyDocumentLanguage(language: Language) {
    document.documentElement?.setAttribute("lang", language.code)
}

/** 사용자가 토글로 고른 언어를 저장한다. 자동으로 고른 첫 언어는 저장하지 않아, 브라우저 언어를 바꾸면 따라간다. */
fun saveLanguage(language: Language) {
    runCatching { localStorage.setItem(LANGUAGE_KEY, language.code) }
}
