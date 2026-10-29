package log.haeti.core.ui

/** 소개·푸터에 노출하는 외부 링크. 라벨과 주소 쌍이다. */
object HaetiLinks {
    /** GitHub 프로필. */
    const val GITHUB = "https://github.com/haeti-dev"

    /** RSS 피드. `<base href>` 기준 상대 경로라 배포 경로가 바뀌어도 맞는다. 배포 빌드에서만 생긴다. */
    const val RSS = "feed.xml"

    /** 메일 보내기. */
    const val EMAIL = "mailto:pakengo@gmail.com"

    /**
     * 라벨과 주소 쌍 목록. 이메일 라벨만 화면 언어를 따른다.
     *
     * @param emailLabel 이메일 링크 라벨
     */
    fun all(emailLabel: String): List<Pair<String, String>> = listOf("GitHub" to GITHUB, emailLabel to EMAIL)
}
