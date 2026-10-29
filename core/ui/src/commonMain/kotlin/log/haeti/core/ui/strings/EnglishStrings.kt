package log.haeti.core.ui.strings

import log.haeti.core.model.Language
import log.haeti.core.navigation.path.PageTitles

/** 영어 문구. 글 본문은 번역본(`index.en.md`)이 있을 때만 영어다. */
val EnglishStrings =
    HaetiStrings(
        language = Language.EN,
        navBlog = "Blog",
        navProjects = "Projects",
        navAbout = "About",
        toggleTheme = "Toggle dark mode",
        chooseLanguage = "Display language",
        introHeadline = "The path, not just the answer",
        introDescription =
            "AI can hand you the answer faster than ever. " +
                "What I write here is the path it took to get there: building things myself, " +
                "taking apart how they work inside, and fixing what I got wrong while learning. " +
                "If you're stuck somewhere along the same road, I hope it feels a little less lonely.",
        linkEmail = "Email",
        pastPosts = "Earlier posts",
        all = "All",
        feedError = "Couldn't load the posts. Refresh the page to try again.",
        feedEmpty = "No posts yet.",
        searchPlaceholder = "Search titles and posts",
        searchResults = "Search results",
        searchEmpty = "No posts match. Try other words.",
        clearSearch = "Clear search",
        backToList = "All posts",
        previousPost = "Previous",
        nextPost = "Next",
        translationMissing = "This post is only available in Korean.",
        postNotFoundTitle = "Post not found",
        postNotFoundMessage = "The link may have changed or the post was taken down. Try finding it in the list.",
        postErrorTitle = "Couldn't load this post",
        postErrorMessage = "Refresh the page to try again.",
        notFoundTitle = "Page not found",
        notFoundMessage = "The link may have changed, or the page doesn't exist yet.",
        goToList = "Go to all posts",
        comingSoon = "Coming soon",
        browsePosts = "Browse the posts meanwhile",
        projectsMessage = "I'm sorting through the things I've built. They'll be gathered here soon.",
        aboutMessage = "What I've worked on and what I care about will be written up here soon.",
        pageTitles = PageTitles.English,
        categories =
            mapOf(
                "안드로이드" to "Android",
                "코틀린" to "Kotlin",
                "컴포즈" to "Compose",
                "회고" to "Retrospective",
                "웹" to "Web",
            ),
    )
