package log.haeti.core.ui.strings

import log.haeti.core.model.Language
import log.haeti.core.navigation.path.PageTitles

/** 한국어 문구. 원문 언어다. */
val KoreanStrings =
    HaetiStrings(
        language = Language.KO,
        navBlog = "블로그",
        navProjects = "프로젝트",
        navAbout = "소개",
        toggleTheme = "다크 모드 켜기/끄기",
        chooseLanguage = "화면 언어",
        introHeadline = "답보다 과정을 남깁니다",
        introDescription =
            "답은 이제 AI가 더 빨리 알려 줍니다. 여기에는 그 답에 닿기까지 헤맨 길을 씁니다. " +
                "직접 만들어 보고, 내부를 뜯어보고, 공부하다 틀린 것을 고친 과정입니다. " +
                "같은 길에서 막힌 누군가에게 덜 외로운 길이 되면 좋겠습니다.",
        linkEmail = "이메일",
        pastPosts = "지난 글",
        all = "전체",
        feedError = "글 목록을 불러오지 못했습니다. 새로고침하면 다시 시도합니다.",
        feedEmpty = "아직 발행한 글이 없습니다.",
        searchPlaceholder = "제목·본문에서 글 찾기",
        searchResults = "검색 결과",
        searchEmpty = "찾는 글이 없습니다. 다른 낱말로 찾아보세요.",
        clearSearch = "검색어 지우기",
        backToList = "글 목록",
        previousPost = "이전 글",
        nextPost = "다음 글",
        translationMissing = "",
        postNotFoundTitle = "글을 찾을 수 없습니다",
        postNotFoundMessage = "주소가 바뀌었거나 내려간 글입니다. 목록에서 다시 찾아보세요.",
        postErrorTitle = "글을 불러오지 못했습니다",
        postErrorMessage = "새로고침하면 다시 시도합니다.",
        notFoundTitle = "페이지를 찾을 수 없습니다",
        notFoundMessage = "주소가 바뀌었거나 아직 없는 페이지입니다.",
        goToList = "글 목록으로 가기",
        comingSoon = "준비 중",
        browsePosts = "그동안 글 둘러보기",
        projectsMessage = "만들어 온 것들을 하나씩 정리하고 있습니다. 곧 이 자리에 모아 둡니다.",
        aboutMessage = "어떤 일을 해 왔고 무엇에 마음을 쓰는지, 곧 이 자리에 적어 둡니다.",
        pageTitles = PageTitles.Korean,
        categories = emptyMap(),
    )
