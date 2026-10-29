package log.haeti.web.ui

/**
 * 다크 테마 설정을 읽고 쓰는 인터페이스. 브라우저는 `localStorage`로, 그 밖의 환경은
 * [NoThemePreference]로 구현해 셸 자체는 플랫폼에 의존하지 않는다.
 */
interface ThemePreference {
    /** 저장된 설정을 읽는다. 저장된 적이 없으면 null(시스템 설정을 따른다). */
    fun load(): Boolean?

    /**
     * 설정을 저장한다.
     *
     * @param isDark 다크 테마 여부
     */
    fun save(isDark: Boolean)
}

/** 아무것도 기억하지 않는 구현. 저장 공간이 없는 환경과 테스트·프리뷰에서 쓴다. */
object NoThemePreference : ThemePreference {
    override fun load(): Boolean? = null

    override fun save(isDark: Boolean) = Unit
}
